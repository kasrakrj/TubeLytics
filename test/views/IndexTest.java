package views;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import play.twirl.api.Html;
import views.html.index; // Adjust if the template name is different
public class IndexTest {
    @Test
    public void testYTLytics() {
        // Render the template
        Html html = index.render();

        // Check if the rendered HTML is not null
        assertNotNull(html);

        // Check that the HTML contains the main elements
        assertTrue(html.body().contains("class=\"circle-container\""));
        assertTrue(html.body().contains("class=\"welcome-message\""));
        assertTrue(html.body().contains("Welcome to YT Lytics!"));
        assertTrue(html.body().contains("class=\"search-bar\""));

        // Check for YouTube logo
        assertTrue(html.body().contains("class=\"youtube-logo\""));
        assertTrue(html.body().contains("src=\"https://i.pinimg.com/564x/3a/36/20/3a36206f35352b4230d5fc9f17fcea92.jpg\""));

        // Check for search icon
        assertTrue(html.body().contains("class=\"search-icon\""));
        assertTrue(html.body().contains("src=\"https://i.pinimg.com/564x/3f/55/8a/3f558a7b0b24f49d5f45961e5419ecd5.jpg\""));

        // Check for background image and style properties
        assertTrue(html.body().contains("background-image: url('https://images8.alphacoders.com/119/thumb-1920-1192293.png');"));
        assertTrue(html.body().contains("background-size: cover;"));
        assertTrue(html.body().contains("background-position: center;"));
    }
}
