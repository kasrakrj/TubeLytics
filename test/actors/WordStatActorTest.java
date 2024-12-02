package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestActorRef;
import models.entities.Video;
import models.services.GeneralService;
import models.services.SearchService;
import org.junit.*;
import org.mockito.Mockito;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test class for the {@link WordStatActor}.
 */
public class WordStatActorTest {

    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create("TestSystem");
    }

    @AfterClass
    public static void teardown() {
        akka.testkit.javadsl.TestKit.shutdownActorSystem(system);
        system = null;
    }

    /**
     * A test actor that collects messages sent to it.
     */
    public static class TestReceiverActor extends AbstractActor {
        public final List<Object> messages = Collections.synchronizedList(new ArrayList<>());

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .matchAny(message -> messages.add(message))
                    .build();
        }

        public static Props props() {
            return Props.create(TestReceiverActor.class);
        }
    }

    @Test
    public void testHandleUpdateVideos_Success() throws Exception {
        // Mock the SearchService
        SearchService searchService = mock(SearchService.class);

        // Mock searchVideos to return a list of videos
        List<Video> videos = Arrays.asList(
                new Video("Java Tutorial", "", "", "", "vid1", "", "", ""),
                new Video("Akka Actors", "", "", "", "vid2", "", "", ""),
                new Video("Java Concurrency", "", "", "", "vid3", "", "", "")
        );
        when(searchService.searchVideos(anyString(), anyInt()))
                .thenReturn(CompletableFuture.completedFuture(videos));

        // Create WordStatActor using TestActorRef to access internal state
        TestActorRef<WordStatActor> actorRef = TestActorRef.create(system, Props.create(WordStatActor.class, searchService));

        // Send UpdateVideos message
        actorRef.tell(new WordStatMessages.UpdateVideos("java"), ActorRef.noSender());

        // Wait for processing
        Thread.sleep(500);

        // Access the underlying actor
        WordStatActor actor = actorRef.underlyingActor();

        // Get the wordStats
        Map<String, Long> wordStats = actor.createWordStats(videos);

        // Build expected wordStats
        Map<String, Long> expectedWordStats = new LinkedHashMap<>();
        expectedWordStats.put("java", 2L); // "Java Tutorial", "Java Concurrency"

        // Assert that the wordStats match
        assertEquals(expectedWordStats, actor.getWordStats());
    }

    @Test
    public void testHandleUpdateVideos_Exception() throws Exception {
        // Mock the SearchService
        SearchService searchService = mock(SearchService.class);

        // Mock searchVideos to throw an exception
        when(searchService.searchVideos(anyString(), anyInt()))
                .thenReturn(CompletableFuture.failedFuture(new Exception("Search error")));

        // Create WordStatActor
        TestActorRef<WordStatActor> actorRef = TestActorRef.create(system, Props.create(WordStatActor.class, searchService));

        // Send UpdateVideos message
        actorRef.tell(new WordStatMessages.UpdateVideos("java"), ActorRef.noSender());

        // Wait for processing
        Thread.sleep(500);

        // Access the underlying actor
        WordStatActor actor = actorRef.underlyingActor();

        // wordStats should remain empty due to exception
        assertTrue(actor.getWordStats().isEmpty());
    }

    @Test
    public void testGetWordStats() throws Exception {
        // Mock the SearchService
        SearchService searchService = mock(SearchService.class);

        // Mock searchVideos to return a list of videos
        List<Video> videos = Arrays.asList(
                new Video("Java Tutorial", "", "", "", "vid1", "", "", ""),
                new Video("Akka Actors", "", "", "", "vid2", "", "", ""),
                new Video("Java Concurrency", "", "", "", "vid3", "", "", "")
        );
        when(searchService.searchVideos(anyString(), anyInt()))
                .thenReturn(CompletableFuture.completedFuture(videos));

        // Create WordStatActor
        ActorRef wordStatActor = system.actorOf(Props.create(WordStatActor.class, searchService));

        // Send UpdateVideos message
        wordStatActor.tell(new WordStatMessages.UpdateVideos("java"), ActorRef.noSender());

        // Wait for processing
        Thread.sleep(500);

        // Create TestReceiverActor to receive the response
        TestActorRef<TestReceiverActor> receiverRef = TestActorRef.create(system, TestReceiverActor.props());
        TestReceiverActor receiver = receiverRef.underlyingActor();

        // Send GetWordStats message
        wordStatActor.tell(new WordStatMessages.GetWordStats(), receiverRef);

        // Wait for the reply
        Thread.sleep(500);

        // Check that a message was received
        assertEquals(1, receiver.messages.size());

        // Extract the wordStats from the received message
        @SuppressWarnings("unchecked")
        Map<String, Long> wordStats = (Map<String, Long>) receiver.messages.get(0);

        // Build expected wordStats
        Map<String, Long> expectedWordStats = new LinkedHashMap<>();
        expectedWordStats.put("java", 2L); // "Java Tutorial", "Java Concurrency"

        // Assert that the wordStats match
        assertEquals(expectedWordStats, wordStats);
    }

    @Test
    public void testProps() {
        // Mock the SearchService
        SearchService searchService = mock(SearchService.class);

        // Create props using the method
        Props props = WordStatActor.props(searchService);

        // Ensure props create the correct actor type
        ActorRef actorRef = system.actorOf(props);
        assertNotNull(actorRef);
    }

    @Test
    public void testInitialization() {
        // Mock the SearchService
        SearchService searchService = mock(SearchService.class);

        // Create WordStatActor
        ActorRef wordStatActor = system.actorOf(Props.create(WordStatActor.class, searchService));

        // Assert that the actor was created successfully
        assertNotNull(wordStatActor);
    }
}
