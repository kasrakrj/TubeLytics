package views;
import models.entities.Video;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import play.twirl.api.Html;
import play.test.Helpers;
import play.test.WithApplication;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class SearchResultsTest extends WithApplication{
    private Map<String, List<Video>> searchHistory;
    private String overallSentiment;
    private Map<String, String> individualSentiments;

    @Before
    public void setUp() {
        // Mock Video objects with Mockito
        Video video1 = Mockito.mock(Video.class);
        when(video1.getTitle()).thenReturn("Happy Video");
        when(video1.getDescription()).thenReturn("A very happy video description.");
        when(video1.getThumbnailUrl()).thenReturn("http://example.com/thumbnail1.jpg");
        when(video1.getVideoId()).thenReturn("video123");
        when(video1.getChannelId()).thenReturn("channel123");
        when(video1.getChannelTitle()).thenReturn("Happy Channel");

        Video video2 = Mockito.mock(Video.class);
        when(video2.getTitle()).thenReturn("Sad Video");
        when(video2.getDescription()).thenReturn("A very sad video description.");
        when(video2.getThumbnailUrl()).thenReturn("http://example.com/thumbnail2.jpg");
        when(video2.getVideoId()).thenReturn("video456");
        when(video2.getChannelId()).thenReturn("channel456");
        when(video2.getChannelTitle()).thenReturn("Sad Channel");

        // Setup mock data for searchHistory and sentiments
        searchHistory = Map.of("happy", List.of(video1), "sad", List.of(video2));
        overallSentiment = ":-)";
        individualSentiments = Map.of("happy", ":-)", "sad", ":-(");
    }

    @Test
    public void testTemplateRendersTitle() {
        Html content = views.html.searchResults.render(searchHistory, overallSentiment, individualSentiments);
        String htmlContent = Helpers.contentAsString(content);

        // Check the main title
        assertTrue(htmlContent.contains("<h1>YT Lytics - Search Results</h1>"));
    }

    @Test
    public void testTemplateRendersSearchResults() {
        Html content = views.html.searchResults.render(searchHistory, overallSentiment, individualSentiments);
        String htmlContent = Helpers.contentAsString(content);

        // Check for search keyword titles and individual sentiments
        assertTrue(htmlContent.contains("Search Results for 'happy'"));
        assertTrue(htmlContent.contains("Sentiment for 'happy': :-)"));
        assertTrue(htmlContent.contains("Search Results for 'sad'"));
        assertTrue(htmlContent.contains("Sentiment for 'sad': :-("));

        // Verify that mock methods were called
        verify(searchHistory.get("happy").get(0), atLeastOnce()).getTitle();
        verify(searchHistory.get("sad").get(0), atLeastOnce()).getDescription();
    }

    @Test
    public void testTemplateRendersThumbnailAndLinks() {
        Html content = views.html.searchResults.render(searchHistory, overallSentiment, individualSentiments);
        String htmlContent = Helpers.contentAsString(content);

        // Check for thumbnails and YouTube video links
        assertTrue(htmlContent.contains("http://example.com/thumbnail1.jpg"));
        assertTrue(htmlContent.contains("https://www.youtube.com/watch?v=video123"));
        assertTrue(htmlContent.contains("http://example.com/thumbnail2.jpg"));
        assertTrue(htmlContent.contains("https://www.youtube.com/watch?v=video456"));

        // Verify that mock methods were called for channel and tags links
        verify(searchHistory.get("happy").get(0), atLeastOnce()).getChannelId();
        verify(searchHistory.get("sad").get(0), atLeastOnce()).getVideoId();
    }

    @Test
    public void testTemplateRendersSearchForm() {
        Html content = views.html.searchResults.render(searchHistory, overallSentiment, individualSentiments);
        String htmlContent = Helpers.contentAsString(content);

        // Check for search form elements
        assertTrue(htmlContent.contains("<form action=\"/search\" method=\"GET\">"));
        assertTrue(htmlContent.contains("name=\"keyword\" placeholder=\"Enter search keywords\""));
        assertTrue(htmlContent.contains("<button type=\"submit\">Search</button>"));
    }
}
