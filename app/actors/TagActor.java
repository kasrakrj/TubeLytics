package actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import models.services.TagsService;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

public class TagActor extends AbstractActor {

    private final TagsService tagsService;
    private final String sessionId;

    /**
     * Creates Props for TagActor.
     *
     * @param tagsService The TagsService instance.
     * @param sessionId   The session ID.
     * @return A Props instance.
     */
    public static Props props(TagsService tagsService, String sessionId) {
        return Props.create(TagActor.class, () -> new TagActor(tagsService, sessionId));
    }

    public TagActor(TagsService tagsService, String sessionId) {
        this.tagsService = tagsService;
        this.sessionId = sessionId;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, this::onFetchTagsForVideo)
                .build();
    }

    private void onFetchTagsForVideo(String videoId) {
        CompletionStage<List<String>> tagsFuture = tagsService.getTagsByVideoId(videoId);

        tagsFuture.thenAccept(tags -> {
            // Create a Map to send back
            Map<String, Object> response = new HashMap<>();
            response.put("type", "tags");
            response.put("videoId", videoId);
            response.put("tags", tags);

            // Send the response back to the sender (UserActor)
            getSender().tell(response, getSelf());
        }).exceptionally(ex -> {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("type", "error");
            errorResponse.put("message", "Error fetching tags: " + ex.getMessage());

            // Send the error back to the sender
            getSender().tell(errorResponse, getSelf());
            return null;
        });
    }
}
