package models.services;

import models.entities.Video;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.net.http.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class YouTubeService {

    private static final String API_KEY = "AIzaSyBCYzFvdDbkPslgU8WvAqX_dMk9RHMG1Ug";
    private static final String YOUTUBE_SEARCH_URL = "https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&maxResults=";
    private static final String YOUTUBE_CHANNEL_URL = "https://www.googleapis.com/youtube/v3/channels?part=snippet,statistics&id=";
    private static final String YOUTUBE_CHANNEL_VIDEOS_URL = "https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&maxResults=10&channelId=";

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
            return parseVideos(items);
        });
    }

    // Method to get channel info
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
            return parseVideos(items);
        });
    }

    // Helper method to parse video list from JSON
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
            String channelId = snippet.getString("channelId"); // Fetch channelId from snippet
            videos.add(new Video(title, description, channelTitle, thumbnailUrl, videoId, channelId));
        }
        return videos;
    }
}
