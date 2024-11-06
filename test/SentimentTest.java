import models.Sentiment;
import models.entities.Video;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

public class SentimentTest {

    private Sentiment sentimentAnalyzer;

    @BeforeEach
    public void setUp() {
        sentimentAnalyzer = new Sentiment();
    }

    // Test for positive sentiment
    @Test
    public void testCalculateSentiment_Positive() {
        String description = "I am so happy and thrilled!";
        assertEquals(":-)", sentimentAnalyzer.calculateSentiment(description));
    }

    // Test for negative sentiment
    @Test
    public void testCalculateSentiment_Negative() {
        String description = "I feel sad and disappointed.";
        assertEquals(":-(", sentimentAnalyzer.calculateSentiment(description));
    }

    // Test for neutral sentiment
    @Test
    public void testCalculateSentiment_Neutral() {
        String description = "It's an ordinary day.";
        assertEquals(":-|", sentimentAnalyzer.calculateSentiment(description));
    }

    // Test for empty description
    @Test
    public void testCalculateSentiment_EmptyDescription() {
        String description = "";
        assertEquals(":-|", sentimentAnalyzer.calculateSentiment(description));
    }

    // Test for null description
    @Test
    public void testCalculateSentiment_NullDescription() {
        String description = null;
        assertEquals(":-|", sentimentAnalyzer.calculateSentiment(description));
    }

    // Test for average sentiment of multiple videos with mixed sentiments
    @Test
    public void testAnalyzeSentiment_MixedVideos() {
        List<Video> videos = List.of(
                new Video("Positive Video", "I am so happy!", "Channel 1", "url1", "id1"),
                new Video("Negative Video", "This is a sad story", "Channel 2", "url2", "id2"),
                new Video("Neutral Video", "Just an ordinary day.", "Channel 3", "url3", "id3")
        );

        CompletableFuture<String> sentimentFuture = sentimentAnalyzer.AnalyzeSentiment(videos);
        assertEquals(":-|", sentimentFuture.join());
    }

    // Test for average sentiment when all videos are positive
    @Test
    public void testAnalyzeSentiment_AllPositiveVideos() {
        List<Video> videos = List.of(
                new Video("Positive Video 1", "I am happy", "Channel 1", "url1", "id1"),
                new Video("Positive Video 2", "Such a joyful day!", "Channel 2", "url2", "id2"),
                new Video("Positive Video 3", "Life is amazing!", "Channel 3", "url3", "id3")
        );

        CompletableFuture<String> sentimentFuture = sentimentAnalyzer.AnalyzeSentiment(videos);
        assertEquals(":-)", sentimentFuture.join());
    }

    // Test for average sentiment when all videos are negative
    @Test
    public void testAnalyzeSentiment_AllNegativeVideos() {
        List<Video> videos = List.of(
                new Video("Negative Video 1", "I am sad", "Channel 1", "url1", "id1"),
                new Video("Negative Video 2", "This is disappointing", "Channel 2", "url2", "id2"),
                new Video("Negative Video 3", "Feeling very down", "Channel 3", "url3", "id3")
        );

        CompletableFuture<String> sentimentFuture = sentimentAnalyzer.AnalyzeSentiment(videos);
        assertEquals(":-(", sentimentFuture.join());
    }

    // Test for average sentiment when all videos are neutral
    @Test
    public void testAnalyzeSentiment_AllNeutralVideos() {
        List<Video> videos = List.of(
                new Video("Neutral Video 1", "An ordinary day", "Channel 1", "url1", "id1"),
                new Video("Neutral Video 2", "Just a regular update", "Channel 2", "url2", "id2"),
                new Video("Neutral Video 3", "Nothing much happened", "Channel 3", "url3", "id3")
        );

        CompletableFuture<String> sentimentFuture = sentimentAnalyzer.AnalyzeSentiment(videos);
        assertEquals(":-|", sentimentFuture.join());
    }

    // Test for average sentiment with an empty video list
    @Test
    public void testAnalyzeSentiment_EmptyVideoList() {
        List<Video> videos = List.of();

        CompletableFuture<String> sentimentFuture = sentimentAnalyzer.AnalyzeSentiment(videos);
        assertEquals(":-|", sentimentFuture.join());
    }

    // Test for average sentiment with a null video list
    @Test
    public void testAnalyzeSentiment_NullVideoList() {
        CompletableFuture<String> sentimentFuture = sentimentAnalyzer.AnalyzeSentiment(null);
        assertEquals(":-|", sentimentFuture.join());
    }
}
