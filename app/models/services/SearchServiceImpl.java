package models.services;

import models.entities.SearchQuery;
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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SearchServiceImpl implements SearchService {

    private static final String API_KEY = "AIzaSyBCYzFvdDbkPslgU8WvAqX_dMk9RHMG1Ug";
    private final HttpClient httpClient;

    public SearchServiceImpl(HttpClient httpClient) {
        this.httpClient = httpClient;
    }


    @Override
    public CompletionStage<SearchQuery> searchVideos(String keyword) {
        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        return fetchSearchQueryAsync(keyword, API_KEY);

    }



    private CompletionStage<SearchQuery> fetchSearchQueryAsync(String keyword, String apiKey) {
        String apiUrl = buildApiUrl(keyword, apiKey);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenCompose(response -> {
                    if (response.statusCode() == 200) {
                        List<Video> videos = parseResponse(response.body());
                        SearchQuery searchQuery = new SearchQuery(keyword, videos); // Corrected line
                        return CompletableFuture.completedFuture(searchQuery);
                    } else {
                        return CompletableFuture.failedFuture(new RuntimeException("HTTP error: " + response.statusCode()));
                    }
                });
    }



    private String buildApiUrl(String keyword, String apiKey) {
        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        return "https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&maxResults=10&q="
                + encodedKeyword + "&key=" + apiKey;
    }

    private List<Video> parseResponse(String responseBody) {
        try {
            JSONObject jsonResponse = new JSONObject(responseBody);
            JSONArray items = jsonResponse.optJSONArray("items");
            if (items != null) {
                return parseVideos(items);
            } else {
                return List.of();
            }
        } catch (Exception e) {
            throw new RuntimeException("JSON parsing error", e);
        }
    }

    private List<Video> parseVideos(JSONArray items) {
        return IntStream.range(0, items.length())
                .mapToObj(items::getJSONObject)
                .map(this::mapToVideo)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    private Optional<Video> mapToVideo(JSONObject item) {
        try {
            JSONObject idObject = item.optJSONObject("id");
            JSONObject snippet = item.optJSONObject("snippet");

            if (idObject == null || snippet == null) {
                return Optional.empty();
            }

            String kind = idObject.optString("kind");
            if (!"youtube#video".equals(kind)) {
                return Optional.empty();
            }

            String videoId = idObject.optString("videoId");
            String title = snippet.optString("title", "No Title");
            String description = snippet.optString("description", "No Description");
            String channelTitle = snippet.optString("channelTitle", "Unknown Channel");

            JSONObject thumbnails = snippet.optJSONObject("thumbnails");
            String thumbnailUrl = "";
            if (thumbnails != null) {
                JSONObject defaultThumbnail = thumbnails.optJSONObject("default");
                if (defaultThumbnail != null) {
                    thumbnailUrl = defaultThumbnail.optString("url", "");
                }
            }

            return Optional.of(new Video(title, description, channelTitle, thumbnailUrl, videoId));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
