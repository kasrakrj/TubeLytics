package models.services;

import models.entities.Video;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.net.http.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class SearchService {
    YouTubeService youTubeService = new YouTubeService();
    private final String API_KEY = youTubeService.getApiKey();
    private final String API_URL = youTubeService.getApiUrl();
    private final String YOUTUBE_SEARCH_URL = API_URL+"/search?part=snippet&order=date&type=video&maxResults=";


    // Method to call the YouTube API and process the response
    public CompletionStage<List<Video>> searchVideos(String keyword, int numOfResults) {
        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        String apiUrl = YOUTUBE_SEARCH_URL + numOfResults + "&q=" + encodedKeyword + "&key=" + API_KEY;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl)).build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(response -> {
            String responseBody = response.body();
            JSONObject json = new JSONObject(responseBody);
            JSONArray items = json.getJSONArray("items");

            // Parse the items array properly
            return youTubeService.parseVideos(items);
        });
    }

    // Method to get channel info

    // Helper method to parse video list from JSON

}
