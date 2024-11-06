package controllers;

import models.entities.Video;
import models.Sentiment;
import models.services.YouTubeService;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.Map;
import java.util.stream.Collectors;

public class YoutubeController extends Controller {

    private final YouTubeService youTubeService;
    private final Sentiment sentimentAnalyzer;
    private LinkedHashMap<String, List<Video>> searchHistory = new LinkedHashMap<>();
    private Map<String, String> individualSentiments = new LinkedHashMap<>();
    private static final int MAX_SEARCHES = 10;

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

        return youTubeService.searchVideos(keyword).thenCompose(videos -> {
            // Handle max search limit
            if (searchHistory.size() >= MAX_SEARCHES) {
                String oldestKeyword = searchHistory.keySet().iterator().next();
                searchHistory.remove(oldestKeyword);
                individualSentiments.remove(oldestKeyword);
            }

            // Store individual search results
            searchHistory.put(keyword, videos);

            // Calculate individual sentiment asynchronously
            return sentimentAnalyzer.AnalyzeSentiment(videos).thenCompose(individualSentiment -> {
                individualSentiments.put(keyword, individualSentiment);

                // Calculate overall sentiment for all searches asynchronously
                List<Video> allVideos = searchHistory.values().stream()
                        .flatMap(List::stream)
                        .collect(Collectors.toList());

                return sentimentAnalyzer.AnalyzeSentiment(allVideos).thenApply(overallSentiment ->
                        ok(views.html.searchResults.render(searchHistory, overallSentiment, individualSentiments))
                );
            });
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return internalServerError(views.html.errorPage.render("An error occurred while fetching search results."));
        });
    }
}
