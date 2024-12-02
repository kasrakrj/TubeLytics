package actors;

import org.junit.Test;

import static org.junit.Assert.*;

public class WordStatMessagesTest {

    @Test
    public void testUpdateVideosMessageInitialization() {
        // Arrange
        String keyword = "testKeyword";

        // Act
        WordStatMessages.UpdateVideos updateVideosMessage = new WordStatMessages.UpdateVideos(keyword);

        // Assert
        assertNotNull("The UpdateVideos message instance should not be null", updateVideosMessage);
        assertEquals("The keyword should be set correctly", keyword, updateVideosMessage.keyword);
    }

    @Test
    public void testUpdateVideosMessageNullKeyword() {
        // Arrange
        String keyword = null;

        // Act
        WordStatMessages.UpdateVideos updateVideosMessage = new WordStatMessages.UpdateVideos(keyword);

        // Assert
        assertNotNull("The UpdateVideos message instance should not be null", updateVideosMessage);
        assertNull("The keyword should be null", updateVideosMessage.keyword);
    }

    @Test
    public void testGetWordStatsMessageInstance() {
        // Act
        WordStatMessages.GetWordStats getWordStatsMessage = new WordStatMessages.GetWordStats();

        // Assert
        assertNotNull("The GetWordStats message instance should not be null", getWordStatsMessage);
    }
}
