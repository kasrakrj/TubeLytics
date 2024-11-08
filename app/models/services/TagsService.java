package models.services;

import models.entities.Video;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TagsService {
   YouTubeService youTubeService = new YouTubeService();

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



    // New method to get video details by videoId
    public CompletionStage<Video> getVideoByVideoId(String videoId) {
        String apiUrl =youTubeService.getApiUrl()+ "/videos?part=snippet&id="
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
