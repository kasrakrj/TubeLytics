package actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import models.entities.Video;
import models.services.TagsService;

import java.util.List;
import java.util.concurrent.CompletionStage;

import static akka.pattern.Patterns.pipe;

/**
 * Actor responsible for fetching video information and tags.
 */
public class TagActor extends AbstractActor {

    private final TagsService tagsService;

    /**
     * Creates Props for TagActor.
     *
     * @param tagsService The TagsService instance.
     * @return A Props instance.
     */
    public static Props props(TagsService tagsService) {
        return Props.create(TagActor.class, () -> new TagActor(tagsService));
    }

    public TagActor(TagsService tagsService) {
        this.tagsService = tagsService;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TagMessages.GetVideo.class, this::handleGetVideo)
                .match(TagMessages.GetTags.class, this::handleGetTags)
                .build();
    }

    private void handleGetVideo(TagMessages.GetVideo message) {
        String videoId = message.getVideoId();

        CompletionStage<Object> futureResponse = tagsService.getVideoByVideoId(videoId)
                .handle((video, ex) -> {
                    if (ex != null) {
                        return new TagMessages.TagsError(ex.getMessage());
                    } else {
                        return new TagMessages.GetVideoResponse(video);
                    }
                });

        pipe(futureResponse, getContext().dispatcher()).to(sender());
    }

    private void handleGetTags(TagMessages.GetTags message) {
        String videoId = message.getVideoId();

        CompletionStage<Object> futureResponse = tagsService.getTagsByVideoId(videoId)
                .handle((tags, ex) -> {
                    if (ex != null) {
                        return new TagMessages.TagsError(ex.getMessage());
                    } else {
                        return new TagMessages.GetTagsResponse(tags);
                    }
                });

        pipe(futureResponse, getContext().dispatcher()).to(sender());
    }
}
