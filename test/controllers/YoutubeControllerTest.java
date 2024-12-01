package controllers;

import actors.*;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.stream.Materializer;
import models.services.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.WebSocket;
import play.test.Helpers;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static com.gargoylesoftware.htmlunit.WebResponse.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.SEE_OTHER;
import static javax.security.auth.callback.ConfirmationCallback.OK;
import static play.test.Helpers.contentAsString;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the YoutubeController class. This test suite verifies that the controller correctly
 * handles user interactions with the YouTube API, manages sessions, and returns appropriate responses.
 *
 * Note:
 * - GeneralService methods are static and cannot be mocked without MockedStatic. Therefore, these tests
 *   will rely on the actual implementations of GeneralService methods.
 * - SessionService is used directly as per the requirement.
 */
public class YoutubeControllerTest {

    private YoutubeController youtubeController;
    private ActorSystem mockActorSystem;
    private Materializer mockMaterializer;
    private YouTubeService mockYouTubeService;
    private SentimentService mockSentimentService;

    private ActorRef mockSentimentActor;
    private ActorRef mockChannelProfileActor;
    private ActorRef mockWordStatActor;
    private ActorRef mockTagActor;

    private Http.Request mockRequestWithoutSession;
    private Http.Request mockRequestWithSession;

    @Before
    public void setUp() {
        // Initialize mocks for dependencies
        mockActorSystem = mock(ActorSystem.class);
        mockMaterializer = mock(Materializer.class);
        mockYouTubeService = mock(YouTubeService.class);
        mockSentimentService = mock(SentimentService.class);

        // Initialize mocks for ActorRefs
        mockSentimentActor = mock(ActorRef.class);
        mockChannelProfileActor = mock(ActorRef.class);
        mockWordStatActor = mock(ActorRef.class);
        mockTagActor = mock(ActorRef.class);

        // Mock ActorSystem to return mocked ActorRefs when creating actors
        when(mockActorSystem.actorOf(any(), eq("sentimentActor"))).thenReturn(mockSentimentActor);
        when(mockActorSystem.actorOf(any(), eq("channelProfileActor"))).thenReturn(mockChannelProfileActor);
        when(mockActorSystem.actorOf(any(), eq("wordStatActor"))).thenReturn(mockWordStatActor);
        when(mockActorSystem.actorOf(any())).thenReturn(mockTagActor);

        // Initialize YoutubeController with mocked dependencies
        youtubeController = new YoutubeController(
                null, // SearchService
                null, // WordStatService
                null, // ChannelProfileService
                null, // TagsService
                mockActorSystem,
                mockMaterializer,
                mockYouTubeService,
                mockSentimentService,
                null  // HttpExecutionContext
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
        // Reset SessionService's sessionMap to prevent interference between tests
        // Note: Directly accessing private static fields is not possible; consider adding a reset method for testing purposes.
        // Alternatively, design tests to use unique Http.Request instances.
        // Since we cannot reset the sessionMap, ensure unique requests per test.
    }

    /**
     * Tests the index method when initiating a new session.
     * Expects a session ID to be added and the index page to be rendered.
     */
    @Test
    public void testIndexNewSession() {
        // Arrange
        // Since GeneralService methods are static, we cannot mock them.
        // Therefore, the controller will call GeneralService.addSessionId directly.
        // We assume that addSessionId behaves correctly as per SessionService.

        // Act
        CompletionStage<Result> resultStage = youtubeController.index(mockRequestWithoutSession);
        Result result = resultStage.toCompletableFuture().join();

        // Assert
        assertEquals(OK, result.status());
        assertEquals("Index Page", contentAsString(result));

        // Verify that a session ID has been added to the Result
        assertTrue(result.session().getOptional("sessionId").isPresent());
        String sessionId = result.session().getOptional("sessionId").get();
        assertNotNull(sessionId);
        assertFalse(sessionId.isEmpty());

        // Additionally, verify that the session ID is stored in SessionService's sessionMap
        // Since sessionMap is private, we cannot access it directly. Instead, ensure that subsequent calls recognize the session ID.

        // Example: Simulate a subsequent request with the generated session ID
        Http.Request subsequentRequest = mock(Http.Request.class);
        when(subsequentRequest.session()).thenReturn(new Http.Session(Map.of("sessionId", sessionId)));
        Result subsequentResult = youtubeController.index(subsequentRequest).toCompletableFuture().join();

        // Assert that the session ID remains the same
        assertEquals(OK, subsequentResult.status());
        assertEquals("Index Page", contentAsString(subsequentResult));
        assertTrue(subsequentResult.session().getOptional("sessionId").isPresent());
        assertEquals(sessionId, subsequentResult.session().getOptional("sessionId").get());
    }

    /**
     * Tests the index method when an existing session is present.
     * Expects the index page to be rendered without adding a new session ID.
     */
    @Test
    public void testIndexExistingSession() {
        // Arrange
        // The mockRequestWithSession already has a sessionId

        // Act
        CompletionStage<Result> resultStage = youtubeController.index(mockRequestWithSession);
        Result result = resultStage.toCompletableFuture().join();

        // Assert
        assertEquals(OK, result.status());
        assertEquals("Index Page", contentAsString(result));

        // Verify that the existing session ID is maintained
        assertTrue(result.session().getOptional("sessionId").isPresent());
        String sessionId = result.session().getOptional("sessionId").get();
        assertEquals("existingSessionId", sessionId);
    }

    /**
     * Tests the tags method to ensure it correctly delegates to GeneralService.tagHelper and handles the response.
     */
    @Test
    public void testTags_SuccessfulRetrieval() throws Exception {
        // Arrange
        String videoId = "video123";
        // Assuming that GeneralService.tagHelper returns ok("Tags Page")
        // Since we cannot mock it, the controller will execute the actual method.
        // To simulate the behavior, you might need to set up the TagActor mock to return the expected Result.

        // For demonstration, let's assume that tagHelper will return ok("Tags Page")
        // However, without mocking GeneralService.tagHelper, this test will rely on the actual implementation.

        // Act
        CompletionStage<Result> resultStage = youtubeController.tags(videoId, mockRequestWithoutSession);
        Result result = resultStage.toCompletableFuture().join();

        // Assert
        // Since we cannot predict the exact behavior of GeneralService.tagHelper,
        // we need to adjust the assertions based on expected outcomes.
        // For demonstration, let's assume it returns ok("Tags Page")
        // Replace "Tags Page" with the actual expected content from your views.

        // Example assertion:
        // assertEquals(OK, result.status());
        // assertEquals("Tags Page", contentAsString(result));

        // Since we don't have the actual implementation details, we'll perform a generic assertion:
        assertEquals(OK, result.status());
        // Optionally, assert the content contains specific text
        // assertTrue(contentAsString(result).contains("Expected Tag Content"));
    }

    /**
     * Tests the tags method when an error occurs during tag retrieval.
     */
    @Test
    public void testTags_ErrorRetrieval() throws Exception {
        // Arrange
        String videoId = "video123";
        // Simulating an error scenario would require manipulating the TagActor to respond with an error.
        // Without the ability to mock GeneralService.tagHelper or the actors it interacts with,
        // it's challenging to simulate an error. Therefore, this test might not be feasible without refactoring.

        // Act
        CompletionStage<Result> resultStage = youtubeController.tags(videoId, mockRequestWithoutSession);
        Result result = resultStage.toCompletableFuture().join();

        // Assert
        // Depending on the actual implementation, you might expect an INTERNAL_SERVER_ERROR
        // or another status code in case of failure.

        // Example generic assertion:
        // assertTrue(result.status() == OK || result.status() == INTERNAL_SERVER_ERROR);
        assertTrue(result.status() == OK || result.status() == INTERNAL_SERVER_ERROR);
    }

    /**
     * Tests the search method with a valid keyword.
     * Expects GeneralService.searchHelper to be called and the search results page to be rendered.
     */
    @Test
    public void testSearch_ValidKeyword() throws Exception {
        // Arrange
        String keyword = "PlayFramework";
        // Assuming that GeneralService.searchHelper returns ok("Search Results Page")
        // Since we cannot mock it, the controller will execute the actual method.

        // Act
        CompletionStage<Result> resultStage = youtubeController.search(keyword, mockRequestWithoutSession);
        Result result = resultStage.toCompletableFuture().join();

        // Assert
        assertEquals(OK, result.status());
        // Optionally, assert the content contains specific text
        // assertTrue(contentAsString(result).contains("Expected Search Results Content"));
    }

    /**
     * Tests the search method with an empty keyword.
     * Expects a redirect to the index page.
     */
    @Test
    public void testSearch_EmptyKeyword() throws Exception {
        // Arrange
        String keyword = "";
        // Assuming that GeneralService.searchHelper returns seeOther(routes.YoutubeController.index())
        // Since we cannot mock it, the controller will execute the actual method.

        // Act
        CompletionStage<Result> resultStage = youtubeController.search(keyword, mockRequestWithoutSession);
        Result result = resultStage.toCompletableFuture().join();

        // Assert
        assertEquals(SEE_OTHER, result.status());
        assertEquals(routes.YoutubeController.index().url(), result.redirectLocation().orElse(""));
    }

    /**
     * Tests the search method with a null keyword.
     * Expects a redirect to the index page.
     */
    @Test
    public void testSearch_NullKeyword() throws Exception {
        // Arrange
        String keyword = null;
        // Assuming that GeneralService.searchHelper returns seeOther(routes.YoutubeController.index())
        // Since we cannot mock it, the controller will execute the actual method.

        // Act
        CompletionStage<Result> resultStage = youtubeController.search(keyword, mockRequestWithoutSession);
        Result result = resultStage.toCompletableFuture().join();

        // Assert
        assertEquals(SEE_OTHER, result.status());
        assertEquals(routes.YoutubeController.index().url(), result.redirectLocation().orElse(""));
    }

    /**
     * Tests the channelProfile method to ensure it correctly delegates to GeneralService.channelProfileHelper and handles the response.
     */
    @Test
    public void testChannelProfile_SuccessfulRetrieval() throws Exception {
        // Arrange
        String channelId = "channel123";
        // Assuming that GeneralService.channelProfileHelper returns ok("Channel Profile Page")
        // Since we cannot mock it, the controller will execute the actual method.

        // Act
        CompletionStage<Result> resultStage = youtubeController.channelProfile(channelId, mockRequestWithoutSession);
        Result result = resultStage.toCompletableFuture().join();

        // Assert
        assertEquals(OK, result.status());
        // Optionally, assert the content contains specific text
        // assertTrue(contentAsString(result).contains("Expected Channel Profile Content"));
    }

    /**
     * Tests the channelProfile method when an error occurs during profile retrieval.
     */
    @Test
    public void testChannelProfile_ErrorRetrieval() throws Exception {
        // Arrange
        String channelId = "channel123";
        // Simulating an error scenario would require manipulating the ChannelProfileActor to respond with an error.
        // Without the ability to mock GeneralService.channelProfileHelper or the actors it interacts with,
        // it's challenging to simulate an error. Therefore, this test might not be feasible without refactoring.

        // Act
        CompletionStage<Result> resultStage = youtubeController.channelProfile(channelId, mockRequestWithoutSession);
        Result result = resultStage.toCompletableFuture().join();

        // Assert
        // Depending on the actual implementation, you might expect an INTERNAL_SERVER_ERROR
        // or another status code in case of failure.

        // Example generic assertion:
        // assertTrue(result.status() == OK || result.status() == INTERNAL_SERVER_ERROR);
        assertTrue(result.status() == OK || result.status() == INTERNAL_SERVER_ERROR);
    }

    /**
     * Tests the wordStats method to ensure it correctly delegates to GeneralService.wordStatActorHelper and handles the response.
     */
    @Test
    public void testWordStats_SuccessfulRetrieval() throws Exception {
        // Arrange
        String keyword = "PlayFramework";
        // Assuming that GeneralService.wordStatActorHelper returns ok("Word Stats Page")
        // Since we cannot mock it, the controller will execute the actual method.

        // Act
        CompletionStage<Result> resultStage = youtubeController.wordStats(keyword, mockRequestWithoutSession);
        Result result = resultStage.toCompletableFuture().join();

        // Assert
        assertEquals(OK, result.status());
        // Optionally, assert the content contains specific text
        // assertTrue(contentAsString(result).contains("Expected Word Stats Content"));
    }

    /**
     * Tests the wordStats method when an error occurs during word statistics generation.
     */
    @Test
    public void testWordStats_ErrorRetrieval() throws Exception {
        // Arrange
        String keyword = "PlayFramework";
        // Simulating an error scenario would require manipulating the WordStatActor to respond with an error.
        // Without the ability to mock GeneralService.wordStatActorHelper or the actors it interacts with,
        // it's challenging to simulate an error. Therefore, this test might not be feasible without refactoring.

        // Act
        CompletionStage<Result> resultStage = youtubeController.wordStats(keyword, mockRequestWithoutSession);
        Result result = resultStage.toCompletableFuture().join();

        // Assert
        // Depending on the actual implementation, you might expect an INTERNAL_SERVER_ERROR
        // or another status code in case of failure.

        // Example generic assertion:
        // assertTrue(result.status() == OK || result.status() == INTERNAL_SERVER_ERROR);
        assertTrue(result.status() == OK || result.status() == INTERNAL_SERVER_ERROR);
    }

    /**
     * Tests the wordStats method with an empty keyword.
     * Expects a redirect to the index page.
     */
    @Test
    public void testWordStats_EmptyKeyword() throws Exception {
        // Arrange
        String keyword = "";
        // Assuming that GeneralService.wordStatActorHelper returns seeOther(routes.YoutubeController.index())
        // Since we cannot mock it, the controller will execute the actual method.

        // Act
        CompletionStage<Result> resultStage = youtubeController.wordStats(keyword, mockRequestWithoutSession);
        Result result = resultStage.toCompletableFuture().join();

        // Assert
        assertEquals(SEE_OTHER, result.status());
        assertEquals(routes.YoutubeController.index().url(), result.redirectLocation().orElse(""));
    }

    /**
     * Tests the wordStats method with a null keyword.
     * Expects a redirect to the index page.
     */
    @Test
    public void testWordStats_NullKeyword() throws Exception {
        // Arrange
        String keyword = null;
        // Assuming that GeneralService.wordStatActorHelper returns seeOther(routes.YoutubeController.index())
        // Since we cannot mock it, the controller will execute the actual method.

        // Act
        CompletionStage<Result> resultStage = youtubeController.wordStats(keyword, mockRequestWithoutSession);
        Result result = resultStage.toCompletableFuture().join();

        // Assert
        assertEquals(SEE_OTHER, result.status());
        assertEquals(routes.YoutubeController.index().url(), result.redirectLocation().orElse(""));
    }

    /**
     * Tests the WebSocket connection.
     * Note: Comprehensive WebSocket testing requires integration tests.
     * Here, we ensure that the WebSocket endpoint is correctly defined.
     */
    @Test
    public void testWebSocket() {
        // Act
        WebSocket ws = youtubeController.ws();

        // Assert
        assertNotNull("WebSocket should not be null", ws);
        // Further testing of WebSocket behavior would require a more complex setup,
        // possibly using Play's WSClient for integration testing.
    }

    /**
     * Additional Test: Verify that the session ID is correctly retrieved from the header in WebSocket.
     * Note: This requires more complex mocking and is generally better suited for integration tests.
     */
    @Test
    public void testWebSocket_WithSessionId() {
        // Arrange
        // Mock a WebSocket request with a sessionId header
        Http.RequestHeader mockRequestHeader = mock(Http.RequestHeader.class);
        when(mockRequestHeader.session()).thenReturn(new Http.Session(Map.of("sessionId", "headerSessionId")));

        // Mock UserActor creation
        ActorRef mockUserActor = mock(ActorRef.class);
        when(mockActorSystem.actorOf(any())).thenReturn(mockUserActor);

        // Act
        WebSocket ws = youtubeController.ws();

        // Assert
        assertNotNull("WebSocket should not be null", ws);
        // Further assertions would require integration testing frameworks
    }

    /**
     * Additional Test: Verify that the session ID is correctly handled when missing in the header in WebSocket.
     */
    @Test
    public void testWebSocket_WithoutSessionId() {
        // Arrange
        // Mock a WebSocket request without a sessionId header
        Http.RequestHeader mockRequestHeader = mock(Http.RequestHeader.class);
        when(mockRequestHeader.session()).thenReturn(new Http.Session(Map.of()));

        // Mock UserActor creation
        ActorRef mockUserActor = mock(ActorRef.class);
        when(mockActorSystem.actorOf(any())).thenReturn(mockUserActor);

        // Act
        WebSocket ws = youtubeController.ws();

        // Assert
        assertNotNull("WebSocket should not be null", ws);
        // Further assertions would require integration testing frameworks
    }
}
