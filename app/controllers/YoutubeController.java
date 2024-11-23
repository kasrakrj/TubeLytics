package controllers;

import actors.UserActor;
import akka.actor.ActorSystem;
import akka.stream.Materializer;
import akka.stream.javadsl.Flow;
import models.entities.Video;
import play.libs.streams.ActorFlow;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import models.services.SearchService;
import models.services.WordStatService;
import models.services.ChannelProfileService;
import models.services.TagsService;
import play.mvc.WebSocket;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

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
     * Helper method to retrieve the session ID from the request. If not present, generates a new one.
     *
     * @param request The HTTP request from the client.
     * @return The session ID as a String.
     * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     */
    private String getSessionId(Http.Request request) {
        return request.session().getOptional("sessionId").orElse(null);
    }

    /**
     * Renders the index page, initializing a session ID if it doesn't exist.
     *
     * @param request The HTTP request from the client.
     * @return CompletionStage of the Result, rendering the index page.
     * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
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
                                .thenApply(tags -> ok(views.html.tagsPage.render(video, tags))
                                        .addingToSession(request, "sessionId", getSessionId(request)))
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

        return searchService.searchVideos(standardizedKeyword, NUM_OF_RESULTS_SENTIMENT)
                .thenCompose(videos -> {
                    // Limit to top 10 videos
                    List<Video> top10Videos = videos.stream().limit(DEFAULT_NUM_OF_RESULTS).collect(Collectors.toList());

                    // Add only top 10 videos to the search history
                    searchService.addSearchResultToHistory(finalSessionId, standardizedKeyword, top10Videos);

                    // Calculate individual sentiments
                    CompletionStage<Map<String, String>> individualSentimentsCombined = searchService.calculateSentiments(finalSessionId);

                    return individualSentimentsCombined.thenApply(individualSentiments -> {
                        // Retrieve the entire search history for the session
                        Map<String, List<Video>> searchHistory = searchService.getSearchHistory(finalSessionId);

                        // Pass the entire search history to the view
                        Result result = ok(views.html.searchResults.render(
                                searchHistory,
                                null,
                                individualSentiments,
                                standardizedKeyword
                        ));
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
                        (channelInfo, videos) -> ok(views.html.channelProfile.render(channelInfo, videos))
                                .addingToSession(request, "sessionId", getSessionId(request))
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

    public WebSocket ws() {
        return WebSocket.Text.accept(request -> {
            String sessionId = getSessionId(request);
            return ActorFlow.actorRef(
                    out -> UserActor.props(out, searchService, sessionId),
                    actorSystem,
                    materializer
            );
        });
    }

    private String getSessionId(Http.RequestHeader request) {
        return request.session().getOptional("sessionId").orElse(null);
    }

}
