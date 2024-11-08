package views;

import org.junit.Test;
import play.twirl.api.Html;
import static org.junit.Assert.assertTrue;
public class NoResultsTest {

    @Test
    public void testNoResults() {
        // Define a sample keyword
        String keyword = "testKeyword";

        // Render the template with the keyword
        Html renderedHtml = views.html.noResults.render(keyword);
        String renderedHtmlString = renderedHtml.body();

        // Print rendered HTML for debugging
        System.out.println("Rendered HTML: " + renderedHtmlString);

        // Check if the rendered HTML contains the expected title and message
        assertTrue(renderedHtmlString.contains("<title>No Results for testKeyword</title>"));
        assertTrue(renderedHtmlString.contains("<h1>No Results for \"testKeyword\"</h1>"));
        assertTrue(renderedHtmlString.contains("Sorry, we couldn't find any videos matching your search."));
    }
}
