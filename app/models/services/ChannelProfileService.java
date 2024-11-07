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

public class ChannelProfileService {
    YouTubeService youTubeService = new YouTubeService();
    private final String API_KEY = youTubeService.getApiKey();
    private final String API_URL = youTubeService.getApiUrl();
    private final String YOUTUBE_CHANNEL_URL = API_URL+"/channels?part=snippet,statistics&id=";
    private final String YOUTUBE_CHANNEL_VIDEOS_URL = API_URL+"/search?part=snippet&type=video&";

    public CompletionStage<JSONObject> getChannelInfo(String channelId) {
        String apiUrl = YOUTUBE_CHANNEL_URL + channelId + "&key=" + API_KEY;
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl)).build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(response -> {
            String responseBody = response.body();
            JSONObject json = new JSONObject(responseBody);
            return json.getJSONArray("items").getJSONObject(0).getJSONObject("snippet");
        });
    }

    // Method to get last 10 videos of a channel
    public CompletionStage<List<Video>> getChannelVideos(String channelId, int maxResults) {
        String apiUrl = YOUTUBE_CHANNEL_VIDEOS_URL + channelId + "&maxResults=" + maxResults + "&key=" + API_KEY;
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl)).build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(response -> {
            String responseBody = response.body();
            JSONObject json = new JSONObject(responseBody);
            JSONArray items = json.getJSONArray("items");
            return youTubeService.parseVideos(items);
        });
    }



}
