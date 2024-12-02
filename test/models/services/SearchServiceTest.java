package models.services;

import models.entities.Video;
import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletionStage;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the SearchService class.
 */
public class SearchServiceTest {
    private SearchService searchService;
    private SentimentService mockSentimentService;
    private YouTubeService mockYouTubeService;
    private ConcurrentHashMap<String, List<Video>> testCache;

    @Before
    public void setUp() {
        mockSentimentService = Mockito.mock(SentimentService.class);
        mockYouTubeService = Mockito.mock(YouTubeService.class);

        // Define mock behavior for YouTubeService
        when(mockYouTubeService.getApiUrl()).thenReturn("https://www.googleapis.com/youtube/v3");
        when(mockYouTubeService.getApiKey()).thenReturn("FAKE_API_KEY");

        testCache = new ConcurrentHashMap<>();
        searchService = new SearchService(mockSentimentService, mockYouTubeService);
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

        // Call fetchNewVideos
        List<Video> newVideos = searchService.fetchNewVideos(keyword, numOfResults, processedVideoIds)
                .toCompletableFuture()
                .get();

        // Verify the results
        assertNotNull("New videos should not be null", newVideos);
        assertEquals("Should return the expected number of mock videos", 2, newVideos.size());

        Set<String> videoIds = new HashSet<>();
        for (Video video : newVideos) {
            assertNotNull("Video ID should not be null", video.getVideoId());
            assertTrue("Video ID should be unique", videoIds.add(video.getVideoId()));
        }
    }

    /**
     * Tests fetchNewVideos in production mode.
     */
    @Test
    public void testFetchNewVideosInProductionMode() throws Exception {
        searchService.isTestingMode = false;
        String keyword = "testKeyword";
        int numOfResults = 3;
        Set<String> processedVideoIds = new HashSet<>(Set.of("videoId1", "videoId2"));

        // Create a spy for searchService
        SearchService spySearchService = Mockito.spy(searchService);

        // Mock the searchVideos method
        List<Video> allVideos = List.of(
                new Video("Title1", "Desc1", "Channel1", "Thumb1", "videoId1", "channelId1", "URL1", "2024-12-01"),
                new Video("Title2", "Desc2", "Channel2", "Thumb2", "videoId2", "channelId2", "URL2", "2024-12-02"),
                new Video("Title3", "Desc3", "Channel3", "Thumb3", "videoId3", "channelId3", "URL3", "2024-12-03")
        );
        doReturn(CompletableFuture.completedFuture(allVideos))
                .when(spySearchService)
                .searchVideos(eq(keyword), eq(numOfResults));

        // Call fetchNewVideos
        List<Video> newVideos = spySearchService.fetchNewVideos(keyword, numOfResults, processedVideoIds)
                .toCompletableFuture()
                .get();

        // Verify the results
        assertNotNull("New videos should not be null", newVideos);
        assertEquals("Should return the expected number of new videos", 1, newVideos.size());
        assertEquals("New video ID should match", "videoId3", newVideos.get(0).getVideoId());
    }

    /**
     * Tests updateVideosForKeyword with an existing keyword.
     */
    @Test
    public void testUpdateVideosForKeywordExistingKeyword() {
        String sessionId = "session1";
        String keyword = "testKeyword";

        // Prepare initial search history
        List<Video> initialVideos = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            initialVideos.add(new Video("Title" + i, "Desc" + i, "Channel" + i, "Thumb" + i, "videoId" + i, "channelId" + i, "URL" + i, "2024-12-0" + i));
        }
        searchService.updateVideosForKeyword(sessionId, keyword, initialVideos);

        // Add new videos
        List<Video> newVideos = List.of(
                new Video("NewTitle1", "NewDesc1", "NewChannel1", "NewThumb1", "newVideoId1", "newChannelId1", "NewURL1", "2024-12-09"),
                new Video("NewTitle2", "NewDesc2", "NewChannel2", "NewThumb2", "newVideoId2", "newChannelId2", "NewURL2", "2024-12-10")
        );
        searchService.updateVideosForKeyword(sessionId, keyword, newVideos);

        // Retrieve the updated search history
        Map<String, List<Video>> searchHistory = searchService.getSearchHistory(sessionId);

        // Verify the updated search history
        assertTrue("Search history should contain the keyword", searchHistory.containsKey(keyword));
        List<Video> updatedVideos = searchHistory.get(keyword);
        assertEquals("Updated videos should have 10 entries", 10, updatedVideos.size());
        assertEquals("New videos should be at the top", "newVideoId1", updatedVideos.get(0).getVideoId());
        assertEquals("New videos should be at the top", "newVideoId2", updatedVideos.get(1).getVideoId());
    }

    /**
     * Tests updateVideosForKeyword with a new keyword.
     */
    @Test
    public void testUpdateVideosForKeywordNewKeyword() {
        String sessionId = "session1";
        String keyword = "newKeyword";

        // Add new videos
        List<Video> newVideos = List.of(
                new Video("NewTitle1", "NewDesc1", "NewChannel1", "NewThumb1", "newVideoId1", "newChannelId1", "NewURL1", "2024-12-09"),
                new Video("NewTitle2", "NewDesc2", "NewChannel2", "NewThumb2", "newVideoId2", "newChannelId2", "NewURL2", "2024-12-10")
        );
        searchService.updateVideosForKeyword(sessionId, keyword, newVideos);

        // Retrieve the updated search history
        Map<String, List<Video>> searchHistory = searchService.getSearchHistory(sessionId);

        // Verify the updated search history
        assertTrue("Search history should contain the new keyword", searchHistory.containsKey(keyword));
        List<Video> updatedVideos = searchHistory.get(keyword);
        assertEquals("Updated videos should match the new videos", newVideos.size(), updatedVideos.size());
        assertEquals("First video should match", "newVideoId1", updatedVideos.get(0).getVideoId());
        assertEquals("Second video should match", "newVideoId2", updatedVideos.get(1).getVideoId());
    }
}
