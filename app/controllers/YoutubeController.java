package controllers;

import actors.UserActor;
import akka.actor.ActorSystem;
import akka.stream.Materializer;
import akka.stream.javadsl.Flow;
import models.entities.Video;
import models.services.*;
import play.libs.streams.ActorFlow;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import play.mvc.WebSocket;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static models.services.ContorllerHelper.isKeywordValid;
import static models.services.SessionService.*;

/**
 * The YoutubeController class provides the main entry points for handling user interactions
 * with the YouTube API, including searching for videos, viewing tags, generating word statistics,
 * and retrieving channel profiles. It manages session data and handles asynchronous requests.
 *
 * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
 */
public class YoutubeController extends Controller {

    private final int DEFAULT_NUM_OF_RESULTS = 10;
    private final int NUM_OF_RESULTS_SENTIMENT = 50;


    private final SearchService searchService;
    private final WordStatService wordStatService;
    private final ChannelProfileService channelProfileService;
    private final TagsService tagsService;

    private final ActorSystem actorSystem;
    private final Materializer materializer;

    /**
     * Constructs a YoutubeController with injected dependencies.
     *
     * @param searchService         The service for searching YouTube videos.
     * @param wordStatService       The service for generating word statistics from video descriptions.
     * @param channelProfileService The service for retrieving YouTube channel profiles.
     * @param tagsService           The service for retrieving tags associated with videos.
     * @param actorSystem
     * @param materializer
     * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     */
    @Inject
    public YoutubeController(SearchService searchService,
                             WordStatService wordStatService,
                             ChannelProfileService channelProfileService,
                             TagsService tagsService,
                             ActorSystem actorSystem,
                             Materializer materializer) {
        this.searchService = searchService;
        this.wordStatService = wordStatService;
        this.channelProfileService = channelProfileService;
        this.tagsService = tagsService;
        this.actorSystem = actorSystem;
        this.materializer = materializer;
    }

    /**
     * Renders the index page, initializing a session ID if it doesn't exist.
     *
     * @param request The HTTP request from the client.
     * @return CompletionStage of the Result, rendering the index page.
     * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     */
    public CompletionStage<Result> index(Http.Request request) {
        return CompletableFuture.completedFuture(addSessionId(request, ok(views.html.index.render())));
    }

    /**
     * Renders the tags page for a specific video by ID.
     *
     * @param videoID The ID of the video for which to retrieve tags.
     * @param request The HTTP request from the client.
     * @return CompletionStage of the Result, rendering the tags page or an error page if an issue occurs.
     * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     */
    public CompletionStage<Result> tags(String videoID, Http.Request request) {
        return tagsService.getVideoByVideoId(videoID)
                .thenCompose(video ->
                        tagsService.getTagsByVideo(video)
                                .thenApply(tags -> addSessionId(request, ok(views.html.tagsPage.render(video, tags))))
                ).exceptionally(ex -> {
                    ex.printStackTrace();
                    return internalServerError(views.html.errorPage.render("An error occurred while fetching tags."));
                });
    }

    /**
     * Performs a video search based on a keyword, storing search history and calculating sentiment.
     *
     * @param keyword The keyword to search for.
     * @param request The HTTP request from the client.
     * @return CompletionStage of the Result, rendering the search results page or an error page if an issue occurs.
     * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     */
    public CompletionStage<Result> search(String keyword, Http.Request request) {
        if (!isKeywordValid(keyword)) {
            return CompletableFuture.completedFuture(
                    redirect(routes.YoutubeController.index()).withSession(request.session())
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




    /**
     * Renders the channel profile page for a given channel ID, including channel information and videos.
     *
     * @param channelId The ID of the YouTube channel.
     * @param request   The HTTP request from the client.
     * @return CompletionStage of the Result, rendering the channel profile page or an error page if an issue occurs.
     * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     */
    public CompletionStage<Result> channelProfile(String channelId, Http.Request request) {
        return channelProfileService.getChannelInfo(channelId)
                .thenCombine(channelProfileService.getChannelVideos(channelId, 10),
                        (channelInfo, videos) -> addSessionId(request, ok(views.html.channelProfile.render(channelInfo, videos)))
                ).exceptionally(ex -> {
                    ex.printStackTrace();
                    return internalServerError(views.html.errorPage.render("An error occurred while fetching channel profile."));
                });
    }

    /**
     * Generates word statistics for a given search keyword and displays them on the word statistics page.
     *
     * @param keyword The keyword for which to generate word statistics.
     * @param request The HTTP request from the client.
     * @return CompletionStage of the Result, rendering the word statistics page or an error page if an issue occurs.
     * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     */
    public CompletionStage<Result> wordStats(String keyword, Http.Request request) {
        if (!isKeywordValid(keyword)) {
            System.out.println("Keyword is not valid");
            return CompletableFuture.completedFuture(redirect(routes.YoutubeController.index()));
        }

        String standardizedKeyword = keyword.trim().toLowerCase();

        return searchService.searchVideos(standardizedKeyword, NUM_OF_RESULTS_SENTIMENT)
                .thenApply(videos -> {
                    searchService.addSearchResult(getSessionId(request), standardizedKeyword, videos);
                    return addSessionId(request, ok(views.html.wordStats.render(standardizedKeyword, wordStatService.createWordStats(videos))));
                }).exceptionally(ex -> {
                    ex.printStackTrace();
                    return internalServerError(views.html.errorPage.render("An error occurred while fetching word stats."));
                });
    }

    public WebSocket ws() {
        return WebSocket.Text.accept(request -> {
            String sessionId = getSessionIdByHeader(request);
            return ActorFlow.actorRef(
                    out -> UserActor.props(out, searchService, sessionId),
                    actorSystem,
                    materializer
            );
        });
    }



}
