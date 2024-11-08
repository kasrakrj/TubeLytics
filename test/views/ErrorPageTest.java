package views;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import play.twirl.api.Html;
import views.html.errorPage;

public class ErrorPageTest {
    @Test
    public void testError() {
        // Sample error message
        String errorMessage = "An unexpected error occurred.";

        // Render the template
        Html html = errorPage.render(errorMessage);

        //  Check if the rendered HTML is not null
        assertNotNull(html);

        // Check if the HTML contains the expected header and message
        assertTrue(html.body().contains("<h1>Error</h1>"));
        assertTrue(html.body().contains("<p>An unexpected error occurred.</p>"));
    }
}
