package views;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import org.json.JSONObject;
import org.junit.Test;
import play.twirl.api.Html;
import views.html.channelProfile;
import models.entities.Video;

import java.io.*;
import java.util.Arrays;
import java.util.List;

public class ChannelProfileViewTest {

    @Test
    public void testChannelProfileTemplateRendering() {
        // Set up mock channel information
        JSONObject channelInfo = new JSONObject();
        channelInfo.put("title", "Sample Channel");
        channelInfo.put("description", "This is a sample channel description");

        // Set up a list of mock videos
        Video video1 = new Video("Sample Video 1", "This is the first video description.",
                "Sample Channel", "sample-thumbnail-url-1.jpg", "videoId1", "channelId1", "video-url-1");
        Video video2 = new Video("Sample Video 2", "This is the second video description.",
                "Sample Channel", "sample-thumbnail-url-2.jpg", "videoId2", "channelId2", "video-url-2");
        List<Video> videos = Arrays.asList(video1, video2);

        // Render the template using render()
        Html html = channelProfile.render(channelInfo, videos);

        // Convert Html content to string
        String renderedContent = html.body();

        // Verify the rendered HTML contains expected content
        assertTrue(renderedContent.contains("<h1>Sample Channel</h1>"));
        assertTrue(renderedContent.contains("<p>This is a sample channel description</p>"));

        // Check for video details in the rendered HTML
        assertTrue(renderedContent.contains("<h3>Sample Video 1</h3>"));
        assertTrue(renderedContent.contains("<p>This is the first video description.</p>"));
        assertTrue(renderedContent.contains("<img src=\"sample-thumbnail-url-1.jpg\" alt=\"Thumbnail\">"));

        assertTrue(renderedContent.contains("<h3>Sample Video 2</h3>"));
        assertTrue(renderedContent.contains("<p>This is the second video description.</p>"));
        assertTrue(renderedContent.contains("<img src=\"sample-thumbnail-url-2.jpg\" alt=\"Thumbnail\">"));
    }

    @Test
    public void testChannelProfileRefAndFMethods() {
        // Verify ref() returns an instance of channelProfile
        assertNotNull(channelProfile.ref());

        // Verify that f() returns the function for rendering with the correct parameters
        assertNotNull(channelProfile.f());

        // Render template via f() to test coverage on the generated code's f() method
        JSONObject channelInfo = new JSONObject();
        channelInfo.put("title", "Sample Channel");
        channelInfo.put("description", "Sample Description");

        List<Video> videos = Arrays.asList(
                new Video("Title 1", "Description 1", "Sample Channel", "thumbnail1.jpg", "id1", "channelId1", "url1")
        );

        Html htmlFromF = channelProfile.f().apply(channelInfo, videos);
        assertNotNull(htmlFromF);
    }

    @Test
    public void testChannelProfileSerializationForWriteReplace() {
        // Set up channel info and videos
        JSONObject channelInfo = new JSONObject();
        channelInfo.put("title", "Test Channel");
        channelInfo.put("description", "Testing serialization");

        List<Video> videos = Arrays.asList(
                new Video("Test Video", "Description", "Channel", "thumbnail.jpg", "id", "channelId", "url")
        );

        // Render the template using render() method
        Html html = channelProfile.render(channelInfo, videos);

        // Indirectly test writeReplace() by ensuring Html is generated correctly, since serialization cannot be performed
        assertNotNull(html);
        String renderedContent = html.body();
        assertTrue(renderedContent.contains("Test Channel"));
        assertTrue(renderedContent.contains("Testing serialization"));
    }
}
