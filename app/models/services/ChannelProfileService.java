package models.services;

import models.entities.Video;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Inject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Service class for retrieving YouTube channel information and videos.
 *
 * <p>This service utilizes the YouTube Data API to fetch detailed information about channels and their videos.
 * The service depends on {@link YouTubeService} for API configurations, including the base URL and API key.
 * It provides methods to fetch channel details and retrieve a list of videos from a specified channel.</p>
 *
 * <p>Asynchronous HTTP requests are made using {@link HttpClient} to interact with the YouTube API.
 * Results are processed and returned as {@link CompletionStage}, allowing non-blocking calls and
 * exceptional handling for errors during HTTP requests or JSON parsing.</p>
 *
 * <p>Main features include:</p>
 * <ul>
 *     <li>Fetching detailed channel information using the channel ID.</li>
 *     <li>Retrieving a list of videos uploaded to a specific channel.</li>
 *     <li>Processing the API response and parsing JSON to produce usable objects.</li>
 * </ul>
 *
 * @author Zahra Rasoulifar
 */
public class ChannelProfileService {
    private final YouTubeService youTubeService;

    /**
     * Constructs a new {@code ChannelProfileService} with the specified {@link YouTubeService}.
     *
     * @param youTubeService the YouTube service providing API configurations
     * @author Zahra Rasoulifar
     */
    @Inject
    public ChannelProfileService(YouTubeService youTubeService) {
        this.youTubeService = youTubeService;
    }

    /**
     * Creates a new {@link HttpClient} instance.
     *
     * <p>This method provides a configurable HTTP client to interact with the YouTube Data API.
     * The client is created using Java's built-in {@link HttpClient}, supporting asynchronous HTTP requests.</p>
     *
     * @return a new {@link HttpClient} instance
     */
    protected HttpClient createHttpClient() {
        return HttpClient.newHttpClient();
    }

    /**
     * Retrieves channel information for the specified channel ID.
     *
     * <p>This method sends an asynchronous HTTP GET request to the YouTube Data API's "channels" endpoint,
     * requesting the snippet part for the given channel ID. The snippet contains details such as the channel's title,
     * description, and thumbnail information.</p>
     *
     * <p>The returned {@link CompletionStage} completes with a {@link JSONObject} containing the channel information
     * if successful, or completes exceptionally if an error occurs during the HTTP request or JSON parsing.</p>
     *
     * @param channelId the YouTube channel ID
     * @return a {@link CompletionStage} containing a {@link JSONObject} with the channel's snippet information
     */
    public CompletionStage<JSONObject> getChannelInfo(String channelId) {
        String youtubeChannelUrl = youTubeService.getApiUrl() + "/search?part=snippet&type=video&";
        String apiUrl = youtubeChannelUrl + "channelId=" + channelId + "&key=" + youTubeService.getApiKey();
        HttpClient client = createHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl)).build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(response -> {
            String responseBody = response.body();
            JSONObject json = new JSONObject(responseBody);
            return json.getJSONArray("items").getJSONObject(0).getJSONObject("snippet");
        });
    }

    /**
     * Retrieves a list of videos from the specified YouTube channel.
     *
     * <p>This method sends an asynchronous HTTP GET request to the YouTube Data API's "search" endpoint,
     * requesting the snippet part for videos uploaded to the specified channel ID. The method returns a
     * {@link CompletionStage} containing a list of {@link Video} objects upon successful completion.</p>
     *
     * <p>The {@code maxResults} parameter limits the number of videos retrieved in the response. If an error
     * occurs during the HTTP request or JSON parsing, the {@link CompletionStage} completes exceptionally.</p>
     *
     * @param channelId  the YouTube channel ID
     * @param maxResults the maximum number of videos to retrieve
     * @return a {@link CompletionStage} containing a {@link List} of {@link Video} objects
     */
    public CompletionStage<List<Video>> getChannelVideos(String channelId, int maxResults) {
        String youtubeChannelVideosUrl = youTubeService.getApiUrl() + "/search?part=snippet&type=video&";
        String apiUrl = youtubeChannelVideosUrl + "channelId=" + channelId + "&maxResults=" + maxResults + "&key=" + youTubeService.getApiKey();
        HttpClient client = createHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl)).build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(response -> {
            String responseBody = response.body();
            JSONObject json = new JSONObject(responseBody);
            JSONArray items = json.getJSONArray("items");
            return youTubeService.parseVideos(items);
        });
    }
}
