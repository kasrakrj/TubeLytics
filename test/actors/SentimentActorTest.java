package actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import models.entities.Video;
import models.services.SentimentService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class SentimentActorTest {

    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testAnalyzeVideos() {
        new TestKit(system) {{
            // Mock SentimentService
            SentimentService sentimentService = mock(SentimentService.class);

            // Stub the sentiment calculation for individual videos
            when(sentimentService.calculateSentiment(anyString())).thenReturn("Happy");

            // Create the SentimentActor
            ActorRef sentimentActor = system.actorOf(SentimentActor.props(sentimentService));

            // Prepare test videos
            Video video1 = new Video("Title1", "Description1", "Channel1", "https://example.com/thumb1", "video1", "channel1", "https://example.com/video1", "2024-12-01");
            Video video2 = new Video("Title2", "Description2", "Channel2", "https://example.com/thumb2", "video2", "channel2", "https://example.com/video2", "2024-12-01");
            List<Video> videos = List.of(video1, video2);

            // Send an AnalyzeVideos message
            sentimentActor.tell(new SentimentMessages.AnalyzeVideos(videos), getRef());

            // Expect a Map<String, String> as a response
            Map<String, String> expectedResponse = Map.of(
                    "video1", "Happy",
                    "video2", "Happy"
            );
            Map<String, String> actualResponse = expectMsgClass(Map.class);

            // Verify the response
            assertEquals(expectedResponse, actualResponse);

            // Verify that SentimentService was called with the correct inputs
            verify(sentimentService).calculateSentiment("Description1");
            verify(sentimentService).calculateSentiment("Description2");
        }};
    }

    @Test
    public void testGetOverallSentiment() {
        new TestKit(system) {{
            // Mock SentimentService
            SentimentService sentimentService = mock(SentimentService.class);

            // Stub the overall sentiment calculation
            when(sentimentService.avgSentiment(anyList())).thenReturn(CompletableFuture.completedFuture("Neutral"));

            // Create the SentimentActor
            ActorRef sentimentActor = system.actorOf(SentimentActor.props(sentimentService));

            // Prepare test videos
            Video video1 = new Video("Title1", "Description1", "Channel1", "https://example.com/thumb1", "video1", "channel1", "https://example.com/video1", "2024-12-01");
            Video video2 = new Video("Title2", "Description2", "Channel2", "https://example.com/thumb2", "video2", "channel2", "https://example.com/video2", "2024-12-01");
            List<Video> videos = List.of(video1, video2);

            // Send a GetOverallSentiment message
            sentimentActor.tell(new SentimentMessages.GetOverallSentiment(videos), getRef());

            // Expect a String response
            String expectedResponse = "Neutral";
            String actualResponse = expectMsgClass(String.class);

            // Verify the response
            assertEquals(expectedResponse, actualResponse);

            // Verify that SentimentService was called with the correct input
            verify(sentimentService).avgSentiment(videos);
        }};
    }
}
