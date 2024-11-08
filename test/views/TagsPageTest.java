package views;
import models.entities.Video;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import play.twirl.api.Html;
import play.test.Helpers;
import play.test.WithApplication;

import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
public class TagsPageTest extends WithApplication{
    private Video video;
    private List<String> tags;

    @Before
    public void setUp() {
        // Mock Video object
        video = Mockito.mock(Video.class);
        when(video.getTitle()).thenReturn("Sample Video Title");
        when(video.getDescription()).thenReturn("A sample video description.");
        when(video.getThumbnailUrl()).thenReturn("http://example.com/sample-thumbnail.jpg");
        when(video.getVideoURL()).thenReturn("https://www.youtube.com/watch?v=sample123");
        when(video.getChannelId()).thenReturn("channel123");
        when(video.getChannelTitle()).thenReturn("Sample Channel");

        // Mock tag list
        tags = List.of("education", "tutorial", "sample");
    }

    @Test
    public void testTemplateRendersVideoDetails() {
        Html content = views.html.tagsPage.render(video, tags);
        String htmlContent = Helpers.contentAsString(content);

        // Check for video title and link
        assertTrue(htmlContent.contains("Tags for"));
        assertTrue(htmlContent.contains("Sample Video Title"));
        assertTrue(htmlContent.contains("https://www.youtube.com/watch?v=sample123"));

        // Check for channel link and description
        assertTrue(htmlContent.contains("Sample Channel"));
        assertTrue(htmlContent.contains("A sample video description."));

        // Verify that mock methods were called
        verify(video, atLeastOnce()).getTitle();
        verify(video, atLeastOnce()).getChannelTitle();
        verify(video, atLeastOnce()).getVideoURL();
    }

    @Test
    public void testTemplateRendersTags() {
        Html content = views.html.tagsPage.render(video, tags);
        String htmlContent = Helpers.contentAsString(content);

        // Check for individual tags and their links
        assertTrue(htmlContent.contains("education"));
        assertTrue(htmlContent.contains("tutorial"));
        assertTrue(htmlContent.contains("sample"));

        // Verify that the tags are rendered as links
        for (String tag : tags) {
            assertTrue(htmlContent.contains("href=\"/search?keyword=" + tag + "\""));
        }
    }
}
