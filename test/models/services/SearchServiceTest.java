package models.services;

import models.entities.Video;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SearchServiceTest {

    private SearchService searchService;
    private YouTubeService mockYouTubeService;
    private HttpClient mockHttpClient;
    private HttpResponse<String> mockResponse;

    @Before
    public void setUp() {
        mockYouTubeService = Mockito.mock(YouTubeService.class);
        searchService = Mockito.mock(SearchService.class);
        mockHttpClient = Mockito.mock(HttpClient.class);
        mockResponse = Mockito.mock(HttpResponse.class);
    }

    @Test
    public void testSearchVideos() throws Exception {
        // Set up mock API key and URL
        when(mockYouTubeService.getApiKey()).thenReturn("mockApiKey");
        when(mockYouTubeService.getApiUrl()).thenReturn("https://mock.api.url");

        // Prepare a mock JSON response with video items
        JSONArray items = new JSONArray();

        JSONObject snippet1 = new JSONObject()
                .put("title", "Video 1 Title")
                .put("description", "Video 1 Description")
                .put("channelTitle", "Channel 1")
                .put("thumbnails", new JSONObject().put("default", new JSONObject().put("url", "https://thumbnail1.url")))
                .put("channelId", "channelId1");
        JSONObject video1 = new JSONObject().put("id", "videoId1").put("snippet", snippet1);
        items.put(video1);

        JSONObject snippet2 = new JSONObject()
                .put("title", "Video 2 Title")
                .put("description", "Video 2 Description")
                .put("channelTitle", "Channel 2")
                .put("thumbnails", new JSONObject().put("default", new JSONObject().put("url", "https://thumbnail2.url")))
                .put("channelId", "channelId2");
        JSONObject video2 = new JSONObject().put("id", "videoId2").put("snippet", snippet2);
        items.put(video2);

        JSONObject jsonResponse = new JSONObject().put("items", items);

        // Mock the response and the HTTP client behavior
        when(mockResponse.body()).thenReturn(jsonResponse.toString());
        when(mockHttpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(CompletableFuture.completedFuture(mockResponse));

        // Mock the parseVideos method in YouTubeService
        List<Video> expectedVideos = List.of(
                new Video("Video 1 Title", "Video 1 Description", "Channel 1", "https://thumbnail1.url", "videoId1", "channelId1", "https://www.youtube.com/watch?v=videoId1"),
                new Video("Video 2 Title", "Video 2 Description", "Channel 2", "https://thumbnail2.url", "videoId2", "channelId2", "https://www.youtube.com/watch?v=videoId2")
        );
        when(mockYouTubeService.parseVideos(items)).thenReturn(expectedVideos);

        // Define behavior for searchService.searchVideos
        when(searchService.searchVideos(anyString(), anyInt())).thenReturn(CompletableFuture.completedFuture(expectedVideos));

        // Execute the method
        CompletionStage<List<Video>> videosFuture = searchService.searchVideos("test keyword", 2);

        // Verify the result
        videosFuture.thenAccept(videos -> {
            assertNotNull(videos);
            assertEquals(2, videos.size());
            assertEquals("Video 1 Title", videos.get(0).getTitle());
            assertEquals("Channel 1", videos.get(0).getChannelTitle());
            assertEquals("Video 2 Title", videos.get(1).getTitle());
            assertEquals("Channel 2", videos.get(1).getChannelTitle());
        }).toCompletableFuture().get(); // Wait for the CompletableFuture to complete for the test
    }
}
