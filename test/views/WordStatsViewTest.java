package views;
import org.junit.Before;
import org.junit.Test;
import play.twirl.api.Html;
import play.test.Helpers;
import play.test.WithApplication;

import java.util.Map;

import static org.junit.Assert.assertTrue;
public class WordStatsViewTest extends WithApplication{
    private String keyword;
    private Map<String, Long> wordCounts;

    @Before
    public void mockSetup() {
        keyword = "Sample Keyword";
        wordCounts = Map.of("sample", 10L, "keyword", 5L, "test", 2L);
    }

    @Test
    public void testTitleAndKeyword() {
        Html content = views.html.wordStats.render(keyword, wordCounts);
        String htmlContent = Helpers.contentAsString(content);

        // Check main title and keyword display
        assertTrue(htmlContent.contains("Word stats for \"Sample Keyword\""));
    }

    @Test
    public void testWordCounts() {
        Html content = views.html.wordStats.render(keyword, wordCounts);
        String htmlContent = Helpers.contentAsString(content);
        // Check if each word and its count appear
        assertTrue(htmlContent.contains("<strong>sample</strong>"));
        assertTrue(htmlContent.contains("<span>10</span>"));
        assertTrue(htmlContent.contains("<strong>keyword</strong>"));
        assertTrue(htmlContent.contains("<span>5</span>"));
        assertTrue(htmlContent.contains("<strong>test</strong>"));
        assertTrue(htmlContent.contains("<span>2</span>"));
    }
}

