package models.services;

import models.entities.Video;
import org.checkerframework.checker.units.qual.A;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Inject;
import java.net.URLEncoder;
import java.net.http.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.inject.Singleton;

/**
 * The SearchService class provides methods to search for videos on YouTube, manage search history,
 * and perform sentiment analysis on search results. It integrates with SentimentService and YouTubeService.
 */
public class SearchService {
    final YouTubeService youTubeService;
    private final String API_KEY;
    private final String API_URL;
    private final String YOUTUBE_SEARCH_URL;
    private static final int MAX_SEARCH_HISTORY = 10; // Limit to 10 results
    final Map<String, LinkedHashMap<String, List<Video>>> sessionSearchHistoryMap = new ConcurrentHashMap<>();
    final SentimentService sentimentService;
    final ConcurrentMap<String, List<Video>> cache = new ConcurrentHashMap<>();
    HttpClient httpClient;
    boolean isTestingMode = true;


    @Inject
    public SearchService(SentimentService sentimentService, YouTubeService youTubeService) {
        this.sentimentService = sentimentService;
        this.youTubeService = youTubeService;
        this.API_KEY = youTubeService.getApiKey();
        this.API_URL = youTubeService.getApiUrl();
        this.YOUTUBE_SEARCH_URL = API_URL + "/search?part=snippet&order=date&type=video&maxResults=";
        this.httpClient = HttpClient.newHttpClient();
    }
    public String getYOUTUBE_SEARCH_URL(){
        return YOUTUBE_SEARCH_URL;
    }
    public HttpClient getHttpClient(){
        return httpClient;
    }
    public ConcurrentMap<String, List<Video>> getCache(){
        return cache;
    }

    public String getAPI_KEY(){
        return API_KEY;
    }
    public String getAPI_URL(){
        return API_URL;
    }

    /**
     * Fetches videos based on a keyword and number of results. Results are cached to avoid redundant API calls.
     */
    public CompletionStage<List<Video>> searchVideos(String keyword, int numOfResults) {
        String cacheKey = keyword + ":" + numOfResults;
        if (cache.containsKey(cacheKey)) {
            return CompletableFuture.completedFuture(cache.get(cacheKey));
        }

        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        String apiUrl = YOUTUBE_SEARCH_URL + numOfResults + "&q=" + encodedKeyword + "&key=" + API_KEY;

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl)).build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    String responseBody = response.body();
                    JSONObject json = new JSONObject(responseBody);
                    JSONArray items = json.getJSONArray("items");
                    List<Video> videos = youTubeService.parseVideos(items);
                    cache.put(cacheKey, videos);
                    return videos;
                });
    }

    /**
     * Fetches new videos for a keyword, ensuring no duplicates using processed video IDs.
     */
    public CompletionStage<List<Video>> fetchNewVideos(String keyword, int numOfResults, Set<String> processedVideoIds) {
        if (isTestingMode) {
            return CompletableFuture.completedFuture(generateMockVideos(keyword, 2, processedVideoIds));
        }
        return searchVideos(keyword, numOfResults)
                .thenApply(videos -> videos.stream()
                        .filter(video -> isNewVideo(video, processedVideoIds))
                        .collect(Collectors.toList())
                )
                .exceptionally(e -> {
                    System.err.println("Error fetching videos for keyword: " + e.getMessage());
                    return Collections.emptyList();
                });
    }

    /**
     * Updates the search history for a session and keyword with new videos.
     */
    public void updateVideosForKeyword(String sessionId, String keyword, List<Video> newVideos) {
        LinkedHashMap<String, List<Video>> searchHistory = sessionSearchHistoryMap.computeIfAbsent(sessionId, k -> new LinkedHashMap<>());
        synchronized (searchHistory) {
            List<Video> existingVideos = searchHistory.getOrDefault(keyword, new ArrayList<>());
            existingVideos.addAll(0, newVideos); // Add new videos at the top

            // Trim to the most recent 10 videos
            if (existingVideos.size() > MAX_SEARCH_HISTORY) {
                existingVideos = existingVideos.subList(0, MAX_SEARCH_HISTORY);
            }

            searchHistory.put(keyword, existingVideos);
        }
    }


    /**
     * Generates mock videos for testing mode.
     */
    public List<Video> generateMockVideos(String keyword, int numOfResults, Set<String> processedVideoIds) {
        return IntStream.range(0, numOfResults)
                .mapToObj(i -> {
                    String videoId;
                    do {
                        videoId = UUID.randomUUID().toString();
                    } while (processedVideoIds.contains(videoId));

                    Video video = new Video();
                    video.setVideoId(videoId);
                    video.setTitle("Mock Video " + (i + 1) + " for keyword: " + keyword);
                    video.setDescription("Description for mock video " + (i + 1));
                    video.setThumbnailUrl("https://picsum.photos/120/80?random=" + UUID.randomUUID());
                    video.setChannelId("UCH57DD9ssIIVfuav-j2iavw");
                    video.setChannelTitle("Mock Channel " + (i + 1));
                    video.setPublishedAt(ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT));
                    return video;
                })
                .collect(Collectors.toList());
    }

    /**
     * Updates all session search histories with new videos for the given keyword.
     * If any session contains the keyword, the new videos are added to the respective session's keyword list.
     * Ensures the total videos for each keyword do not exceed the limit.
     *
     * @param keyword   The search keyword for which new videos are added.
     * @param newVideos The new videos to add for the keyword.
     */
    public void updateVideosForKeyword(String keyword, List<Video> newVideos) {
        sessionSearchHistoryMap.values().stream()
                .map(searchHistory -> {
                    synchronized (searchHistory) {
                        if (searchHistory.containsKey(keyword)) {
                            List<Video> existingVideos = new ArrayList<>(searchHistory.getOrDefault(keyword, new ArrayList<>()));
                            existingVideos.addAll(0, newVideos); // Add new videos at the top

                            // Trim to the most recent 10 videos
                            if (existingVideos.size() > MAX_SEARCH_HISTORY) {
                                existingVideos = existingVideos.subList(0, MAX_SEARCH_HISTORY);
                            }

                            searchHistory.put(keyword, existingVideos);
                        }
                    }
                    return searchHistory;
                })
                .count(); // Ensures the stream is executed
    }



    /**
     * Retrieves the search history for a session.
     */
    public Map<String, List<Video>> getSearchHistory(String sessionId) {
        LinkedHashMap<String, List<Video>> searchHistory = sessionSearchHistoryMap.get(sessionId);
        if (searchHistory == null) {
            return Collections.emptyMap();
        }
        synchronized (searchHistory) {
            return new LinkedHashMap<>(searchHistory); // Return a copy to avoid concurrent modifications
        }
    }
    public CompletionStage<Map<String, String>> calculateSentiments(String sessionId) {
        Map<String, List<Video>> searchHistory = getSearchHistory(sessionId);

        // Convert each entry to a CompletionStage of Map.Entry
        List<CompletionStage<Map.Entry<String, String>>> sentimentStages = searchHistory.entrySet().stream()
                .map(entry -> sentimentService.avgSentiment(entry.getValue())
                        .thenApply(sentiment -> Map.entry(entry.getKey(), sentiment)))
                .collect(Collectors.toList());

        // Start with an empty CompletableFuture<Map<String, String>>
        return sentimentStages.stream()
                .reduce(
                        CompletableFuture.completedFuture(new HashMap<>()), // Initial accumulator
                        (combinedFuture, currentStage) -> combinedFuture.thenCombine(currentStage, (map, entry) -> {
                            map.put(entry.getKey(), entry.getValue());
                            return map;
                        }),
                        (f1, f2) -> f1.thenCombine(f2, (map1, map2) -> {
                            map1.putAll(map2);
                            return map1;
                        })
                );
    }




    /**
     * Clears the search history for a session.
     */
    public void clearSearchHistory(String sessionId) {
        sessionSearchHistoryMap.remove(sessionId);
    }

    /**
     * Checks if a video is new by verifying its ID against processed video IDs.
     */
    private boolean isNewVideo(Video video, Set<String> processedVideoIds) {
        return processedVideoIds.add(video.getVideoId());
    }

    /**
     * Adds or updates a search result for a session, keeping history limited to 10 searches.
     */
    public void addSearchResult(String sessionId, String keyword, List<Video> videos) {
        LinkedHashMap<String, List<Video>> searchHistory = sessionSearchHistoryMap.computeIfAbsent(sessionId, k -> new LinkedHashMap<>());
        synchronized (searchHistory) {
            if (searchHistory.size() >= MAX_SEARCH_HISTORY) {
                removeOldestEntry(searchHistory);
            }

            // Trim videos to the most recent 10
            List<Video> trimmedVideos = videos.size() > MAX_SEARCH_HISTORY ? videos.subList(0, MAX_SEARCH_HISTORY) : videos;
            searchHistory.put(keyword, trimmedVideos);
        }
    }


    /**
     * Removes the oldest entry from the search history.
     */
    public void removeOldestEntry(LinkedHashMap<String, List<Video>> searchHistory) {
        if (!searchHistory.isEmpty()) {
            String oldestKey = searchHistory.keySet().iterator().next();
            searchHistory.remove(oldestKey);
        }
    }

}
