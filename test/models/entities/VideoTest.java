//package models.entities;
//
//import org.junit.Test;
//import static org.junit.Assert.*;
//
//public class VideoTest {
//
//    @Test
//    public void testDefaultConstructor() {
//        Video video = new Video();
//
//        assertNull("Title should be null", video.getTitle());
//        assertNull("Description should be null", video.getDescription());
//        assertNull("Channel title should be null", video.getChannelTitle());
//        assertNull("Thumbnail URL should be null", video.getThumbnailUrl());
//        assertNull("Video ID should be null", video.getVideoId());
//        assertNull("Channel ID should be null", video.getChannelId());
//        assertNull("Video URL should be null", video.getVideoURL());
//    }
//
//    @Test
//    public void testParameterizedConstructor() {
//        String title = "Sample Title";
//        String description = "Sample Description";
//        String channelTitle = "Sample Channel Title";
//        String thumbnailUrl = "http://example.com/thumbnail.jpg";
//        String videoId = "abc123";
//        String channelId = "channel456";
//        String videoURL = "http://youtube.com/watch?v=abc123";
//
//        Video video = new Video(title, description, channelTitle, thumbnailUrl, videoId, channelId, videoURL);
//
//        assertEquals("Title should be set correctly", title, video.getTitle());
//        assertEquals("Description should be set correctly", description, video.getDescription());
//        assertEquals("Channel title should be set correctly", channelTitle, video.getChannelTitle());
//        assertEquals("Thumbnail URL should be set correctly", thumbnailUrl, video.getThumbnailUrl());
//        assertEquals("Video ID should be set correctly", videoId, video.getVideoId());
//        assertEquals("Channel ID should be set correctly", channelId, video.getChannelId());
//        assertEquals("Video URL should be set correctly", videoURL, video.getVideoURL());
//    }
//
//    @Test
//    public void testSettersAndGetters() {
//        Video video = new Video();
//
//        String title = "Updated Title";
//        String description = "Updated Description";
//        String channelTitle = "Updated Channel Title";
//        String thumbnailUrl = "http://example.com/updated_thumbnail.jpg";
//        String videoId = "updated123";
//        String channelId = "updated456";
//        String videoURL = "http://youtube.com/watch?v=updated123";
//
//        video.setTitle(title);
//        video.setDescription(description);
//        video.setChannelTitle(channelTitle);
//        video.setThumbnailUrl(thumbnailUrl);
//        video.setVideoId(videoId);
//        video.setChannelId(channelId);
//        video.setVideoURL(videoURL);
//
//        assertEquals("Title should be retrieved correctly", title, video.getTitle());
//        assertEquals("Description should be retrieved correctly", description, video.getDescription());
//        assertEquals("Channel title should be retrieved correctly", channelTitle, video.getChannelTitle());
//        assertEquals("Thumbnail URL should be retrieved correctly", thumbnailUrl, video.getThumbnailUrl());
//        assertEquals("Video ID should be retrieved correctly", videoId, video.getVideoId());
//        assertEquals("Channel ID should be retrieved correctly", channelId, video.getChannelId());
//        assertEquals("Video URL should be retrieved correctly", videoURL, video.getVideoURL());
//    }
//
//    @Test
//    public void testTitleSetterGetter() {
//        Video video = new Video();
//        String title = "Test Title";
//
//        video.setTitle(title);
//
//        assertEquals("Title should be set and retrieved correctly", title, video.getTitle());
//    }
//
//    @Test
//    public void testDescriptionSetterGetter() {
//        Video video = new Video();
//        String description = "Test Description";
//
//        video.setDescription(description);
//
//        assertEquals("Description should be set and retrieved correctly", description, video.getDescription());
//    }
//
//    @Test
//    public void testChannelTitleSetterGetter() {
//        Video video = new Video();
//        String channelTitle = "Test Channel Title";
//
//        video.setChannelTitle(channelTitle);
//
//        assertEquals("Channel title should be set and retrieved correctly", channelTitle, video.getChannelTitle());
//    }
//
//    @Test
//    public void testThumbnailUrlSetterGetter() {
//        Video video = new Video();
//        String thumbnailUrl = "http://example.com/test_thumbnail.jpg";
//
//        video.setThumbnailUrl(thumbnailUrl);
//
//        assertEquals("Thumbnail URL should be set and retrieved correctly", thumbnailUrl, video.getThumbnailUrl());
//    }
//
//    @Test
//    public void testVideoIdSetterGetter() {
//        Video video = new Video();
//        String videoId = "testVideoId";
//
//        video.setVideoId(videoId);
//
//        assertEquals("Video ID should be set and retrieved correctly", videoId, video.getVideoId());
//    }
//
//    @Test
//    public void testChannelIdSetterGetter() {
//        Video video = new Video();
//        String channelId = "testChannelId";
//
//        video.setChannelId(channelId);
//
//        assertEquals("Channel ID should be set and retrieved correctly", channelId, video.getChannelId());
//    }
//
//    @Test
//    public void testVideoURLSetterGetter() {
//        Video video = new Video();
//        String videoURL = "http://youtube.com/watch?v=testVideoId";
//
//        video.setVideoURL(videoURL);
//
//        assertEquals("Video URL should be set and retrieved correctly", videoURL, video.getVideoURL());
//    }
//}
