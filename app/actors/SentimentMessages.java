package actors;

import models.entities.Video;

import java.util.List;
/**
 * A utility class containing message types used by the {@link SentimentActor}.
 *
 * <p>The messages represent operations that the actor can handle, such as analyzing
 * individual video sentiments or calculating overall sentiment for a list of videos.</p>
 *
 * <p>Message Types:
 * <ul>
 *   <li>{@link AnalyzeVideos} - Message for analyzing individual sentiments of videos.</li>
 *   <li>{@link GetOverallSentiment} - Message for calculating the overall sentiment for a list of videos.</li>
 * </ul>
 * </p>
 *
 * <p>Each message type encapsulates the data required for the operation.</p>
 *
 * @author Hosna Habibi
 */
public class SentimentMessages {
    /**
     * A message type used to request sentiment analysis for a list of videos.
     *
     * <p>This message is sent to the {@link SentimentActor} to analyze individual
     * sentiments of each video in the provided list.</p>
     */
    public static class AnalyzeVideos {
        private final List<Video> videos;
        /**
         * Constructs an {@code AnalyzeVideos} message with the given list of videos.
         *
         * @param videos the list of videos to analyze
         */
        public AnalyzeVideos(List<Video> videos) {
            this.videos = videos;
        }
        /**
         * Returns the list of videos to be analyzed.
         *
         * @return the list of videos
         */
        public List<Video> getVideos() {
            return videos;
        }
    }
    /**
     * A message type used to request overall sentiment analysis for a list of videos.
     *
     * <p>This message is sent to the {@link SentimentActor} to calculate the overall
     * sentiment for the provided list of videos.</p>
     */
    public static class GetOverallSentiment {
        private final List<Video> videos;
        /**
         * Constructs a {@code GetOverallSentiment} message with the given list of videos.
         *
         * @param videos the list of videos to analyze
         */
        public GetOverallSentiment(List<Video> videos) {
            this.videos = videos;
        }
        /**
         * Returns the list of videos for which to calculate the overall sentiment.
         *
         * @return the list of videos
         */
        public List<Video> getVideos() {
            return videos;
        }
    }
}
