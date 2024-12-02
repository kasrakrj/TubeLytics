package models.services;

import models.entities.Video;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletionStage;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Complete test suite for the SearchService class.
 */
public class SearchServiceTest {
    private SearchService searchService;
    private SentimentService mockSentimentService;
    private YouTubeService mockYouTubeService;
    private HttpClient mockHttpClient;
    private ConcurrentHashMap<String, List<Video>> testCache;

    @Before
    public void setUp() {
        mockSentimentService = Mockito.mock(SentimentService.class);
        mockYouTubeService = Mockito.mock(YouTubeService.class);
        mockHttpClient = Mockito.mock(HttpClient.class);

        when(mockYouTubeService.getApiUrl()).thenReturn("https://www.googleapis.com/youtube/v3");
        when(mockYouTubeService.getApiKey()).thenReturn("FAKE_API_KEY");

        testCache = new ConcurrentHashMap<>();
        searchService = new SearchService(mockSentimentService, mockYouTubeService);
    }

    /**
     * Tests the constructor to ensure proper initialization.
     */
    @Test
    public void testConstructor() {
        assertNotNull("SearchService instance should not be null", searchService);
        assertNotNull("YouTubeService should not be null", searchService.youTubeService);
        assertNotNull("SentimentService should not be null", searchService.sentimentService);
    }
    @Test
    public void testSearchVideos() throws Exception {
        // Arrange
        String keyword = "testKeyword";
        int numOfResults = 2;
        String cacheKey = keyword + ":" + numOfResults;

        // Mock JSON response from YouTube API
        HttpResponse<String> mockResponse = Mockito.mock(HttpResponse.class);
        String mockResponseBody = "{"
                + "\"items\": ["
                + "  {"
                + "    \"id\": { \"videoId\": \"video1\" },"
                + "    \"snippet\": {"
                + "      \"title\": \"Test Video 1\","
                + "      \"description\": \"Description 1\","
                + "      \"channelTitle\": \"Channel 1\","
                + "      \"thumbnails\": { \"default\": { \"url\": \"Thumbnail1\" } }"
                + "    }"
                + "  },"
                + "  {"
                + "    \"id\": { \"videoId\": \"video2\" },"
                + "    \"snippet\": {"
                + "      \"title\": \"Test Video 2\","
                + "      \"description\": \"Description 2\","
                + "      \"channelTitle\": \"Channel 2\","
                + "      \"thumbnails\": { \"default\": { \"url\": \"Thumbnail2\" } }"
                + "    }"
                + "  }"
                + "]"
                + "}";
        when(mockResponse.body()).thenReturn(mockResponseBody);

        // Mock HttpClient behavior
        HttpClient mockHttpClient = Mockito.mock(HttpClient.class);
        when(mockHttpClient.sendAsync(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString())))
                .thenReturn(CompletableFuture.completedFuture(mockResponse));

        // Replace the httpClient field in SearchService using reflection
        java.lang.reflect.Field httpClientField = SearchService.class.getDeclaredField("httpClient");
        httpClientField.setAccessible(true);
        httpClientField.set(searchService, mockHttpClient);

        // Mock parsing logic in YouTubeService
        List<Video> mockVideos = Arrays.asList(
                new Video("Test Video 1", "Description 1", "Channel 1", "Thumbnail1", "video1", "channelId1", "url1", "2024-12-01"),
                new Video("Test Video 2", "Description 2", "Channel 2", "Thumbnail2", "video2", "channelId2", "url2", "2024-12-02")
        );
        when(mockYouTubeService.parseVideos(any(JSONArray.class))).thenReturn(mockVideos);

        // Act
        List<Video> resultVideos = searchService.searchVideos(keyword, numOfResults)
                .toCompletableFuture()
                .get();

        // Assert
        assertNotNull("The result videos list should not be null", resultVideos);
        assertEquals("The number of videos should match the mocked data", 2, resultVideos.size());
        assertEquals("The first video title should match", "Test Video 1", resultVideos.get(0).getTitle());

        // Verify that the cache is updated
        assertTrue("Cache should contain the key", searchService.getCache().containsKey(cacheKey));
        assertEquals("Cached videos should match the result videos", mockVideos, searchService.getCache().get(cacheKey));
    }

    @Test
    public void testIsNewVideo() throws Exception {
        // Arrange
        Set<String> processedVideoIds = new HashSet<>();
        Video video1 = new Video("Video 1", "Description", "Channel", "Thumbnail", "id1", "channelId", "URL", "2024-12-01");
        Video video2 = new Video("Video 2", "Description", "Channel", "Thumbnail", "id2", "channelId", "URL", "2024-12-02");

        // Use reflection to access the private method
        Method isNewVideoMethod = SearchService.class.getDeclaredMethod("isNewVideo", Video.class, Set.class);
        isNewVideoMethod.setAccessible(true);

        // Act
        boolean isVideo1New = (boolean) isNewVideoMethod.invoke(searchService, video1, processedVideoIds);
        boolean isVideo1Duplicate = (boolean) isNewVideoMethod.invoke(searchService, video1, processedVideoIds);
        boolean isVideo2New = (boolean) isNewVideoMethod.invoke(searchService, video2, processedVideoIds);

        // Assert
        assertTrue("Video 1 should be new", isVideo1New);
        assertFalse("Video 1 should not be new on second addition", isVideo1Duplicate);
        assertTrue("Video 2 should be new", isVideo2New);
        assertTrue("Processed video IDs should contain Video 1's ID", processedVideoIds.contains("id1"));
        assertTrue("Processed video IDs should contain Video 2's ID", processedVideoIds.contains("id2"));
    }


    /**
     * Tests fetchNewVideos in testing mode.
     */
    @Test
    public void testFetchNewVideosInTestingMode() throws Exception {
        searchService.isTestingMode = true;
        String keyword = "testKeyword";
        int numOfResults = 5;
        Set<String> processedVideoIds = new HashSet<>();

        List<Video> newVideos = searchService.fetchNewVideos(keyword, numOfResults, processedVideoIds)
                .toCompletableFuture()
                .get();

        assertNotNull("New videos should not be null", newVideos);
        assertEquals("Should return the expected number of mock videos", 2, newVideos.size());
    }

    /**
     * Tests calculateSentiments method.
     */
    @Test
    public void testCalculateSentiments() throws Exception {
        String sessionId = "session1";

        // Mock search history and sentiment calculation
        String keyword1 = "keyword1";
        String keyword2 = "keyword2";
        List<Video> videos1 = List.of(new Video("Title1", "Desc1", "Channel1", "Thumb1", "videoId1", "channelId1", "URL1", "2024-12-01"));
        List<Video> videos2 = List.of(new Video("Title2", "Desc2", "Channel2", "Thumb2", "videoId2", "channelId2", "URL2", "2024-12-02"));

        searchService.addSearchResult(sessionId, keyword1, videos1);
        searchService.addSearchResult(sessionId, keyword2, videos2);

        when(mockSentimentService.avgSentiment(anyList()))
                .thenReturn(CompletableFuture.completedFuture("positive"));

        Map<String, String> sentiments = searchService.calculateSentiments(sessionId)
                .toCompletableFuture()
                .get();

        assertNotNull("Sentiments should not be null", sentiments);
        assertEquals("Sentiment for keyword1 should be positive", "positive", sentiments.get(keyword1));
        assertEquals("Sentiment for keyword2 should be positive", "positive", sentiments.get(keyword2));
    }

    /**
     * Tests updateVideosForKeyword (global session version).
     */
    @Test
    public void testUpdateVideosForKeyword_Global() {
        String keyword = "testKeyword";
        List<Video> newVideos = List.of(
                new Video("NewTitle", "NewDesc", "NewChannel", "NewThumb", "newVideoId", "newChannelId", "NewURL", "2024-12-09")
        );

        searchService.updateVideosForKeyword(keyword, newVideos);

        for (LinkedHashMap<String, List<Video>> history : searchService.sessionSearchHistoryMap.values()) {
            assertTrue("History should contain the keyword", history.containsKey(keyword));
            assertEquals("Keyword should have updated videos", newVideos, history.get(keyword));
        }
    }

    /**
     * Tests updateVideosForKeyword (session-specific version).
     */
    @Test
    public void testUpdateVideosForKeyword_Session() {
        String sessionId = "session1";
        String keyword = "testKeyword";
        List<Video> videos = List.of(
                new Video("Title1", "Desc1", "Channel1", "Thumb1", "videoId1", "channelId1", "URL1", "2024-12-01")
        );

        searchService.updateVideosForKeyword(sessionId, keyword, videos);

        Map<String, List<Video>> searchHistory = searchService.getSearchHistory(sessionId);
        assertNotNull("Search history should not be null", searchHistory);
        assertEquals("Should contain the new videos", videos, searchHistory.get(keyword));
    }

    /**
     * Tests removeOldestEntry.
     */
    @Test
    public void testRemoveOldestEntry() {
        LinkedHashMap<String, List<Video>> searchHistory = new LinkedHashMap<>();
        for (int i = 1; i <= 10; i++) {
            searchHistory.put("keyword" + i, List.of(
                    new Video("Title" + i, "Desc" + i, "Channel" + i, "Thumb" + i, "videoId" + i, "channelId" + i, "URL" + i, "2024-12-0" + i)
            ));
        }

        searchService.removeOldestEntry(searchHistory);

        assertEquals("Search history size should be reduced by 1", 9, searchHistory.size());
        assertFalse("Oldest entry should be removed", searchHistory.containsKey("keyword1"));
    }

    /**
     * Tests clearSearchHistory.
     */
    @Test
    public void testClearSearchHistory() {
        String sessionId = "session1";
        String keyword = "testKeyword";

        List<Video> videos = List.of(
                new Video("Title", "Desc", "Channel", "Thumb", "videoId", "channelId", "URL", "2024-12-01")
        );
        searchService.updateVideosForKeyword(sessionId, keyword, videos);

        assertFalse("Search history should not be empty before clearing", searchService.getSearchHistory(sessionId).isEmpty());

        searchService.clearSearchHistory(sessionId);

        assertTrue("Search history should be empty after clearing", searchService.getSearchHistory(sessionId).isEmpty());
    }

    /**
     * Tests accessor methods.
     */
    @Test
    public void testAccessorMethods() {
        assertEquals("API URL should match", "https://www.googleapis.com/youtube/v3", searchService.getAPI_URL());
        assertEquals("API Key should match", "FAKE_API_KEY", searchService.getAPI_KEY());
        assertNotNull("HttpClient should not be null", searchService.getHttpClient());
        assertNotNull("Cache should not be null", searchService.getCache());
        assertEquals("YouTube search URL should match",
                "https://www.googleapis.com/youtube/v3/search?part=snippet&order=date&type=video&maxResults=",
                searchService.getYOUTUBE_SEARCH_URL());
    }
}
