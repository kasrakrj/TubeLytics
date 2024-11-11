package models.services;

import models.entities.Video;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * The TagsService class provides methods for retrieving tags and video details from YouTube using the YouTube Data API.
 * It utilizes YouTubeService to interact with the API and parse the responses.
 */
public class TagsService {
    private final YouTubeService youTubeService = new YouTubeService();

    /**
     * Retrieves the tags associated with a specific video by making an asynchronous request to the YouTube Data API.
     *
     * @param video The Video object containing the video ID for which to retrieve tags.
     * @return CompletionStage of a list of tags associated with the video.
     */
    public CompletionStage<List<String>> getTagsByVideo(Video video) {
        String apiUrl = "https://www.googleapis.com/youtube/v3/videos?part=snippet&id="
                + video.getVideoId() + "&key=" + youTubeService.getApiKey();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl)).build();

        // Asynchronously send the request and process the response to get tags
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    String responseBody = response.body();
                    JSONObject json = new JSONObject(responseBody);
                    JSONArray items = json.getJSONArray("items");
                    // Parse the JSON response to extract tags
                    return youTubeService.parseTags(items);
                });
    }

    /**
     * Retrieves video details based on the video ID by making an asynchronous request to the YouTube Data API.
     *
     * @param videoId The ID of the video for which to retrieve details.
     * @return CompletionStage of a Video object containing details of the specified video.
     */
    public CompletionStage<Video> getVideoByVideoId(String videoId) {
        String apiUrl = youTubeService.getApiUrl() + "/videos?part=snippet&id="
                + videoId + "&key=" + youTubeService.getApiKey();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl)).build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(response -> {
            String responseBody = response.body();
            JSONObject json = new JSONObject(responseBody);
            JSONObject item = json.getJSONArray("items").getJSONObject(0);
            return youTubeService.parseVideo(item);
        });
    }
}
