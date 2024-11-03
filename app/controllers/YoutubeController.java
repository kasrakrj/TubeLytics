package controllers;

import models.entities.Video;
import models.Sentiment;
import models.services.YouTubeService;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class YoutubeController extends Controller {

    private final YouTubeService youTubeService;
    private final Sentiment sentimentAnalyzer;

    @Inject
    public YoutubeController(YouTubeService youTubeService, Sentiment sentimentAnalyzer) {
        this.youTubeService = youTubeService;
        this.sentimentAnalyzer = sentimentAnalyzer;
    }

    public Result index() {
        return ok(views.html.index.render());
    }

    public CompletionStage<Result> search(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return CompletableFuture.completedFuture(redirect(routes.YoutubeController.index()));
        }

        // Call the YouTubeService to fetch and process video results
        return youTubeService.searchVideos(keyword)
                .thenApply(videos -> {
                    if (videos.isEmpty()) {
                        return ok(views.html.noResults.render(keyword));  // Handle empty search results
                    }

                    // Use the injected sentimentAnalyzer to analyze video sentiments
                    String sentiment = sentimentAnalyzer.AnalyzeSentiment(videos);

                    // Pass keyword, videos, and sentiment to render
                    return ok(views.html.searchResults.render(keyword, videos, sentiment));
                })
                .exceptionally(ex -> {
                    // Log the error and display an error message
                    ex.printStackTrace();
                    return internalServerError(views.html.errorPage.render("An error occurred while fetching search results."));
                });
    }
}
