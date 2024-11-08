package controllers;

import models.entities.Video;
import models.services.*;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class YoutubeController extends Controller {

    private static final int DEFAULT_NUM_OF_RESULTS = 10;
    private static final int NUM_OF_RESULTS_SENTIMENT = 50;

    private final SearchService searchService;
    private final SentimentService sentimentServiceAnalyzer;
    private final WordStatService wordStatService;
    private final ChannelProfileService channelProfileService;
    private final TagsService tagsService;

    @Inject
    public YoutubeController(SearchService searchService,
                             SentimentService sentimentServiceAnalyzer,
                             WordStatService wordStatService,
                             ChannelProfileService channelProfileService,
                             TagsService tagsService) {
        this.searchService = searchService;
        this.sentimentServiceAnalyzer = sentimentServiceAnalyzer;
        this.wordStatService = wordStatService;
        this.channelProfileService = channelProfileService;
        this.tagsService = tagsService;
    }

    public CompletionStage<Result> index() {
        return CompletableFuture.completedFuture(ok(views.html.index.render()));
    }

    public CompletionStage<Result> tags(String videoID) {
        return tagsService.getVideoByVideoId(videoID)
                .thenCompose(video ->
                        tagsService.getTagsByVideo(video)
                                .thenApply(tags -> ok(views.html.tagsPage.render(video, tags)))
                ).exceptionally(ex -> {
                    ex.printStackTrace();
                    return internalServerError(views.html.errorPage.render("An error occurred while fetching tags."));
                });
    }

    public CompletionStage<Result> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return CompletableFuture.completedFuture(redirect(routes.YoutubeController.index()));
        }

        String standardizedKeyword = keyword.trim().toLowerCase();

        return searchService.searchVideos(standardizedKeyword, DEFAULT_NUM_OF_RESULTS)
                .thenCompose(videos -> {
                    // Add the search result to the history
                    searchService.addSearchResult(standardizedKeyword, videos);

                    // Compute individual sentiment for the current search
                    CompletionStage<String> individualSentimentFuture = sentimentServiceAnalyzer.avgSentiment(videos);

                    // Compute overall sentiment from all videos in history
                    List<Video> allVideos = searchService.getAllVideosForSentiment(NUM_OF_RESULTS_SENTIMENT);
                    CompletionStage<String> overallSentimentFuture = sentimentServiceAnalyzer.avgSentiment(allVideos);

                    // Prepare individual sentiments for all keywords
                    Map<String, List<Video>> searchHistory = searchService.getSearchHistory();
                    Map<String, CompletionStage<String>> individualSentimentsFutures = new LinkedHashMap<>();

                    for (Map.Entry<String, List<Video>> entry : searchHistory.entrySet()) {
                        String key = entry.getKey();
                        List<Video> vidList = entry.getValue();
                        individualSentimentsFutures.put(key, sentimentServiceAnalyzer.avgSentiment(vidList));
                    }

                    // Combine individual sentiments into a Map<String, String>
                    CompletionStage<Map<String, String>> individualSentimentsCombined = CompletableFuture
                            .allOf(individualSentimentsFutures.values().toArray(new CompletableFuture[0]))
                            .thenApply(v -> {
                                Map<String, String> sentiments = new LinkedHashMap<>();
                                for (Map.Entry<String, CompletionStage<String>> entry : individualSentimentsFutures.entrySet()) {
                                    String key = entry.getKey();
                                    String sentiment = entry.getValue().toCompletableFuture().join();
                                    sentiments.put(key, sentiment);
                                }
                                return sentiments;
                            });

                    // Combine all futures and render the result
                    return individualSentimentsCombined.thenCombine(overallSentimentFuture, (individualSentiments, overallSentiment) ->
                            ok(views.html.searchResults.render(searchHistory, overallSentiment, individualSentiments))
                    );
                }).exceptionally(ex -> {
                    ex.printStackTrace();
                    return internalServerError(views.html.errorPage.render("An error occurred while fetching search results."));
                });
    }

    public CompletionStage<Result> channelProfile(String channelId) {
        return channelProfileService.getChannelInfo(channelId)
                .thenCombine(channelProfileService.getChannelVideos(channelId, 10),
                        (channelInfo, videos) -> ok(views.html.channelProfile.render(channelInfo, videos))
                ).exceptionally(ex -> {
                    ex.printStackTrace();
                    return internalServerError(views.html.errorPage.render("An error occurred while fetching channel profile."));
                });
    }

    public CompletionStage<Result> wordStats(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return CompletableFuture.completedFuture(redirect(routes.YoutubeController.index()));
        }

        String standardizedKeyword = keyword.trim().toLowerCase();

        return searchService.searchVideos(standardizedKeyword, NUM_OF_RESULTS_SENTIMENT)
                .thenApply(videos -> {
                    Map<String, Long> wordStats = wordStatService.createWordStats(videos);
                    return ok(views.html.wordStats.render(standardizedKeyword, wordStats));
                }).exceptionally(ex -> {
                    ex.printStackTrace();
                    return internalServerError(views.html.errorPage.render("An error occurred while fetching word stats."));
                });
    }
}
