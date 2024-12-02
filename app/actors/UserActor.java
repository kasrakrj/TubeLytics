package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import models.entities.Video;
import models.services.SearchService;
import org.json.JSONObject;
import scala.concurrent.duration.Duration;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * The {@code UserActor} class represents an Akka actor that manages user-specific interactions and real-time video search updates.
 * It handles user search history, processes video results, and communicates with external clients and services.
 */
public class UserActor extends AbstractActor {

    private final List<String> searchHistory = new ArrayList<>();
    private final Set<String> processedVideoIds = new HashSet<>();
    private final ActorRef out;
    private final SearchService searchService;
    private final String sessionId;
    private final ActorRef sentimentActor;

    /**
     * Factory method for creating {@code Props} for the {@code UserActor}.
     *
     * @param out            The {@code ActorRef} for output communication with the client.
     * @param searchService  The {@code SearchService} for managing video search and history.
     * @param sentimentActor The {@code ActorRef} for sentiment analysis actor.
     * @param sessionId      The user's session ID.
     * @return The {@code Props} object for creating {@code UserActor} instances.
     */
    public static Props props(ActorRef out, SearchService searchService, ActorRef sentimentActor, String sessionId) {
        return Props.create(UserActor.class, () -> new UserActor(out, searchService, sentimentActor, sessionId));
    }

    /**
     * Constructs a {@code UserActor} with the specified parameters.
     *
     * @param out            The {@code ActorRef} for output communication with the client.
     * @param searchService  The {@code SearchService} for managing video search and history.
     * @param sentimentActor The {@code ActorRef} for sentiment analysis actor.
     * @param sessionId      The user's session ID.
     */
    public UserActor(ActorRef out, SearchService searchService, ActorRef sentimentActor, String sessionId) {
        this.out = out;
        this.searchService = searchService;
        this.sentimentActor = sentimentActor;
        this.sessionId = sessionId;

        Map<String, List<Video>> initialSearchHistory = searchService.getSearchHistory(sessionId);

        if (initialSearchHistory != null) {
            searchHistory.addAll(initialSearchHistory.keySet());
            initialSearchHistory.values().stream()
                    .flatMap(List::stream)
                    .map(Video::getVideoId)
                    .forEach(processedVideoIds::add);
        }
    }

    /**
     * Initializes the {@code UserActor}.
     * Schedules periodic tasks for fetching videos and sending heartbeats.
     */
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

    /**
     * Defines the message handling behavior for the {@code UserActor}.
     * Handles periodic tasks such as "FetchVideos" and "Heartbeat".
     *
     * @return The {@code Receive} object defining message handling behavior.
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, message -> {
                    if (message.equals("Heartbeat")) {
                        sendHeartbeat();
                    } else if (message.equals("FetchVideos")) {
                        System.out.println("FetchVideos triggered at: " + LocalDateTime.now());
                        searchHistory.forEach(this::fetchAndSendResults);
                    } else {
                        // Handle other messages
                    }
                })
                .build();
    }

    /**
     * Fetches new video results for a given keyword, processes the results, and sends them to the client.
     *
     * @param keyword The keyword to search for.
     */
    private void fetchAndSendResults(String keyword) {
        System.out.println("Fetching results for keyword: " + keyword);
        searchService.fetchNewVideos(keyword, 10, processedVideoIds)
                .thenAccept(newResults -> {
                    System.out.println("Fetched " + newResults.size() + " new videos for keyword: " + keyword);
                    if (!newResults.isEmpty()) {
                        searchService.updateVideosForKeyword(keyword, newResults);
                        newResults.stream()
                                .map(video -> videoToJson(video, keyword))
                                .forEach(json -> out.tell(json, self()));
                    }
                })
                .exceptionally(e -> {
                    System.err.println("Error fetching results for keyword '" + keyword + "': " + e.getMessage());
                    return null;
                });
    }

    /**
     * Converts a {@code Video} object to a JSON representation.
     *
     * @param video   The {@code Video} object to convert.
     * @param keyword The associated keyword for the video.
     * @return A JSON string representation of the video.
     */
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

    /**
     * Sends a heartbeat message to the client.
     * The message indicates that the actor is still active.
     */
    private void sendHeartbeat() {
        JSONObject json = new JSONObject();
        json.put("type", "heartbeat");
        out.tell(json.toString(), self());
    }
}
