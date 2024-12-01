package models.services;

import models.entities.Video;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.URI;
import java.net.http.*;
import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Service for fetching video details and tags using YouTubeService's methods where possible.
 */
@Singleton
public class TagsService {

    private final YouTubeService youTubeService;
    private final HttpClient httpClient;

    @Inject
    public TagsService(YouTubeService youTubeService) {
        this.youTubeService = youTubeService;
        this.httpClient = HttpClient.newHttpClient();
    }

    public CompletionStage<Video> getVideoByVideoId(String videoId) {
        String apiUrl = String.format("%s/videos?part=snippet&id=%s&key=%s",
                youTubeService.getApiUrl(), videoId, youTubeService.getApiKey());
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl)).build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    JSONObject jsonResponse = new JSONObject(response.body());
                    JSONArray items = jsonResponse.getJSONArray("items");
                    if (items.length() > 0) {
                        JSONObject item = items.getJSONObject(0);
                        return youTubeService.parseVideo(item); // Use parseVideo from YouTubeService
                    } else {
                        return null; // Handle accordingly (e.g., return Optional<Video> or throw an exception)
                    }
                });
    }

    public CompletionStage<List<String>> getTagsByVideoId(String videoId) {
        String apiUrl = String.format("%s/videos?part=snippet&id=%s&key=%s",
                youTubeService.getApiUrl(), videoId, youTubeService.getApiKey());
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl)).build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    JSONObject jsonResponse = new JSONObject(response.body());
                    JSONArray items = jsonResponse.getJSONArray("items");
                    return youTubeService.parseTags(items); // Use parseTags from YouTubeService
                });
    }
}
