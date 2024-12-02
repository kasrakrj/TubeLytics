package actors;
import models.entities.Video;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
public class SentimentMessagesTest {
    @Test
    public void testOuterClassInitialization() {
        // Explicitly create an instance of the outer class
        SentimentMessages sentimentMessages = new SentimentMessages();
        assertNotNull(sentimentMessages.toString(), "SentimentMessages class instance should not be null");
    }

    @Test
    public void testAnalyzeVideosMessage() {
        // Prepare test data
        Video video1 = new Video("Title 1", "Description 1", "Channel 1", "https://thumbnail1.com", "videoId1", "channelId1", "https://video1.com", "2024-01-01T10:00:00Z");
        Video video2 = new Video("Title 2", "Description 2", "Channel 2", "https://thumbnail2.com", "videoId2", "channelId2", "https://video2.com", "2024-01-02T11:00:00Z");
        List<Video> videos = List.of(video1, video2);

        // Create AnalyzeVideos message
        SentimentMessages.AnalyzeVideos analyzeVideos = new SentimentMessages.AnalyzeVideos(videos);

        // Validate the message
        assertNotNull(analyzeVideos);
        assertEquals(2, analyzeVideos.getVideos().size());
        assertEquals(video1, analyzeVideos.getVideos().get(0));
        assertEquals(video2, analyzeVideos.getVideos().get(1));
    }

    @Test
    public void testGetOverallSentimentMessage() {
        // Prepare test data
        Video video1 = new Video("Title 1", "Description 1", "Channel 1", "https://thumbnail1.com", "videoId1", "channelId1", "https://video1.com", "2024-01-01T10:00:00Z");
        Video video2 = new Video("Title 2", "Description 2", "Channel 2", "https://thumbnail2.com", "videoId2", "channelId2", "https://video2.com", "2024-01-02T11:00:00Z");
        List<Video> videos = List.of(video1, video2);

        // Create GetOverallSentiment message
        SentimentMessages.GetOverallSentiment getOverallSentiment = new SentimentMessages.GetOverallSentiment(videos);

        // Validate the message
        assertNotNull(getOverallSentiment);
        assertEquals(2, getOverallSentiment.getVideos().size());
        assertEquals(video1, getOverallSentiment.getVideos().get(0));
        assertEquals(video2, getOverallSentiment.getVideos().get(1));
    }
}
