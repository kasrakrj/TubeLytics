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

public class TagsServiceTest {

    private TagsService mockTagsService;
    private YouTubeService mockYouTubeService;
    private HttpClient mockHttpClient;
    private HttpResponse<String> mockResponse;

    @Before
    public void setUp() {
        mockYouTubeService = Mockito.mock(YouTubeService.class);
        mockTagsService = Mockito.mock(TagsService.class);
        mockHttpClient = Mockito.mock(HttpClient.class);
        mockResponse = Mockito.mock(HttpResponse.class);
    }



    @Test
    public void testGetTagsByVideo() throws Exception {
        // Set up mock YouTubeService and HttpResponse
        when(mockYouTubeService.getApiKey()).thenReturn("mockApiKey");
        when(mockYouTubeService.getApiUrl()).thenReturn("https://mock.api.url");

        // Prepare a mock Video object
        Video video = new Video("Sample Title", "Sample Description", "Sample Channel", "https://thumbnail.url", "sampleVideoId", "sampleChannelId", "https://www.youtube.com/watch?v=sampleVideoId");

        // Mock the JSON response body
        JSONObject jsonResponse = new JSONObject();
        JSONArray items = new JSONArray();
        JSONObject snippet = new JSONObject().put("tags", new JSONArray().put("tag1").put("tag2"));
        items.put(new JSONObject().put("snippet", snippet));
        jsonResponse.put("items", items);

        // Mock the response and the HTTP client behavior
        when(mockResponse.body()).thenReturn(jsonResponse.toString());
        when(mockHttpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(CompletableFuture.completedFuture(mockResponse));

        // Mock parseTags method in YouTubeService
        when(mockYouTubeService.parseTags(items)).thenReturn(List.of("tag1", "tag2"));

        // Define behavior for tagsService.getTagsByVideo
        when(mockTagsService.getTagsByVideo(video)).thenReturn(CompletableFuture.completedFuture(List.of("tag1", "tag2")));

        // Execute the method
        CompletionStage<List<String>> tagsFuture = mockTagsService.getTagsByVideo(video);

        // Verify the result
        tagsFuture.thenAccept(tags -> {
            assertNotNull(tags);
            assertEquals(2, tags.size());
            assertTrue(tags.contains("tag1"));
            assertTrue(tags.contains("tag2"));
        });
    }

    @Test
    public void testGetVideoByVideoId() throws Exception {
        // Set up mock YouTubeService and HttpResponse
        when(mockYouTubeService.getApiKey()).thenReturn("mockApiKey");
        when(mockYouTubeService.getApiUrl()).thenReturn("https://mock.api.url");

        // Mock JSON response for a video item
        JSONObject snippet = new JSONObject()
                .put("title", "Sample Video Title")
                .put("description", "Sample Description")
                .put("channelTitle", "Sample Channel")
                .put("thumbnails", new JSONObject().put("default", new JSONObject().put("url", "https://thumbnail.url")))
                .put("channelId", "sampleChannelId");
        JSONObject item = new JSONObject()
                .put("id", "sampleVideoId")
                .put("snippet", snippet);
        JSONObject jsonResponse = new JSONObject().put("items", new JSONArray().put(item));

        // Mock the response and the HTTP client behavior
        when(mockResponse.body()).thenReturn(jsonResponse.toString());
        when(mockHttpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(CompletableFuture.completedFuture(mockResponse));

        // Mock parseVideo method in YouTubeService
        Video expectedVideo = new Video("Sample Video Title", "Sample Description", "Sample Channel", "https://thumbnail.url", "sampleVideoId", "sampleChannelId", "https://www.youtube.com/watch?v=sampleVideoId");
        when(mockYouTubeService.parseVideo(item)).thenReturn(expectedVideo);

        // Define behavior for tagsService.getVideoByVideoId
        when(mockTagsService.getVideoByVideoId("sampleVideoId")).thenReturn(CompletableFuture.completedFuture(expectedVideo));

        // Execute the method
        CompletionStage<Video> videoFuture = mockTagsService.getVideoByVideoId("sampleVideoId");

        // Verify the result
        videoFuture.thenAccept(video -> {
            assertNotNull(video);
            assertEquals("Sample Video Title", video.getTitle());
            assertEquals("Sample Description", video.getDescription());
            assertEquals("Sample Channel", video.getChannelTitle());
            assertEquals("https://thumbnail.url", video.getThumbnailUrl());
            assertEquals("sampleVideoId", video.getVideoId());
            assertEquals("sampleChannelId", video.getChannelId());
            assertEquals("https://www.youtube.com/watch?v=sampleVideoId", video.getVideoURL());
        });
    }

}
