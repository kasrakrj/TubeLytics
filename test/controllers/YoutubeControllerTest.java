package controllers;

import models.entities.Video;
import models.services.*;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.mvc.Result;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static play.mvc.Http.Status.OK;
import static play.mvc.Http.Status.SEE_OTHER;

public class YoutubeControllerTest {

    private YoutubeController youtubeController;
    private SearchService mockSearchService;
    private SentimentService mockSentimentService;
    private WordStatService mockWordStatService;
    private ChannelProfileService mockChannelProfileService;
    private TagsService mockTagsService;

    @Before
    public void setUp() {
        mockSearchService = mock(SearchService.class);
        mockSentimentService = mock(SentimentService.class);
        mockWordStatService = mock(WordStatService.class);
        mockChannelProfileService = mock(ChannelProfileService.class);
        mockTagsService = mock(TagsService.class);

        youtubeController = new YoutubeController(
                mockSearchService, mockSentimentService, mockWordStatService, mockChannelProfileService, mockTagsService);
    }

    @Test
    public void testIndexNewSession() {
        Http.Request request = mock(Http.Request.class);
        when(request.session()).thenReturn(new Http.Session(Map.of()));

        CompletionStage<Result> resultStage = youtubeController.index(request);
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(OK, result.status());
        assertNotNull("Session ID should be added for a new session", result.session().getOptional("sessionId").orElse(null));
    }

    @Test
    public void testIndexExistingSession() {
        Http.Request request = mock(Http.Request.class);
        when(request.session()).thenReturn(new Http.Session(Map.of("sessionId", "existingSessionId")));

        CompletionStage<Result> resultStage = youtubeController.index(request);
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(OK, result.status());
    }

    @Test
    public void testTags() {
        Http.Request request = mock(Http.Request.class);
        when(request.session()).thenReturn(new Http.Session(Map.of("sessionId", "existingSessionId")));

        Video video = new Video("Sample Title", "Description", "Channel", "https://thumbnail.url", "videoId", "channelId", "https://www.youtube.com/watch?v=videoId");
        when(mockTagsService.getVideoByVideoId("videoId")).thenReturn(CompletableFuture.completedFuture(video));
        when(mockTagsService.getTagsByVideo(any(Video.class))).thenReturn(CompletableFuture.completedFuture(List.of("tag1", "tag2")));

        CompletionStage<Result> resultStage = youtubeController.tags("videoId", request);
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(OK, result.status());
    }

    @Test
    public void testSearch() {
        Http.Request request = mock(Http.Request.class);
        when(request.session()).thenReturn(new Http.Session(Map.of("sessionId", "existingSessionId")));

        List<Video> videos = List.of(new Video("Sample Video", "Description", "Channel", "https://thumbnail.url", "videoId", "channelId", "https://www.youtube.com/watch?v=videoId"));
        when(mockSearchService.searchVideos(anyString(), anyInt())).thenReturn(CompletableFuture.completedFuture(videos));

        Map<String, String> individualSentiments = Map.of("keyword", "positive");
        when(mockSearchService.calculateIndividualSentiments(anyString())).thenReturn(CompletableFuture.completedFuture(individualSentiments));
        when(mockSearchService.calculateOverallSentiment(anyString(), anyInt())).thenReturn(CompletableFuture.completedFuture("positive"));

        CompletionStage<Result> resultStage = youtubeController.search("keyword", request);
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(OK, result.status());
    }

    @Test
    public void testSearchWithEmptyKeyword() {
        Http.Request request = mock(Http.Request.class);
        when(request.session()).thenReturn(new Http.Session(Map.of("sessionId", "existingSessionId")));

        CompletionStage<Result> resultStage = youtubeController.search("", request);
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(SEE_OTHER, result.status()); // 303 See Other
        assertEquals(routes.YoutubeController.index().url(), result.redirectLocation().orElse(""));
    }

    @Test
    public void testSearchWithNullKeyword() {
        Http.Request request = mock(Http.Request.class);
        when(request.session()).thenReturn(new Http.Session(Map.of("sessionId", "existingSessionId")));

        CompletionStage<Result> resultStage = youtubeController.search(null, request);
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(SEE_OTHER, result.status()); // 303 See Other
        assertEquals(routes.YoutubeController.index().url(), result.redirectLocation().orElse(""));
    }

    @Test
    public void testChannelProfile() {
        Http.Request request = mock(Http.Request.class);
        when(request.session()).thenReturn(new Http.Session(Map.of("sessionId", "existingSessionId")));

        JSONObject channelInfo = new JSONObject();
        channelInfo.put("title", "Sample Channel");
        channelInfo.put("description", "Sample Channel Description");
        channelInfo.put("subscriberCount", 1000);
        channelInfo.put("videoCount", 50);

        when(mockChannelProfileService.getChannelInfo("channelId")).thenReturn(CompletableFuture.completedFuture(channelInfo));

        List<Video> videos = List.of(
                new Video("Video1", "Description1", "Sample Channel", "https://thumbnail1.url", "videoId1", "channelId1", "https://www.youtube.com/watch?v=videoId1"),
                new Video("Video2", "Description2", "Sample Channel", "https://thumbnail2.url", "videoId2", "channelId2", "https://www.youtube.com/watch?v=videoId2")
        );
        when(mockChannelProfileService.getChannelVideos("channelId", 10)).thenReturn(CompletableFuture.completedFuture(videos));

        CompletionStage<Result> resultStage = youtubeController.channelProfile("channelId", request);
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(OK, result.status());
    }

    @Test
    public void testWordStats() {
        Http.Request request = mock(Http.Request.class);
        when(request.session()).thenReturn(new Http.Session(Map.of("sessionId", "existingSessionId")));

        List<Video> videos = List.of(new Video("Sample Video", "Description", "Channel", "https://thumbnail.url", "videoId", "channelId", "https://www.youtube.com/watch?v=videoId"));
        when(mockSearchService.searchVideos(anyString(), anyInt())).thenReturn(CompletableFuture.completedFuture(videos));

        Map<String, Long> wordStats = Map.of("sample", 1L);
        when(mockWordStatService.createWordStats(videos)).thenReturn(wordStats);

        CompletionStage<Result> resultStage = youtubeController.wordStats("keyword", request);
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(OK, result.status());
    }

    /**
     * Tests the wordStats method when the keyword is null.
     * Expects a redirect to the index page.
     */
    @Test
    public void testWordStatsWithNullKeyword() {
        Http.Request request = mock(Http.Request.class);
        when(request.session()).thenReturn(new Http.Session(Map.of("sessionId", "existingSessionId")));

        CompletionStage<Result> resultStage = youtubeController.wordStats(null, request);
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(SEE_OTHER, result.status()); // 303 See Other
        assertEquals(routes.YoutubeController.index().url(), result.redirectLocation().orElse(""));
    }

    /**
     * Tests the wordStats method when the keyword is an empty string or contains only whitespace.
     * Expects a redirect to the index page.
     */
    @Test
    public void testWordStatsWithEmptyKeyword() {
        Http.Request request = mock(Http.Request.class);
        when(request.session()).thenReturn(new Http.Session(Map.of("sessionId", "existingSessionId")));

        // Test with empty string
        CompletionStage<Result> resultStageEmpty = youtubeController.wordStats("", request);
        Result resultEmpty = resultStageEmpty.toCompletableFuture().join();

        assertEquals(SEE_OTHER, resultEmpty.status()); // 303 See Other
        assertEquals(routes.YoutubeController.index().url(), resultEmpty.redirectLocation().orElse(""));

        // Test with whitespace-only string
        CompletionStage<Result> resultStageWhitespace = youtubeController.wordStats("   ", request);
        Result resultWhitespace = resultStageWhitespace.toCompletableFuture().join();

        assertEquals(SEE_OTHER, resultWhitespace.status()); // 303 See Other
        assertEquals(routes.YoutubeController.index().url(), resultWhitespace.redirectLocation().orElse(""));
    }
}
