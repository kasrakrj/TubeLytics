package views;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import org.json.JSONObject;
import org.junit.Test;
import play.twirl.api.Html;
import views.html.channelProfile;
import models.entities.Video;

import java.util.Arrays;
import java.util.List;

public class ChannelProfileViewTest {

    @Test
    public void channelProfileTest() {
        // Mock channel info
        JSONObject channelInfo = new JSONObject();
        channelInfo.put("title", "Channel");
        channelInfo.put("description", "Music videos from all around the world.");

        // List of mock videos
        Video video1 = new Video("Video 1", "Happy mappy music video.",
                "Channel", "sample-thumbnail-url-1.jpg", "videoId1", "channelId1", "video-url-1", "2024-11-27T11:01:06Z'");
        Video video2 = new Video("Video 2", "Hello music video by unknown artist.",
                "Channel", "sample-thumbnail-url-2.jpg", "videoId2", "channelId2", "video-url-2", "2024-11-27T11:01:06Z'");
        Video video3 = new Video("Video 3", "Top rated music video of 2024.",
                "Channel", "sample-thumbnail-url-3.jpg", "videoId3", "channelId3", "video-url-3", "2024-11-27T11:01:06Z");

        List<Video> videos = Arrays.asList(video1, video2, video3);

        // Render the template
        Html html = channelProfile.render(channelInfo, videos);

        // Convert content to string
        String renderedContent = html.body();

        // Verify the rendered HTML
        assertTrue(renderedContent.contains("<h1>Channel</h1>"));
        assertTrue(renderedContent.contains("<p>Music videos from all around the world.</p>"));

        // Check for video details
        assertTrue(renderedContent.contains("<h3>Video 1</h3>"));
        assertTrue(renderedContent.contains("<p>Happy mappy music video.</p>"));
        assertTrue(renderedContent.contains("<img src=\"sample-thumbnail-url-1.jpg\" alt=\"Thumbnail\">"));

        assertTrue(renderedContent.contains("<h3>Video 2</h3>"));
        assertTrue(renderedContent.contains("<p>Hello music video by unknown artist.</p>"));
        assertTrue(renderedContent.contains("<img src=\"sample-thumbnail-url-2.jpg\" alt=\"Thumbnail\">"));
    }

    /*@Test
    public void Ref_Methods_ChannelProf_Test() {
        // Verify ref() returns an instance of channelProfile
        assertNotNull(channelProfile.ref());

        // Verify that f() returns the function for rendering with the correct parameters
        assertNotNull(channelProfile.f());

        // Render template via f() to test coverage on the generated code's f() method
        JSONObject channelInfo = new JSONObject();
        channelInfo.put("title", "Channel");
        channelInfo.put("description", "Music videos from all around the world.");

        List<Video> videos = Arrays.asList(
                new Video("Title 1", "Description 1", "Sample Channel", "thumbnail1.jpg", "id1", "channelId1", "url1")
        );

        Html htmlFromF = channelProfile.f().apply(channelInfo, videos);
        assertNotNull(htmlFromF);
    }*/

  /*  @Test
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
    }*/
}
