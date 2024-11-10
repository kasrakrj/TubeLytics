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
public class TagsViewTest extends WithApplication{
    private Video video;
    private List<String> tags;

    @Before
    public void mockSetup() {
        // Mock Video object
        video = Mockito.mock(Video.class);
        when(video.getTitle()).thenReturn("Best Music Video ever");
        when(video.getDescription()).thenReturn("The best in 2024.");
        when(video.getThumbnailUrl()).thenReturn("http://example.com/sample-thumbnail.jpg");
        when(video.getVideoURL()).thenReturn("https://www.youtube.com/watch?v=videosample");
        when(video.getChannelId()).thenReturn("ChannelId1");
        when(video.getChannelTitle()).thenReturn("MusicVideoChannel");
        // Mock tag list
        tags = List.of("music", "art", "sample");
    }

    @Test
    public void testingVideoDetails() {
        Html content = views.html.tagsPage.render(video, tags);
        String htmlContent = Helpers.contentAsString(content);

        // Check video title
        assertTrue(htmlContent.contains("Tags for"));
        assertTrue(htmlContent.contains("Best Music Video ever"));
        assertTrue(htmlContent.contains("https://www.youtube.com/watch?v=videosample"));

        // Check channel link and description
        assertTrue(htmlContent.contains("MusicVideoChannel"));
        assertTrue(htmlContent.contains("The best in 2024."));

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
        assertTrue(htmlContent.contains("music"));
        assertTrue(htmlContent.contains("art"));
        assertTrue(htmlContent.contains("sample"));

        // Verify that the tags are rendered as links
        for (String tag : tags) {
            assertTrue(htmlContent.contains("href=\"/search?keyword=" + tag + "\""));
        }
    }
}
