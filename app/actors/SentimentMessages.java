package actors;

import models.entities.Video;

import java.util.List;

public class SentimentMessages {

    public static class AnalyzeVideos {
        private final List<Video> videos;

        public AnalyzeVideos(List<Video> videos) {
            this.videos = videos;
        }

        public List<Video> getVideos() {
            return videos;
        }
    }

    public static class GetOverallSentiment {
        private final List<Video> videos;

        public GetOverallSentiment(List<Video> videos) {
            this.videos = videos;
        }

        public List<Video> getVideos() {
            return videos;
        }
    }
}
