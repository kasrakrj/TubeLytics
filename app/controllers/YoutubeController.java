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

/**
 * @author: Zahra Rasouli, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
 * The YoutubeController class provides the main entry points for handling user interactions
 * with the YouTube API, including searching for videos, viewing tags, generating word statistics,
 * and retrieving channel profiles. It manages session data and handles asynchronous requests.
 */
public class YoutubeController extends Controller {

    private  final int DEFAULT_NUM_OF_RESULTS = 10;
    private  final int NUM_OF_RESULTS_SENTIMENT = 50;



    private final SearchService searchService;
    private final WordStatService wordStatService;
    private final ChannelProfileService channelProfileService;
    private final TagsService tagsService;

    /**
     * @author: Zahra Rasouli, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     * Constructs a YoutubeController with injected dependencies.
     *
     * @param searchService          The service for searching YouTube videos.
     * @param wordStatService        The service for generating word statistics from video descriptions.
     * @param channelProfileService  The service for retrieving YouTube channel profiles.
     * @param tagsService            The service for retrieving tags associated with videos.
     */
    @Inject
    public YoutubeController(SearchService searchService,
                             WordStatService wordStatService,
                             ChannelProfileService channelProfileService,
                             TagsService tagsService) {
        this.searchService = searchService;
        this.wordStatService = wordStatService;
        this.channelProfileService = channelProfileService;
        this.tagsService = tagsService;
    }

    /**
     * @author: Zahra Rasouli, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     * Helper method to retrieve the session ID from the request. If not present, generates a new one.
     *
     * @param request The HTTP request from the client.
     * @return The session ID as a String.
     */
    private String getSessionId(Http.Request request) {
        return request.session().getOptional("sessionId").orElse(null);
    }

    /**
     * @author: Zahra Rasouli, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     * Renders the index page, initializing a session ID if it doesn't exist.
     *
     * @param request The HTTP request from the client.
     * @return CompletionStage of the Result, rendering the index page.
     */
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

    /**
     * @author: Zahra Rasouli, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     * Renders the tags page for a specific video by ID.
     *
     * @param videoID The ID of the video for which to retrieve tags.
     * @param request The HTTP request from the client.
     * @return CompletionStage of the Result, rendering the tags page or an error page if an issue occurs.
     */
    public CompletionStage<Result> tags(String videoID, Http.Request request) {
        return tagsService.getVideoByVideoId(videoID)
                .thenCompose(video ->
                        tagsService.getTagsByVideo(video)
                                .thenApply(tags -> ok(views.html.tagsPage.render(video, tags))
                                        .addingToSession(request, "sessionId", getSessionId(request)))
                ).exceptionally(ex -> {
                    ex.printStackTrace();
                    return internalServerError(views.html.errorPage.render("An error occurred while fetching tags."));
                });
    }

    /**
     * @author: Zahra Rasouli, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     * Performs a video search based on a keyword, storing search history and calculating sentiment.
     *
     * @param keyword The keyword to search for.
     * @param request The HTTP request from the client.
     * @return CompletionStage of the Result, rendering the search results page or an error page if an issue occurs.
     */
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

    /**
     * @author: Zahra Rasouli, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     * Renders the channel profile page for a given channel ID, including channel information and videos.
     *
     * @param channelId The ID of the YouTube channel.
     * @param request   The HTTP request from the client.
     * @return CompletionStage of the Result, rendering the channel profile page or an error page if an issue occurs.
     */
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

    /**
     * @author: Zahra Rasouli, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     * Generates word statistics for a given search keyword and displays them on the word statistics page.
     *
     * @param keyword The keyword for which to generate word statistics.
     * @param request The HTTP request from the client.
     * @return CompletionStage of the Result, rendering the word statistics page or an error page if an issue occurs.
     */
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
