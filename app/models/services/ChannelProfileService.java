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
 * <p>This service uses the YouTube Data API to fetch channel details and video lists.
 * It depends on {@link YouTubeService} for API configurations such as base URL and API key.</p>
 */
public class ChannelProfileService {
    YouTubeService youTubeService;

    /**
     * Constructs a new {@code ChannelProfileService} with the specified {@link YouTubeService}.
     *
     * @param youTubeService the YouTube service providing API configurations
     */
    @Inject
    public ChannelProfileService(YouTubeService youTubeService) {
        this.youTubeService = youTubeService;
    }

    /**
     * Creates a new {@link HttpClient} instance.
     *
     * @return a new {@link HttpClient}
     */
    protected HttpClient createHttpClient() {
        return HttpClient.newHttpClient();
    }

    /**
     * Retrieves channel information for the specified channel ID.
     *
     * <p>This method sends an asynchronous HTTP GET request to the YouTube Data API's "search" endpoint,
     * requesting the snippet part for the given channel ID. It returns a {@link CompletionStage}
     * that will contain the channel's snippet information as a {@link JSONObject} upon completion.
     * The {@code CompletionStage} may complete exceptionally if an error occurs during the HTTP request
     * or JSON parsing.</p>
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
     * requesting the snippet part for videos in the given channel ID. It returns a {@link CompletionStage}
     * that will contain a {@link List} of {@link Video} objects upon completion.
     * The {@code CompletionStage} may complete exceptionally if an error occurs during the HTTP request
     * or JSON parsing.</p>
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
