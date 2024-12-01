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

public class SentimentActor extends AbstractActor {

    private final SentimentService sentimentService;

    public static Props props(SentimentService sentimentService) {
        return Props.create(SentimentActor.class, sentimentService);
    }

    public SentimentActor(SentimentService sentimentService) {
        this.sentimentService = sentimentService;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SentimentMessages.AnalyzeVideos.class, this::handleAnalyzeVideos)
                .match(SentimentMessages.GetOverallSentiment.class, this::handleGetOverallSentiment)
                .build();
    }

    /**
     * Handles analyzing individual sentiments for a list of videos.
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
     */
    private void handleGetOverallSentiment(SentimentMessages.GetOverallSentiment message) {
        List<Video> videos = message.getVideos();

        // Use SentimentService to calculate the overall sentiment asynchronously
        CompletionStage<String> overallSentimentFuture = sentimentService.avgSentiment(videos);

        // Pipe the result back to the sender
        Patterns.pipe(overallSentimentFuture, context().dispatcher()).to(sender());
    }
}
