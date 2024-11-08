package models.services;

import models.entities.Video;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Singleton
public class SessionManagerService {
    private static final int MAX_SEARCH_HISTORY = 10;
    private final Map<String, LinkedHashMap<String, List<Video>>> sessionSearchHistoryMap = new ConcurrentHashMap<>();

    /**
     * Adds a search result to the user's session history.
     *
     * @param sessionId The unique session identifier.
     * @param keyword   The search keyword.
     * @param videos    The list of videos returned from the search.
     */
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

    /**
     * Retrieves the search history for a given session.
     *
     * @param sessionId The unique session identifier.
     * @return A copy of the search history map.
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
     * Retrieves all videos from the search history up to a specified limit for sentiment analysis.
     *
     * @param sessionId The unique session identifier.
     * @param limit     The maximum number of videos to retrieve.
     * @return A list of videos for sentiment analysis.
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
     * Clears the search history for a given session.
     *
     * @param sessionId The unique session identifier.
     */
    public void clearSearchHistory(String sessionId) {
        sessionSearchHistoryMap.remove(sessionId);
    }
}
