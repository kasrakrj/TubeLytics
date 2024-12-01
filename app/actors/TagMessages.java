package actors;

import models.entities.Video;

import java.io.Serializable;
import java.util.List;

/**
 * Messages used by the TagActor.
 */
public class TagMessages {

    /**
     * Message to request video and tags for a given video ID.
     */
    public static class GetVideoAndTags implements Serializable {
        private final String videoId;

        public GetVideoAndTags(String videoId) {
            this.videoId = videoId;
        }

        public String getVideoId() {
            return videoId;
        }
    }

    /**
     * Response message containing video and tags.
     */
    public static class VideoAndTagsResponse implements Serializable {
        private final Video video;
        private final List<String> tags;

        public VideoAndTagsResponse(Video video, List<String> tags) {
            this.video = video;
            this.tags = tags;
        }

        public Video getVideo() {
            return video;
        }

        public List<String> getTags() {
            return tags;
        }
    }

    /**
     * Error message indicating an issue occurred during processing.
     */
    public static class TagError implements Serializable {
        private final String errorMessage;

        public TagError(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
