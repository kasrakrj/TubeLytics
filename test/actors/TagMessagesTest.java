package actors;

import models.entities.Video;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.*;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the TagMessagesTest class. This test suite verifies the correctness of message classes
 * used for communication with TagActor.
 */
public class TagMessagesTest {

    private Video mockVideo;
    private List<String> mockTags;

    /**
     * Sets up the necessary mocks before each test.
     * Initializes a mock Video object and a list of mock tags.
     */
    @Before
    public void setUp() {
        // Mock Video object
        mockVideo = mock(Video.class);
        when(mockVideo.getVideoId()).thenReturn("video123");
        when(mockVideo.getTitle()).thenReturn("Sample Title");

        // Mock tags list
        mockTags = Arrays.asList("tag1", "tag2", "tag3");
    }

    /**
     * Tests the GetVideo message to ensure it stores and retrieves the videoId correctly.
     */
    @Test
    public void testGetVideoMessage() throws Exception {
        String videoId = "video123";

        // Create GetVideo message
        TagMessages.GetVideo getVideoMessage = new TagMessages.GetVideo(videoId);

        // Use reflection to access the private field videoId
        String retrievedVideoId = getPrivateField(getVideoMessage, "videoId", String.class);

        // Assert that the videoId is correctly stored
        assertEquals("Video ID should match the one provided.", videoId, retrievedVideoId);

        // Test serialization
        TagMessages.GetVideo deserializedMessage = serializeAndDeserialize(getVideoMessage);
        String deserializedVideoId = getPrivateField(deserializedMessage, "videoId", String.class);
        assertEquals("Deserialized video ID should match.", videoId, deserializedVideoId);
    }

    /**
     * Tests the GetTags message to ensure it stores and retrieves the videoId correctly.
     */
    @Test
    public void testGetTagsMessage() throws Exception {
        String videoId = "video456";

        // Create GetTags message
        TagMessages.GetTags getTagsMessage = new TagMessages.GetTags(videoId);

        // Use reflection to access the private field videoId
        String retrievedVideoId = getPrivateField(getTagsMessage, "videoId", String.class);

        // Assert that the videoId is correctly stored
        assertEquals("Video ID should match the one provided.", videoId, retrievedVideoId);

        // Test serialization
        TagMessages.GetTags deserializedMessage = serializeAndDeserialize(getTagsMessage);
        String deserializedVideoId = getPrivateField(deserializedMessage, "videoId", String.class);
        assertEquals("Deserialized video ID should match.", videoId, deserializedVideoId);
    }

    /**
     * Tests the GetVideoResponse message to ensure it stores and retrieves the Video object correctly.
     */
    @Test
    public void testGetVideoResponseMessage() throws Exception {
        // Create GetVideoResponse message
        TagMessages.GetVideoResponse response = new TagMessages.GetVideoResponse(mockVideo);

        // Assert that the Video object is correctly stored
        assertNotNull("Video should not be null.", response.getVideo());
        assertEquals("Video ID should match.", "video123", response.getVideo().getVideoId());

        // Test serialization
        TagMessages.GetVideoResponse deserializedResponse = serializeAndDeserialize(response);
        assertNotNull("Deserialized Video should not be null.", deserializedResponse.getVideo());
        assertEquals("Deserialized Video ID should match.", "video123", deserializedResponse.getVideo().getVideoId());
    }

    /**
     * Tests the GetTagsResponse message to ensure it stores and retrieves the tags list correctly.
     */
    @Test
    public void testGetTagsResponseMessage() throws Exception {
        // Create GetTagsResponse message
        TagMessages.GetTagsResponse response = new TagMessages.GetTagsResponse(mockTags);

        // Assert that the tags list is correctly stored
        assertNotNull("Tags list should not be null.", response.getTags());
        assertEquals("Tags list should match.", mockTags, response.getTags());

        // Test serialization
        TagMessages.GetTagsResponse deserializedResponse = serializeAndDeserialize(response);
        assertNotNull("Deserialized tags list should not be null.", deserializedResponse.getTags());
        assertEquals("Deserialized tags list should match.", mockTags, deserializedResponse.getTags());
    }

    /**
     * Tests the TagsError message to ensure it stores and retrieves the error message correctly.
     */
    @Test
    public void testTagsErrorMessage() throws Exception {
        String errorMessage = "An error occurred.";

        // Create TagsError message
        TagMessages.TagsError error = new TagMessages.TagsError(errorMessage);

        // Assert that the error message is correctly stored
        assertEquals("Error message should match.", errorMessage, error.getErrorMessage());

        // Test serialization
        TagMessages.TagsError deserializedError = serializeAndDeserialize(error);
        assertEquals("Deserialized error message should match.", errorMessage, deserializedError.getErrorMessage());
    }

    /**
     * Utility method to access private fields via reflection.
     *
     * @param obj       The object containing the private field.
     * @param fieldName The name of the private field.
     * @param clazz     The expected type of the field.
     * @param <T>       The type parameter.
     * @return The value of the private field.
     * @throws Exception If reflection fails.
     */
    private <T> T getPrivateField(Object obj, String fieldName, Class<T> clazz) throws Exception {
        java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return clazz.cast(field.get(obj));
    }

    /**
     * Utility method to serialize and deserialize an object.
     *
     * @param obj The object to serialize and deserialize.
     * @param <T> The type parameter.
     * @return The deserialized object.
     * @throws Exception If serialization fails.
     */
    private <T extends Serializable> T serializeAndDeserialize(T obj) throws Exception {
        // Serialize to a byte array
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteOut);
        out.writeObject(obj);
        out.flush();
        byte[] bytes = byteOut.toByteArray();

        // Deserialize from the byte array
        ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
        ObjectInputStream in = new ObjectInputStream(byteIn);
        return (T) in.readObject();
    }
}
