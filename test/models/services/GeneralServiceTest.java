package models.services;

import actors.ChannelProfileMessages;
import actors.SentimentMessages;
import actors.TagMessages;
import actors.WordStatMessages;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.TestKit;
import akka.testkit.TestProbe;
import controllers.routes;
import models.entities.Video;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.mvc.Result;

import scala.concurrent.duration.Duration;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static play.mvc.Http.Status.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class GeneralServiceTest {

    // Akka ActorSystem and TestProbes
    private ActorSystem system;
    private TestProbe mockTagActorProbe;
    private TestProbe mockChannelProfileActorProbe;
    private TestProbe mockWordStatActorProbe;
    private TestProbe mockSentimentActorProbe;

    // Mocked services
    private SearchService mockSearchService;
    private WordStatService mockWordStatService;

    // Mocked Http.Request
    private Http.Request mockRequest;

    @Before
    public void setUp() {
        // Initialize ActorSystem
        system = ActorSystem.create("TestActorSystem");

        // Initialize TestProbes for each actor
        mockTagActorProbe = new TestProbe(system);
        mockChannelProfileActorProbe = new TestProbe(system);
        mockWordStatActorProbe = new TestProbe(system);
        mockSentimentActorProbe = new TestProbe(system);

        // Initialize mocked services
        mockSearchService = mock(SearchService.class);
        mockWordStatService = mock(WordStatService.class);

        // Initialize mocked Http.Request without session ID
        mockRequest = mock(Http.Request.class);
        when(mockRequest.session()).thenReturn(new Http.Session(Map.of()));
    }

    @After
    public void teardown() {
        // Define a timeout duration (e.g., 5 seconds) using Scala's Duration
        Duration timeout = Duration.create(5, "seconds");

        // Define whether to wait for termination
        boolean awaitTermination = true;

        // Shutdown the ActorSystem with the specified timeout and awaitTermination
        TestKit.shutdownActorSystem(system, timeout, awaitTermination);

        // Nullify the ActorSystem reference for garbage collection
        system = null;
    }

    // a. Testing isKeywordValid
    @Test
    public void testIsKeywordValid_ValidKeyword() {
        String keyword = "Play Framework";
        assertTrue("Keyword should be valid", GeneralService.isKeywordValid(keyword));
    }

    @Test
    public void testIsKeywordValid_NullKeyword() {
        String keyword = null;
        assertFalse("Null keyword should be invalid", GeneralService.isKeywordValid(keyword));
    }

    @Test
    public void testIsKeywordValid_EmptyKeyword() {
        String keyword = "   ";
        assertFalse("Empty keyword should be invalid", GeneralService.isKeywordValid(keyword));
    }

    // b. Testing tagHelper
    @Test
    public void testTagHelper_SuccessfulRetrieval() throws Exception {
        String videoId = "video123";

        // Prepare mock responses
        Video video = new Video("Sample Title", "Description", "Channel", "https://thumbnail.url", videoId, "channel123", "https://www.youtube.com/watch?v=videoId123", "2024-11-24");
        TagMessages.GetVideoResponse videoResponse = new TagMessages.GetVideoResponse(video);
        TagMessages.GetTagsResponse tagsResponse = new TagMessages.GetTagsResponse(List.of("tag1", "tag2", "tag3"));

        // Invoke tagHelper asynchronously
        CompletionStage<Result> resultStage = GeneralService.tagHelper(mockTagActorProbe.ref(), videoId, mockRequest);

        // Expect GetVideo message and reply
        TagMessages.GetVideo receivedGetVideo = mockTagActorProbe.expectMsgClass(TagMessages.GetVideo.class);
        assertEquals(videoId, receivedGetVideo.getVideoId());
        mockTagActorProbe.reply(videoResponse);

        // Expect GetTags message and reply
        TagMessages.GetTags receivedGetTags = mockTagActorProbe.expectMsgClass(TagMessages.GetTags.class);
        assertEquals(videoId, receivedGetTags.getVideoId());
        mockTagActorProbe.reply(tagsResponse);

        // Await and retrieve the Result
        Result result = resultStage.toCompletableFuture().get();

        // Assertions
        assertEquals(OK, result.status());
        // Optionally, verify content if accessible
        // Example:
        // assertTrue(contentAsString(result).contains("Sample Title"));
        // assertTrue(contentAsString(result).contains("tag1"));
    }

    @Test
    public void testTagHelper_VideoError() throws Exception {
        String videoId = "video123";

        // Prepare mock responses with an error in video retrieval
        TagMessages.TagsError errorResponse = new TagMessages.TagsError("Failed to fetch video.");
        TagMessages.GetTagsResponse tagsResponse = new TagMessages.GetTagsResponse(List.of("tag1", "tag2", "tag3"));

        // Invoke tagHelper asynchronously
        CompletionStage<Result> resultStage = GeneralService.tagHelper(mockTagActorProbe.ref(), videoId, mockRequest);

        // Expect GetVideo message and reply with error
        TagMessages.GetVideo receivedGetVideo = mockTagActorProbe.expectMsgClass(TagMessages.GetVideo.class);
        assertEquals(videoId, receivedGetVideo.getVideoId());
        mockTagActorProbe.reply(errorResponse);

        // Expect GetTags message and reply
        TagMessages.GetTags receivedGetTags = mockTagActorProbe.expectMsgClass(TagMessages.GetTags.class);
        assertEquals(videoId, receivedGetTags.getVideoId());
        mockTagActorProbe.reply(tagsResponse);

        // Await and retrieve the Result
        Result result = resultStage.toCompletableFuture().get();

        // Assertions
        assertEquals(INTERNAL_SERVER_ERROR, result.status());
        // Optionally, verify error content
        // Example:
        // assertTrue(contentAsString(result).contains("Failed to fetch video."));
    }

    @Test
    public void testTagHelper_TagsError() throws Exception {
        String videoId = "video123";

        // Prepare mock responses with an error in tags retrieval
        Video video = new Video("Sample Title", "Description", "Channel", "https://thumbnail.url", videoId, "channel123", "https://www.youtube.com/watch?v=videoId123", "2024-11-24");
        TagMessages.GetVideoResponse videoResponse = new TagMessages.GetVideoResponse(video);
        TagMessages.TagsError errorResponse = new TagMessages.TagsError("Failed to fetch tags.");

        // Invoke tagHelper asynchronously
        CompletionStage<Result> resultStage = GeneralService.tagHelper(mockTagActorProbe.ref(), videoId, mockRequest);

        // Expect GetVideo message and reply
        TagMessages.GetVideo receivedGetVideo = mockTagActorProbe.expectMsgClass(TagMessages.GetVideo.class);
        assertEquals(videoId, receivedGetVideo.getVideoId());
        mockTagActorProbe.reply(videoResponse);

        // Expect GetTags message and reply with error
        TagMessages.GetTags receivedGetTags = mockTagActorProbe.expectMsgClass(TagMessages.GetTags.class);
        assertEquals(videoId, receivedGetTags.getVideoId());
        mockTagActorProbe.reply(errorResponse);

        // Await and retrieve the Result
        Result result = resultStage.toCompletableFuture().get();

        // Assertions
        assertEquals(INTERNAL_SERVER_ERROR, result.status());
        // Optionally, verify error content
        // Example:
        // assertTrue(contentAsString(result).contains("Failed to fetch tags."));
    }

    // c. Testing channelProfileHelper
    @Test
    public void testChannelProfileHelper_SuccessfulRetrieval() throws Exception {
        String channelId = "channel123";

        // Prepare mock responses
        JSONObject channelInfo = new JSONObject();
        channelInfo.put("title", "Sample Channel");
        channelInfo.put("description", "Sample Description");
        channelInfo.put("subscriberCount", 1000);
        channelInfo.put("videoCount", 50);
        ChannelProfileMessages.ChannelInfoResponse infoResponse = new ChannelProfileMessages.ChannelInfoResponse(channelInfo);

        List<Video> videos = List.of(
                new Video("Video1", "Description1", "Channel1", "https://thumbnail1.url", "videoId1", "channelId1", "https://www.youtube.com/watch?v=videoId1", "2024-11-24"),
                new Video("Video2", "Description2", "Channel2", "https://thumbnail2.url", "videoId2", "channelId2", "https://www.youtube.com/watch?v=videoId2", "2024-11-24")
        );
        ChannelProfileMessages.ChannelVideosResponse videosResponse = new ChannelProfileMessages.ChannelVideosResponse(videos);

        // Invoke channelProfileHelper asynchronously
        CompletionStage<Result> resultStage = GeneralService.channelProfileHelper(mockChannelProfileActorProbe.ref(), channelId, mockRequest);

        // Expect GetChannelInfo message and reply
        ChannelProfileMessages.GetChannelInfo receivedGetChannelInfo = mockChannelProfileActorProbe.expectMsgClass(ChannelProfileMessages.GetChannelInfo.class);
        assertEquals(channelId, receivedGetChannelInfo.getChannelId());
        mockChannelProfileActorProbe.reply(infoResponse);

        // Expect GetChannelVideos message and reply
        ChannelProfileMessages.GetChannelVideos receivedGetChannelVideos = mockChannelProfileActorProbe.expectMsgClass(ChannelProfileMessages.GetChannelVideos.class);
        assertEquals(channelId, receivedGetChannelVideos.getChannelId());
        assertEquals(10, receivedGetChannelVideos.getMaxResults());
        mockChannelProfileActorProbe.reply(videosResponse);

        // Await and retrieve the Result
        Result result = resultStage.toCompletableFuture().get();

        // Assertions
        assertEquals(OK, result.status());
        // Optionally, verify content if accessible
        // Example:
        // assertTrue(contentAsString(result).contains("Sample Channel"));
        // assertTrue(contentAsString(result).contains("Video1"));
    }

    @Test
    public void testChannelProfileHelper_ErrorRetrieval() throws Exception {
        String channelId = "channel123";

        // Prepare mock responses with an error
        ChannelProfileMessages.ChannelProfileError errorResponse = new ChannelProfileMessages.ChannelProfileError("Failed to fetch channel profile.");
        ChannelProfileMessages.ChannelVideosResponse videosResponse = new ChannelProfileMessages.ChannelVideosResponse(
                List.of(new Video("Video1", "Description1", "Channel1", "https://thumbnail1.url", "videoId1", "channelId1", "https://www.youtube.com/watch?v=videoId1", "2024-11-24"))
        );

        // Invoke channelProfileHelper asynchronously
        CompletionStage<Result> resultStage = GeneralService.channelProfileHelper(mockChannelProfileActorProbe.ref(), channelId, mockRequest);

        // Expect GetChannelInfo message and reply with error
        ChannelProfileMessages.GetChannelInfo receivedGetChannelInfo = mockChannelProfileActorProbe.expectMsgClass(ChannelProfileMessages.GetChannelInfo.class);
        assertEquals(channelId, receivedGetChannelInfo.getChannelId());
        mockChannelProfileActorProbe.reply(errorResponse);

        // Expect GetChannelVideos message and reply
        ChannelProfileMessages.GetChannelVideos receivedGetChannelVideos = mockChannelProfileActorProbe.expectMsgClass(ChannelProfileMessages.GetChannelVideos.class);
        assertEquals(channelId, receivedGetChannelVideos.getChannelId());
        assertEquals(10, receivedGetChannelVideos.getMaxResults());
        mockChannelProfileActorProbe.reply(videosResponse);

        // Await and retrieve the Result
        Result result = resultStage.toCompletableFuture().get();

        // Assertions
        assertEquals(INTERNAL_SERVER_ERROR, result.status());
        // Optionally, verify error content
        // Example:
        // assertTrue(contentAsString(result).contains("Failed to fetch channel profile."));
    }

    // d. Testing wordStatHelper
    @Test
    public void testWordStatHelper_ValidKeyword() throws Exception {
        String keyword = "PlayFramework";

        // Prepare mock responses
        List<Video> videos = List.of(
                new Video("Video1", "Description1", "Channel1", "https://thumbnail1.url", "videoId1", "channelId1", "https://www.youtube.com/watch?v=videoId1", "2024-11-24"),
                new Video("Video2", "Description2", "Channel2", "https://thumbnail2.url", "videoId2", "channelId2", "https://www.youtube.com/watch?v=videoId2", "2024-11-24")
        );

        when(mockSearchService.searchVideos(eq("playframework"), eq(GeneralService.NUM_OF_RESULTS_WORD_STATS)))
                .thenReturn(CompletableFuture.completedFuture(videos));
        doNothing().when(mockSearchService).addSearchResult(eq("sessionId"), eq("playframework"), eq(videos));

        Map<String, Long> wordStats = Map.of("playframework", 10L, "scala", 8L, "java", 5L);
        when(mockWordStatService.createWordStats(eq(videos))).thenReturn(wordStats);

        // Invoke wordStatHelper asynchronously
        CompletionStage<Result> resultStage = GeneralService.wordStatHelper(mockSearchService, mockWordStatService, keyword, mockRequest);

        // Await and retrieve the Result
        Result result = resultStage.toCompletableFuture().get();

        // Assertions
        assertEquals(OK, result.status());
        // Optionally, verify content if accessible
        // Example:
        // assertTrue(contentAsString(result).contains("playframework: 10"));
        // assertTrue(contentAsString(result).contains("scala: 8"));
    }

    @Test
    public void testWordStatHelper_InvalidKeyword() throws Exception {
        String keyword = "   ";

        // Invoke wordStatHelper with invalid keyword asynchronously
        CompletionStage<Result> resultStage = GeneralService.wordStatHelper(mockSearchService, mockWordStatService, keyword, mockRequest);
        Result result = resultStage.toCompletableFuture().get();

        // Assertions
        assertEquals(SEE_OTHER, result.status());
        assertEquals(routes.YoutubeController.index().url(), result.redirectLocation().orElse(""));
    }

    // e. Testing wordStatActorHelper
    @Test
    public void testWordStatActorHelper_SuccessfulRetrieval() throws Exception {
        String keyword = "PlayFramework";

        // Prepare mock responses
        // For UpdateVideos, no response needed as it's a void operation
        // For GetWordStats, provide word stats
        Map<String, Long> wordStats = Map.of("playframework", 10L, "scala", 8L, "java", 5L);

        // Invoke wordStatActorHelper asynchronously
        CompletionStage<Result> resultStage = GeneralService.wordStatActorHelper(mockSearchService, mockWordStatActorProbe.ref(), keyword, mockRequest);

        // Expect UpdateVideos message and reply
        WordStatMessages.UpdateVideos receivedUpdateVideos = mockWordStatActorProbe.expectMsgClass(WordStatMessages.UpdateVideos.class);
        assertEquals(keyword, receivedUpdateVideos.keyword);
        mockWordStatActorProbe.reply(null); // Simulate successful processing

        // Expect GetWordStats message and reply with wordStats
        WordStatMessages.GetWordStats receivedGetWordStats = mockWordStatActorProbe.expectMsgClass(WordStatMessages.GetWordStats.class);
        mockWordStatActorProbe.reply(wordStats);

        // Await and retrieve the Result
        Result result = resultStage.toCompletableFuture().get();

        // Assertions
        assertEquals(OK, result.status());
        // Optionally, verify content if accessible
        // Example:
        // assertTrue(contentAsString(result).contains("playframework: 10"));
        // assertTrue(contentAsString(result).contains("scala: 8"));
        // assertTrue(contentAsString(result).contains("java: 5"));
    }

    // f. Testing searchHelper
    @Test
    public void testSearchHelper_ValidKeyword_SuccessfulRetrieval() throws Exception {
        String keyword = "PlayFramework";

        // Prepare mock responses
        List<Video> videos = List.of(
                new Video("Video1", "Description1", "Channel1", "https://thumbnail1.url", "videoId1", "channelId1", "https://www.youtube.com/watch?v=videoId1", "2024-11-24"),
                new Video("Video2", "Description2", "Channel2", "https://thumbnail2.url", "videoId2", "channelId2", "https://www.youtube.com/watch?v=videoId2", "2024-11-24"),
                new Video("Video3", "Description3", "Channel3", "https://thumbnail3.url", "videoId3", "channelId3", "https://www.youtube.com/watch?v=videoId3", "2024-11-24")
        );

        when(mockSearchService.searchVideos(eq("playframework"), eq(GeneralService.NUM_OF_RESULTS_SENTIMENT)))
                .thenReturn(CompletableFuture.completedFuture(videos));

        // Simulate adding search result
        doNothing().when(mockSearchService).addSearchResult(eq("sessionId"), eq("playframework"), eq(videos));

        // Prepare sentiment responses
        Map<String, String> individualSentiments = Map.of(
                "videoId1", "positive",
                "videoId2", "negative",
                "videoId3", "neutral"
        );

        when(mockSearchService.calculateSentiments(eq("sessionId")))
                .thenReturn(CompletableFuture.completedFuture(Map.of("playframework", "mixed")));

        when(mockSearchService.getSearchHistory(eq("sessionId")))
                .thenReturn(Map.of("playframework", videos.stream().limit(10).collect(Collectors.toList())));

        // Invoke searchHelper asynchronously
        CompletionStage<Result> resultStage = GeneralService.searchHelper(mockSearchService, mockSentimentActorProbe.ref(), keyword, mockRequest);

        // Expect AnalyzeVideos message and reply with individualSentiments
        SentimentMessages.AnalyzeVideos receivedAnalyzeVideos = mockSentimentActorProbe.expectMsgClass(SentimentMessages.AnalyzeVideos.class);
        assertEquals(videos, receivedAnalyzeVideos.getVideos());
        mockSentimentActorProbe.reply(individualSentiments);

        // Await and retrieve the Result
        Result result = resultStage.toCompletableFuture().get();

        // Assertions
        assertEquals(OK, result.status());
        // Optionally, verify content if accessible
        // Example:
        // assertTrue(contentAsString(result).contains("playframework"));
        // assertTrue(contentAsString(result).contains("mixed"));
        // assertTrue(contentAsString(result).contains("videoId1: positive"));
        // assertTrue(contentAsString(result).contains("videoId2: negative"));
    }

    @Test
    public void testSearchHelper_InvalidKeyword() throws Exception {
        String keyword = "   ";

        // Invoke searchHelper with invalid keyword asynchronously
        CompletionStage<Result> resultStage = GeneralService.searchHelper(mockSearchService, mockSentimentActorProbe.ref(), keyword, mockRequest);
        Result result = resultStage.toCompletableFuture().get();

        // Assertions
        assertEquals(SEE_OTHER, result.status());
        assertEquals(routes.YoutubeController.index().url(), result.redirectLocation().orElse(""));
    }

    // g. Testing wordStatActorHelper with Exception
    @Test
    public void testWordStatActorHelper_ExceptionInActor() throws Exception {
        String keyword = "PlayFramework";

        // Invoke wordStatActorHelper asynchronously
        CompletionStage<Result> resultStage = GeneralService.wordStatActorHelper(mockSearchService, mockWordStatActorProbe.ref(), keyword, mockRequest);

        // Expect UpdateVideos message and reply
        WordStatMessages.UpdateVideos receivedUpdateVideos = mockWordStatActorProbe.expectMsgClass(WordStatMessages.UpdateVideos.class);
        assertEquals(keyword, receivedUpdateVideos.keyword);
        mockWordStatActorProbe.reply(null); // Simulate successful processing

        // Expect GetWordStats message and reply with exception
        WordStatMessages.GetWordStats receivedGetWordStats = mockWordStatActorProbe.expectMsgClass(WordStatMessages.GetWordStats.class);
        mockWordStatActorProbe.reply(new RuntimeException("Word stats retrieval failed"));

        // Await and retrieve the Result
        Result result = resultStage.toCompletableFuture().get();

        // Assertions
        assertEquals(INTERNAL_SERVER_ERROR, result.status());
        // Optionally, verify error content
        // Example:
        // assertTrue(contentAsString(result).contains("An error occurred while fetching word stats."));
    }

    // h. Testing searchHelper with Actor Errors
    @Test
    public void testSearchHelper_SentimentActorError() throws Exception {
        String keyword = "PlayFramework";

        // Prepare mock responses
        List<Video> videos = List.of(
                new Video("Video1", "Description1", "Channel1", "https://thumbnail1.url", "videoId1", "channelId1", "https://www.youtube.com/watch?v=videoId1", "2024-11-24"),
                new Video("Video2", "Description2", "Channel2", "https://thumbnail2.url", "videoId2", "channelId2", "https://www.youtube.com/watch?v=videoId2", "2024-11-24")
        );

        when(mockSearchService.searchVideos(eq("playframework"), eq(GeneralService.NUM_OF_RESULTS_SENTIMENT)))
                .thenReturn(CompletableFuture.completedFuture(videos));

        // Simulate adding search result
        doNothing().when(mockSearchService).addSearchResult(eq("sessionId"), eq("playframework"), eq(videos));

        when(mockSearchService.calculateSentiments(eq("sessionId")))
                .thenReturn(CompletableFuture.completedFuture(Map.of("playframework", "mixed")));

        when(mockSearchService.getSearchHistory(eq("sessionId")))
                .thenReturn(Map.of("playframework", videos.stream().limit(10).collect(Collectors.toList())));

        // Invoke searchHelper asynchronously
        CompletionStage<Result> resultStage = GeneralService.searchHelper(mockSearchService, mockSentimentActorProbe.ref(), keyword, mockRequest);

        // Expect AnalyzeVideos message and reply with exception
        SentimentMessages.AnalyzeVideos receivedAnalyzeVideos = mockSentimentActorProbe.expectMsgClass(SentimentMessages.AnalyzeVideos.class);
        assertEquals(videos, receivedAnalyzeVideos.getVideos());
        mockSentimentActorProbe.reply(new RuntimeException("Sentiment analysis failed"));

        // Await and retrieve the Result
        Result result = resultStage.toCompletableFuture().get();

        // Assertions
        assertEquals(INTERNAL_SERVER_ERROR, result.status());
        // Optionally, verify error content
        // Example:
        // assertTrue(contentAsString(result).contains("An error occurred while fetching search results."));
    }

    // i. Testing wordStatHelper with Exception in SearchService
    @Test
    public void testWordStatHelper_SearchServiceException() throws Exception {
        String keyword = "PlayFramework";

        // Mock SearchService to throw an exception during search
        when(mockSearchService.searchVideos(eq("playframework"), eq(GeneralService.NUM_OF_RESULTS_WORD_STATS)))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Search failed")));

        // Invoke wordStatHelper asynchronously
        CompletionStage<Result> resultStage = GeneralService.wordStatHelper(mockSearchService, mockWordStatService, keyword, mockRequest);
        Result result = resultStage.toCompletableFuture().get();

        // Assertions
        assertEquals(INTERNAL_SERVER_ERROR, result.status());
        // Optionally, verify error content
        // Example:
        // assertTrue(contentAsString(result).contains("An error occurred while fetching word stats."));
    }

    // j. Testing tagHelper with Exception in Actor Communication
    @Test
    public void testTagHelper_ActorCommunicationException() throws Exception {
        String videoId = "video123";

        // Invoke tagHelper asynchronously
        CompletionStage<Result> resultStage = GeneralService.tagHelper(mockTagActorProbe.ref(), videoId, mockRequest);

        // Expect GetVideo message and reply with exception
        TagMessages.GetVideo receivedGetVideo = mockTagActorProbe.expectMsgClass(TagMessages.GetVideo.class);
        assertEquals(videoId, receivedGetVideo.getVideoId());
        mockTagActorProbe.reply(new RuntimeException("Actor communication failed"));

        // Expect GetTags message and reply
        TagMessages.GetTags receivedGetTags = mockTagActorProbe.expectMsgClass(TagMessages.GetTags.class);
        assertEquals(videoId, receivedGetTags.getVideoId());
        mockTagActorProbe.reply(new TagMessages.GetTagsResponse(List.of("tag1", "tag2")));

        // Await and retrieve the Result
        Result result = resultStage.toCompletableFuture().get();

        // Assertions
        assertEquals(INTERNAL_SERVER_ERROR, result.status());
        // Optionally, verify error content
        // Example:
        // assertTrue(contentAsString(result).contains("An error occurred while fetching tags."));
    }
}
