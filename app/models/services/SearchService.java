package models.services;

import models.entities.Video;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Inject;
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

    private final SessionManagerService sessionManagerService;
    private final SentimentService sentimentService;

    @Inject
    public SearchService(SessionManagerService sessionManagerService, SentimentService sentimentService) {
        this.sessionManagerService = sessionManagerService;
        this.sentimentService = sentimentService;
    }

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

    public CompletionStage<Map<String, String>> calculateIndividualSentiments(String sessionId) {
        Map<String, List<Video>> searchHistory = sessionManagerService.getSearchHistory(sessionId);

        // Compute sentiment for each keyword in search history
        List<CompletableFuture<Map.Entry<String, String>>> sentimentFutures = searchHistory.entrySet().stream()
                .map(entry -> sentimentService.avgSentiment(entry.getValue())
                        .thenApply(sentiment -> Map.entry(entry.getKey(), sentiment))
                        .toCompletableFuture())
                .collect(Collectors.toList());

        return CompletableFuture.allOf(sentimentFutures.toArray(new CompletableFuture[0]))
                .thenApply(v -> sentimentFutures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    public CompletionStage<String> calculateOverallSentiment(String sessionId, int numOfResults) {
        List<Video> allVideos = sessionManagerService.getAllVideosForSentiment(sessionId, numOfResults);
        return sentimentService.avgSentiment(allVideos);
    }

    public void addSearchResultToHistory(String sessionId, String keyword, List<Video> videos) {
        sessionManagerService.addSearchResult(sessionId, keyword, videos);
    }
}
