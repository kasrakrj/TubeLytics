package actors;

import models.entities.Video;

import java.io.Serializable;
import java.util.List;

/**
 * Messages used for communication with TagActorTest.
 */
public class TagMessagesTest {

    // Message to request video information
    public static class GetVideo implements Serializable {
        private final String videoId;

        public GetVideo(String videoId) {
            this.videoId = videoId;
        }

        public String getVideoId() {
            return videoId;
        }
    }

    // Message to request tags for a video
    public static class GetTags implements Serializable {
        private final String videoId;

        public GetTags(String videoId) {
            this.videoId = videoId;
        }

        public String getVideoId() {
            return videoId;
        }
    }

    // Response message containing video information
    public static class GetVideoResponse implements Serializable {
        private final Video video;

        public GetVideoResponse(Video video) {
            this.video = video;
        }

        public Video getVideo() {
            return video;
        }
    }

    // Response message containing tags
    public static class GetTagsResponse implements Serializable {
        private final List<String> tags;

        public GetTagsResponse(List<String> tags) {
            this.tags = tags;
        }

        public List<String> getTags() {
            return tags;
        }
    }

    // Error message indicating an issue occurred during processing
    public static class TagsError implements Serializable {
        private final String errorMessage;

        public TagsError(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
