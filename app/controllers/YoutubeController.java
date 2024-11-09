package controllers;

import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import models.services.SearchService;
import models.services.SentimentService;
import models.services.WordStatService;
import models.services.ChannelProfileService;
import models.services.TagsService;

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

    /**
     * Helper method to retrieve the session ID. If not present, generate a new one.
     */
    private String getSessionId(Http.Request request) {
        return request.session().getOptional("sessionId").orElse(null);
    }


    public CompletionStage<Result> index(Http.Request request) {
        String sessionId = getSessionId(request);
        if (sessionId == null) {
            sessionId = UUID.randomUUID().toString();
            return CompletableFuture.completedFuture(
                    ok(views.html.index.render())
                            .addingToSession(request, "sessionId", sessionId)
            );
        }
        return CompletableFuture.completedFuture(ok(views.html.index.render()));
    }

    public CompletionStage<Result> tags(String videoID, Http.Request request) {
        // Similar adjustments to handle sessionId if needed
        return tagsService.getVideoByVideoId(videoID)
                .thenCompose(video ->
                        tagsService.getTagsByVideo(video)
                                .thenApply(tags -> ok(views.html.tagsPage.render(video, tags)).addingToSession(request, "sessionId", getSessionId(request)))
                ).exceptionally(ex -> {
                    ex.printStackTrace();
                    return internalServerError(views.html.errorPage.render("An error occurred while fetching tags."));
                });
    }

    public CompletionStage<Result> search(String keyword, Http.Request request) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return CompletableFuture.completedFuture(
                    redirect(routes.YoutubeController.index()).withSession(request.session())
            );
        }

        String standardizedKeyword = keyword.trim().toLowerCase();
        String sessionId = getSessionId(request);

        final boolean isNewSession;
        if (sessionId == null) {
            sessionId = UUID.randomUUID().toString();
            isNewSession = true;
        } else {
            isNewSession = false;
        }

        final String finalSessionId = sessionId;

        return searchService.searchVideos(standardizedKeyword, DEFAULT_NUM_OF_RESULTS)
                .thenCompose(videos -> {
                    searchService.addSearchResultToHistory(finalSessionId, standardizedKeyword, videos);

                    CompletionStage<String> overallSentimentFuture = searchService.calculateOverallSentiment(finalSessionId, NUM_OF_RESULTS_SENTIMENT);
                    CompletionStage<Map<String, String>> individualSentimentsCombined = searchService.calculateIndividualSentiments(finalSessionId);

                    return individualSentimentsCombined.thenCombine(overallSentimentFuture, (individualSentiments, overallSentiment) -> {
                        Result result = ok(views.html.searchResults.render(searchService.getSearchHistory(finalSessionId), overallSentiment, individualSentiments));
                        if (isNewSession) {
                            result = result.addingToSession(request, "sessionId", finalSessionId);
                        }
                        return result;
                    });
                }).exceptionally(ex -> {
                    ex.printStackTrace();
                    return internalServerError(views.html.errorPage.render("An error occurred while fetching search results."));
                });
    }



    public CompletionStage<Result> channelProfile(String channelId, Http.Request request) {
        return channelProfileService.getChannelInfo(channelId)
                .thenCombine(channelProfileService.getChannelVideos(channelId, 10),
                        (channelInfo, videos) -> ok(views.html.channelProfile.render(channelInfo, videos))
                                .addingToSession(request, "sessionId", getSessionId(request))
                ).exceptionally(ex -> {
                    ex.printStackTrace();
                    return internalServerError(views.html.errorPage.render("An error occurred while fetching channel profile."));
                });
    }

    public CompletionStage<Result> wordStats(String keyword, Http.Request request) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return CompletableFuture.completedFuture(redirect(routes.YoutubeController.index()));
        }

        String standardizedKeyword = keyword.trim().toLowerCase();
        String sessionId = getSessionId(request);

        return searchService.searchVideos(standardizedKeyword, NUM_OF_RESULTS_SENTIMENT)
                .thenApply(videos -> {
                    searchService.addSearchResult(sessionId, standardizedKeyword, videos);
                    Map<String, Long> wordStats = wordStatService.createWordStats(videos);
                    return ok(views.html.wordStats.render(standardizedKeyword, wordStats))
                            .addingToSession(request, "sessionId", sessionId);
                }).exceptionally(ex -> {
                    ex.printStackTrace();
                    return internalServerError(views.html.errorPage.render("An error occurred while fetching word stats."));
                });
    }
}
