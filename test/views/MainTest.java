/*package views;

import org.junit.Test;
import org.mockito.Mockito;
import play.twirl.api.Html;
import views.html.main;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
public class MainTest {
    @Test
    public void testTemplateRendering() {
        // Mock the Html content
        Html mockContent = mock(Html.class);
        when(mockContent.body()).thenReturn("Mocked Html Content");

        // Create the main template with title and mocked content
        String title = "Test Title";
        Html renderedHtml = main.render(title, mockContent);

        // Assert that the rendered Html contains the title and a placeholder for the content
        String renderedHtmlString = renderedHtml.body();

        assertTrue(renderedHtmlString.contains("<title>" + title + "</title>"));
        assertTrue(renderedHtmlString.contains(mockContent.body()));

        // Verify that the mock content's body method was called
        verify(mockContent, times(1)).body();
    }
}*/
