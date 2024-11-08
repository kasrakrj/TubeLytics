package models.services;
import models.entities.Video;
import models.services.ChannelProfileService;
import models.services.YouTubeService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class ChannelProfileServiceTest {

    @Mock
    private YouTubeService youTubeService;

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<String> httpResponse;

    private ChannelProfileService channelProfileService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        channelProfileService = new ChannelProfileService(youTubeService) {
            @Override
            protected HttpClient createHttpClient() {
                return httpClient; // Return the mocked HttpClient
            }
        };
    }

    @Test
    public void testGetChannelInfo() throws Exception {
        // Arrange
        String channelId = "testChannelId";
        when(youTubeService.getApiUrl()).thenReturn("http://api.youtube.com");
        when(youTubeService.getApiKey()).thenReturn("testApiKey");

        String responseBody = "{\"items\":[{\"snippet\":{\"title\":\"Test Video\",\"description\":\"Test Description\"}}]}";

        CompletableFuture<HttpResponse<String>> futureResponse = CompletableFuture.completedFuture(httpResponse);
        when(httpClient.sendAsync(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString())))
                .thenReturn(futureResponse);
        when(httpResponse.body()).thenReturn(responseBody);

        // Act
        CompletionStage<JSONObject> result = channelProfileService.getChannelInfo(channelId);

        // Assert
        JSONObject snippet = result.toCompletableFuture().get();
        assertEquals("Test Video", snippet.getString("title"));
        assertEquals("Test Description", snippet.getString("description"));

        // Verify the correct URL was called
        verify(httpClient).sendAsync(argThat(request -> request.uri().toString().equals(
                "http://api.youtube.com/search?part=snippet&type=video&channelId=" + channelId + "&key=testApiKey"
        )), eq(HttpResponse.BodyHandlers.ofString()));
    }

}
