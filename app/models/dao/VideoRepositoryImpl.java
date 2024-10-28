package models.dao;

import models.entities.Video;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class VideoRepositoryImpl implements VideoRepository {

    private final HttpClient httpClient;

    public VideoRepositoryImpl() {
        this.httpClient = HttpClient.newHttpClient();
    }

    // Asynchronous method to fetch videos from the API and parse them
    public CompletionStage<List<Video>> fetchVideosFromApiAsync(String apiUrl) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    JSONObject jsonResponse = new JSONObject(response.body());
                    JSONArray items = jsonResponse.getJSONArray("items");
                    return parseVideos(items);  // Parse JSON data within the repository
                });
    }

    // Synchronous method to fetch videos from the API (optional)
    public List<Video> fetchVideosFromApi(String apiUrl) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(apiUrl))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JSONObject jsonResponse = new JSONObject(response.body());
            JSONArray items = jsonResponse.getJSONArray("items");
            return parseVideos(items);  // Parse JSON data directly in the repository
        } else {
            throw new RuntimeException("Failed to fetch videos. Status code: " + response.statusCode());
        }
    }

    // Helper method to parse the JSON array into a list of Video objects using streams
    private List<Video> parseVideos(JSONArray items) {
        return IntStream.range(0, items.length())
                .mapToObj(items::getJSONObject)
                .map(this::mapToVideo)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    // Helper method to map a single JSONObject to a Video object
    private Optional<Video> mapToVideo(JSONObject item) {
        try {
            JSONObject snippet = item.getJSONObject("snippet");

            String title = snippet.optString("title", "No Title");
            String description = snippet.optString("description", "No Description");
            String channelTitle = snippet.optString("channelTitle", "Unknown Channel");
            String thumbnailUrl = snippet
                    .getJSONObject("thumbnails")
                    .getJSONObject("default")
                    .optString("url", "No Thumbnail URL");
            String videoId = item.getJSONObject("id").optString("videoId", "No Video ID");

            return Optional.of(new Video(title, description, channelTitle, thumbnailUrl, videoId));
        } catch (Exception e) {
            System.err.println("Error parsing video item: " + e.getMessage());
            return Optional.empty();
        }
    }
}
