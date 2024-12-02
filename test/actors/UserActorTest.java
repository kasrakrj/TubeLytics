package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestActorRef;
import models.entities.Video;
import models.services.SearchService;
import org.json.JSONObject;
import org.junit.*;
import org.mockito.Mockito;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link UserActor} class.
 */
public class UserActorTest {

    /**
     * Akka ActorSystem used for testing.
     */
    static ActorSystem system;

    /**
     * Set up the ActorSystem before any test cases are run.
     */
    @BeforeClass
    public static void setup() {
        system = ActorSystem.create("TestSystem");
    }

    /**
     * Tear down the ActorSystem after all test cases have been executed.
     */
    @AfterClass
    public static void teardown() {
        akka.testkit.javadsl.TestKit.shutdownActorSystem(system);
        system = null;
    }

    /**
     * Converts a {@link Video} object into a JSON string representation.
     *
     * @param video   the video object to convert
     * @param keyword the associated keyword for the video
     * @return a JSON string representation of the video
     */
    private String videoToJson(Video video, String keyword) {
        JSONObject json = new JSONObject();
        json.put("type", "video");
        json.put("keyword", keyword);
        json.put("videoId", video.getVideoId());
        json.put("title", video.getTitle());
        json.put("description", video.getDescription());
        json.put("thumbnailUrl", video.getThumbnailUrl());
        json.put("channelId", video.getChannelId());
        json.put("channelTitle", video.getChannelTitle());
        return json.toString();
    }

    /**
     * A test actor that collects messages sent to it.
     */
    public static class TestOutActor extends AbstractActor {
        public final List<Object> messages = Collections.synchronizedList(new ArrayList<>());

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .matchAny(message -> messages.add(message))
                    .build();
        }

        public static Props props() {
            return Props.create(TestOutActor.class);
        }
    }

    /**
     * Tests that the "Heartbeat" message is correctly processed by the UserActor.
     */
    @Test
    public void testHeartbeat() throws Exception {
        // Create the 'out' actor
        TestActorRef<TestOutActor> outActorRef = TestActorRef.create(system, TestOutActor.props());
        TestOutActor outActor = outActorRef.underlyingActor();

        // Mock the SearchService
        SearchService searchService = mock(SearchService.class);
        when(searchService.getSearchHistory(anyString())).thenReturn(new HashMap<>());

        // Create UserActor
        String sessionId = "testSession";
        ActorRef sentimentActor = null;
        ActorRef userActor = system.actorOf(Props.create(UserActor.class, outActorRef, searchService, sentimentActor, sessionId));

        // Send the "Heartbeat" message to the UserActor
        userActor.tell("Heartbeat", ActorRef.noSender());

        // Wait a bit to allow message processing
        Thread.sleep(100);

        // Check the messages collected by outActor
        Assert.assertEquals(1, outActor.messages.size());
        String expectedJson = new JSONObject().put("type", "heartbeat").toString();
        Assert.assertEquals(expectedJson, outActor.messages.get(0));
    }

    /**
     * Tests that the "FetchVideos" message is correctly processed and new videos are sent as messages.
     */
    @Test
    public void testFetchVideos() throws Exception {
        // Create the 'out' actor
        TestActorRef<TestOutActor> outActorRef = TestActorRef.create(system, TestOutActor.props());
        TestOutActor outActor = outActorRef.underlyingActor();

        // Mock the SearchService
        SearchService searchService = mock(SearchService.class);
        // Set up the search history with one keyword
        String keyword = "testKeyword";
        Map<String, List<Video>> searchHistoryMap = new HashMap<>();
        searchHistoryMap.put(keyword, new ArrayList<>());
        when(searchService.getSearchHistory(anyString())).thenReturn(searchHistoryMap);

        // Mock fetchNewVideos to return a list of new videos
        List<Video> newVideos = Arrays.asList(
                new Video("Video1", "Description1", "Channel1", "ThumbnailUrl1", "VideoId1", "ChannelId1", "VideoUrl1", "PublishedAt1"),
                new Video("Video2", "Description2", "Channel2", "ThumbnailUrl2", "VideoId2", "ChannelId2", "VideoUrl2", "PublishedAt2")
        );
        when(searchService.fetchNewVideos(eq(keyword), anyInt(), anySet()))
                .thenReturn(CompletableFuture.completedFuture(newVideos));

        // Mock updateVideosForKeyword to do nothing
        doNothing().when(searchService).updateVideosForKeyword(anyString(), eq(keyword), anyList());

        // Create UserActor
        String sessionId = "testSession";
        ActorRef sentimentActor = null;
        ActorRef userActor = system.actorOf(Props.create(UserActor.class, outActorRef, searchService, sentimentActor, sessionId));

        // Send the "FetchVideos" message to the UserActor
        userActor.tell("FetchVideos", ActorRef.noSender());

        // Wait a bit to allow message processing
        Thread.sleep(500);

        // Check the messages collected by outActor
        List<Object> messages = outActor.messages;
        Assert.assertEquals(newVideos.size(), messages.size());

        // Verify each message
        for (int i = 0; i < newVideos.size(); i++) {
            String expectedJson = videoToJson(newVideos.get(i), keyword);
            Assert.assertEquals(expectedJson, messages.get(i));
        }
    }

    /**
     * Tests that unhandled messages are ignored by the UserActor.
     */
    @Test
    public void testUnhandledMessage() throws Exception {
        // Mock the SearchService
        SearchService searchService = mock(SearchService.class);

        // Create UserActor
        String sessionId = "testSession";
        ActorRef sentimentActor = null;
        // 'out' actor can be null since it's not used in this test
        ActorRef userActor = system.actorOf(Props.create(UserActor.class, ActorRef.noSender(), searchService, sentimentActor, sessionId));

        // Send an unhandled message
        userActor.tell(42, ActorRef.noSender());

        // No exception should be thrown
        Thread.sleep(100);
    }

    /**
     * Tests the creation of UserActor props.
     */
    @Test
    public void testProps() {
        // Mock the SearchService
        SearchService searchService = mock(SearchService.class);

        // Create UserActor props
        String sessionId = "testSession";
        ActorRef sentimentActor = null;
        ActorRef outActor = ActorRef.noSender();
        Props props = UserActor.props(outActor, searchService, sentimentActor, sessionId);

        // Create UserActor using props
        ActorRef userActor = system.actorOf(props);
        assertNotNull(userActor);
    }

    /**
     * Tests the behavior when an error occurs during the "FetchVideos" operation.
     */
    @Test
    public void testFetchVideos_Error() throws Exception {
        // Create the 'out' actor
        TestActorRef<TestOutActor> outActorRef = TestActorRef.create(system, TestOutActor.props());
        TestOutActor outActor = outActorRef.underlyingActor();

        // Mock the SearchService
        SearchService searchService = mock(SearchService.class);
        // Set up the search history with one keyword
        String keyword = "testKeyword";
        Map<String, List<Video>> searchHistoryMap = new HashMap<>();
        searchHistoryMap.put(keyword, new ArrayList<>());
        when(searchService.getSearchHistory(anyString())).thenReturn(searchHistoryMap);

        // Mock fetchNewVideos to throw an exception
        when(searchService.fetchNewVideos(eq(keyword), anyInt(), anySet()))
                .thenReturn(CompletableFuture.failedFuture(new Exception("Fetch error")));

        // Create UserActor
        String sessionId = "testSession";
        ActorRef sentimentActor = null;
        ActorRef userActor = system.actorOf(Props.create(UserActor.class, outActorRef, searchService, sentimentActor, sessionId));

        // Send the "FetchVideos" message to the UserActor
        userActor.tell("FetchVideos", ActorRef.noSender());

        // Wait a bit to allow message processing
        Thread.sleep(500);

        // Expect no messages due to error
        Assert.assertTrue(outActor.messages.isEmpty());
    }

    /**
     * Tests that the UserActor can be created with a null sentimentActor.
     */
    @Test
    public void testInitialization_NullSentimentActor() {
        // Mock the SearchService
        SearchService searchService = mock(SearchService.class);

        // Create UserActor with null sentimentActor
        String sessionId = "testSession";
        ActorRef outActor = ActorRef.noSender();
        ActorRef userActor = system.actorOf(Props.create(UserActor.class, outActor, searchService, null, sessionId));

        // Assert that the actor was created successfully
        assertNotNull(userActor);
    }

    /**
     * Tests the behavior when the search history is null during the "FetchVideos" operation.
     */
    @Test
    public void testFetchVideos_NullSearchHistory() throws Exception {
        // Create the 'out' actor
        TestActorRef<TestOutActor> outActorRef = TestActorRef.create(system, TestOutActor.props());
        TestOutActor outActor = outActorRef.underlyingActor();

        // Mock the SearchService
        SearchService searchService = mock(SearchService.class);
        // Mock getSearchHistory to return null
        when(searchService.getSearchHistory(anyString())).thenReturn(null);

        // Create UserActor
        String sessionId = "testSession";
        ActorRef sentimentActor = null;
        ActorRef userActor = system.actorOf(Props.create(UserActor.class, outActorRef, searchService, sentimentActor, sessionId));

        // Send the "FetchVideos" message
        userActor.tell("FetchVideos", ActorRef.noSender());

        // Wait a bit to allow message processing
        Thread.sleep(500);

        // Expect no messages due to null search history
        Assert.assertTrue(outActor.messages.isEmpty());
    }
}
