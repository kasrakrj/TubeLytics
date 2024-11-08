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

public class ChannelProfileService {
    YouTubeService youTubeService;

    @Inject
    public ChannelProfileService(YouTubeService youTubeService) {
        this.youTubeService = youTubeService;
    }

    protected HttpClient createHttpClient() {
        return HttpClient.newHttpClient();
    }

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
