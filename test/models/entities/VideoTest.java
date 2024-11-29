package models.entities;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the {@link Video} class.
 *
 * This test class validates the functionality of:
 * - Constructors (default and parameterized).
 * - Getter and setter methods.
 * - `equals`, `hashCode`, and `toString` implementations.
 *
 * Ensures the correctness of the {@code Video} class by covering all major functionalities.
 */
public class VideoTest {

    /**
     * Tests the default constructor to ensure all fields are initialized to {@code null}.
     */
    @Test
    public void testDefaultConstructor() {
        Video video = new Video();

        assertNull("Title should be null", video.getTitle());
        assertNull("Description should be null", video.getDescription());
        assertNull("Channel title should be null", video.getChannelTitle());
        assertNull("Thumbnail URL should be null", video.getThumbnailUrl());
        assertNull("Video ID should be null", video.getVideoId());
        assertNull("Channel ID should be null", video.getChannelId());
        assertNull("Video URL should be null", video.getVideoURL());
    }

    /**
     * Tests the parameterized constructor to ensure fields are correctly set.
     */
    @Test
    public void testParameterizedConstructor() {
        String title = "Sample Title";
        String description = "Sample Description";
        String channelTitle = "Sample Channel Title";
        String thumbnailUrl = "http://example.com/thumbnail.jpg";
        String videoId = "abc123";
        String channelId = "channel456";
        String videoURL = "http://youtube.com/watch?v=abc123";
        String publishedAt = "2020-01-01T00:00:00Z";

        Video video = new Video(title, description, channelTitle, thumbnailUrl, videoId, channelId, videoURL, publishedAt);

        assertEquals("Title should be set correctly", title, video.getTitle());
        assertEquals("Description should be set correctly", description, video.getDescription());
        assertEquals("Channel title should be set correctly", channelTitle, video.getChannelTitle());
        assertEquals("Thumbnail URL should be set correctly", thumbnailUrl, video.getThumbnailUrl());
        assertEquals("Video ID should be set correctly", videoId, video.getVideoId());
        assertEquals("Channel ID should be set correctly", channelId, video.getChannelId());
        assertEquals("Video URL should be set correctly", videoURL, video.getVideoURL());
        assertEquals("Published at should be set correctly", publishedAt, video.getPublishedAt());
    }

    /**
     * Tests the getter and setter methods for all fields.
     */
    @Test
    public void testSettersAndGetters() {
        Video video = new Video();

        String title = "Updated Title";
        String description = "Updated Description";
        String channelTitle = "Updated Channel Title";
        String thumbnailUrl = "http://example.com/updated_thumbnail.jpg";
        String videoId = "updated123";
        String channelId = "updated456";
        String videoURL = "http://youtube.com/watch?v=updated123";

        video.setTitle(title);
        video.setDescription(description);
        video.setChannelTitle(channelTitle);
        video.setThumbnailUrl(thumbnailUrl);
        video.setVideoId(videoId);
        video.setChannelId(channelId);
        video.setVideoURL(videoURL);

        assertEquals("Title should be retrieved correctly", title, video.getTitle());
        assertEquals("Description should be retrieved correctly", description, video.getDescription());
        assertEquals("Channel title should be retrieved correctly", channelTitle, video.getChannelTitle());
        assertEquals("Thumbnail URL should be retrieved correctly", thumbnailUrl, video.getThumbnailUrl());
        assertEquals("Video ID should be retrieved correctly", videoId, video.getVideoId());
        assertEquals("Channel ID should be retrieved correctly", channelId, video.getChannelId());
        assertEquals("Video URL should be retrieved correctly", videoURL, video.getVideoURL());
    }

    /**
     * Tests the `toString` method to ensure it returns the expected string representation of a {@code Video}.
     */
    @Test
    public void testToString() {
        Video video = new Video(
                "Title",
                "Description",
                "Channel Title",
                "http://thumbnail.url",
                "video123",
                "channel123",
                "http://video.url",
                "2024-11-29"
        );

        String expectedToString = "Video{" +
                "title='Title', " +
                "description='Description', " +
                "channelTitle='Channel Title', " +
                "thumbnailUrl='http://thumbnail.url', " +
                "videoId='video123', " +
                "channelId='channel123', " +
                "VideoURL='http://video.url', " +
                "publishedAt='2024-11-29'}";

        assertEquals("toString method should return the expected string representation", expectedToString, video.toString());
    }

    /**
     * Tests the `equals` method when comparing the same instance.
     */
    @Test
    public void testEquals_SameInstance() {
        Video video = new Video(
                "Title",
                "Description",
                "Channel Title",
                "http://thumbnail.url",
                "video123",
                "channel123",
                "http://video.url",
                "2024-11-29"
        );

        assertTrue("equals should return true for the same instance", video.equals(video));
    }

    /**
     * Tests the `equals` method when comparing with an object of a different class.
     */
    @Test
    public void testEquals_DifferentClass() {
        Video video = new Video(
                "Title",
                "Description",
                "Channel Title",
                "http://thumbnail.url",
                "video123",
                "channel123",
                "http://video.url",
                "2024-11-29"
        );

        Object notAVideo = new Object();
        assertFalse("equals should return false when comparing with an object of a different class", video.equals(notAVideo));
    }

    /**
     * Tests the `equals` method for two {@code Video} objects with different values for `videoId` or `channelId`.
     */
    @Test
    public void testEquals_DifferentValues() {
        Video video1 = new Video(
                "Title1",
                "Description1",
                "Channel Title1",
                "http://thumbnail1.url",
                "video123",
                "channel123",
                "http://video1.url",
                "2024-11-29"
        );

        Video video2 = new Video(
                "Title2",
                "Description2",
                "Channel Title2",
                "http://thumbnail2.url",
                "video124",
                "channel124",
                "http://video2.url",
                "2024-11-30"
        );

        assertFalse("equals should return false for videos with different IDs or channel IDs", video1.equals(video2));
    }

    /**
     * Tests the `hashCode` method to ensure consistent values for equivalent {@code Video} objects.
     */
    @Test
    public void testHashCode_SameValues() {
        Video video1 = new Video(
                "Title",
                "Description",
                "Channel Title",
                "http://thumbnail.url",
                "video123",
                "channel123",
                "http://video.url",
                "2024-11-29"
        );

        Video video2 = new Video(
                "Different Title",
                "Different Description",
                "Different Channel Title",
                "http://different-thumbnail.url",
                "video123",
                "channel123",
                "http://different-video.url",
                "2024-11-30"
        );

        assertEquals("hashCode should be the same for videos with the same videoId and channelId", video1.hashCode(), video2.hashCode());
    }

    /**
     * Tests the `hashCode` method to ensure different values for {@code Video} objects with different `videoId` or `channelId`.
     */
    @Test
    public void testHashCode_DifferentValues() {
        Video video1 = new Video(
                "Title1",
                "Description1",
                "Channel Title1",
                "http://thumbnail1.url",
                "video123",
                "channel123",
                "http://video1.url",
                "2024-11-29"
        );

        Video video2 = new Video(
                "Title2",
                "Description2",
                "Channel Title2",
                "http://thumbnail2.url",
                "video124",
                "channel124",
                "http://video2.url",
                "2024-11-30"
        );

        assertNotEquals("hashCode should be different for videos with different videoId or channelId", video1.hashCode(), video2.hashCode());
    }

    /**
     * Tests the consistency of the `hashCode` method for the same {@code Video} object.
     */
    @Test
    public void testHashCode_Consistency() {
        Video video = new Video(
                "Title",
                "Description",
                "Channel Title",
                "http://thumbnail.url",
                "video123",
                "channel123",
                "http://video.url",
                "2024-11-29"
        );

        int initialHashCode = video.hashCode();
        int subsequentHashCode = video.hashCode();

        assertEquals("hashCode should be consistent for the same object", initialHashCode, subsequentHashCode);
    }
}
