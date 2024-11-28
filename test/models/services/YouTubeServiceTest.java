package models.services;

import models.entities.Video;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for the YouTubeService class. This test suite verifies functionality
 * related to parsing videos and tags from YouTube API JSON responses.
 */
public class YouTubeServiceTest {

    private YouTubeService youTubeService;

    /**
     * Initializes the YouTubeService instance before each test.
     */
    @Before
    public void setUp() {
        youTubeService = new YouTubeService(searchService);
    }

    /**
     * Tests parseVideo when the 'id' field in JSON data is a simple string.
     * Verifies that the video ID is correctly parsed and set in the Video object.
     */
    @Test
    public void testParseVideoWithSimpleId() {
        // Prepare mock JSON data with 'id' as a simple string
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
        assertEquals("sampleVideoId", video.getVideoId());
    }

    /**
     * Tests parseVideo when the 'id' field in JSON data is an object containing 'videoId'.
     * Verifies that the video ID is correctly extracted from the nested JSON object.
     */
    @Test
    public void testParseVideoWithObjectId() {
        // Prepare mock JSON data with 'id' as an object containing 'videoId'
        JSONObject snippet = new JSONObject()
                .put("title", "Sample Video Title")
                .put("description", "Sample Video Description")
                .put("channelTitle", "Sample Channel")
                .put("thumbnails", new JSONObject().put("default", new JSONObject().put("url", "https://sample.thumbnail.url")))
                .put("channelId", "sampleChannelId");

        JSONObject item = new JSONObject()
                .put("id", new JSONObject().put("videoId", "sampleVideoId"))
                .put("snippet", snippet);

        // Parse video using YouTubeService
        Video video = youTubeService.parseVideo(item);

        // Verify the parsed video details
        assertNotNull(video);
        assertEquals("sampleVideoId", video.getVideoId());
    }

    /**
     * Tests parseVideo with an empty JSON object, verifying that it returns null.
     * This ensures parseVideo handles cases where no data is available correctly.
     */
    @Test
    public void testParseVideoWithEmptyItem() {
        // Prepare an empty JSON object
        JSONObject emptyItem = new JSONObject();

        // Parse video using YouTubeService
        Video video = youTubeService.parseVideo(emptyItem);

        // Verify that the result is null for an empty item
        assertNull(video);
    }

    /**
     * Tests getApiKey to ensure it returns a non-null API key.
     */
    @Test
    public void testGetApiKey() {
        assertNotNull(youTubeService.getApiKey());
    }

    /**
     * Tests getApiUrl to ensure it returns a non-null API URL.
     */
    @Test
    public void testGetApiUrl() {
        assertNotNull(youTubeService.getApiUrl());
    }

    /**
     * Tests parseVideos with JSON data for multiple videos.
     * Verifies that the returned list of Video objects matches the expected video details.
     */
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

    /**
     * Tests parseTags with JSON data containing tags.
     * Verifies that the returned list of tags matches the expected tags in the data.
     */
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

    /**
     * Tests parseTags with JSON data that does not contain any tags.
     * Verifies that an empty list is returned when no tags are present in the data.
     */
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

    @Test
    public void testParseTagsWithItemsNoTags() {
        JSONObject snippet = new JSONObject();
        JSONObject item = new JSONObject().put("snippet", snippet);
        JSONArray items = new JSONArray().put(item);

        List<String> tags = youTubeService.parseTags(items);

        assertNotNull(tags);
        assertTrue(tags.isEmpty());
    }

    @Test
    public void testParseTagsWithEmptyArray() {
        JSONArray items = new JSONArray();

        List<String> tags = youTubeService.parseTags(items);

        assertNotNull(tags);
        assertTrue(tags.isEmpty());
    }

    @Test
    public void testParseTagsWithMissingSnippet() {
        JSONObject item = new JSONObject();
        JSONArray items = new JSONArray().put(item);

        List<String> tags = youTubeService.parseTags(items);

        assertNotNull(tags);
        assertTrue(tags.isEmpty());
    }
    @Test
    public void testParseVideoWithIdAsString() {
        // Prepare mock JSON data with 'id' as a simple string
        JSONObject snippet = new JSONObject()
                .put("title", "Video with String ID")
                .put("description", "Description for video with String ID")
                .put("channelTitle", "Channel with String ID")
                .put("thumbnails", new JSONObject().put("default", new JSONObject().put("url", "https://string.id.thumbnail.url")))
                .put("channelId", "stringChannelId");

        JSONObject item = new JSONObject()
                .put("id", "stringVideoId")
                .put("snippet", snippet);

        // Parse video using YouTubeService
        Video video = youTubeService.parseVideo(item);

        // Verify the parsed video details
        assertNotNull(video);
        assertEquals("stringVideoId", video.getVideoId());
        assertEquals("Video with String ID", video.getTitle());
        assertEquals("Description for video with String ID", video.getDescription());
        assertEquals("Channel with String ID", video.getChannelTitle());
        assertEquals("https://string.id.thumbnail.url", video.getThumbnailUrl());
    }
    @Test
    public void testParseVideoWithNonStringNonObjectId() {
        // Prepare mock JSON data with 'id' as an integer (unexpected type)
        JSONObject snippet = new JSONObject()
                .put("title", "Video with Unexpected ID Type")
                .put("description", "Description for video with unexpected ID type")
                .put("channelTitle", "Channel with Unexpected ID Type")
                .put("thumbnails", new JSONObject().put("default", new JSONObject().put("url", "https://unexpected.id.thumbnail.url")))
                .put("channelId", "unexpectedChannelId");

        JSONObject item = new JSONObject()
                .put("id", 12345)  // Integer instead of String or JSONObject
                .put("snippet", snippet);

        // Parse video using YouTubeService
        Video video = youTubeService.parseVideo(item);

        // Verify the parsed video details
        assertNotNull(video);
        assertNull(video.getVideoId());  // Since 'id' is not a String or JSONObject, videoId should be null
        assertEquals("Video with Unexpected ID Type", video.getTitle());
        assertEquals("Description for video with unexpected ID type", video.getDescription());
        assertEquals("Channel with Unexpected ID Type", video.getChannelTitle());
        assertEquals("https://unexpected.id.thumbnail.url", video.getThumbnailUrl());
    }


}

