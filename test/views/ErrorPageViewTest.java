package views;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import play.twirl.api.Html;
import views.html.errorPage;

public class ErrorPageViewTest {
    @Test
    public void errorTest() {
        // Error message sample
        String errorMessage = "An unexpected error occurred.";
        // Render
        Html html = errorPage.render(errorMessage);
        //  Check for the HTML not be null
        assertNotNull(html);
        // Check if the HTML contains the expected header and message
        assertTrue(html.body().contains("<h1>Error</h1>"));
        assertTrue(html.body().contains("<p>An unexpected error occurred.</p>"));
    }
}
