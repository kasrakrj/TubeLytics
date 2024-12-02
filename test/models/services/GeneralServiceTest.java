package models.services;

import actors.ChannelProfileMessages;
import actors.SentimentMessages;
import actors.TagMessages;
import actors.WordStatMessages;
import akka.actor.ActorRef;
import akka.pattern.Patterns;
import controllers.routes;
import models.entities.Video;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.mvc.Result;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.SEE_OTHER;
import static javax.security.auth.callback.ConfirmationCallback.OK;
import static models.services.SessionService.addSessionId;
import static models.services.SessionService.getSessionId;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static play.mvc.Results.*;

public class GeneralServiceTest {

    // Mocked dependencies
    private ActorRef mockTagActor;
    private ActorRef mockChannelProfileActor;
    private ActorRef mockWordStatActor;
    private ActorRef mockSentimentActor;
    private SearchService mockSearchService;
    private WordStatService mockWordStatService;
    private Http.Request mockRequest;

    @Before
    public void setUp() {
        // Initialize mocks
        mockTagActor = mock(ActorRef.class);
        mockChannelProfileActor = mock(ActorRef.class);
        mockWordStatActor = mock(ActorRef.class);
        mockSentimentActor = mock(ActorRef.class);
        mockSearchService = mock(SearchService.class);
        mockWordStatService = mock(WordStatService.class);
        mockRequest = mock(Http.Request.class);

        // Mock SessionService methods if using Mockito's inline mocking
        // Otherwise, ensure SessionService behaves as expected
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
        Http.Request request = mock(Http.Request.class);
        when(request.session()).thenReturn(new Http.Session(Map.of("sessionId", "session1")));

        // Mock TagActor responses
        TagMessages.GetVideoResponse videoResponse = new TagMessages.GetVideoResponse(
                new Video("Sample Title", "Description", "Channel", "https://thumbnail.url", "videoId123", "channelId123", "https://www.youtube.com/watch?v=videoId123", "2024-11-24")
        );
        TagMessages.GetTagsResponse tagsResponse = new TagMessages.GetTagsResponse(
                List.of("tag1", "tag2", "tag3")
        );

        // Mock Patterns.ask to return the mocked responses
        when(Patterns.ask(eq(mockTagActor), any(TagMessages.GetVideo.class), any(Duration.class)))
                .thenReturn(CompletableFuture.completedFuture(videoResponse));
        when(Patterns.ask(eq(mockTagActor), any(TagMessages.GetTags.class), any(Duration.class)))
                .thenReturn(CompletableFuture.completedFuture(tagsResponse));

        // Invoke tagHelper
        CompletionStage<Result> resultStage = GeneralService.tagHelper(mockTagActor, videoId, request);
        Result result = resultStage.toCompletableFuture().get();

        // Assertions
        assertEquals(OK, result.status());
        // Additional assertions can be made to verify the content of the Result if necessary
    }

    @Test
    public void testTagHelper_VideoError() throws Exception {
        String videoId = "video123";
        Http.Request request = mock(Http.Request.class);
        when(request.session()).thenReturn(new Http.Session(Map.of("sessionId", "session1")));

        // Mock TagActor responses with an error
        TagMessages.TagsError errorResponse = new TagMessages.TagsError("Failed to fetch video.");
        TagMessages.GetTagsResponse tagsResponse = new TagMessages.GetTagsResponse(
                List.of("tag1", "tag2", "tag3")
        );

        // Mock Patterns.ask to return the mocked responses
        when(Patterns.ask(eq(mockTagActor), any(TagMessages.GetVideo.class), any(Duration.class)))
                .thenReturn(CompletableFuture.completedFuture(errorResponse));
        when(Patterns.ask(eq(mockTagActor), any(TagMessages.GetTags.class), any(Duration.class)))
                .thenReturn(CompletableFuture.completedFuture(tagsResponse));

        // Invoke tagHelper
        CompletionStage<Result> resultStage = GeneralService.tagHelper(mockTagActor, videoId, request);
        Result result = resultStage.toCompletableFuture().get();

        // Assertions
        assertEquals(INTERNAL_SERVER_ERROR, result.status());
        // Additional assertions can be made to verify the error page content
    }

    @Test
    public void testTagHelper_TagsError() throws Exception {
        String videoId = "video123";
        Http.Request request = mock(Http.Request.class);
        when(request.session()).thenReturn(new Http.Session(Map.of("sessionId", "session1")));

        // Mock TagActor responses with an error in tags
        TagMessages.GetVideoResponse videoResponse = new TagMessages.GetVideoResponse(
                new Video("Sample Title", "Description", "Channel", "https://thumbnail.url", "videoId123", "channelId123", "https://www.youtube.com/watch?v=videoId123", "2024-11-24")
        );
        TagMessages.TagsError errorResponse = new TagMessages.TagsError("Failed to fetch tags.");

        // Mock Patterns.ask to return the mocked responses
        when(Patterns.ask(eq(mockTagActor), any(TagMessages.GetVideo.class), any(Duration.class)))
                .thenReturn(CompletableFuture.completedFuture(videoResponse));
        when(Patterns.ask(eq(mockTagActor), any(TagMessages.GetTags.class), any(Duration.class)))
                .thenReturn(CompletableFuture.completedFuture(errorResponse));

        // Invoke tagHelper
        CompletionStage<Result> resultStage = GeneralService.tagHelper(mockTagActor, videoId, request);
        Result result = resultStage.toCompletableFuture().get();

        // Assertions
        assertEquals(INTERNAL_SERVER_ERROR, result.status());
        // Additional assertions can be made to verify the error page content
    }

    // c. Testing channelProfileHelper
    @Test
    public void testChannelProfileHelper_SuccessfulRetrieval() throws Exception {
        String channelId = "channel123";
        Http.Request request = mock(Http.Request.class);
        when(request.session()).thenReturn(new Http.Session(Map.of("sessionId", "session1")));

        // Mock ChannelProfileActor responses
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

        // Mock Patterns.ask to return the mocked responses
        when(Patterns.ask(eq(mockChannelProfileActor), any(ChannelProfileMessages.GetChannelInfo.class), any(Duration.class)))
                .thenReturn(CompletableFuture.completedFuture(infoResponse));
        when(Patterns.ask(eq(mockChannelProfileActor), any(ChannelProfileMessages.GetChannelVideos.class), any(Duration.class)))
                .thenReturn(CompletableFuture.completedFuture(videosResponse));

        // Invoke channelProfileHelper
        CompletionStage<Result> resultStage = GeneralService.channelProfileHelper(mockChannelProfileActor, channelId, request);
        Result result = resultStage.toCompletableFuture().get();

        // Assertions
        assertEquals(OK, result.status());
        // Additional assertions can be made to verify the content of the Result if necessary
    }

    @Test
    public void testChannelProfileHelper_ErrorRetrieval() throws Exception {
        String channelId = "channel123";
        Http.Request request = mock(Http.Request.class);
        when(request.session()).thenReturn(new Http.Session(Map.of("sessionId", "session1")));

        // Mock ChannelProfileActor responses with an error
        ChannelProfileMessages.ChannelProfileError errorResponse = new ChannelProfileMessages.ChannelProfileError("Failed to fetch channel profile.");
        ChannelProfileMessages.ChannelVideosResponse videosResponse = new ChannelProfileMessages.ChannelVideosResponse(
                List.of(new Video("Video1", "Description1", "Channel1", "https://thumbnail1.url", "videoId1", "channelId1", "https://www.youtube.com/watch?v=videoId1", "2024-11-24"))
        );

        // Mock Patterns.ask to return the mocked responses
        when(Patterns.ask(eq(mockChannelProfileActor), any(ChannelProfileMessages.GetChannelInfo.class), any(Duration.class)))
                .thenReturn(CompletableFuture.completedFuture(errorResponse));
        when(Patterns.ask(eq(mockChannelProfileActor), any(ChannelProfileMessages.GetChannelVideos.class), any(Duration.class)))
                .thenReturn(CompletableFuture.completedFuture(videosResponse));

        // Invoke channelProfileHelper
        CompletionStage<Result> resultStage = GeneralService.channelProfileHelper(mockChannelProfileActor, channelId, request);
        Result result = resultStage.toCompletableFuture().get();

        // Assertions
        assertEquals(INTERNAL_SERVER_ERROR, result.status());
        // Additional assertions can be made to verify the error page content
    }

    // d. Testing wordStatHelper
    @Test
    public void testWordStatHelper_ValidKeyword() throws Exception {
        String keyword = "PlayFramework";
        Http.Request request = mock(Http.Request.class);
        when(request.session()).thenReturn(new Http.Session(Map.of("sessionId", "session1")));

        List<Video> videos = List.of(
                new Video("Video1", "Description1", "Channel1", "https://thumbnail1.url", "videoId1", "channelId1", "https://www.youtube.com/watch?v=videoId1", "2024-11-24"),
                new Video("Video2", "Description2", "Channel2", "https://thumbnail2.url", "videoId2", "channelId2", "https://www.youtube.com/watch?v=videoId2", "2024-11-24")
        );

        when(mockSearchService.searchVideos(eq("playframework"), eq(50)))
                .thenReturn(CompletableFuture.completedFuture(videos));
        doNothing().when(mockSearchService).addSearchResult(eq("session1"), eq("playframework"), eq(videos));

        Map<String, Long> wordStats = Map.of("play", 2L, "framework", 1L);
        when(mockWordStatService.createWordStats(eq(videos))).thenReturn(wordStats);

        // Invoke wordStatHelper
        CompletionStage<Result> resultStage = GeneralService.wordStatHelper(mockSearchService, mockWordStatService, keyword, request);
        Result result = resultStage.toCompletableFuture().get();

        // Assertions
        assertEquals(OK, result.status());
        // Additional assertions can be made to verify the content of the Result if necessary
    }

    @Test
    public void testWordStatHelper_InvalidKeyword() throws Exception {
        String keyword = "   ";
        Http.Request request = mock(Http.Request.class);
        when(request.session()).thenReturn(new Http.Session(Map.of("sessionId", "session1")));

        // Invoke wordStatHelper with invalid keyword
        CompletionStage<Result> resultStage = GeneralService.wordStatHelper(mockSearchService, mockWordStatService, keyword, request);
        Result result = resultStage.toCompletableFuture().get();

        // Assertions
        assertEquals(SEE_OTHER, result.status());
        assertEquals(routes.YoutubeController.index().url(), result.redirectLocation().orElse(""));
    }

    // e. Testing wordStatActorHelper


    // f. Testing searchHelper
    @Test
    public void testSearchHelper_ValidKeyword_SuccessfulRetrieval() throws Exception {
        String keyword = "PlayFramework";
        Http.Request request = mock(Http.Request.class);
        when(request.session()).thenReturn(new Http.Session(Map.of("sessionId", "session1")));

        List<Video> videos = List.of(
                new Video("Video1", "Description1", "Channel1", "https://thumbnail1.url", "videoId1", "channelId1", "https://www.youtube.com/watch?v=videoId1", "2024-11-24"),
                new Video("Video2", "Description2", "Channel2", "https://thumbnail2.url", "videoId2", "channelId2", "https://www.youtube.com/watch?v=videoId2", "2024-11-24")
        );

        when(mockSearchService.searchVideos(eq("playframework"), eq(50)))
                .thenReturn(CompletableFuture.completedFuture(videos));
        doNothing().when(mockSearchService).addSearchResult(eq("session1"), eq("playframework"), eq(videos.stream().limit(10).collect(Collectors.toList())));

        // Mock sentiment actor response
        Map<String, String> individualSentiments = Map.of("videoId1", "positive", "videoId2", "negative");
        when(Patterns.ask(eq(mockSentimentActor), any(SentimentMessages.AnalyzeVideos.class), any(Duration.class)))
                .thenReturn(CompletableFuture.completedFuture(individualSentiments));

        when(mockSearchService.calculateSentiments(eq("session1")))
                .thenReturn(CompletableFuture.completedFuture(Map.of("playframework", "mixed")));

        // Invoke searchHelper
        CompletionStage<Result> resultStage = GeneralService.searchHelper(mockSearchService, mockSentimentActor, keyword, request);
        Result result = resultStage.toCompletableFuture().get();

        // Assertions
        assertEquals(OK, result.status());
        // Additional assertions can be made to verify the content of the Result if necessary
    }

    @Test
    public void testSearchHelper_InvalidKeyword() throws Exception {
        String keyword = "   ";
        Http.Request request = mock(Http.Request.class);
        when(request.session()).thenReturn(new Http.Session(Map.of("sessionId", "session1")));

        // Invoke searchHelper with invalid keyword
        CompletionStage<Result> resultStage = GeneralService.searchHelper(mockSearchService, mockSentimentActor, keyword, request);
        Result result = resultStage.toCompletableFuture().get();

        // Assertions
        assertEquals(SEE_OTHER, result.status());
        assertEquals(routes.YoutubeController.index().url(), result.redirectLocation().orElse(""));
    }

    // g. Testing wordStatHelper with Actor
    @Test
    public void testWordStatActorHelper_SuccessfulRetrieval() throws Exception {
        String keyword = "PlayFramework";
        Http.Request request = mock(Http.Request.class);
        when(request.session()).thenReturn(new Http.Session(Map.of("sessionId", "session1")));

        // Mock actor responses
        Map<String, Long> wordStats = Map.of("play", 2L, "framework", 1L);
        WordStatMessages.GetWordStats statsResponse = new WordStatMessages.GetWordStats();

        when(Patterns.ask(eq(mockWordStatActor), any(WordStatMessages.GetWordStats.class), any(Duration.class)))
                .thenReturn(CompletableFuture.completedFuture(statsResponse));

        // Invoke wordStatActorHelper
        CompletionStage<Result> resultStage = GeneralService.wordStatActorHelper(mockSearchService, mockWordStatActor, keyword, request);
        Result result = resultStage.toCompletableFuture().get();

        // Assertions
        assertEquals(OK, result.status());
        // Additional assertions can be made to verify the content of the Result if necessary

        // Verify that the actor was asked to update videos
        verify(mockWordStatActor, times(1)).tell(any(WordStatMessages.UpdateVideos.class), any());
    }


    // h. Testing searchHelper with Actor Errors
    @Test
    public void testSearchHelper_SentimentActorError() throws Exception {
        String keyword = "PlayFramework";
        Http.Request request = mock(Http.Request.class);
        when(request.session()).thenReturn(new Http.Session(Map.of("sessionId", "session1")));

        List<Video> videos = List.of(
                new Video("Video1", "Description1", "Channel1", "https://thumbnail1.url", "videoId1", "channelId1", "https://www.youtube.com/watch?v=videoId1", "2024-11-24"),
                new Video("Video2", "Description2", "Channel2", "https://thumbnail2.url", "videoId2", "channelId2", "https://www.youtube.com/watch?v=videoId2", "2024-11-24")
        );

        when(mockSearchService.searchVideos(eq("playframework"), eq(50)))
                .thenReturn(CompletableFuture.completedFuture(videos));
        doNothing().when(mockSearchService).addSearchResult(eq("session1"), eq("playframework"), eq(videos.stream().limit(10).collect(Collectors.toList())));

        // Mock sentiment actor to throw an exception
        when(Patterns.ask(eq(mockSentimentActor), any(SentimentMessages.AnalyzeVideos.class), any(Duration.class)))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Sentiment analysis failed")));

        // Invoke searchHelper
        CompletionStage<Result> resultStage = GeneralService.searchHelper(mockSearchService, mockSentimentActor, keyword, request);
        Result result = resultStage.toCompletableFuture().get();

        // Assertions
        assertEquals(INTERNAL_SERVER_ERROR, result.status());
        // Additional assertions can be made to verify the error page content
    }

    // i. Testing wordStatHelper with Exception in SearchService
    @Test
    public void testWordStatHelper_SearchServiceException() throws Exception {
        String keyword = "PlayFramework";
        Http.Request request = mock(Http.Request.class);
        when(request.session()).thenReturn(new Http.Session(Map.of("sessionId", "session1")));

        // Mock SearchService to throw an exception
        when(mockSearchService.searchVideos(eq("playframework"), eq(50)))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Search failed")));

        // Invoke wordStatHelper
        CompletionStage<Result> resultStage = GeneralService.wordStatHelper(mockSearchService, mockWordStatService, keyword, request);
        Result result = resultStage.toCompletableFuture().get();

        // Assertions
        assertEquals(INTERNAL_SERVER_ERROR, result.status());
        // Additional assertions can be made to verify the error page content
    }

    // j. Testing tagHelper with Exception in Actor Communication
    @Test
    public void testTagHelper_ActorCommunicationException() throws Exception {
        String videoId = "video123";
        Http.Request request = mock(Http.Request.class);
        when(request.session()).thenReturn(new Http.Session(Map.of("sessionId", "session1")));

        // Mock Patterns.ask to throw an exception
        when(Patterns.ask(eq(mockTagActor), any(TagMessages.GetVideo.class), any(Duration.class)))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Actor communication failed")));
        when(Patterns.ask(eq(mockTagActor), any(TagMessages.GetTags.class), any(Duration.class)))
                .thenReturn(CompletableFuture.completedFuture(new TagMessages.GetTagsResponse(List.of("tag1", "tag2"))));

        // Invoke tagHelper
        CompletionStage<Result> resultStage = GeneralService.tagHelper(mockTagActor, videoId, request);
        Result result = resultStage.toCompletableFuture().get();

        // Assertions
        assertEquals(INTERNAL_SERVER_ERROR, result.status());
        // Additional assertions can be made to verify the error page content
    }
}
