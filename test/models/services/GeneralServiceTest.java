package models.services;

import actors.ChannelProfileActor;
import actors.SentimentActor;
import actors.SupervisorActor;
import actors.TagActor;
import akka.actor.ActorSystem;
import akka.stream.Materializer;
import controllers.YoutubeController;
import controllers.routes;
import models.entities.Video;
import models.services.*;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static play.mvc.Http.Status.OK;
import static play.mvc.Http.Status.SEE_OTHER;

public class GeneralServiceTest {

    private YoutubeController youtubeController;
    private SearchService mockSearchService;
    private SentimentService mockSentimentService;
    private WordStatService mockWordStatService;
    private ChannelProfileService mockChannelProfileService;
    private TagsService mockTagsService;
    private ActorSystem mockActorSystem;
    private Materializer mockMaterializer;
    private ChannelProfileActor mockChannelProfileActor;
    private TagActor mockTagActor;
    private YouTubeService mockYouTubeService;
    private SentimentActor mockSentimentActor;
    private SupervisorActor mockSupervisorActor;
    private HttpExecutionContext mockHttpExecutionContext;


    @Before
    public void setUp() {
        mockSearchService = mock(SearchService.class);
        mockWordStatService = mock(WordStatService.class);
        mockChannelProfileService = mock(ChannelProfileService.class);
        mockTagsService = mock(TagsService.class);
        mockActorSystem = mock(ActorSystem.class);
        mockHttpExecutionContext= mock(HttpExecutionContext.class);
        youtubeController = new YoutubeController(
                mockSearchService,
                mockWordStatService,
                mockChannelProfileService,
                mockTagsService,
                mockActorSystem,
                mockMaterializer,
                mockYouTubeService,
                mockSentimentService,
                mockHttpExecutionContext);
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

        Video video = new Video("Sample Title", "Description", "Channel", "https://thumbnail.url", "videoId", "channelId", "https://www.youtube.com/watch?v=videoId","2024-11-24");
        when(mockTagsService.getVideoByVideoId("videoId")).thenReturn(CompletableFuture.completedFuture(video));

        CompletionStage<Result> resultStage = youtubeController.tags("videoId", request);
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(OK, result.status());
    }

    @Test
    public void testSearch() {
        Http.Request request = mock(Http.Request.class);
        when(request.session()).thenReturn(new Http.Session(Map.of("sessionId", "existingSessionId")));

        List<Video> videos = List.of(new Video("Sample Video", "Description", "Channel", "https://thumbnail.url", "videoId", "channelId", "https://www.youtube.com/watch?v=videoId","2024-11-24"));
        when(mockSearchService.searchVideos(anyString(), anyInt())).thenReturn(CompletableFuture.completedFuture(videos));

        Map<String, String> individualSentiments = Map.of("keyword", "positive");
        when(mockSearchService.calculateSentiments(anyString())).thenReturn(CompletableFuture.completedFuture(individualSentiments));
        //when(mockSearchService.calculateSentiment(anyString(), anyInt())).thenReturn(CompletableFuture.completedFuture("positive"));

        // Mocking searchService.getSearchHistory to return some history
        when(mockSearchService.getSearchHistory("existingSessionId")).thenReturn(Collections.emptyMap());

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
                new Video("Video1", "Description1", "Sample Channel", "https://thumbnail1.url", "videoId1", "channelId1", "https://www.youtube.com/watch?v=videoId1","2024-11-24"),
                new Video("Video2", "Description2", "Sample Channel", "https://thumbnail2.url", "videoId2", "channelId2", "https://www.youtube.com/watch?v=videoId2","2024-11-24")
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

        List<Video> videos = List.of(new Video("Sample Video", "Description", "Channel", "https://thumbnail.url", "videoId", "channelId", "https://www.youtube.com/watch?v=videoId","2024-11-24"));
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

    /**
     * Tests the search method when there is no existing session.
     * Expects a new session ID to be generated and added to the response.
     */
    @Test
    public void testSearchNewSession() {
        // Mock a request with no sessionId
        Http.Request request = mock(Http.Request.class);
        when(request.session()).thenReturn(new Http.Session(Map.of())); // No sessionId

        // Mock the searchService methods
        List<Video> videos = List.of(
                new Video("New Session Video", "New Description", "New Channel", "https://newthumbnail.url", "newVideoId", "newChannelId", "https://www.youtube.com/watch?v=newVideoId","2024-11-24")
        );
        when(mockSearchService.searchVideos(anyString(), eq(10))) // 10 is DEFAULT_NUM_OF_RESULTS
                .thenReturn(CompletableFuture.completedFuture(videos));

        // Mock adding to search history (void method)
        doNothing().when(mockSearchService).addSearchResult(anyString(), anyString(), anyList());

        // Mock sentiment calculations
        Map<String, String> individualSentiments = Map.of("video1", "positive", "video2", "negative");
        when(mockSearchService.calculateSentiments(anyString()))
                .thenReturn(CompletableFuture.completedFuture(individualSentiments));

        // Mock getting search history to return empty map
        when(mockSearchService.getSearchHistory(anyString()))
                .thenReturn(Collections.emptyMap());

        // Execute the search method with a new session
        String searchKeyword = "newKeyword";
        CompletionStage<Result> resultStage = youtubeController.search(searchKeyword, request);
        Result result = resultStage.toCompletableFuture().join();

        // Verify the result
        assertEquals(OK, result.status());

        // Verify that a new sessionId was added
        String newSessionId = result.session().getOptional("sessionId").orElse(null);
        assertNotNull("A new session ID should be generated and added to the session", newSessionId);
        assertFalse("The new session ID should not be empty", newSessionId.isEmpty());

        // Verify that addSearchResultToHistory was called with the new sessionId
        verify(mockSearchService).addSearchResult(eq(newSessionId), eq(searchKeyword.toLowerCase()), eq(videos));

        // Verify that sentiment calculations were called with the new sessionId
        //verify(mockSearchService).calculateOverallSentiment(eq(newSessionId), eq(50));
        verify(mockSearchService).calculateSentiments(eq(newSessionId));
    }
}
