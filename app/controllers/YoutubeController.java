package controllers;

import models.entities.Video;
import models.Sentiment;
import models.services.WordStatService;
import models.services.YouTubeService;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import scala.collection.immutable.Map$;

public class YoutubeController extends Controller {

    private final YouTubeService youTubeService;
    private final Sentiment sentimentAnalyzer;
    private final WordStatService wordStatService;
    private LinkedHashMap<String, List<Video>> searchHistory = new LinkedHashMap<>();
    private Map<String, String> individualSentiments = new LinkedHashMap<>();
    private static final int MAX_SEARCHES = 10;

    @Inject
    public YoutubeController(YouTubeService youTubeService, Sentiment sentimentAnalyzer, WordStatService wordStatService) {
        this.youTubeService = youTubeService;
        this.sentimentAnalyzer = sentimentAnalyzer;
        this.wordStatService = wordStatService;
    }

    public Result index() {
        return ok(views.html.index.render());
    }

    public CompletionStage<Result> search(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return CompletableFuture.completedFuture(redirect(routes.YoutubeController.index()));
        }

        // Fetch videos and then analyze sentiments asynchronously
        return youTubeService.searchVideos(keyword).thenCompose(videos -> {
            // Remove the oldest search if we exceed max search history limit
            if (searchHistory.size() >= MAX_SEARCHES) {
                String oldestKeyword = searchHistory.keySet().iterator().next();
                searchHistory.remove(oldestKeyword);
                individualSentiments.remove(oldestKeyword);
            }

            // Store the results for the current search
            searchHistory.put(keyword, videos);

            // Analyze individual sentiment for the search
            return sentimentAnalyzer.avgSentiment(videos).thenCompose(individualSentiment -> {
                individualSentiments.put(keyword, individualSentiment);

                // Calculate overall sentiment based on all searches in history
                List<Video> allVideos = searchHistory.values().stream()
                        .flatMap(List::stream)
                        .collect(Collectors.toList());

                // Calculate overall sentiment asynchronously
                return sentimentAnalyzer.avgSentiment(allVideos).thenApply(overallSentiment ->
                        ok(views.html.searchResults.render(searchHistory, overallSentiment, individualSentiments))
                );
            });
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return internalServerError(views.html.errorPage.render("An error occurred while fetching search results."));
        });
    }


    public CompletionStage<Result> channelProfile(String channelId) {
        // Fetch channel info and videos, then render the profile view
        return youTubeService.getChannelInfo(channelId)
                .thenCombine(youTubeService.getChannelVideos(channelId, 10), (channelInfo, videos) ->
                        ok(views.html.channelProfile.render(channelInfo, videos))
                )
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return internalServerError(views.html.errorPage.render("An error occurred while fetching channel profile."));
                });
    }

    public CompletionStage<Result> wordStats(String keyword){
        return youTubeService.searchVideos(keyword).thenApply(videos -> {
            Map<String, Long> wordStats = wordStatService.createWordStats(videos);
            return ok(views.html.wordStats.render(keyword, wordStats));
            }).exceptionally(ex -> {
            ex.printStackTrace();
            return internalServerError(views.html.errorPage.render("An error occurred while fetching word stats."));
            });
    }

}
