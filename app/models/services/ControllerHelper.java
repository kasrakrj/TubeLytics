package models.services;

import models.entities.Video;
import play.mvc.Http;
import play.mvc.Result;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static models.services.SessionService.addSessionId;
import static models.services.SessionService.getSessionId;
import static play.mvc.Results.*;

public class ControllerHelper {

    public static final int DEFAULT_NUM_OF_RESULTS = 10;
    public static final int NUM_OF_RESULTS_SENTIMENT = 50;
    public static final int NUM_OF_RESULTS_WORD_STATS = 50;

    public static boolean isKeywordValid(String keyword) {
        return keyword != null && !keyword.trim().isEmpty();
    }

    public static CompletionStage<Result> tagHelper(TagsService tagsService, String videoID, Http.Request request){
        return tagsService.getVideoByVideoId(videoID)
                .thenCompose(video ->
                        tagsService.getTagsByVideo(video)
                                .thenApply(tags -> addSessionId(request, ok(views.html.tagsPage.render(video, tags))))
                ).exceptionally(ex -> {
                    ex.printStackTrace();
                    return internalServerError(views.html.errorPage.render("An error occurred while fetching tags."));
                });
    }

    public static CompletionStage<Result> channelProfileHelper(ChannelProfileService channelProfileService, String channelId, Http.Request request){
        return channelProfileService.getChannelInfo(channelId)
                .thenCombine(channelProfileService.getChannelVideos(channelId, 10),
                        (channelInfo, videos) -> addSessionId(request, ok(views.html.channelProfile.render(channelInfo, videos)))
                ).exceptionally(ex -> {
                    ex.printStackTrace();
                    return internalServerError(views.html.errorPage.render("An error occurred while fetching channel profile."));
                });
    }

    public static CompletionStage<Result> wordStatHelper(SearchService searchService, WordStatService wordStatService, String keyword, Http.Request request){
        if (!isKeywordValid(keyword)) {
            System.out.println("Keyword is not valid");
            return CompletableFuture.completedFuture(redirect(controllers.routes.YoutubeController.index()));
        }

        String standardizedKeyword = keyword.trim().toLowerCase();

        return searchService.searchVideos(standardizedKeyword, NUM_OF_RESULTS_WORD_STATS)
                .thenApply(videos -> {
                    searchService.addSearchResult(getSessionId(request), standardizedKeyword, videos);
                    return addSessionId(request, ok(views.html.wordStats.render(standardizedKeyword, wordStatService.createWordStats(videos))));
                }).exceptionally(ex -> {
                    ex.printStackTrace();
                    return internalServerError(views.html.errorPage.render("An error occurred while fetching word stats."));
                });
    }

    public static CompletionStage<Result> searchHelper(SearchService searchService, String keyword, Http.Request request){
        if (!isKeywordValid(keyword)) {
            return CompletableFuture.completedFuture(
                    redirect(controllers.routes.YoutubeController.index()).withSession(request.session())
            );
        }

        String standardizedKeyword = keyword.trim().toLowerCase();

        // TODO: SHOULD BE UPDATED TO SET TO 50 FOR SENTIMENT
        return searchService.searchVideos(standardizedKeyword, NUM_OF_RESULTS_SENTIMENT)
                .thenCompose(videos -> {
                    // Limit to top 10 videos
                    List<Video> top10Videos = videos.stream().limit(DEFAULT_NUM_OF_RESULTS).collect(Collectors.toList());

                    // Add only top 10 videos to the search history
                    searchService.addSearchResultToHistory(getSessionId(request), standardizedKeyword, top10Videos);

                    // Calculate individual sentiments
                    CompletionStage<Map<String, String>> individualSentimentsCombined = searchService.calculateSentiments(getSessionId(request));

                    return individualSentimentsCombined.thenApply(individualSentiments -> {
                        // Retrieve the entire search history for the session
                        Map<String, List<Video>> searchHistory = searchService.getSearchHistory(getSessionId(request));

                        // Pass the entire search history to the view
                        Result result = ok(views.html.searchResults.render(
                                searchHistory,
                                null,
                                individualSentiments,
                                standardizedKeyword
                        ));

                        return addSessionId(request, result);
                    });
                }).exceptionally(ex -> {
                    ex.printStackTrace();
                    return internalServerError(views.html.errorPage.render("An error occurred while fetching search results."));
                });
    }
}
