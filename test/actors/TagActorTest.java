package actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import models.services.TagsService;

import java.util.concurrent.CompletionStage;

import static akka.pattern.Patterns.pipe;

/**
 * Actor responsible for fetching video information and tags.
 */
public class TagActorTest extends AbstractActor {

    private final TagsService tagsService;

    /**
     * Creates Props for TagActorTest.
     *
     * @param tagsService The TagsService instance.
     * @return A Props instance.
     */
    public static Props props(TagsService tagsService) {
        return Props.create(TagActorTest.class, () -> new TagActorTest(tagsService));
    }

    public TagActorTest(TagsService tagsService) {
        this.tagsService = tagsService;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TagMessagesTest.GetVideo.class, this::handleGetVideo)
                .match(TagMessagesTest.GetTags.class, this::handleGetTags)
                .build();
    }

    private void handleGetVideo(TagMessagesTest.GetVideo message) {
        String videoId = message.getVideoId();

        CompletionStage<Object> futureResponse = tagsService.getVideoByVideoId(videoId)
                .handle((video, ex) -> {
                    if (ex != null) {
                        return new TagMessagesTest.TagsError(ex.getMessage());
                    } else {
                        return new TagMessagesTest.GetVideoResponse(video);
                    }
                });

        pipe(futureResponse, getContext().dispatcher()).to(sender());
    }

    private void handleGetTags(TagMessagesTest.GetTags message) {
        String videoId = message.getVideoId();

        CompletionStage<Object> futureResponse = tagsService.getTagsByVideoId(videoId)
                .handle((tags, ex) -> {
                    if (ex != null) {
                        return new TagMessagesTest.TagsError(ex.getMessage());
                    } else {
                        return new TagMessagesTest.GetTagsResponse(tags);
                    }
                });

        pipe(futureResponse, getContext().dispatcher()).to(sender());
    }
}
