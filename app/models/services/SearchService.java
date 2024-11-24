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

/**
 * The SearchService class provides methods to search for videos on YouTube, manage search history,
 * and perform sentiment analysis on search results. It leverages YouTubeService to interact with the YouTube API
 * and SentimentService to analyze the sentiment of video descriptions.
 * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
 */
public class SearchService {
    private final YouTubeService youTubeService;
    private final String API_KEY;
    private final String API_URL;
    private final String YOUTUBE_SEARCH_URL;
    private static final int MAX_SEARCH_HISTORY = 10;
    private final Map<String, LinkedHashMap<String, List<Video>>> sessionSearchHistoryMap = new ConcurrentHashMap<>();

    public String getAPI_KEY() {
        return API_KEY;
    }

    public String getAPI_URL() {
        return API_URL;
    }

    public String getYOUTUBE_SEARCH_URL() {
        return YOUTUBE_SEARCH_URL;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    private final SentimentService sentimentService;

    // In-memory cache for storing search results
    private ConcurrentMap<String, List<Video>> cache = new ConcurrentHashMap<>();

    public ConcurrentMap<String, List<Video>> getCache() {
        return cache;
    }

    private  HttpClient httpClient ;

    /**
     * Constructs a SearchService instance with the provided SentimentService, YouTubeService, cache, and HttpClient.
     * Initializes API details for YouTubeService and sets up a cache for search results.
     *
     * @param sentimentService Service used for calculating sentiment of video descriptions.
     * @param youTubeService   Service used for interacting with the YouTube API.
     * @param cache            In-memory cache for storing search results.
     * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     */
    @Inject
    public SearchService(SentimentService sentimentService, YouTubeService youTubeService, ConcurrentHashMap<String, List<Video>> cache) {
        this.sentimentService = sentimentService;
        this.youTubeService = youTubeService;
        this.API_KEY = youTubeService.getApiKey();
        this.API_URL = youTubeService.getApiUrl();
        this.YOUTUBE_SEARCH_URL = API_URL + "/search?part=snippet&order=date&type=video&maxResults=";
        this.cache = cache;
        this.httpClient=HttpClient.newHttpClient();
    }


    public SearchService(SentimentService sentimentService, YouTubeService youTubeService, ConcurrentHashMap<String, List<Video>> cache,HttpClient httpClient ) {
        this.sentimentService = sentimentService;
        this.youTubeService = youTubeService;
        this.API_KEY = youTubeService.getApiKey();
        this.API_URL = youTubeService.getApiUrl();
        this.YOUTUBE_SEARCH_URL = API_URL + "/search?part=snippet&order=date&type=video&maxResults=";
        this.cache = cache;
        this.httpClient=httpClient;
    }

    /**
     * Searches for videos on YouTube based on a keyword and the specified number of results.
     * Results are cached to optimize performance and avoid redundant API calls.
     *
     * @param keyword      The search keyword.
     * @param numOfResults The number of results to fetch.
     * @return CompletionStage of a list of videos matching the search criteria.
     * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     */
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

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl)).build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
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

    /**
     * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     * Retrieves the search history for a specific session, limited to the 10 most recent searches.
     *
     * @param sessionId The session ID for retrieving the search history.
     * @return A map of keywords and lists of videos representing the search history.
     * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     */
    public Map<String, List<Video>> getSearchHistory(String sessionId) {
        LinkedHashMap<String, List<Video>> searchHistory = sessionSearchHistoryMap.get(sessionId);
        if (searchHistory == null) {
            return Collections.emptyMap();
        }
        synchronized (searchHistory) {
            return new LinkedHashMap<>(searchHistory);
        }
    }

    /**
     * Calculates individual sentiment for each keyword in the search history of a session.
     * Uses asynchronous processing for sentiment analysis of video descriptions.
     *
     * @param sessionId The session ID for which to calculate individual sentiments.
     * @return CompletionStage of a map where each keyword is associated with its sentiment.
     * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     */
    public CompletionStage<Map<String, String>> calculateSentiments(String sessionId) {
        Map<String, List<Video>> searchHistory = getSearchHistory(sessionId);

        // Map each search history entry to a CompletionStage of Map.Entry
        List<CompletionStage<Map.Entry<String, String>>> sentimentStages = searchHistory.entrySet().stream()
                .map(entry -> sentimentService.avgSentiment(entry.getValue())
                        .thenApply(sentiment -> Map.entry(entry.getKey(), sentiment)))
                .collect(Collectors.toList());

        // Combine all CompletionStages into a single CompletionStage of the final map
        return CompletableFuture.allOf(sentimentStages.toArray(new CompletableFuture[0]))
                .thenCompose(v -> {
                    // Collect the results asynchronously after all stages are complete
                    List<CompletionStage<Map.Entry<String, String>>> completedStages = sentimentStages;
                    CompletionStage<Map<String, String>> finalResultStage = CompletableFuture.completedFuture(new HashMap<>());
                    for (CompletionStage<Map.Entry<String, String>> stage : completedStages) {
                        finalResultStage = finalResultStage.thenCombine(stage, (map, entry) -> {
                            map.put(entry.getKey(), entry.getValue());
                            return map;
                        });
                    }
                    return finalResultStage;
                });
    }


    /**
     * Calculates the overall sentiment for a sessionâ€™s videos, up to the specified limit.
     *
     * @param sessionId    The session ID for which to calculate overall sentiment.
     * @param numOfResults The maximum number of videos to consider for sentiment analysis.
     * @return CompletionStage of a string representing the overall sentiment.
     * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     */
   /* public CompletionStage<String> calculateOverallSentiment(String sessionId, int numOfResults) {
        List<Video> allVideos = getAllVideosForSentiment(sessionId, numOfResults);
        return sentimentService.avgSentiment(allVideos);
    }*/

    /**
     * Adds a search result to the history for a given session and keyword.
     * Ensures that only the 10 most recent searches are retained.
     *
     * @param sessionId The session ID.
     * @param keyword   The search keyword.
     * @param videos    The list of videos to add to the history.
     * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     */
    public void addSearchResultToHistory(String sessionId, String keyword, List<Video> videos) {
        addSearchResult(sessionId, keyword, videos);
    }

    /**
     * Adds or updates a search result in the history for a session, retaining only 10 latest searches.
     * If the search history exceeds 10 entries, the oldest entry is removed.
     *
     * @param sessionId The session ID.
     * @param keyword   The search keyword.
     * @param videos    The list of videos to add or update in the history.
     * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     */


    public void addSearchResult(String sessionId, String keyword, List<Video> videos) {
        LinkedHashMap<String, List<Video>> searchHistory = sessionSearchHistoryMap.computeIfAbsent(sessionId, k -> new LinkedHashMap<>());

        synchronized (searchHistory) {
            if (searchHistory.size() >= MAX_SEARCH_HISTORY) {
                removeOldestEntry(searchHistory); // Use helper method
            }
            searchHistory.put(keyword, videos);
        }
    }

    //  removeOldestEntry method in SearchService
    public void removeOldestEntry(LinkedHashMap<String, List<Video>> searchHistory) {
        if (!searchHistory.isEmpty()) {
            // Get the first key and remove it
            String oldestKey = searchHistory.entrySet().iterator().next().getKey();
            searchHistory.remove(oldestKey);
        }
    }



    /**
     * Gathers all videos from a session's search history, up to a specified limit, for sentiment analysis.
     *
     * @param sessionId The session ID for retrieving all videos.
     * @param limit     The maximum number of videos to gather.
     * @return A list of videos for sentiment analysis.
     * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     */
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


    /**
     * Clears the search history for a specified session.
     *
     * @param sessionId The session ID for which to clear the history.
     * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     */
    public void clearSearchHistory(String sessionId) {
        sessionSearchHistoryMap.remove(sessionId);
    }
}
