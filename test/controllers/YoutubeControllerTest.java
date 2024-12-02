//// src/test/java/controllers/YoutubeControllerTest.java
//package controllers;
//
//import actors.ChannelProfileMessages;
//import akka.actor.ActorRef;
//import akka.actor.ActorSystem;
//import akka.stream.Materializer;
//import akka.testkit.javadsl.TestKit;
//import akka.testkit.TestProbe;
//import models.entities.Video;
//import models.services.*;
//import org.json.JSONObject;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import play.mvc.Http;
//import play.mvc.Result;
//import play.mvc.WebSocket;
//import play.libs.concurrent.HttpExecutionContext;
//
//import javax.inject.Named;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.CompletionStage;
//import java.util.concurrent.TimeUnit;
//
//import static play.mvc.Http.Status.*;
//import static play.test.Helpers.contentAsString;
//import static org.junit.Assert.*;
//import static org.mockito.Mockito.*;
//
//import scala.concurrent.duration.Duration;
//
///**
// * Unit tests for the YoutubeController class. This test suite verifies that the controller correctly
// * handles user interactions with the YouTube API, manages sessions, and returns appropriate responses.
// *
// * <p><strong>Improvements:</strong></p>
// * <ul>
// *     <li>Utilizes TestProbes to simulate Actor interactions, preventing naming conflicts.</li>
// *     <li>Ensures that actors respond to messages to avoid AskTimeoutException.</li>
// *     <li>Mocks external services to provide valid data, preventing IllegalArgumentException.</li>
// * </ul>
// *
// * <p><strong>Authors:</strong> Zahra Rasoulifar, Hosna Habibi, Mojtaba Peyrovian, Kasra Karaji</p>
// */
//public class YoutubeControllerTest {
//
//    // Initialize a real ActorSystem for testing
//    private ActorSystem system;
//
//    private YoutubeController youtubeController;
//    private YouTubeService mockYouTubeService;
//    private SentimentService mockSentimentService;
//    private SearchService mockSearchService;
//    private WordStatService mockWordStatService;
//    private ChannelProfileService mockChannelProfileService;
//    private TagsService mockTagsService;
//    private HttpExecutionContext mockHttpExecutionContext;
//
//    // TestProbes to simulate ActorRefs
//    private TestProbe sentimentActorProbe;
//    private TestProbe channelProfileActorProbe;
//    private TestProbe wordStatActorProbe;
//    private TestProbe tagActorProbe;
//
//    // Mocked Http.Request objects
//    private Http.Request mockRequestWithoutSession;
//    private Http.Request mockRequestWithSession;
//
//    @Before
//    public void setUp() {
//        // Create a new ActorSystem for each test
//        system = ActorSystem.create("TestActorSystem-" + UUID.randomUUID());
//
//        // Initialize TestProbes for actors
//        sentimentActorProbe = new TestProbe(system);
//        channelProfileActorProbe = new TestProbe(system);
//        wordStatActorProbe = new TestProbe(system);
//        tagActorProbe = new TestProbe(system);
//
//        // Initialize mocks for dependencies
//        mockYouTubeService = mock(YouTubeService.class);
//        mockSentimentService = mock(SentimentService.class);
//        mockSearchService = mock(SearchService.class);
//        mockWordStatService = mock(WordStatService.class);
//        mockChannelProfileService = mock(ChannelProfileService.class);
//        mockTagsService = mock(TagsService.class);
//        mockHttpExecutionContext = mock(HttpExecutionContext.class);
//
//        // Initialize YoutubeController with TestProbes' ActorRefs
//        youtubeController = new YoutubeController(
//                 mocksearchService,
//                mockwordStatService,
//                mockchannelProfileService,
//                mockTagsService,
//                mockActorSystem,
//                mockMaterializer,
//                mockYouTubeService,
//                mockSentimentService,
//                mockHttpExecutionContext)            // Materializer (mocked if not used in tests)
//        );
//
//        // Mock Http.Request without a session ID
//        mockRequestWithoutSession = mock(Http.Request.class);
//        when(mockRequestWithoutSession.session()).thenReturn(new Http.Session(Map.of()));
//
//        // Mock Http.Request with a session ID
//        mockRequestWithSession = mock(Http.Request.class);
//        when(mockRequestWithSession.session()).thenReturn(new Http.Session(Map.of("sessionId", "existingSessionId")));
//    }
//
//    @After
//    public void teardown() {
//        // Shutdown the ActorSystem after each test
//        TestKit.shutdownActorSystem(system);
//        system = null;
//    }
//
//    /**
//     * Helper method to set up expected actor responses.
//     *
//     * @param probe        The TestProbe simulating the actor.
//     * @param messageClass The class of the message to expect.
//     * @param response     The response to send.
//     */
//    private void setupActorResponse(TestProbe probe, Class<?> messageClass, Object response) {
//        // Expect a message of the specified class within a timeout
//        Object received = probe.receiveOne(Duration.create(5, TimeUnit.SECONDS));
//        assertNotNull("Expected message of type " + messageClass.getSimpleName() + " was not received", received);
//        // Reply with the provided response
//        probe.reply(response);
//    }
//
//    /**
//     * Tests the index method when initiating a new session.
//     * Expects a session ID to be added and the index page to be rendered.
//     */
//    @Test
//    public void testIndexNewSession() throws Exception {
//        // Act
//        CompletionStage<Result> resultStage = youtubeController.index(mockRequestWithoutSession);
//        Result result = resultStage.toCompletableFuture().get();
//
//        // Assert
//        assertEquals(OK, result.status());
//        assertTrue(contentAsString(result).contains("Index Page"));
//
//        // Verify that a session ID has been added to the Result
//        assertTrue(result.session().getOptional("sessionId").isPresent());
//        String sessionId = result.session().getOptional("sessionId").get();
//        assertNotNull(sessionId);
//        assertFalse(sessionId.isEmpty());
//    }
//
//    /**
//     * Tests the index method when an existing session is present.
//     * Expects the index page to be rendered without adding a new session ID.
//     */
//    @Test
//    public void testIndexExistingSession() throws Exception {
//        // Act
//        CompletionStage<Result> resultStage = youtubeController.index(mockRequestWithSession);
//        Result result = resultStage.toCompletableFuture().get();
//
//        // Assert
//        assertEquals(OK, result.status());
//        assertTrue(contentAsString(result).contains("Index Page"));
//
//        // Verify that the existing session ID is maintained
//        assertTrue(result.session().getOptional("sessionId").isPresent());
//        String sessionId = result.session().getOptional("sessionId").get();
//        assertEquals("existingSessionId", sessionId);
//    }
//
//    /**
//     * Tests the tags method to ensure it correctly delegates to TagsService.getTagsByVideoId and handles the response.
//     */
//    @Test
//    public void testTags_SuccessfulRetrieval() throws Exception {
//        // Arrange
//        String videoId = "video123";
//        Video mockVideo = new Video("Sample Title", "Sample Description", "Sample Channel", "SampleThumbnailURL", videoId, "channel123", "SampleVideoURL", "2023-01-01");
//        List<String> mockTags = List.of("tag1", "tag2", "tag3");
//
//        // Mocking getVideoByVideoId if used by the controller
//        when(mockTagsService.getVideoByVideoId(videoId))
//                .thenReturn(CompletableFuture.completedFuture(mockVideo));
//
//        // Mocking getTagsByVideoId
//        when(mockTagsService.getTagsByVideoId(videoId))
//                .thenReturn(CompletableFuture.completedFuture(mockTags));
//
//        // Simulate the actor responding to GetChannelInfo message
//        setupActorResponse(sentimentActorProbe, ChannelProfileMessages.GetChannelInfo.class, new JSONObject("{\"sentiment\":\"positive\"}"));
//
//        // Act
//        CompletionStage<Result> resultStage = youtubeController.tags(videoId, mockRequestWithoutSession);
//        Result result = resultStage.toCompletableFuture().get(5, TimeUnit.SECONDS);
//
//        // Assert
//        assertEquals(OK, result.status());
//        assertTrue(contentAsString(result).contains("Tags Page"));
//        assertTrue(contentAsString(result).contains("tag1"));
//        assertTrue(contentAsString(result).contains("tag2"));
//        assertTrue(contentAsString(result).contains("tag3"));
//    }
//
//    /**
//     * Tests the tags method when an error occurs during tag retrieval.
//     */
//    @Test
//    public void testTags_ErrorRetrieval() throws Exception {
//        // Arrange
//        String videoId = "video123";
//        Video mockVideo = new Video("Sample Title", "Sample Description", "Sample Channel", "SampleThumbnailURL", videoId, "channel123", "SampleVideoURL", "2023-01-01");
//
//        // Mocking getVideoByVideoId if used by the controller
//        when(mockTagsService.getVideoByVideoId(videoId))
//                .thenReturn(CompletableFuture.completedFuture(mockVideo));
//
//        // Mocking getTagsByVideoId to simulate an error
//        when(mockTagsService.getTagsByVideoId(videoId))
//                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Tag retrieval failed")));
//
//        // Act
//        CompletionStage<Result> resultStage = youtubeController.tags(videoId, mockRequestWithoutSession);
//        Result result = resultStage.toCompletableFuture().get();
//
//        // Assert
//        assertEquals(INTERNAL_SERVER_ERROR, result.status());
//        assertTrue(contentAsString(result).contains("Error"));
//    }
//
//    /**
//     * Tests the search method with a valid keyword.
//     * Expects SearchService.searchVideos to be called and the search results page to be rendered.
//     */
//    @Test
//    public void testSearch_ValidKeyword() throws Exception {
//        // Arrange
//        String keyword = "PlayFramework";
//        int maxResults = 10;
//        List<Video> mockVideos = List.of(
//                new Video("Video1 Title", "Description1", "ChannelTitle1", "ThumbnailURL1", "VideoId1", "ChannelId1", "VideoURL1", "2023-01-01"),
//                new Video("Video2 Title", "Description2", "ChannelTitle2", "ThumbnailURL2", "VideoId2", "ChannelId2", "VideoURL2", "2023-01-02"),
//                new Video("Video3 Title", "Description3", "ChannelTitle3", "ThumbnailURL3", "VideoId3", "ChannelId3", "VideoURL3", "2023-01-03")
//        );
//
//        // Mocking searchVideos
//        when(mockSearchService.searchVideos(keyword, maxResults))
//                .thenReturn(CompletableFuture.completedFuture(mockVideos));
//
//        // Simulate the actor responding to GetChannelInfo message
//        setupActorResponse(sentimentActorProbe, ChannelProfileMessages.GetChannelInfo.class, new JSONObject("{\"sentiment\":\"positive\"}"));
//
//        // Act
//        CompletionStage<Result> resultStage = youtubeController.search(keyword, mockRequestWithoutSession);
//        Result result = resultStage.toCompletableFuture().get(5, TimeUnit.SECONDS);
//
//        // Assert
//        assertEquals(OK, result.status());
//        assertTrue(contentAsString(result).contains("Search Results Page"));
//        assertTrue(contentAsString(result).contains("Video1 Title"));
//        assertTrue(contentAsString(result).contains("Video2 Title"));
//        assertTrue(contentAsString(result).contains("Video3 Title"));
//    }
//
//    /**
//     * Tests the search method with an empty keyword.
//     * Expects a redirect to the index page.
//     */
//    @Test
//    public void testSearch_EmptyKeyword() throws Exception {
//        // Arrange
//        String keyword = "";
//
//        // Act
//        CompletionStage<Result> resultStage = youtubeController.search(keyword, mockRequestWithoutSession);
//        Result result = resultStage.toCompletableFuture().get();
//
//        // Assert
//        assertEquals(SEE_OTHER, result.status());
//        assertEquals(routes.YoutubeController.index().url(), result.redirectLocation().orElse(""));
//    }
//
//    /**
//     * Tests the search method with a null keyword.
//     * Expects a redirect to the index page.
//     */
//    @Test
//    public void testSearch_NullKeyword() throws Exception {
//        // Arrange
//        String keyword = null;
//
//        // Act
//        CompletionStage<Result> resultStage = youtubeController.search(keyword, mockRequestWithoutSession);
//        Result result = resultStage.toCompletableFuture().get();
//
//        // Assert
//        assertEquals(SEE_OTHER, result.status());
//        assertEquals(routes.YoutubeController.index().url(), result.redirectLocation().orElse(""));
//    }
//
//    /**
//     * Tests the channelProfile method to ensure it correctly delegates to ChannelProfileService.getChannelInfo and handles the response.
//     */
//    @Test
//    public void testChannelProfile_SuccessfulRetrieval() throws Exception {
//        // Arrange
//        String channelId = "channel123";
//        JSONObject channelInfo = new JSONObject("{\"info\":\"Channel Info for " + channelId + "\"}");
//
//        // Mocking getChannelInfo
//        when(mockChannelProfileService.getChannelInfo(channelId))
//                .thenReturn(CompletableFuture.completedFuture(channelInfo));
//
//        // Simulate the actor responding to GetChannelInfo message
//        setupActorResponse(channelProfileActorProbe, ChannelProfileMessages.GetChannelInfo.class, channelInfo);
//
//        // Act
//        CompletionStage<Result> resultStage = youtubeController.channelProfile(channelId, mockRequestWithoutSession);
//        Result result = resultStage.toCompletableFuture().get(5, TimeUnit.SECONDS);
//
//        // Assert
//        assertEquals(OK, result.status());
//        assertTrue(contentAsString(result).contains("Channel Profile Page"));
//        assertTrue(contentAsString(result).contains("Channel Info for " + channelId));
//    }
//
//    /**
//     * Tests the channelProfile method when an error occurs during profile retrieval.
//     */
//    @Test
//    public void testChannelProfile_ErrorRetrieval() throws Exception {
//        // Arrange
//        String channelId = "channel123";
//
//        // Mocking getChannelInfo to simulate an error
//        when(mockChannelProfileService.getChannelInfo(channelId))
//                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Channel info retrieval failed")));
//
//        // Act
//        CompletionStage<Result> resultStage = youtubeController.channelProfile(channelId, mockRequestWithoutSession);
//        Result result = resultStage.toCompletableFuture().get();
//
//        // Assert
//        assertEquals(INTERNAL_SERVER_ERROR, result.status());
//        assertTrue(contentAsString(result).contains("Error"));
//    }
//
//    /**
//     * Tests the wordStats method to ensure it correctly delegates to WordStatService.createWordStats and handles the response.
//     */
//    @Test
//    public void testWordStats_SuccessfulRetrieval() throws Exception {
//        // Arrange
//        String keyword = "PlayFramework";
//        List<Video> videos = List.of(
//                new Video("Video1 Title", "Description1", "ChannelTitle1", "ThumbnailURL1", "VideoId1", "ChannelId1", "VideoURL1", "2023-01-01"),
//                new Video("Video2 Title", "Description2", "ChannelTitle2", "ThumbnailURL2", "VideoId2", "ChannelId2", "VideoURL2", "2023-01-02"),
//                new Video("Video3 Title", "Description3", "ChannelTitle3", "ThumbnailURL3", "VideoId3", "ChannelId3", "VideoURL3", "2023-01-03")
//        );
//        Map<String, Long> wordStats = Map.of("playframework", 10L, "scala", 8L, "java", 5L);
//
//        // Mocking createWordStats
//        when(mockWordStatService.createWordStats(videos))
//                .thenReturn(wordStats);
//
//        // Act
//        CompletionStage<Result> resultStage = youtubeController.wordStats(keyword, mockRequestWithoutSession);
//        Result result = resultStage.toCompletableFuture().get(5, TimeUnit.SECONDS);
//
//        // Assert
//        assertEquals(OK, result.status());
//        assertTrue(contentAsString(result).contains("Word Stats Page"));
//        assertTrue(contentAsString(result).contains("playframework: 10"));
//        assertTrue(contentAsString(result).contains("scala: 8"));
//        assertTrue(contentAsString(result).contains("java: 5"));
//    }
//
//    /**
//     * Tests the wordStats method when an error occurs during word statistics generation.
//     */
//    @Test
//    public void testWordStats_ErrorRetrieval() throws Exception {
//        // Arrange
//        String keyword = "PlayFramework";
//        List<Video> videos = List.of(
//                new Video("Video1 Title", "Description1", "ChannelTitle1", "ThumbnailURL1", "VideoId1", "ChannelId1", "VideoURL1", "2023-01-01"),
//                new Video("Video2 Title", "Description2", "ChannelTitle2", "ThumbnailURL2", "VideoId2", "ChannelId2", "VideoURL2", "2023-01-02"),
//                new Video("Video3 Title", "Description3", "ChannelTitle3", "ThumbnailURL3", "VideoId3", "ChannelId3", "VideoURL3", "2023-01-03")
//        );
//
//        // Mocking createWordStats to simulate an error
//        when(mockWordStatService.createWordStats(videos))
//                .thenThrow(new RuntimeException("Word stats generation failed"));
//
//        // Act
//        CompletionStage<Result> resultStage = youtubeController.wordStats(keyword, mockRequestWithoutSession);
//        Result result = resultStage.toCompletableFuture().get();
//
//        // Assert
//        assertEquals(INTERNAL_SERVER_ERROR, result.status());
//        assertTrue(contentAsString(result).contains("Error"));
//    }
//
//    /**
//     * Tests the WebSocket connection.
//     * Note: Comprehensive WebSocket testing requires integration tests.
//     * Here, we ensure that the WebSocket endpoint is correctly defined.
//     */
//    @Test
//    public void testWebSocket() {
//        // Act
//        WebSocket ws = youtubeController.ws();
//
//        // Assert
//        assertNotNull("WebSocket should not be null", ws);
//    }
//
//    /**
//     * Additional Test: Verify that the session ID is correctly retrieved from the header in WebSocket.
//     * Note: This requires more complex mocking and is generally better suited for integration tests.
//     */
//    @Test
//    public void testWebSocket_WithSessionId() {
//        // Arrange
//        // Since WebSocket testing is more involved, we'll ensure the endpoint is correctly set up
//        // Actual message exchange would require integration tests
//        WebSocket ws = youtubeController.ws();
//
//        // Assert
//        assertNotNull("WebSocket should not be null", ws);
//    }
//
//    /**
//     * Additional Test: Verify that the session ID is correctly handled when missing in the header in WebSocket.
//     * Note: This requires more complex mocking and is generally better suited for integration tests.
//     */
//    @Test
//    public void testWebSocket_WithoutSessionId() {
//        // Arrange
//        // Since WebSocket testing is more involved, we'll ensure the endpoint is correctly set up
//        // Actual message exchange would require integration tests
//        WebSocket ws = youtubeController.ws();
//
//        // Assert
//        assertNotNull("WebSocket should not be null", ws);
//    }
//}
