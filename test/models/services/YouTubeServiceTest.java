package models.services;

import models.entities.Video;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class YouTubeServiceTest {

    private YouTubeService youTubeService;

    @Before
    public void setUp() {
        youTubeService = new YouTubeService();
    }

    @Test
    public void testParseVideo() {
        // Prepare mock JSON data for a video item
        JSONObject snippet = new JSONObject()
                .put("title", "Sample Video Title")
                .put("description", "Sample Video Description")
                .put("channelTitle", "Sample Channel")
                .put("thumbnails", new JSONObject().put("default", new JSONObject().put("url", "https://sample.thumbnail.url")))
                .put("channelId", "sampleChannelId");

        JSONObject item = new JSONObject()
                .put("id", "sampleVideoId")
                .put("snippet", snippet);

        // Parse video using YouTubeService
        Video video = youTubeService.parseVideo(item);

        // Verify the parsed video details
        assertNotNull(video);
        assertEquals("Sample Video Title", video.getTitle());
        assertEquals("Sample Video Description", video.getDescription());
        assertEquals("Sample Channel", video.getChannelTitle());
        assertEquals("https://sample.thumbnail.url", video.getThumbnailUrl());
        assertEquals("sampleVideoId", video.getVideoId());
        assertEquals("sampleChannelId", video.getChannelId());
        assertEquals("https://www.youtube.com/watch?v=sampleVideoId", video.getVideoURL());
    }

    @Test
    public void testParseVideoWithMissingFields() {
        // Prepare mock JSON data with missing optional fields
        JSONObject snippet = new JSONObject()
                .put("title", "Sample Video Title")
                .put("thumbnails", new JSONObject().put("default", new JSONObject().put("url", "https://sample.thumbnail.url")));

        JSONObject item = new JSONObject()
                .put("id", "sampleVideoId")
                .put("snippet", snippet);

        // Parse video using YouTubeService
        Video video = youTubeService.parseVideo(item);

        // Verify the parsed video details with defaults
        assertNotNull(video);
        assertEquals("Sample Video Title", video.getTitle());
        assertEquals("", video.getDescription()); // Default empty description
        assertEquals("Unknown Channel", video.getChannelTitle()); // Default unknown channel title
    }

    @Test
    public void testParseVideos() {
        // Prepare mock JSON data for multiple videos
        JSONArray items = new JSONArray();
        items.put(new JSONObject()
                .put("id", "videoId1")
                .put("snippet", new JSONObject()
                        .put("title", "Video 1")
                        .put("channelTitle", "Channel 1")
                        .put("thumbnails", new JSONObject().put("default", new JSONObject().put("url", "https://thumbnail1.url")))
                        .put("channelId", "channelId1")));
        items.put(new JSONObject()
                .put("id", "videoId2")
                .put("snippet", new JSONObject()
                        .put("title", "Video 2")
                        .put("channelTitle", "Channel 2")
                        .put("thumbnails", new JSONObject().put("default", new JSONObject().put("url", "https://thumbnail2.url")))
                        .put("channelId", "channelId2")));

        // Parse videos using YouTubeService
        List<Video> videos = youTubeService.parseVideos(items);

        // Verify the parsed video list
        assertNotNull(videos);
        assertEquals(2, videos.size());
        assertEquals("Video 1", videos.get(0).getTitle());
        assertEquals("videoId1", videos.get(0).getVideoId());
        assertEquals("Video 2", videos.get(1).getTitle());
        assertEquals("videoId2", videos.get(1).getVideoId());
    }

    @Test
    public void testParseTags() {
        // Prepare mock JSON data with tags
        JSONObject snippet = new JSONObject()
                .put("tags", new JSONArray().put("tag1").put("tag2").put("tag3"));
        JSONObject item = new JSONObject().put("snippet", snippet);
        JSONArray items = new JSONArray().put(item);

        // Parse tags using YouTubeService
        List<String> tags = youTubeService.parseTags(items);

        // Verify the parsed tags list
        assertNotNull(tags);
        assertEquals(3, tags.size());
        assertTrue(tags.contains("tag1"));
        assertTrue(tags.contains("tag2"));
        assertTrue(tags.contains("tag3"));
    }

    @Test
    public void testParseTagsNoTags() {
        // Prepare mock JSON data without tags
        JSONObject snippet = new JSONObject();
        JSONObject item = new JSONObject().put("snippet", snippet);
        JSONArray items = new JSONArray().put(item);

        // Parse tags using YouTubeService
        List<String> tags = youTubeService.parseTags(items);

        // Verify the result is an empty list
        assertNotNull(tags);
        assertTrue(tags.isEmpty());
    }
}
