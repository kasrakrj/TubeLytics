package actors;

import models.entities.Video;

import java.util.List;

/**
 * Messages used by the WordStatActor.
 */
public class WordStatMessages {

    /**
     * Message to update the video list and recalculate word statistics.
     */
    public static class UpdateVideos {
        public final String keyword;

        public UpdateVideos(String keyword) {
            this.keyword = keyword;
        }
    }

    /**
     * Message to request the current word statistics.
     */
    public static class GetWordStats {
    }
}
