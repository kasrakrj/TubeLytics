// File: ChannelProfileMessages.java
package actors;

import java.io.Serializable;
import models.entities.Video;
import org.json.JSONObject;
import java.util.List;

public class ChannelProfileMessages {

    // Message to get channel info
    public static class GetChannelInfo implements Serializable {
        private final String channelId;

        public GetChannelInfo(String channelId) {
            this.channelId = channelId;
        }

        public String getChannelId() {
            return channelId;
        }
    }

    // Message to get channel videos
    public static class GetChannelVideos implements Serializable {
        private final String channelId;
        private final int maxResults;

        public GetChannelVideos(String channelId, int maxResults) {
            this.channelId = channelId;
            this.maxResults = maxResults;
        }

        public String getChannelId() {
            return channelId;
        }

        public int getMaxResults() {
            return maxResults;
        }
    }

    // Response message for channel info
    public static class ChannelInfoResponse implements Serializable {
        private final JSONObject channelInfo;

        public ChannelInfoResponse(JSONObject channelInfo) {
            this.channelInfo = channelInfo;
        }

        public JSONObject getChannelInfo() {
            return channelInfo;
        }
    }

    // Response message for channel videos
    public static class ChannelVideosResponse implements Serializable {
        private final List<Video> videos;

        public ChannelVideosResponse(List<Video> videos) {
            this.videos = videos;
        }

        public List<Video> getVideos() {
            return videos;
        }
    }

    // Error message
    public static class ChannelProfileError implements Serializable {
        private final String errorMessage;

        public ChannelProfileError(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
