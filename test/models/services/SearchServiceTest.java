package models.services;

import models.entities.Video;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the SearchService class. This test suite verifies caching behavior, search history management,
 * and sentiment analysis functionality in SearchService.
 */
public class SearchServiceTest {

    private SearchService searchService;
    private SentimentService mockSentimentService;
    private YouTubeService mockYouTubeService;
    private HttpClient mockHttpClient;
    private HttpResponse<String> mockHttpResponse;
    private ConcurrentHashMap<String, List<Video>> testCache;

    /**
     * Sets up the necessary mocks and dependencies before each test.
     * Initializes SearchService with a mock SentimentService, mock YouTubeService, mock HttpClient, and a custom cache.
     */
    @Before
    public void setUp() throws Exception {
        // Mock dependencies
        mockSentimentService = Mockito.mock(SentimentService.class);
        mockYouTubeService = Mockito.mock(YouTubeService.class);
        mockHttpClient = Mockito.mock(HttpClient.class);
        mockHttpResponse = Mockito.mock(HttpResponse.class);

        // Define mock behavior for YouTubeService
        when(mockYouTubeService.getApiUrl()).thenReturn("https://www.googleapis.com/youtube/v3");
        when(mockYouTubeService.getApiKey()).thenReturn("FAKE_API_KEY"); // Use a dummy key for testing

        // Initialize test cache
        testCache = new ConcurrentHashMap<>();

        // Create SearchService with injected dependencies
        searchService = new SearchService(mockSentimentService, mockYouTubeService);

        // Use reflection to set the private httpClient field
        Field httpClientField = SearchService.class.getDeclaredField("httpClient");
        httpClientField.setAccessible(true);
        httpClientField.set(searchService, mockHttpClient);

        // Set the cache field to our test cache
        Field cacheField = SearchService.class.getDeclaredField("cache");
        cacheField.setAccessible(true);
        cacheField.set(searchService, testCache);

        // Disable testing mode if necessary
        Field isTestingModeField = SearchService.class.getDeclaredField("isTestingMode");
        isTestingModeField.setAccessible(true);
        isTestingModeField.set(searchService, false);
    }

    /**
     * Tests that searchVideos performs an API call and populates the cache when no cached data is available.
     * This verifies that the API call is made as expected and that the results are cached for future requests.
     */
    @Test
    public void testSearchVideosWithoutCache() throws Exception {
        String keyword = "test";
        int numOfResults = 2;

        // Define the expected API URL
        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        String expectedApiUrl = "https://www.googleapis.com/youtube/v3/search?part=snippet&order=date&type=video&maxResults="
                + numOfResults + "&q=" + encodedKeyword + "&key=FAKE_API_KEY";

        // Create a mock JSON response that includes the "items" array
        String mockJsonResponse = "{ \"items\": [ " +
                "{ \"id\": { \"videoId\": \"videoId1\" }, " +
                "\"snippet\": { " +
                "\"title\": \"Video 1\", " +
                "\"description\": \"Description 1\", " +
                "\"channelTitle\": \"Channel 1\", " +
                "\"thumbnails\": { \"default\": { \"url\": \"https://thumbnail.url\" } }, " +
                "\"channelId\": \"channelId1\" " +
                "} }, " +
                "{ \"id\": { \"videoId\": \"videoId2\" }, " +
                "\"snippet\": { " +
                "\"title\": \"Video 2\", " +
                "\"description\": \"Description 2\", " +
                "\"channelTitle\": \"Channel 2\", " +
                "\"thumbnails\": { \"default\": { \"url\": \"https://thumbnail2.url\" } }, " +
                "\"channelId\": \"channelId2\" " +
                "} } " +
                "] }";

        // Configure the mockHttpResponse to return the mock JSON
        when(mockHttpResponse.body()).thenReturn(mockJsonResponse);
        when(mockHttpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(CompletableFuture.completedFuture(mockHttpResponse));

        // Configure the mockYouTubeService to return expectedVideos when parseVideos is called with any JSONArray
        List<Video> expectedVideos = List.of(
                new Video("Video 1", "Description 1", "Channel 1", "https://thumbnail.url", "videoId1", "channelId1", "https://www.youtube.com/watch?v=videoId1", "published_date1"),
                new Video("Video 2", "Description 2", "Channel 2", "https://thumbnail2.url", "videoId2", "channelId2", "https://www.youtube.com/watch?v=videoId2", "published_date2")
        );
        when(mockYouTubeService.parseVideos(any(JSONArray.class))).thenReturn(expectedVideos);

        // Call searchVideos, which should perform the mocked API call
        CompletionStage<List<Video>> videosFuture = searchService.searchVideos(keyword, numOfResults);

        // Validate that the videos match the mocked API response
        videosFuture.thenAccept(videos -> {
            assertNotNull("Videos should not be null", videos);
            assertEquals("API response should match expected size", 2, videos.size());

            Video video1 = videos.get(0);
            assertEquals("Video 1", video1.getTitle());
            assertEquals("Description 1", video1.getDescription());
            assertEquals("Channel 1", video1.getChannelTitle());
            assertEquals("https://thumbnail.url", video1.getThumbnailUrl());
            assertEquals("videoId1", video1.getVideoId());
            assertEquals("channelId1", video1.getChannelId());

            Video video2 = videos.get(1);
            assertEquals("Video 2", video2.getTitle());
            assertEquals("Description 2", video2.getDescription());
            assertEquals("Channel 2", video2.getChannelTitle());
            assertEquals("https://thumbnail2.url", video2.getThumbnailUrl());
            assertEquals("videoId2", video2.getVideoId());
            assertEquals("channelId2", video2.getChannelId());
        }).toCompletableFuture().get();

        // Verify that the cache was populated
        String cacheKey = keyword + ":" + numOfResults;
        assertTrue("Cache should contain the key after search", testCache.containsKey(cacheKey));
        List<Video> cachedVideos = testCache.get(cacheKey);
        assertEquals("Cached videos should match expected size", 2, cachedVideos.size());

        // Verify that YouTubeService.parseVideos was called once with the correct JSONArray
        ArgumentCaptor<JSONArray> jsonArrayCaptor = ArgumentCaptor.forClass(JSONArray.class);
        verify(mockYouTubeService, times(1)).parseVideos(jsonArrayCaptor.capture());
        assertEquals("JSONArray should have expected number of items", 2, jsonArrayCaptor.getValue().length());

        // Verify that HttpClient.sendAsync was called once with the correct URI
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockHttpClient, times(1)).sendAsync(requestCaptor.capture(), any(HttpResponse.BodyHandler.class));

        HttpRequest capturedRequest = requestCaptor.getValue();
        assertEquals("HTTP method should be GET", "GET", capturedRequest.method());
        assertEquals("URI should match expected", URI.create(expectedApiUrl), capturedRequest.uri());
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
                new Video("Video 1", "Description 1", "Channel 1", "https://thumbnail.url", "videoId1", "channelId1", "https://www.youtube.com/watch?v=videoId1", "publishedAt1"),
                new Video("Video 2", "Description 2", "Channel 2", "https://thumbnail2.url", "videoId2", "channelId2", "https://www.youtube.com/watch?v=videoId2", "publishedAt2")
        );

        // Insert expected videos into the cache
        String cacheKey = keyword + ":" + numOfResults;
        testCache.put(cacheKey, expectedVideos);

        // Call searchVideos, which should retrieve the cached result
        CompletionStage<List<Video>> videosFuture = searchService.searchVideos(keyword, numOfResults);

        // Validate that cached result is returned
        List<Video> videos = videosFuture.toCompletableFuture().get();
        assertNotNull("Videos should not be null", videos);
        assertEquals("Cached videos should match expected size", expectedVideos.size(), videos.size());
        for (int i = 0; i < videos.size(); i++) {
            assertEquals("Titles should match", expectedVideos.get(i).getTitle(), videos.get(i).getTitle());
            assertEquals("Channel titles should match", expectedVideos.get(i).getChannelTitle(), videos.get(i).getChannelTitle());
        }

        // Verify that YouTubeService.parseVideos and HttpClient.sendAsync were not called since cache was used
        verify(mockYouTubeService, times(0)).parseVideos(any(JSONArray.class));
        verify(mockHttpClient, times(0)).sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    /**
     * Verifies that adding more than MAX_SEARCH_HISTORY search results to a session's search history
     * results in the oldest entries being removed, maintaining the history size within the limit.
     */
    @Test
    public void testSearchHistoryMaintainsMaxSize() {
        String sessionId = "session1";
        int maxHistory = 10; // Assuming MAX_SEARCH_HISTORY is 10
        int totalSearches = 12; // Adding 2 more than the max to trigger removal

        // Add initial MAX_SEARCH_HISTORY entries
        for (int i = 1; i <= maxHistory; i++) {
            String keyword = "keyword" + i;
            List<Video> videos = List.of(
                    new Video("Video " + i, "Description " + i, "Channel " + i, "https://thumbnail" + i + ".url", "videoId" + i, "channelId" + i, "https://www.youtube.com/watch?v=videoId" + i, "published_date" + i)
            );
            searchService.addSearchResult(sessionId, keyword, videos);
        }

        // Add additional entries to exceed the MAX_SEARCH_HISTORY
        for (int i = maxHistory + 1; i <= totalSearches; i++) {
            String keyword = "keyword" + i;
            List<Video> videos = List.of(
                    new Video("Video " + i, "Description " + i, "Channel " + i, "https://thumbnail" + i + ".url", "videoId" + i, "channelId" + i, "https://www.youtube.com/watch?v=videoId" + i, "published_date" + i)
            );
            searchService.addSearchResult(sessionId, keyword, videos);
        }

        // Retrieve the search history
        Map<String, List<Video>> history = searchService.getSearchHistory(sessionId);

        // Assert that the history size does not exceed MAX_SEARCH_HISTORY
        assertTrue("History size should not exceed MAX_SEARCH_HISTORY", history.size() <= maxHistory);

        // Verify that the first two entries have been removed
        for (int i = 1; i <= 2; i++) {
            String oldKeyword = "keyword" + i;
            assertFalse("Old keyword should have been removed from history", history.containsKey(oldKeyword));
        }

        // Verify that the latest entries are present
        for (int i = 3; i <= 12; i++) {
            String keyword = "keyword" + i;
            assertTrue("Latest keyword should be present in history", history.containsKey(keyword));
        }

        // Optionally, verify that the oldest remaining entry is "keyword3"
        assertTrue("The oldest entry should be 'keyword3'", history.containsKey("keyword3"));
    }

    /**
     * Verifies that addSearchResult successfully adds a search result to the session's search history.
     * This ensures that each session's search history is correctly maintained.
     */
  /*  @Test
    public void testAddSearchResult() {
        String sessionId = "session1";
        String keyword = "testKeyword";
        List<Video> videos = List.of(new Video("Test Video", "Description", "Channel", "https://thumbnail.url", "videoId", "channelId", "https://www.youtube.com/watch?v=videoId", "published_date"));

        // Add result to history
        searchService.addSearchResult(sessionId, keyword, videos);

        // Validate that the history contains the added result
        Map<String, List<Video>> history = searchService.getSearchHistory(sessionId);
        assertTrue("History should contain the keyword", history.containsKey(keyword));
        assertEquals("History videos should match expected", videos, history.get(keyword));
    }*/

    /**
     * Verifies that clearSearchHistory removes all search results from the session's search history.
     * This test ensures that the history is correctly cleared when requested.
     */
    @Test
    public void testClearSearchHistory() {
        String sessionId = "session1";
        String keyword = "testKeyword";
        List<Video> videos = List.of(new Video("Test Video", "Description", "Channel", "https://thumbnail.url", "videoId", "channelId", "https://www.youtube.com/watch?v=videoId", "published_date"));

        // Add a result to history
        searchService.addSearchResult(sessionId, keyword, videos);
        assertFalse("History should not be empty before clearing", searchService.getSearchHistory(sessionId).isEmpty());

        // Clear search history
        searchService.clearSearchHistory(sessionId);

        // Validate that the history is cleared
        assertTrue("History should be empty after clearing", searchService.getSearchHistory(sessionId).isEmpty());
    }

    /**
     * Tests the calculateSentiments method to ensure it calculates sentiments for each keyword in the session's search history.
     */
    @Test
    public void testCalculateSentiments() throws Exception {
        String sessionId = "session1";

        // Mock sentimentService.avgSentiment to return a predefined sentiment
        when(mockSentimentService.avgSentiment(anyList())).thenReturn(CompletableFuture.completedFuture("positive"));

        // Prepare search history for the session
        String keyword1 = "keyword1";
        String keyword2 = "keyword2";
        List<Video> videos1 = List.of(new Video("Video1", "Description1", "Channel1", "Thumbnail1", "videoId1", "channelId1", "VideoURL1", "publishedAt1"));
        List<Video> videos2 = List.of(new Video("Video2", "Description2", "Channel2", "Thumbnail2", "videoId2", "channelId2", "VideoURL2", "publishedAt2"));
        searchService.addSearchResult(sessionId, keyword1, videos1);
        searchService.addSearchResult(sessionId, keyword2, videos2);

        // Call calculateSentiments
        CompletionStage<Map<String, String>> sentimentsFuture = searchService.calculateSentiments(sessionId);

        // Wait for future to complete
        Map<String, String> sentiments = sentimentsFuture.toCompletableFuture().get();

        // Verify that sentiments are calculated for each keyword
        assertNotNull("Sentiments should not be null", sentiments);
        assertEquals("Should have sentiments for each keyword", 2, sentiments.size());
        assertEquals("Sentiment should be 'positive' for keyword1", "positive", sentiments.get(keyword1));
        assertEquals("Sentiment should be 'positive' for keyword2", "positive", sentiments.get(keyword2));

        // Verify that avgSentiment was called for each keyword's videos
        verify(mockSentimentService, times(1)).avgSentiment(videos1);
        verify(mockSentimentService, times(1)).avgSentiment(videos2);
    }

    /**
     * Tests the fetchNewVideos method to ensure it returns new videos not present in processedVideoIds.
     */
   /* @Test
    public void testFetchNewVideos() throws Exception {
        String keyword = "testKeyword";
        int numOfResults = 2;
        Set<String> processedVideoIds = new HashSet<>();

        // Create a spy of searchService to mock specific methods
        SearchService spySearchService = Mockito.spy(searchService);

        // Mock the searchVideos method to return a predefined list of videos
        List<Video> mockVideos = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Video video = new Video();
            video.setVideoId("videoId" + i);
            video.setTitle("Video " + i);
            mockVideos.add(video);
        }
        when(spySearchService.searchVideos(eq(keyword), eq(numOfResults))).thenReturn(CompletableFuture.completedFuture(mockVideos));

        // Call fetchNewVideos on the spy
        CompletionStage<List<Video>> newVideosFuture = spySearchService.fetchNewVideos(keyword, numOfResults, processedVideoIds);

        // Wait for the future to complete
        List<Video> newVideos = newVideosFuture.toCompletableFuture().get();

        // Verify that newVideos are returned
        assertNotNull("New videos should not be null", newVideos);
        assertFalse("New videos should not be empty", newVideos.isEmpty());
        assertEquals("Should return the expected number of new videos", 3, newVideos.size());

        // Verify that processedVideoIds has been updated
        for (Video video : newVideos) {
            assertTrue("Processed video IDs should contain the video ID", processedVideoIds.contains(video.getVideoId()));
        }
    }*/


    /**
     * Tests removeOldestEntry to confirm that the oldest entry is removed when history size is at the maximum limit.
     */
    @Test
    public void testRemoveOldestEntry() throws Exception {
        // Access the private removeOldestEntry method via reflection
        Method removeOldestEntryMethod = SearchService.class.getDeclaredMethod("removeOldestEntry", LinkedHashMap.class);
        removeOldestEntryMethod.setAccessible(true);

        // Set up a LinkedHashMap with a size at the limit
        LinkedHashMap<String, List<Video>> searchHistory = new LinkedHashMap<>();
        for (int i = 1; i <= 10; i++) { // Assuming MAX_SEARCH_HISTORY is 10
            searchHistory.put("keyword" + i, List.of(
                    new Video("Video " + i, "Description " + i, "Channel " + i,
                            "https://thumbnail" + i + ".url", "videoId" + i,
                            "channelId" + i, "https://www.youtube.com/watch?v=videoId" + i, "published_date" + i)
            ));
        }

        // Call the removeOldestEntry helper method
        removeOldestEntryMethod.invoke(searchService, searchHistory);

        // Verify that the oldest entry was removed
        assertEquals("History size should be one less after removal", 9, searchHistory.size());
        assertFalse("Oldest entry (keyword1) should be removed", searchHistory.containsKey("keyword1"));
    }

    /**
     * Tests the generateMockVideos method to ensure it generates the expected number of mock videos with unique IDs.
     */
    @Test
    public void testGenerateMockVideos() throws Exception {
        String keyword = "testKeyword";
        int numOfResults = 5;
        Set<String> processedVideoIds = new HashSet<>();

        // Access the private generateMockVideos method via reflection
        Method generateMockVideosMethod = SearchService.class.getDeclaredMethod("generateMockVideos", String.class, int.class, Set.class);
        generateMockVideosMethod.setAccessible(true);

        // Call the method
        @SuppressWarnings("unchecked")
        List<Video> mockVideos = (List<Video>) generateMockVideosMethod.invoke(searchService, keyword, numOfResults, processedVideoIds);

        // Verify the results
        assertNotNull("Mock videos should not be null", mockVideos);
        assertEquals("Should generate the expected number of mock videos", numOfResults, mockVideos.size());

        // Since processedVideoIds is not updated by generateMockVideos, we should not assert its size
        // Instead, verify that all video IDs are unique and not in processedVideoIds
        Set<String> videoIds = new HashSet<>();
        for (Video video : mockVideos) {
            assertNotNull("Video ID should not be null", video.getVideoId());
            assertFalse("Video ID should not be in processedVideoIds", processedVideoIds.contains(video.getVideoId()));
            assertTrue("Video ID should be unique", videoIds.add(video.getVideoId()));
        }
    }


    /**
     * Tests the updateVideosForKeywordAcrossSessions method to ensure it updates all sessions' search histories correctly.
     */
    @Test
    public void testUpdateVideosForKeywordAcrossSessions() throws Exception {
        // Access the private updateVideosForKeyword method via reflection
        Method updateVideosForKeywordMethod = SearchService.class.getDeclaredMethod("updateVideosForKeyword", String.class, List.class);
        updateVideosForKeywordMethod.setAccessible(true);

        // Prepare sessions and search histories
        String sessionId1 = "session1";
        String sessionId2 = "session2";
        String keyword = "testKeyword";

        List<Video> existingVideosSession1 = new ArrayList<>(){
            {
                add(new Video("Video1", "Description1", "Channel1", "Thumbnail1", "videoId1", "channelId1", "VideoURL1", "publishedAt1"));
            }
        };

        List<Video> existingVideosSession2 = new ArrayList<>(){
            {
                add(new Video("Video2", "Description2", "Channel2", "Thumbnail2", "videoId2", "channelId2", "VideoURL2", "publishedAt2"));
            }
        };

        searchService.addSearchResult(sessionId1, keyword, existingVideosSession1);
        searchService.addSearchResult(sessionId2, keyword, existingVideosSession2);

        // New videos to be added
        List<Video> newVideos = new ArrayList<>(){
            {
                add(new Video("Video3", "Description3", "Channel3", "Thumbnail3", "videoId3", "channelId3", "VideoURL3", "publishedAt3"));
            }
        };

        // Call updateVideosForKeyword across sessions
        updateVideosForKeywordMethod.invoke(searchService, keyword, newVideos);

        // Verify that new videos have been added to each session's search history for the keyword
        Map<String, List<Video>> historySession1 = searchService.getSearchHistory(sessionId1);
        Map<String, List<Video>> historySession2 = searchService.getSearchHistory(sessionId2);

        // Verify that the keyword exists in the search histories
        assertTrue("Session1 should have the keyword in search history", historySession1.containsKey(keyword));
        assertTrue("Session2 should have the keyword in search history", historySession2.containsKey(keyword));

        // Verify that the new videos are added and the total videos do not exceed MAX_SEARCH_HISTORY
        List<Video> updatedVideosSession1 = historySession1.get(keyword);
        List<Video> updatedVideosSession2 = historySession2.get(keyword);

        assertNotNull("Updated videos for session1 should not be null", updatedVideosSession1);
        assertNotNull("Updated videos for session2 should not be null", updatedVideosSession2);

        // Since existing videos were one each, and new videos are one, total videos should be 2
        assertEquals("Session1 should have updated number of videos", 2, updatedVideosSession1.size());
        assertEquals("Session2 should have updated number of videos", 2, updatedVideosSession2.size());

        // Verify that the new video is added at the top (index 0)
        assertEquals("New video should be at the top in session1's videos", "videoId3", updatedVideosSession1.get(0).getVideoId());
        assertEquals("New video should be at the top in session2's videos", "videoId3", updatedVideosSession2.get(0).getVideoId());
    }

    /**
     * Tests the isNewVideo method indirectly by checking the behavior through fetchNewVideos.
     */
    @Test
    public void testIsNewVideo() throws Exception {
        Video video = new Video();
        video.setVideoId("videoId1");
        Set<String> processedVideoIds = new HashSet<>();

        // Access the private isNewVideo method via reflection
        Method isNewVideoMethod = SearchService.class.getDeclaredMethod("isNewVideo", Video.class, Set.class);
        isNewVideoMethod.setAccessible(true);

        // First call, videoId not in processedVideoIds
        boolean isNew = (boolean) isNewVideoMethod.invoke(searchService, video, processedVideoIds);
        assertTrue("Video should be new", isNew);

        // Verify that videoId was added to processedVideoIds
        assertTrue("Video ID should be in processedVideoIds", processedVideoIds.contains("videoId1"));

        // Second call, videoId already in processedVideoIds
        isNew = (boolean) isNewVideoMethod.invoke(searchService, video, processedVideoIds);
        assertFalse("Video should not be new", isNew);
    }
}
