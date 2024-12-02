package actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.pattern.Patterns;
import models.entities.Video;
import models.services.SentimentService;
import scala.concurrent.ExecutionContextExecutor;
import scala.concurrent.Future;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
/**
 * An Akka actor responsible for sentiment analysis of YouTube videos.
 *
 * <p>The actor processes incoming messages to perform either individual sentiment analysis
 * for each video or compute the overall sentiment for a list of videos. It interacts with
 * the {@link SentimentService} to handle sentiment analysis logic asynchronously.</p>
 *
 * <p>Supported message types:
 * <ul>
 *   <li>{@link SentimentMessages.AnalyzeVideos} - Analyze individual sentiments for a list of videos.</li>
 *   <li>{@link SentimentMessages.GetOverallSentiment} - Compute the overall sentiment for a list of videos.</li>
 * </ul>
 * </p>
 *
 * <p>Results are sent back to the sender asynchronously using Akka's pipe pattern.</p>
 *
 * @author Hosna Habibi
 */
public class SentimentActor extends AbstractActor {

    private final SentimentService sentimentService;
    /**
     * Factory method to create a {@code Props} instance for this actor.
     *
     * @param sentimentService the sentiment analysis service to be used by the actor
     * @return a {@code Props} instance
     */
    public static Props props(SentimentService sentimentService) {
        return Props.create(SentimentActor.class, sentimentService);
    }
    /**
     * Constructs a {@code SentimentActor} with the given {@code SentimentService}.
     *
     * @param sentimentService the sentiment analysis service to be used by the actor
     */
    public SentimentActor(SentimentService sentimentService) {
        this.sentimentService = sentimentService;
    }
    /**
     * Defines the message-handling behavior for this actor.
     *
     * <p>The actor responds to the following message types:
     * <ul>
     *   <li>{@link SentimentMessages.AnalyzeVideos}</li>
     *   <li>{@link SentimentMessages.GetOverallSentiment}</li>
     * </ul>
     * </p>
     *
     * @return the receive behavior
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SentimentMessages.AnalyzeVideos.class, this::handleAnalyzeVideos)
                .match(SentimentMessages.GetOverallSentiment.class, this::handleGetOverallSentiment)
                .build();
    }

    /**
     * Handles analyzing individual sentiments for a list of videos.
     *
     * <p>This method processes an {@link SentimentMessages.AnalyzeVideos} message
     * by analyzing the sentiment for each video's description asynchronously.
     * The result is a map where the keys are video IDs and the values are sentiment strings.
     * The results are sent back to the sender.</p>
     *
     * @param message the {@code AnalyzeVideos} message containing the list of videos
     */
    private void handleAnalyzeVideos(SentimentMessages.AnalyzeVideos message) {
        List<Video> videos = message.getVideos();

        // Use SentimentService to analyze sentiment asynchronously for each video
        CompletionStage<Map<String, String>> sentimentResultsFuture = CompletableFuture.supplyAsync(() ->
                videos.stream()
                        .collect(Collectors.toMap(
                                Video::getVideoId, // Use Video ID as the key
                                video -> sentimentService.calculateSentiment(video.getDescription()) // Value: Sentiment
                        ))
        );

        // Pipe the results back to the sender
        Patterns.pipe(sentimentResultsFuture, context().dispatcher()).to(sender());
    }


    /**
     * Handles calculating the overall sentiment for a list of videos.
     *
     * <p>This method processes a {@link SentimentMessages.GetOverallSentiment} message
     * by computing the overall sentiment asynchronously using the {@link SentimentService}.
     * The result is sent back to the sender.</p>
     *
     * @param message the {@code GetOverallSentiment} message containing the list of videos
     */
    private void handleGetOverallSentiment(SentimentMessages.GetOverallSentiment message) {
        List<Video> videos = message.getVideos();

        // Use SentimentService to calculate the overall sentiment asynchronously
        CompletionStage<String> overallSentimentFuture = sentimentService.avgSentiment(videos);

        // Pipe the result back to the sender
        Patterns.pipe(overallSentimentFuture, context().dispatcher()).to(sender());
    }
}
