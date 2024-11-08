package models.services;

import models.entities.Video;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.net.http.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class SearchService {
    private final YouTubeService youTubeService = new YouTubeService();
    private final String API_KEY = youTubeService.getApiKey();
    private final String API_URL = youTubeService.getApiUrl();
    private final String YOUTUBE_SEARCH_URL = API_URL + "/search?part=snippet&order=date&type=video&maxResults=";

    private static final int MAX_SEARCHES = 10;

    // Search history map: keyword -> videos
    private final LinkedHashMap<String, List<Video>> searchHistory = new LinkedHashMap<>();

    // Method to call the YouTube API and process the response
    public CompletionStage<List<Video>> searchVideos(String keyword, int numOfResults) {
        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        String apiUrl = YOUTUBE_SEARCH_URL + numOfResults + "&q=" + encodedKeyword + "&key=" + API_KEY;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl)).build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    String responseBody = response.body();
                    JSONObject json = new JSONObject(responseBody);
                    JSONArray items = json.getJSONArray("items");

                    return youTubeService.parseVideos(items);
                });
    }

    // Method to add search results to the search history
    public void addSearchResult(String keyword, List<Video> videos) {
        // Remove the oldest search if the limit is reached
        if (searchHistory.size() >= MAX_SEARCHES) {
            Iterator<String> iterator = searchHistory.keySet().iterator();
            if (iterator.hasNext()) {
                iterator.next();
                iterator.remove();
            }
        }
        searchHistory.put(keyword, videos);
    }

    // Retrieve the search history
    public Map<String, List<Video>> getSearchHistory() {
        return new LinkedHashMap<>(searchHistory);
    }

    // Get all videos from search history for sentiment analysis
    public List<Video> getAllVideosForSentiment(int limit) {
        return searchHistory.values().stream()
                .flatMap(List::stream)
                .limit(limit)
                .collect(Collectors.toList());
    }
}
