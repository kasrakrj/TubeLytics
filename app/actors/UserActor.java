package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.pattern.PatternsCS;
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
    private final String sessionId;
    private final ActorRef sentimentActor;

    public static Props props(ActorRef out, SearchService searchService, ActorRef sentimentActor, String sessionId) {
        return Props.create(UserActor.class, () -> new UserActor(out, searchService, sentimentActor, sessionId));
    }

    public UserActor(ActorRef out, SearchService searchService, ActorRef sentimentActor, String sessionId) {
        this.out = out;
        this.searchService = searchService;
        this.sentimentActor = sentimentActor;
        this.sessionId = sessionId;

        Map<String, List<Video>> initialSearchHistory = searchService.getSearchHistory(sessionId);
        if (initialSearchHistory != null) {
            for (Map.Entry<String, List<Video>> entry : initialSearchHistory.entrySet()) {
                String keyword = entry.getKey();
                List<Video> videos = entry.getValue();
                searchHistory.add(keyword);
                for (Video video : videos) {
                    processedVideoIds.add(video.getVideoId());
                }
            }
        }
    }

    @Override
    public void preStart() {
        getContext().getSystem().scheduler().scheduleWithFixedDelay(
                Duration.create(10, TimeUnit.SECONDS),
                Duration.create(30, TimeUnit.SECONDS),
                self(),
                "FetchVideos",
                getContext().getSystem().dispatcher(),
                self()
        );

        getContext().getSystem().scheduler().scheduleWithFixedDelay(
                Duration.create(15, TimeUnit.SECONDS),
                Duration.create(15, TimeUnit.SECONDS),
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
                        System.out.println("FetchVideos triggered at: " + LocalDateTime.now());
                        for (String keyword : searchHistory) {
                            fetchAndSendResults(keyword);
                        }
                    } else {
                        // Other message handling logic
                    }
                })
                .build();
    }

    private void fetchAndSendResults(String keyword) {
        System.out.println("Fetching results for keyword: " + keyword);
        searchService.fetchNewVideos(keyword, 10, processedVideoIds)
                .thenAccept(newResults -> {
                    System.out.println("Fetched " + newResults.size() + " new videos for keyword: " + keyword);
                    if (!newResults.isEmpty()) {
                        searchService.updateVideosForKeywordAcrossSessions(keyword, newResults);
                        for (Video video : newResults) {
                            String json = videoToJson(video, keyword);
                            out.tell(json, self());
                        }
                    }
                })
                .exceptionally(e -> {
                    System.err.println("Error fetching results for keyword '" + keyword + "': " + e.getMessage());
                    return null;
                });
    }


    private String videoToJson(Video video, String keyword) {
        JSONObject json = new JSONObject();
        json.put("type", "video");
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
