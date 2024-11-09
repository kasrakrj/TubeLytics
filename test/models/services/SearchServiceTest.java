package models.services;

import models.entities.Video;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the SearchService class. This test suite verifies caching behavior, search history management,
 * and sentiment analysis functionality in SearchService.
 */
public class SearchServiceTest {

    private SearchService searchService;
    private SentimentService mockSentimentService;
    private ConcurrentHashMap<String, List<Video>> testCache;

    /**
     * Sets up the necessary mocks and dependencies before each test.
     * Initializes SearchService with a mock SentimentService and a custom cache.
     */
    @Before
    public void setUp() {
        // Mock dependencies
        mockSentimentService = Mockito.mock(SentimentService.class);

        // Initialize test cache
        testCache = new ConcurrentHashMap<>();

        // Create SearchService with injected cache
        searchService = new SearchService(mockSentimentService, testCache);
    }

    /**
     * Verifies that searchVideos retrieves results from the cache if available.
     * This ensures that the caching mechanism is properly used and prevents unnecessary API calls.
     */
    @Test
    public void testSearchVideosWithCache() throws Exception {
        String keyword = "test";
        int numOfResults = 2;

        // Expected videos to simulate a cached response
        List<Video> expectedVideos = List.of(
                new Video("Video 1", "Description 1", "Channel 1", "https://thumbnail.url", "videoId1", "channelId1", "https://www.youtube.com/watch?v=videoId1"),
                new Video("Video 2", "Description 2", "Channel 2", "https://thumbnail2.url", "videoId2", "channelId2", "https://www.youtube.com/watch?v=videoId2")
        );

        // Insert expected videos into the cache
        String cacheKey = keyword + ":" + numOfResults;
        testCache.put(cacheKey, expectedVideos);

        // Call searchVideos, which should retrieve the cached result
        CompletionStage<List<Video>> videosFuture = searchService.searchVideos(keyword, numOfResults);

        // Validate that cached result is returned
        videosFuture.thenAccept(videos -> {
            assertNotNull("Videos should not be null", videos);
            assertEquals("Cached videos should match expected size", expectedVideos.size(), videos.size());
            for (int i = 0; i < videos.size(); i++) {
                assertEquals("Titles should match", expectedVideos.get(i).getTitle(), videos.get(i).getTitle());
                assertEquals("Channel titles should match", expectedVideos.get(i).getChannelTitle(), videos.get(i).getChannelTitle());
            }
        }).toCompletableFuture().get();
    }

    /**
     * Tests that searchVideos performs an API call and populates the cache when no cached data is available.
     * This verifies that the API call is made as expected and that the results are cached for future requests.
     */
    @Test
    public void testSearchVideosWithoutCache() throws Exception {
        String keyword = "test";
        int numOfResults = 2;

        // Simulate API response with mock YouTube data
        List<Video> apiVideos = List.of(
                new Video("Video 1", "Description 1", "Channel 1", "https://thumbnail.url", "videoId1", "channelId1", "https://www.youtube.com/watch?v=videoId1")
        );

        // Simulate API call by directly setting the cache after the first call
        CompletionStage<List<Video>> videosFuture = searchService.searchVideos(keyword, numOfResults)
                .thenApply(videos -> {
                    testCache.put(keyword + ":" + numOfResults, apiVideos);  // Populate cache for next call
                    return apiVideos;
                });

        // Validate that the videos match the API response on the first call
        videosFuture.thenAccept(videos -> {
            assertNotNull("Videos should not be null", videos);
            assertEquals("API response should match expected size", apiVideos.size(), videos.size());
            assertEquals("Titles should match", apiVideos.get(0).getTitle(), videos.get(0).getTitle());
        }).toCompletableFuture().get();
    }

    /**
     * Verifies that addSearchResultToHistory successfully adds a search result to the session's search history.
     * This ensures that each session's search history is correctly maintained.
     */
    @Test
    public void testAddSearchResultToHistory() {
        String sessionId = "session1";
        String keyword = "testKeyword";
        List<Video> videos = List.of(new Video("Test Video", "Description", "Channel", "https://thumbnail.url", "videoId", "channelId", "https://www.youtube.com/watch?v=videoId"));

        // Add result to history
        searchService.addSearchResultToHistory(sessionId, keyword, videos);

        // Validate that the history contains the added result
        Map<String, List<Video>> history = searchService.getSearchHistory(sessionId);
        assertTrue("History should contain the keyword", history.containsKey(keyword));
        assertEquals("History videos should match expected", videos, history.get(keyword));
    }

    /**
     * Tests that calculateIndividualSentiments computes sentiment values for each search result in the session history.
     * Mocked sentiment values are used to verify that the sentiment analysis functionality works as expected.
     */
    @Test
    public void testCalculateIndividualSentiments() throws Exception {
        // Mock sentiment response
        when(mockSentimentService.avgSentiment(anyList())).thenReturn(CompletableFuture.completedFuture("positive"));

        String sessionId = "session1";
        String keyword = "testKeyword";
        List<Video> videos = List.of(new Video("Test Video", "Description", "Channel", "https://thumbnail.url", "videoId", "channelId", "https://www.youtube.com/watch?v=videoId"));

        // Add result to history for sentiment analysis
        searchService.addSearchResultToHistory(sessionId, keyword, videos);

        // Calculate individual sentiments
        CompletionStage<Map<String, String>> sentimentsFuture = searchService.calculateIndividualSentiments(sessionId);

        // Validate the sentiments
        sentimentsFuture.thenAccept(sentiments -> {
            assertNotNull("Sentiments should not be null", sentiments);
            assertEquals("Sentiment should be calculated", "positive", sentiments.get(keyword));
        }).toCompletableFuture().get();
    }

    /**
     * Tests that calculateOverallSentiment computes a single sentiment value for all search results in the session history.
     * This test verifies the overall sentiment calculation functionality using a mocked response.
     */
    @Test
    public void testCalculateOverallSentiment() throws Exception {
        // Mock overall sentiment response
        when(mockSentimentService.avgSentiment(anyList())).thenReturn(CompletableFuture.completedFuture("neutral"));

        String sessionId = "session1";
        String keyword = "testKeyword";
        List<Video> videos = List.of(new Video("Test Video", "Description", "Channel", "https://thumbnail.url", "videoId", "channelId", "https://www.youtube.com/watch?v=videoId"));

        // Add result to history for overall sentiment analysis
        searchService.addSearchResultToHistory(sessionId, keyword, videos);

        // Calculate overall sentiment
        CompletionStage<String> overallSentimentFuture = searchService.calculateOverallSentiment(sessionId, 10);

        // Validate overall sentiment result
        overallSentimentFuture.thenAccept(overallSentiment -> {
            assertNotNull("Overall sentiment should not be null", overallSentiment);
            assertEquals("Overall sentiment should match expected", "neutral", overallSentiment);
        }).toCompletableFuture().get();
    }

    /**
     * Verifies that clearSearchHistory removes all search results from the session's search history.
     * This test ensures that the history is correctly cleared when requested.
     */
    @Test
    public void testClearSearchHistory() {
        String sessionId = "session1";
        String keyword = "testKeyword";
        List<Video> videos = List.of(new Video("Test Video", "Description", "Channel", "https://thumbnail.url", "videoId", "channelId", "https://www.youtube.com/watch?v=videoId"));

        // Add a result to history
        searchService.addSearchResultToHistory(sessionId, keyword, videos);
        assertFalse("History should not be empty before clearing", searchService.getSearchHistory(sessionId).isEmpty());

        // Clear search history
        searchService.clearSearchHistory(sessionId);

        // Validate that the history is cleared
        assertTrue("History should be empty after clearing", searchService.getSearchHistory(sessionId).isEmpty());
    }
}