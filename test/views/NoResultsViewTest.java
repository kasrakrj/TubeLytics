package views;

import org.junit.Test;
import play.twirl.api.Html;
import static org.junit.Assert.assertTrue;
public class NoResultsViewTest {

    @Test
    public void noResultsTest() {
        // Sample keyword
        String keyword = "testKeyword";

        // Render
        Html renderedHtml = views.html.noResults.render(keyword);
        String renderedHtmlString = renderedHtml.body();

        // Check for the HTML to contain the expected title and message
        assertTrue(renderedHtmlString.contains("<title>No Results for testKeyword</title>"));
        assertTrue(renderedHtmlString.contains("<h1>No Results for \"testKeyword\"</h1>"));
        assertTrue(renderedHtmlString.contains("Sorry, we couldn't find any videos matching your search."));
    }
}
