package models.services;

import models.entities.Video;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class SentimentServiceTest {

    private SentimentService sentimentService;

    @Before
    public void setUp() {
        sentimentService = Mockito.spy(new SentimentService());
    }

    @Test
    public void testCalculateSentiment_Positive() {
        String description = "I am so happy and excited!";
        assertEquals(":-)", sentimentService.calculateSentiment(description));
    }

    @Test
    public void testCalculateSentiment_Negative() {
        String description = "I feel very sad and broken.";
        assertEquals(":-(", sentimentService.calculateSentiment(description));
    }

    @Test
    public void testCalculateSentiment_Neutral() {
        String description = "It is a book.";
        assertEquals(":-|", sentimentService.calculateSentiment(description));
    }

    @Test
    public void testCalculateSentiment_Empty() {
        String description = "";
        assertEquals(":-|", sentimentService.calculateSentiment(description));
    }

    @Test
    public void testCalculateSentiment_Null() {
        String description = null;
        assertEquals(":-|", sentimentService.calculateSentiment(description));
    }

    @Test
    public void testAvgSentiment_AllPositive() throws ExecutionException, InterruptedException {
        List<Video> videos = List.of(
                new Video("Title1", "I am so happy!", "Channel1", "", "1", "channel1", "",""),
                new Video("Title2", "This is amazing!", "Channel2", "", "2", "channel2", "","")
        );

        CompletableFuture<String> resultFuture = (CompletableFuture<String>) sentimentService.avgSentiment(videos);
        String result = resultFuture.get();
        assertEquals(":-)", result);
    }

    @Test
    public void testAvgSentiment_AllNegative() throws ExecutionException, InterruptedException {
        List<Video> videos = List.of(
                new Video("Title1", "This is terrible.", "Channel1", "", "1", "channel1", "",""),
                new Video("Title2", "I feel very sad.", "Channel2", "", "2", "channel2", "","")
        );

        CompletableFuture<String> resultFuture = (CompletableFuture<String>) sentimentService.avgSentiment(videos);
        String result = resultFuture.get();
        assertEquals(":-(", result);
    }

    @Test
    public void testAvgSentiment_Neutral() throws ExecutionException, InterruptedException {
        List<Video> videos = List.of(
                new Video("Title1", "I feel happy but also sad.", "Channel1", "", "1", "channel1", "",""),
                new Video("Title2", "This is an ordinary day.", "Channel2", "", "2", "channel2", "","")
        );

        CompletableFuture<String> resultFuture = (CompletableFuture<String>) sentimentService.avgSentiment(videos);
        String result = resultFuture.get();
        assertEquals(":-|", result);
    }

    @Test
    public void testAvgSentiment_EmptyList() throws ExecutionException, InterruptedException {
        List<Video> videos = List.of();

        CompletableFuture<String> resultFuture = (CompletableFuture<String>) sentimentService.avgSentiment(videos);
        String result = resultFuture.get();
        assertEquals(":-|", result);
    }

    @Test
    public void testAvgSentiment_NullList() throws ExecutionException, InterruptedException {
        CompletableFuture<String> resultFuture = (CompletableFuture<String>) sentimentService.avgSentiment(null);
        String result = resultFuture.get();
        assertEquals(":-|", result);
    }

    @Test
    public void testAsyncAnalyzeSentiment() throws ExecutionException, InterruptedException {
        String description = "I am so thrilled!";
        CompletableFuture<String> resultFuture = (CompletableFuture<String>) sentimentService.analyzeAsync(description);
        String result = resultFuture.get();
        assertEquals(":-)", result);
    }

    @Test
    public void testSequenceUtility() throws ExecutionException, InterruptedException {
        // Explicitly treat CompletableFuture as CompletionStage
        List<CompletionStage<Integer>> futures = List.of(
                CompletableFuture.completedFuture(1),
                CompletableFuture.completedFuture(2),
                CompletableFuture.completedFuture(3)
        );

        CompletableFuture<List<Integer>> resultFuture = sentimentService.sequence(futures).toCompletableFuture();
        List<Integer> result = resultFuture.get();

        assertNotNull(result);
        assertEquals(List.of(1, 2, 3), result);
    }

    @Test
    public void testSequenceUtility_EmptyList() throws ExecutionException, InterruptedException {
        // Explicitly cast to List<CompletionStage<Integer>>
        List<CompletionStage<Integer>> futures = List.of(); // Empty list

        CompletableFuture<List<Integer>> resultFuture = sentimentService.sequence(futures).toCompletableFuture();
        List<Integer> result = resultFuture.get();

        assertNotNull(result);
        assertEquals(List.of(), result);
    }
}
