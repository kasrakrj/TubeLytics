package views;

import models.entities.Video;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import play.twirl.api.Html;
import play.test.Helpers;
import play.test.WithApplication;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class SearchResultViewTest extends WithApplication {
    private Video video;
    private Map<String, List<Video>> searchHistory;
    private String overallSentiment;
    private Map<String, String> individualSentiments;

    @Before
    public void mockSetup() {
        // Mock Video object
        video = Mockito.mock(Video.class);
        when(video.getTitle()).thenReturn("Best Music Video.");
        when(video.getDescription()).thenReturn("The best music video of 2024.");
        when(video.getThumbnailUrl()).thenReturn("http://example.com/sample-thumbnail.jpg");
        when(video.getVideoId()).thenReturn("VideoId1");
        when(video.getChannelId()).thenReturn("ChannelId1");
        when(video.getChannelTitle()).thenReturn("Sample Channel");

        // Set up search history and sentiments
        searchHistory = new HashMap<>();
        searchHistory.put("testKeyword", List.of(video));
        overallSentiment = "happy";
        individualSentiments = new HashMap<>();
        individualSentiments.put("testKeyword", "positive");
    }

    @Test
    public void testingRenderingSearchResults() {
        Html content = views.html.searchResults.render(searchHistory, overallSentiment, individualSentiments);
        String htmlContent = Helpers.contentAsString(content);

        // Overall title and form
        assertTrue(htmlContent.contains("YT Lytics - SearchResult Results"));
        assertTrue(htmlContent.contains("Enter search keywords"));

        // Keyword and sentiment
        assertTrue(htmlContent.contains("SearchResult Results for 'testKeyword'"));
        assertTrue(htmlContent.contains("Sentiment for 'testKeyword': positive"));

        // Verify details
        assertTrue(htmlContent.contains("Best Music Video."));
        assertTrue(htmlContent.contains("The best music video of 2024."));
        assertTrue(htmlContent.contains("Sample Channel"));
        assertTrue(htmlContent.contains("href=\"https://www.youtube.com/watch?v=VideoId1\""));

        // Verify interactions
        verify(video, atLeastOnce()).getTitle();
        verify(video, atLeastOnce()).getThumbnailUrl();
        verify(video, atLeastOnce()).getVideoId();
        verify(video, atLeastOnce()).getChannelId();
        verify(video, atLeastOnce()).getChannelTitle();
    }

}
