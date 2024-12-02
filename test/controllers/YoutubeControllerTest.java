package controllers;

import akka.actor.ActorSystem;
import akka.stream.Materializer;
import models.services.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.WebSocket;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static play.mvc.Http.Status.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the YoutubeController class. Verifies that the controller correctly handles user interactions,
 * manages sessions, and returns appropriate responses.
 */
public class YoutubeControllerTest {

    private ActorSystem system;
    private YoutubeController youtubeController;

    // Mocked dependencies
    private YouTubeService mockYouTubeService;
    private SentimentService mockSentimentService;
    private SearchService mockSearchService;
    private WordStatService mockWordStatService;
    private ChannelProfileService mockChannelProfileService;
    private TagsService mockTagsService;
    private Materializer mockMaterializer;

    // Mocked Http.Request objects
    private Http.Request mockRequestWithoutSession;
    private Http.Request mockRequestWithSession;

    @Before
    public void setUp() {
        // Create a new ActorSystem for each test
        system = ActorSystem.create("TestActorSystem-" + UUID.randomUUID());

        // Initialize mocks for dependencies
        mockYouTubeService = mock(YouTubeService.class);
        mockSentimentService = mock(SentimentService.class);
        mockSearchService = mock(SearchService.class);
        mockWordStatService = mock(WordStatService.class);
        mockChannelProfileService = mock(ChannelProfileService.class);
        mockTagsService = mock(TagsService.class);
        mockMaterializer = mock(Materializer.class);

        // Initialize YoutubeController
        youtubeController = new YoutubeController(
                mockSearchService,
                mockWordStatService,
                mockChannelProfileService,
                mockTagsService,
                system,
                mockMaterializer,
                mockYouTubeService,
                mockSentimentService,
                null // Execution context is not used in these tests
        );

        // Mock Http.Request without a session ID
        mockRequestWithoutSession = mock(Http.Request.class);
        when(mockRequestWithoutSession.session()).thenReturn(new Http.Session(Map.of()));

        // Mock Http.Request with a session ID
        mockRequestWithSession = mock(Http.Request.class);
        when(mockRequestWithSession.session()).thenReturn(new Http.Session(Map.of("sessionId", "existingSessionId")));
    }

    @After
    public void tearDown() {
        // Shutdown the ActorSystem after each test
        system.terminate();
        system = null;
    }

    @Test
    public void testIndexNewSession() throws Exception {
        // Act
        CompletionStage<Result> resultStage = youtubeController.index(mockRequestWithoutSession);
        Result result = resultStage.toCompletableFuture().get();

        // Assert
        assertEquals(OK, result.status());
        assertTrue(result.session().getOptional("sessionId").isPresent());
    }

    @Test
    public void testIndexExistingSession() throws Exception {
        // Act
        CompletionStage<Result> resultStage = youtubeController.index(mockRequestWithSession);
        Result result = resultStage.toCompletableFuture().get();

        // Assert
        assertEquals(OK, result.status());
    }

    @Test
    public void testTags_ErrorRetrieval() throws Exception {
        // Arrange
        String videoId = "video123";
        when(mockTagsService.getTagsByVideoId(videoId))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Tag retrieval failed")));

        // Act
        CompletionStage<Result> resultStage = youtubeController.tags(videoId, mockRequestWithoutSession);
        Result result = resultStage.toCompletableFuture().get();

        // Assert
        assertEquals(INTERNAL_SERVER_ERROR, result.status());
    }

    @Test
    public void testSearch_EmptyKeyword() throws Exception {
        // Arrange
        String keyword = "";

        // Act
        CompletionStage<Result> resultStage = youtubeController.search(keyword, mockRequestWithoutSession);
        Result result = resultStage.toCompletableFuture().get();

        // Assert
        assertEquals(SEE_OTHER, result.status());
    }

    @Test
    public void testSearch_NullKeyword() throws Exception {
        // Arrange
        String keyword = null;

        // Act
        CompletionStage<Result> resultStage = youtubeController.search(keyword, mockRequestWithoutSession);
        Result result = resultStage.toCompletableFuture().get();

        // Assert
        assertEquals(SEE_OTHER, result.status());
    }

    @Test
    public void testChannelProfile_ErrorRetrieval() throws Exception {
        // Arrange
        String channelId = "channel123";
        when(mockChannelProfileService.getChannelInfo(channelId))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Channel info retrieval failed")));

        // Act
        CompletionStage<Result> resultStage = youtubeController.channelProfile(channelId, mockRequestWithoutSession);
        Result result = resultStage.toCompletableFuture().get();

        // Assert
        assertEquals(INTERNAL_SERVER_ERROR, result.status());
    }

    @Test
    public void testWordStats_Simple() throws Exception {
        // Arrange
        String keyword = "testKeyword";
        Http.Request mockRequest = mock(Http.Request.class);

        // Act
        CompletionStage<Result> resultStage = youtubeController.wordStats(keyword, mockRequest);
        Result result = resultStage.toCompletableFuture().get();

        // Assert
        assertNotNull("Result should not be null", result);
    }

    @Test
    public void testWebSocketInitialization() {
        // Act
        WebSocket webSocket = youtubeController.ws();

        // Assert
        assertNotNull("WebSocket should not be null", webSocket);
    }

    @Test
    public void testWebSocketWithoutSessionId() {
        // Arrange
        Http.RequestHeader mockRequestHeader = mock(Http.RequestHeader.class);
        when(mockRequestHeader.getHeaders())
                .thenReturn(new Http.Headers(Map.of()));

        // Act
        WebSocket webSocket = youtubeController.ws();

        // Assert
        assertNotNull("WebSocket should be created", webSocket);
    }

}
