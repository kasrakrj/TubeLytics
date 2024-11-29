//package models.services;
//
//import models.entities.Video;
//import org.json.JSONArray;
//import org.junit.Before;
//import org.junit.Test;
//import org.mockito.ArgumentCaptor;
//import org.mockito.Mockito;
//import static org.mockito.Mockito.*;
//
//import java.net.URI;
//import java.net.URLEncoder;
//import java.net.http.*;
//import java.nio.charset.StandardCharsets;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.CompletionStage;
//
//import static org.junit.Assert.*;
//import static org.mockito.ArgumentMatchers.any;
//
///**
// * Unit tests for the SearchService class. This test suite verifies caching behavior, search history management,
// * and sentiment analysis functionality in SearchService.
// */
//public class SearchServiceTest {
//
//    private SearchService searchService;
//    private SentimentService mockSentimentService;
//    private ConcurrentHashMap<String, List<Video>> testCache;
//    private YouTubeService mockYouTubeService;
//    private HttpClient mockHttpClient;
//    private HttpResponse<String> mockHttpResponse;
//
//    /**
//     * Sets up the necessary mocks and dependencies before each test.
//     * Initializes SearchService with a mock SentimentService, mock YouTubeService, mock HttpClient, and a custom cache.
//     */
//    @Before
//    public void setUp() throws Exception {
//        // Mock dependencies
//        mockSentimentService = Mockito.mock(SentimentService.class);
//        mockYouTubeService = Mockito.mock(YouTubeService.class);
//        mockHttpClient = Mockito.mock(HttpClient.class);
//        mockHttpResponse = Mockito.mock(HttpResponse.class);
//
//        // Define mock behavior for YouTubeService
//        when(mockYouTubeService.getApiUrl()).thenReturn("https://www.googleapis.com/youtube/v3");
//        when(mockYouTubeService.getApiKey()).thenReturn("FAKE_API_KEY"); // Use a dummy key for testing
//
//        // Initialize test cache
//        testCache = new ConcurrentHashMap<>();
//
//        // Create SearchService with injected dependencies
//        searchService = new SearchService(mockSentimentService, mockYouTubeService, testCache,mockHttpClient);
//    }
//    /**
//     * Verifies that the @Inject constructor properly initializes the SearchService.
//     * This ensures that all dependencies are correctly set and the HttpClient is instantiated.
//     */
//    @Test
//    public void testInjectConstructor() {
//
//        // Arrange
//        SentimentService mockSentimentService = mock(SentimentService.class);
//        YouTubeService mockYouTubeService = mock(YouTubeService.class);
//        ConcurrentHashMap<String, List<Video>> testCache = new ConcurrentHashMap<>();
//
//        // Define behavior for mockYouTubeService
//        when(mockYouTubeService.getApiUrl()).thenReturn("https://www.googleapis.com/youtube/v3");
//        when(mockYouTubeService.getApiKey()).thenReturn("FAKE_API_KEY");
//
//        // Act
//        SearchService injectedSearchService = new SearchService(mockSentimentService, mockYouTubeService, testCache);
//
//        // Assert
//        assertNotNull("SearchService instance should not be null", injectedSearchService);
//        assertEquals("API_KEY should match", "FAKE_API_KEY", injectedSearchService.getAPI_KEY());
//        assertEquals("API_URL should match", "https://www.googleapis.com/youtube/v3", injectedSearchService.getAPI_URL());
//        assertEquals("YOUTUBE_SEARCH_URL should be correctly constructed",
//                "https://www.googleapis.com/youtube/v3/search?part=snippet&order=date&type=video&maxResults=",
//                injectedSearchService.getYOUTUBE_SEARCH_URL());
//        assertNotNull("HttpClient should be initialized", injectedSearchService.getHttpClient());
//        assertTrue("Cache should be the same instance", injectedSearchService.getCache() == testCache);
//    }
//
//    /**
//     * Verifies that searchVideos retrieves results from the cache if available.
//     * This ensures that the caching mechanism is properly used and prevents unnecessary API calls.
//     */
//    @Test
//    public void testSearchVideosWithCache() throws Exception {
//        String keyword = "test";
//        int numOfResults = 2;
//
//        // Expected videos to simulate a cached response
//        List<Video> expectedVideos = List.of(
//                new Video("Video 1", "Description 1", "Channel 1", "https://thumbnail.url", "videoId1", "channelId1", "https://www.youtube.com/watch?v=videoId1"),
//                new Video("Video 2", "Description 2", "Channel 2", "https://thumbnail2.url", "videoId2", "channelId2", "https://www.youtube.com/watch?v=videoId2")
//        );
//
//        // Insert expected videos into the cache
//        String cacheKey = keyword + ":" + numOfResults;
//        testCache.put(cacheKey, expectedVideos);
//
//        // Call searchVideos, which should retrieve the cached result
//        CompletionStage<List<Video>> videosFuture = searchService.searchVideos(keyword, numOfResults);
//
//        // Validate that cached result is returned
//        videosFuture.thenAccept(videos -> {
//            assertNotNull("Videos should not be null", videos);
//            assertEquals("Cached videos should match expected size", expectedVideos.size(), videos.size());
//            for (int i = 0; i < videos.size(); i++) {
//                assertEquals("Titles should match", expectedVideos.get(i).getTitle(), videos.get(i).getTitle());
//                assertEquals("Channel titles should match", expectedVideos.get(i).getChannelTitle(), videos.get(i).getChannelTitle());
//            }
//        }).toCompletableFuture().get();
//
//        // Verify that YouTubeService.parseVideos and HttpClient.sendAsync were not called since cache was used
//        verify(mockYouTubeService, times(0)).parseVideos(any(JSONArray.class));
//        verify(mockHttpClient, times(0)).sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
//    }
//
//    /**
//     * Tests that searchVideos performs an API call and populates the cache when no cached data is available.
//     * This verifies that the API call is made as expected and that the results are cached for future requests.
//     */
//    @Test
//    public void testSearchVideosWithoutCache() throws Exception {
//        String keyword = "test";
//        int numOfResults = 2;
//
//        // Define the expected API URL
//        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
//        String expectedApiUrl = "https://www.googleapis.com/youtube/v3/search?part=snippet&order=date&type=video&maxResults="
//                + numOfResults + "&q=" + encodedKeyword + "&key=FAKE_API_KEY";
//
//        // Create a mock JSON response that includes the "items" array
//        String mockJsonResponse = "{ \"items\": [ " +
//                "{ \"id\": { \"videoId\": \"videoId1\" }, " +
//                "\"snippet\": { " +
//                "\"title\": \"Video 1\", " +
//                "\"description\": \"Description 1\", " +
//                "\"channelTitle\": \"Channel 1\", " +
//                "\"thumbnails\": { \"default\": { \"url\": \"https://thumbnail.url\" } }, " +
//                "\"channelId\": \"channelId1\" " +
//                "} }, " +
//                "{ \"id\": { \"videoId\": \"videoId2\" }, " +
//                "\"snippet\": { " +
//                "\"title\": \"Video 2\", " +
//                "\"description\": \"Description 2\", " +
//                "\"channelTitle\": \"Channel 2\", " +
//                "\"thumbnails\": { \"default\": { \"url\": \"https://thumbnail2.url\" } }, " +
//                "\"channelId\": \"channelId2\" " +
//                "} } " +
//                "] }";
//
//        // Configure the mockHttpResponse to return the mock JSON
//        when(mockHttpResponse.body()).thenReturn(mockJsonResponse);
//        when(mockHttpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
//                .thenReturn(CompletableFuture.completedFuture(mockHttpResponse));
//
//        // Configure the mockYouTubeService to return expectedVideos when parseVideos is called with any JSONArray
//        List<Video> expectedVideos = List.of(
//                new Video("Video 1", "Description 1", "Channel 1", "https://thumbnail.url", "videoId1", "channelId1", "https://www.youtube.com/watch?v=videoId1"),
//                new Video("Video 2", "Description 2", "Channel 2", "https://thumbnail2.url", "videoId2", "channelId2", "https://www.youtube.com/watch?v=videoId2")
//        );
//        when(mockYouTubeService.parseVideos(any(JSONArray.class))).thenReturn(expectedVideos);
//
//        // Call searchVideos, which should perform the mocked API call
//        CompletionStage<List<Video>> videosFuture = searchService.searchVideos(keyword, numOfResults);
//
//        // Validate that the videos match the mocked API response
//        videosFuture.thenAccept(videos -> {
//            assertNotNull("Videos should not be null", videos);
//            assertEquals("API response should match expected size", 2, videos.size());
//
//            Video video1 = videos.get(0);
//            assertEquals("Video 1", video1.getTitle());
//            assertEquals("Description 1", video1.getDescription());
//            assertEquals("Channel 1", video1.getChannelTitle());
//            assertEquals("https://thumbnail.url", video1.getThumbnailUrl());
//            assertEquals("videoId1", video1.getVideoId());
//            assertEquals("channelId1", video1.getChannelId());
//            assertEquals("https://www.youtube.com/watch?v=videoId1", video1.getVideoURL());
//
//            Video video2 = videos.get(1);
//            assertEquals("Video 2", video2.getTitle());
//            assertEquals("Description 2", video2.getDescription());
//            assertEquals("Channel 2", video2.getChannelTitle());
//            assertEquals("https://thumbnail2.url", video2.getThumbnailUrl());
//            assertEquals("videoId2", video2.getVideoId());
//            assertEquals("channelId2", video2.getChannelId());
//            assertEquals("https://www.youtube.com/watch?v=videoId2", video2.getVideoURL());
//        }).toCompletableFuture().get();
//
//        // Verify that the cache was populated
//        String cacheKey = keyword + ":" + numOfResults;
//        assertTrue("Cache should contain the key after search", testCache.containsKey(cacheKey));
//        List<Video> cachedVideos = testCache.get(cacheKey);
//        assertEquals("Cached videos should match expected size", 2, cachedVideos.size());
//
//        // Verify that YouTubeService.parseVideos was called once with the correct JSONArray
//        ArgumentCaptor<JSONArray> jsonArrayCaptor = ArgumentCaptor.forClass(JSONArray.class);
//        verify(mockYouTubeService, times(1)).parseVideos(jsonArrayCaptor.capture());
//        assertEquals("JSONArray should have expected number of items", 2, jsonArrayCaptor.getValue().length());
//
//        // Verify that HttpClient.sendAsync was called once with the correct URI
//        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
//        verify(mockHttpClient, times(1)).sendAsync(requestCaptor.capture(), any(HttpResponse.BodyHandler.class));
//
//        HttpRequest capturedRequest = requestCaptor.getValue();
//        assertEquals("HTTP method should be GET", "GET", capturedRequest.method());
//        assertEquals("URI should match expected", URI.create(expectedApiUrl), capturedRequest.uri());
//    }
//
//    /**
//     * Verifies that adding more than MAX_SEARCH_HISTORY search results to a session's search history
//     * results in the oldest entries being removed, maintaining the history size within the limit.
//     */
//    @Test
//    public void testSearchHistoryMaintainsMaxSize() throws Exception {
//        String sessionId = "session1";
//        int maxHistory = 10; // Assuming MAX_SEARCH_HISTORY is 10
//        int totalSearches = 12; // Adding 2 more than the max to trigger removal
//
//        // Add initial MAX_SEARCH_HISTORY entries
//        for (int i = 1; i <= maxHistory; i++) {
//            String keyword = "keyword" + i;
//            List<Video> videos = List.of(
//                    new Video("Video " + i, "Description " + i, "Channel " + i, "https://thumbnail" + i + ".url", "videoId" + i, "channelId" + i, "https://www.youtube.com/watch?v=videoId" + i)
//            );
//            searchService.addSearchResultToHistory(sessionId, keyword, videos);
//        }
//
//        // Add additional entries to exceed the MAX_SEARCH_HISTORY
//        for (int i = maxHistory + 1; i <= totalSearches; i++) {
//            String keyword = "keyword" + i;
//            List<Video> videos = List.of(
//                    new Video("Video " + i, "Description " + i, "Channel " + i, "https://thumbnail" + i + ".url", "videoId" + i, "channelId" + i, "https://www.youtube.com/watch?v=videoId" + i)
//            );
//            searchService.addSearchResultToHistory(sessionId, keyword, videos);
//        }
//
//        // Retrieve the search history
//        Map<String, List<Video>> history = searchService.getSearchHistory(sessionId);
//
//        // Assert that the history size does not exceed MAX_SEARCH_HISTORY
//        assertTrue("History size should not exceed MAX_SEARCH_HISTORY", history.size() <= maxHistory);
//
//        // Verify that the first two entries have been removed
//        for (int i = 1; i <= 2; i++) {
//            String oldKeyword = "keyword" + i;
//            assertFalse("Old keyword should have been removed from history", history.containsKey(oldKeyword));
//        }
//
//        // Verify that the latest entries are present
//        for (int i = 3; i <= 12; i++) {
//            String keyword = "keyword" + i;
//            assertTrue("Latest keyword should be present in history", history.containsKey(keyword));
//        }
//
//        // Optionally, verify that the oldest remaining entry is "keyword3"
//        assertTrue("The oldest entry should be 'keyword3'", history.containsKey("keyword3"));
//    }
//
//    /**
//     * Verifies that addSearchResultToHistory successfully adds a search result to the session's search history.
//     * This ensures that each session's search history is correctly maintained.
//     */
//    @Test
//    public void testAddSearchResultToHistory() {
//        String sessionId = "session1";
//        String keyword = "testKeyword";
//        List<Video> videos = List.of(new Video("Test Video", "Description", "Channel", "https://thumbnail.url", "videoId", "channelId", "https://www.youtube.com/watch?v=videoId"));
//
//        // Add result to history
//        searchService.addSearchResultToHistory(sessionId, keyword, videos);
//
//        // Validate that the history contains the added result
//        Map<String, List<Video>> history = searchService.getSearchHistory(sessionId);
//        assertTrue("History should contain the keyword", history.containsKey(keyword));
//        assertEquals("History videos should match expected", videos, history.get(keyword));
//    }
//
//    /**
//     * Tests that calculateIndividualSentiments computes sentiment values for each search result in the session history.
//     * Mocked sentiment values are used to verify that the sentiment analysis functionality works as expected.
//     */
//    @Test
//    public void testCalculateIndividualSentiments() throws Exception {
//        // Mock sentiment response
//        when(mockSentimentService.avgSentiment(anyList())).thenReturn(CompletableFuture.completedFuture("positive"));
//
//        String sessionId = "session1";
//        String keyword = "testKeyword";
//        List<Video> videos = List.of(new Video("Test Video", "Description", "Channel", "https://thumbnail.url", "videoId", "channelId", "https://www.youtube.com/watch?v=videoId"));
//
//        // Add result to history for sentiment analysis
//        searchService.addSearchResultToHistory(sessionId, keyword, videos);
//
//        // Calculate individual sentiments
//        CompletionStage<Map<String, String>> sentimentsFuture = searchService.calculateIndividualSentiments(sessionId);
//
//        // Validate the sentiments
//        sentimentsFuture.thenAccept(sentiments -> {
//            assertNotNull("Sentiments should not be null", sentiments);
//            assertEquals("Sentiment should be calculated", "positive", sentiments.get(keyword));
//        }).toCompletableFuture().get();
//
//        // Verify that SentimentService.avgSentiment was called once with the correct parameters
//        verify(mockSentimentService, times(1)).avgSentiment(videos);
//    }
//
//    /**
//     * Tests that calculateOverallSentiment computes a single sentiment value for all search results in the session history.
//     * This test verifies the overall sentiment calculation functionality using a mocked response.
//     */
//    @Test
//    public void testCalculateOverallSentiment() throws Exception {
//        // Mock overall sentiment response
//        when(mockSentimentService.avgSentiment(anyList())).thenReturn(CompletableFuture.completedFuture("neutral"));
//
//        String sessionId = "session1";
//        String keyword = "testKeyword";
//        List<Video> videos = List.of(new Video("Test Video", "Description", "Channel", "https://thumbnail.url", "videoId", "channelId", "https://www.youtube.com/watch?v=videoId"));
//
//        // Add result to history for overall sentiment analysis
//        searchService.addSearchResultToHistory(sessionId, keyword, videos);
//
//        // Calculate overall sentiment
//        CompletionStage<String> overallSentimentFuture = searchService.calculateOverallSentiment(sessionId, 10);
//
//        // Validate overall sentiment result
//        overallSentimentFuture.thenAccept(overallSentiment -> {
//            assertNotNull("Overall sentiment should not be null", overallSentiment);
//            assertEquals("Overall sentiment should match expected", "neutral", overallSentiment);
//        }).toCompletableFuture().get();
//
//        // Verify that SentimentService.avgSentiment was called once with the correct parameters
//        verify(mockSentimentService, times(1)).avgSentiment(videos);
//    }
//
//    /**
//     * Verifies that clearSearchHistory removes all search results from the session's search history.
//     * This test ensures that the history is correctly cleared when requested.
//     */
//    @Test
//    public void testClearSearchHistory() {
//        String sessionId = "session1";
//        String keyword = "testKeyword";
//        List<Video> videos = List.of(new Video("Test Video", "Description", "Channel", "https://thumbnail.url", "videoId", "channelId", "https://www.youtube.com/watch?v=videoId"));
//
//        // Add a result to history
//        searchService.addSearchResultToHistory(sessionId, keyword, videos);
//        assertFalse("History should not be empty before clearing", searchService.getSearchHistory(sessionId).isEmpty());
//
//        // Clear search history
//        searchService.clearSearchHistory(sessionId);
//
//        // Validate that the history is cleared
//        assertTrue("History should be empty after clearing", searchService.getSearchHistory(sessionId).isEmpty());
//    }
//
//    /**
//     * **New Test Method**
//     *
//     * Verifies that getAllVideosForSentiment returns an empty list when the session has no search history.
//     * This ensures that the `if (searchHistory == null)` condition is properly handled.
//     */
//    @Test
//    public void testGetAllVideosForSentiment_NoHistory() {
//        String nonExistentSessionId = "nonExistentSession";
//        int limit = 5;
//
//        // Ensure no search history exists for the given sessionId
//        assertTrue("Search history should be empty before test",
//                searchService.getSearchHistory(nonExistentSessionId).isEmpty());
//
//        // Invoke getAllVideosForSentiment
//        List<Video> result = searchService.getAllVideosForSentiment(nonExistentSessionId, limit);
//
//        // Assert that the returned list is empty
//        assertNotNull("Result should not be null", result);
//        assertTrue("Result should be empty for non-existent sessionId", result.isEmpty());
//    }
//
//    /**
//     * **Optional Additional Test Method**
//     *
//     * Verifies that getAllVideosForSentiment correctly retrieves videos up to the specified limit when the session has search history.
//     * This indirectly covers the non-null path of the `if` statement.
//     */
//    @Test
//    public void testGetAllVideosForSentiment_WithHistory() {
//        String sessionId = "sessionWithHistory";
//        int limit = 5;
//
//        // Add multiple search results to the session's history
//        for (int i = 1; i <= 10; i++) {
//            String keyword = "keyword" + i;
//            List<Video> videos = List.of(
//                    new Video("Video " + i, "Description " + i, "Channel " + i,
//                            "https://thumbnail" + i + ".url", "videoId" + i,
//                            "channelId" + i, "https://www.youtube.com/watch?v=videoId" + i)
//            );
//            searchService.addSearchResultToHistory(sessionId, keyword, videos);
//        }
//
//        // Invoke getAllVideosForSentiment
//        List<Video> result = searchService.getAllVideosForSentiment(sessionId, limit);
//
//        // Assert that the returned list contains the correct number of videos
//        assertNotNull("Result should not be null", result);
//        assertEquals("Result size should match the limit", limit, result.size());
//
//        // Optionally, verify the content of the videos
//        for (int i = 0; i < limit; i++) {
//            Video video = result.get(i);
//            assertEquals("Video title should match", "Video " + (i + 1), video.getTitle());
//        }
//    }
//
//    /**
//     * Tests removeOldestEntry to confirm that the oldest entry is removed when history size is at the maximum limit.
//     */
//
//    @Test
//    public void testRemoveOldestEntry() {
//        // Set up a LinkedHashMap with a size at the limit
//        LinkedHashMap<String, List<Video>> searchHistory = new LinkedHashMap<>();
//        for (int i = 1; i <= 10; i++) { // Assuming MAX_SEARCH_HISTORY is 10
//            searchHistory.put("keyword" + i, List.of(
//                    new Video("Video " + i, "Description " + i, "Channel " + i,
//                            "https://thumbnail" + i + ".url", "videoId" + i,
//                            "channelId" + i, "https://www.youtube.com/watch?v=videoId" + i)
//            ));
//        }
//
//        // Call the removeOldestEntry helper method
//        searchService.removeOldestEntry(searchHistory);
//
//        // Verify that the oldest entry was removed
//        assertEquals("History size should be one less after removal", 9, searchHistory.size());
//        assertFalse("Oldest entry (keyword1) should be removed", searchHistory.containsKey("keyword1"));
//    }
//
//    /**
//     * Verifies removeOldestEntry's behavior by checking direct state changes to ensure it removes the oldest entry.
//     */
//    @Test
//    public void testRemoveOldestEntryDirectStateVerification() {
//        // Create a LinkedHashMap and fill it to the maximum capacity
//        LinkedHashMap<String, List<Video>> searchHistory = new LinkedHashMap<>();
//        for (int i = 1; i <= 10; i++) { // Assuming MAX_SEARCH_HISTORY is 10
//            searchHistory.put("keyword" + i, List.of(
//                    new Video("Video " + i, "Description " + i, "Channel " + i,
//                            "https://thumbnail" + i + ".url", "videoId" + i,
//                            "channelId" + i, "https://www.youtube.com/watch?v=videoId" + i)
//            ));
//        }
//
//        // Confirm that "keyword1" is the first entry and exists before removal
//        assertTrue("The first entry should be 'keyword1' before removal", searchHistory.containsKey("keyword1"));
//
//        // Call the removeOldestEntry method directly
//        searchService.removeOldestEntry(searchHistory);
//
//        // Verify that the oldest entry ("keyword1") was removed
//        assertEquals("History size should be reduced by 1 after removal", 9, searchHistory.size());
//        assertFalse("Oldest entry (keyword1) should be removed", searchHistory.containsKey("keyword1"));
//
//        // Check that the next expected oldest entry ("keyword2") is now present as the first entry
//        assertTrue("The next entry 'keyword2' should remain after removal", searchHistory.containsKey("keyword2"));
//    }
//
//    /**
//     * Tests removeOldestEntry on an empty search history, confirming no exceptions and no changes to history state.
//     */
//    @Test
//    public void testRemoveOldestEntryWithEmptyHistory() {
//        // Create an empty LinkedHashMap
//        LinkedHashMap<String, List<Video>> searchHistory = new LinkedHashMap<>();
//
//        // Call removeOldestEntry on the empty history
//        searchService.removeOldestEntry(searchHistory);
//
//        // Verify that the history is still empty after calling the method
//        assertTrue("History should remain empty after calling removeOldestEntry on an empty map", searchHistory.isEmpty());
//    }
//
//
//
//}
