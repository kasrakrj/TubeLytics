package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import models.entities.Video;
import models.services.SearchService;
import org.json.JSONArray;
import org.json.JSONObject;
import scala.concurrent.duration.Duration;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class UserActor extends AbstractActor {
    private final List<String> searchHistory = new ArrayList<>();
    private final Set<String> processedVideoIds = new HashSet<>();
    private final ActorRef out;
    private final SearchService searchService;
    private final String sessionId; // Add sessionId to identify the user session

    // Props method to accept the WebSocket's ActorRef and SearchService
    public static Props props(ActorRef out, SearchService searchService, String sessionId) {
        return Props.create(UserActor.class, () -> new UserActor(out, searchService, sessionId));
    }

    // Constructor to initialize the WebSocket out, SearchService, and sessionId
    public UserActor(ActorRef out, SearchService searchService, String sessionId) {
        this.out = out;
        this.searchService = searchService;
        this.sessionId = sessionId;

        // Initialize processedVideoIds with video IDs from the initial search history
        Map<String, List<Video>> initialSearchHistory = searchService.getSearchHistory(sessionId);
        if (initialSearchHistory != null) {
            for (Map.Entry<String, List<Video>> entry : initialSearchHistory.entrySet()) {
                String keyword = entry.getKey();
                List<Video> videos = entry.getValue();
                searchHistory.add(keyword); // Add the keyword to searchHistory
                for (Video video : videos) {
                    processedVideoIds.add(video.getVideoId());
                }
            }
        }
    }

    @Override
    public void preStart() {
        // Schedule periodic updates for search history
        getContext().getSystem().scheduler().scheduleWithFixedDelay(
                Duration.create(10, TimeUnit.SECONDS),  // Initial delay
                Duration.create(30, TimeUnit.SECONDS), // Fetch every 30 seconds
                self(),
                "FetchVideos",
                getContext().getSystem().dispatcher(),
                self()
        );

        // Schedule heartbeat messages
        getContext().getSystem().scheduler().scheduleWithFixedDelay(
                Duration.create(15, TimeUnit.SECONDS), // Initial delay
                Duration.create(15, TimeUnit.SECONDS), // Send heartbeat every 15 seconds
                self(),
                "Heartbeat",
                getContext().getSystem().dispatcher(),
                self()
        );
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, message -> {
                    if (message.equals("Heartbeat")) {
                        sendHeartbeat();
                    } else if (message.equals("FetchVideos")) {
                        for (String keyword : searchHistory) {
                            fetchAndSendResults(keyword);
                        }
                    } else {
                        // Try to parse the message as JSON
                        try {
                            JSONObject json = new JSONObject(message);
                            String type = json.getString("type");
                            if (type.equals("init")) {
                                JSONArray keywordsArray = json.getJSONArray("keywords");
                                for (int i = 0; i < keywordsArray.length(); i++) {
                                    String keyword = keywordsArray.getString(i);
                                    if (!searchHistory.contains(keyword)) {
                                        searchHistory.add(keyword);
                                        fetchAndSendResults(keyword);
                                    }
                                }
                            } else if (type.equals("keyword")) {
                                String keyword = json.getString("keyword");
                                if (!searchHistory.contains(keyword)) {
                                    searchHistory.add(keyword);
                                    fetchAndSendResults(keyword);
                                }
                            }
                        } catch (Exception e) {
                            // Not JSON, assume it's a single keyword
                            String keyword = message.trim();
                            if (!searchHistory.contains(keyword)) {
                                searchHistory.add(keyword);
                                fetchAndSendResults(keyword);
                            }
                        }
                    }
                })
                .build();
    }

    private void fetchAndSendResults(String keyword) {
        List<Video> newResults = searchService.fetchNewVideos(keyword, 10, processedVideoIds);
        System.out.println("Getting " + newResults.size() + " videos for keyword: " + keyword + "|New Results at " + LocalDateTime.now());
        for (Video video : newResults) {
            String json = videoToJson(video, keyword);
            out.tell(json, self());
        }
    }

    private String videoToJson(Video video, String keyword) {
        JSONObject json = new JSONObject();
        json.put("type", "video"); // Add type to distinguish message types
        json.put("keyword", keyword);
        json.put("videoId", video.getVideoId());
        json.put("title", video.getTitle());
        json.put("description", video.getDescription());
        json.put("thumbnailUrl", video.getThumbnailUrl());
        json.put("channelId", video.getChannelId());
        json.put("channelTitle", video.getChannelTitle());
        return json.toString();
    }

    private void sendHeartbeat() {
        JSONObject json = new JSONObject();
        json.put("type", "heartbeat");
        out.tell(json.toString(), self());
    }
}
