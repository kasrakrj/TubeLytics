package models.services;

import models.entities.Video;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class SentimentServiceTest {

    private SentimentService sentimentAnalyzer;

    @Before
    public void setUp() {
        sentimentAnalyzer = Mockito.spy(new SentimentService());
    }

    @Test
    public void testCalculateSentimentPos() {
        String description = "I am so happy and amazed!";
        assertEquals(":-)", sentimentAnalyzer.calculateSentiment(description));
    }

    @Test
    public void testCalculateSentimentNeg() {
        String description = "I feel sad and I really want to cry.";
        assertEquals(":-(", sentimentAnalyzer.calculateSentiment(description));
    }

    @Test
    public void testCalculateSentimentEmpty() {
        String description = "";
        assertEquals(":-|", sentimentAnalyzer.calculateSentiment(description));
    }

    @Test
    public void testCalculateSentimentNull() {
        String description = null;
        assertEquals(":-|", sentimentAnalyzer.calculateSentiment(description));
    }

    @Test
    public void testCalculateSentimentBalanced() {
        String description = "I feel happy but also sad.";
        assertEquals(":-|", sentimentAnalyzer.calculateSentiment(description));
    }

    @Test
    public void testCalculateSentimentNoSent() {
        String description = "This is a professional piece of art.";
        assertEquals(":-|", sentimentAnalyzer.calculateSentiment(description));
    }

    // Test for asynchronous sentiment analysis with mocked calculateSentiment responses
    @Test
    public void testAnalyzeSentimentMockedAvgSentiment() {
        List<Video> videos = List.of(
                new Video("Positive", "I am so happy and amazed!", "Channel", "url1", "id1", "channelId1", "urlv1"),
                new Video("Negative", "The movie makes me both sad and disappointed.", "Channel", "url2", "id2", "channelId2", "urlv2"),
                new Video("Neutral", "Just an ordinary, regular book.", "Channel", "url3", "id3", "channelId3", "urlv3")
        );

        doReturn(":-)").when(sentimentAnalyzer).calculateSentiment("I am so happy and amazed!");
        doReturn(":-(").when(sentimentAnalyzer).calculateSentiment("The movie makes me both sad and disappointed.");
        doReturn(":-|").when(sentimentAnalyzer).calculateSentiment("Just an ordinary, regular book.");

        CompletableFuture<String> sentimentFuture = sentimentAnalyzer.avgSentiment(videos);
        assertEquals(":-|", sentimentFuture.join());

        verify(sentimentAnalyzer, times(1)).calculateSentiment("I am so happy and amazed!");
        verify(sentimentAnalyzer, times(1)).calculateSentiment("The movie makes me both sad and disappointed.");
        verify(sentimentAnalyzer, times(1)).calculateSentiment("Just an ordinary, regular book.");
    }

    // Test for avgSentiment with an empty video list
    @Test
    public void testAnalyzeSentimentEmptyList() {
        List<Video> videos = List.of();

        CompletableFuture<String> sentimentFuture = sentimentAnalyzer.avgSentiment(videos);
        assertEquals(":-|", sentimentFuture.join());
    }

    // Test for avgSentiment with a null video list
    @Test
    public void testAnalyzeSentimentNullList() {
        CompletableFuture<String> sentimentFuture = sentimentAnalyzer.avgSentiment(null);
        assertEquals(":-|", sentimentFuture.join());
    }

    // Test for avgSentiment with a majority of positive videos
    @Test
    public void testAnalyzeSentimentMostlyPositive() {
        List<Video> videos = List.of(
                new Video("Positive", "This is so wonderful. I love it.", "Channel", "url1", "id1", "channelId1", "urlv1"),
                new Video("Positive", "I am thrilled todaaay.", "Channel", "url2", "id2", "channelId2", "urlv2"),
                new Video("Neutral", "It was just a regular book.", "Channel", "url3", "id3", "channelId3", "urlv3")
        );

        doReturn(":-)").when(sentimentAnalyzer).calculateSentiment("This is so wonderful. I love it.");
        doReturn(":-)").when(sentimentAnalyzer).calculateSentiment("I am thrilled todaaay.");
        doReturn(":-|").when(sentimentAnalyzer).calculateSentiment("It was just a regular book.");

        CompletableFuture<String> sentimentFuture = sentimentAnalyzer.avgSentiment(videos);
        assertEquals(":-)", sentimentFuture.join());
    }

    // Test for avgSentiment with a majority of negative videos
    @Test
    public void testAnalyzeSentimentMostlyNegative() {
        List<Video> videos = List.of(
                new Video("Negative", "This makes me sad. I wanna cry.", "Channel", "url1", "id1", "channelId1", "urlv1"),
                new Video("Negative", "Feeling down and angry.", "Channel", "url2", "id2", "channelId2", "urlv2"),
                new Video("Neutral", "It's an average amount of profession.", "Channel", "url3", "id3", "channelId3", "urlv3")
        );

        doReturn(":-(").when(sentimentAnalyzer).calculateSentiment("This makes me sad. I wanna cry.");
        doReturn(":-(").when(sentimentAnalyzer).calculateSentiment("Feeling down and angry.");
        doReturn(":-|").when(sentimentAnalyzer).calculateSentiment("It's an average amount of profession.");

        CompletableFuture<String> sentimentFuture = sentimentAnalyzer.avgSentiment(videos);
        assertEquals(":-(", sentimentFuture.join());
    }
}
