package models.dao;

import models.entities.Video;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class VideoRepository {

    private static final String API_KEY = "AIzaSyBCYzFvdDbkPslgU8WvAqX_dMk9RHMG1Ug";
    private static final String YOUTUBE_SEARCH_URL = "https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&maxResults=10&q=";

    // Method to call the YouTube API and process the response
    public CompletionStage<List<Video>> searchVideos(String keyword) {
        String apiUrl = YOUTUBE_SEARCH_URL + keyword + "&key=" + API_KEY;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl)).build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    String responseBody = response.body();
                    JSONObject json = new JSONObject(responseBody);
                    JSONArray items = json.getJSONArray("items");

                    // Parse the items array properly
                    return parseVideos(items);
                });
    }

    // Helper method to parse the JSON array into a list of Video objects
    private List<Video> parseVideos(JSONArray items) {
        List<Video> videos = new ArrayList<>();

        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            JSONObject snippet = item.getJSONObject("snippet");

            String title = snippet.getString("title");
            String description = snippet.getString("description");
            String channelTitle = snippet.getString("channelTitle");
            String thumbnailUrl = snippet.getJSONObject("thumbnails").getJSONObject("default").getString("url");
            String videoId = item.getJSONObject("id").getString("videoId");

            videos.add(new Video(title, description, channelTitle, thumbnailUrl, videoId));
        }

        return videos;
    }
}
