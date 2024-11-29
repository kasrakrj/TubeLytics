package actors;

import java.io.Serializable;
import models.entities.Video;
import org.json.JSONObject;
import java.util.List;

/**
 * Container class for message types used in channel profile operations.
 *
 * <p>This class defines messages and responses exchanged between the {@link ChannelProfileActor}
 * and other actors or services. The messages encapsulate information such as channel IDs and
 * video data, while the responses provide the requested data or error information.</p>
 *
 * <p>The messages include:
 * <ul>
 *   <li>{@link GetChannelInfo} - Request to fetch channel information.</li>
 *   <li>{@link GetChannelVideos} - Request to fetch videos for a specific channel.</li>
 *   <li>{@link ChannelInfoResponse} - Response containing channel information.</li>
 *   <li>{@link ChannelVideosResponse} - Response containing a list of videos.</li>
 *   <li>{@link ChannelProfileError} - Response indicating an error occurred.</li>
 * </ul>
 * </p>
 *
 * @author Zahra Rasoulifar
 */
public class ChannelProfileMessages {

    /**
     * Message to request channel information.
     */
    public static class GetChannelInfo implements Serializable {
        private final String channelId;

        /**
         * Constructs a new {@code GetChannelInfo} message.
         *
         * @param channelId the ID of the channel to fetch information for
         */
        public GetChannelInfo(String channelId) {
            this.channelId = channelId;
        }

        /**
         * Gets the channel ID.
         *
         * @return the channel ID
         */
        public String getChannelId() {
            return channelId;
        }
    }

    /**
     * Message to request a list of videos from a channel.
     */
    public static class GetChannelVideos implements Serializable {
        private final String channelId;
        private final int maxResults;

        /**
         * Constructs a new {@code GetChannelVideos} message.
         *
         * @param channelId  the ID of the channel to fetch videos from
         * @param maxResults the maximum number of videos to retrieve
         */
        public GetChannelVideos(String channelId, int maxResults) {
            this.channelId = channelId;
            this.maxResults = maxResults;
        }

        /**
         * Gets the channel ID.
         *
         * @return the channel ID
         */
        public String getChannelId() {
            return channelId;
        }

        /**
         * Gets the maximum number of videos to retrieve.
         *
         * @return the maximum number of videos
         */
        public int getMaxResults() {
            return maxResults;
        }
    }

    /**
     * Response message containing channel information.
     */
    public static class ChannelInfoResponse implements Serializable {
        private final JSONObject channelInfo;

        /**
         * Constructs a new {@code ChannelInfoResponse}.
         *
         * @param channelInfo the channel information as a {@link JSONObject}
         */
        public ChannelInfoResponse(JSONObject channelInfo) {
            this.channelInfo = channelInfo;
        }

        /**
         * Gets the channel information.
         *
         * @return the channel information as a {@link JSONObject}
         */
        public JSONObject getChannelInfo() {
            return channelInfo;
        }
    }

    /**
     * Response message containing a list of videos.
     */
    public static class ChannelVideosResponse implements Serializable {
        private final List<Video> videos;

        /**
         * Constructs a new {@code ChannelVideosResponse}.
         *
         * @param videos the list of videos
         */
        public ChannelVideosResponse(List<Video> videos) {
            this.videos = videos;
        }

        /**
         * Gets the list of videos.
         *
         * @return the list of {@link Video} objects
         */
        public List<Video> getVideos() {
            return videos;
        }
    }

    /**
     * Error message indicating an issue occurred during processing.
     */
    public static class ChannelProfileError implements Serializable {
        private final String errorMessage;

        /**
         * Constructs a new {@code ChannelProfileError}.
         *
         * @param errorMessage the error message
         */
        public ChannelProfileError(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        /**
         * Gets the error message.
         *
         * @return the error message
         */
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
