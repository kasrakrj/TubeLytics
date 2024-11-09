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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class SearchService {
    private final YouTubeService youTubeService = new YouTubeService();
    private final String API_KEY = youTubeService.getApiKey();
    private final String API_URL = youTubeService.getApiUrl();
    private final String YOUTUBE_SEARCH_URL = API_URL + "/search?part=snippet&order=date&type=video&maxResults=";
    private static final int MAX_SEARCH_HISTORY = 10;
    private final Map<String, LinkedHashMap<String, List<Video>>> sessionSearchHistoryMap = new ConcurrentHashMap<>();

    private final SentimentService sentimentService;

    // In-memory cache for storing search results
    private final ConcurrentMap<String, List<Video>> cache = new ConcurrentHashMap<>();

    @Inject
    public SearchService( SentimentService sentimentService) {
        this.sentimentService = sentimentService;
    }

    public CompletionStage<List<Video>> searchVideos(String keyword, int numOfResults) {
        String cacheKey = keyword + ":" + numOfResults;

        // Check cache first
        List<Video> cachedResult = cache.get(cacheKey);
        if (cachedResult != null) {
            return CompletableFuture.completedFuture(cachedResult);
        }

        // If not cached, fetch from YouTube API
        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        String apiUrl = YOUTUBE_SEARCH_URL + numOfResults + "&q=" + encodedKeyword + "&key=" + API_KEY;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl)).build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    String responseBody = response.body();
                    JSONObject json = new JSONObject(responseBody);
                    JSONArray items = json.getJSONArray("items");

                    List<Video> videos = youTubeService.parseVideos(items);

                    // Cache the result
                    cache.put(cacheKey, videos);

                    return videos;
                });
    }
    public Map<String, List<Video>> getSearchHistory(String sessionId) {
        LinkedHashMap<String, List<Video>> searchHistory = sessionSearchHistoryMap.get(sessionId);
        if (searchHistory == null) {
            return Collections.emptyMap();
        }
        synchronized (searchHistory) {
            return new LinkedHashMap<>(searchHistory);
        }
    }
    public CompletionStage<Map<String, String>> calculateIndividualSentiments(String sessionId) {
        Map<String, List<Video>> searchHistory = getSearchHistory(sessionId);

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
        List<Video> allVideos = getAllVideosForSentiment(sessionId, numOfResults);
        return sentimentService.avgSentiment(allVideos);
    }

    public void addSearchResultToHistory(String sessionId, String keyword, List<Video> videos) {
        addSearchResult(sessionId, keyword, videos);
    }
    public void addSearchResult(String sessionId, String keyword, List<Video> videos) {
        LinkedHashMap<String, List<Video>> searchHistory = sessionSearchHistoryMap.computeIfAbsent(sessionId, k -> new LinkedHashMap<>());

        synchronized (searchHistory) {
            if (searchHistory.size() >= MAX_SEARCH_HISTORY) {
                Iterator<String> iterator = searchHistory.keySet().iterator();
                if (iterator.hasNext()) {
                    iterator.next();
                    iterator.remove();
                }
            }
            searchHistory.put(keyword, videos);
        }
    }
    public List<Video> getAllVideosForSentiment(String sessionId, int limit) {
        LinkedHashMap<String, List<Video>> searchHistory = sessionSearchHistoryMap.get(sessionId);
        if (searchHistory == null) {
            return Collections.emptyList();
        }

        synchronized (searchHistory) {
            return searchHistory.values().stream()
                    .flatMap(List::stream)
                    .limit(limit)
                    .collect(Collectors.toList());
        }
    }



    public void clearSearchHistory(String sessionId) {
        sessionSearchHistoryMap.remove(sessionId);
    }
}
