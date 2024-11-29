package controllers;

import actors.ChannelProfileActor;
import actors.SentimentActor;
import actors.UserActor;
import actors.WordStatActor;
import akka.actor.ActorSystem;
import akka.actor.ActorRef;
import akka.stream.Materializer;
import models.services.*;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.streams.ActorFlow;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.WebSocket;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static models.services.GeneralService.searchHelper;
import static models.services.SessionService.*;

/**
 * The YoutubeController class provides the main entry points for handling user interactions
 * with the YouTube API, including searching for videos, viewing tags, generating word statistics,
 * and retrieving channel profiles. It manages session data and handles asynchronous requests.
 *
 * @author: Zahra Rasoulifar, Hosna Habibi, Mojtaba Peyrovian, Kasra Karaji
 */
public class YoutubeController extends Controller {

    private final SearchService searchService;
    private final WordStatService wordStatService;
    private final ChannelProfileService channelProfileService;
    private final TagsService tagsService;

    private final ActorSystem actorSystem;
    private final Materializer materializer;

    // Actor reference to interact with SentimentActor
    private final ActorRef sentimentActor;
    private final ActorRef channelProfileActor;
    private final ActorRef wordStatActor;

    private final YouTubeService youTubeService;

    /**
     * Constructs a YoutubeController with injected dependencies.
     *
     * @param searchService         The service for searching YouTube videos.
     * @param wordStatService       The service for generating word statistics from video descriptions.
     * @param channelProfileService The service for retrieving YouTube channel profiles.
     * @param tagsService           The service for retrieving tags associated with videos.
     * @param actorSystem
     * @param materializer
     * @param youTubeService
     * @author: Zahra Rasoulifar, Hosna Habibi, Mojtaba Peyrovian, Kasra Karaji
     */
    @Inject
    public YoutubeController(SearchService searchService,
                             WordStatService wordStatService,
                             ChannelProfileService channelProfileService,
                             TagsService tagsService,
                             ActorSystem actorSystem,
                             Materializer materializer,
                             YouTubeService youTubeService,
                             SentimentService sentimentService,
                             HttpExecutionContext httpExecutionContext) {
        this.searchService = searchService;
        this.wordStatService = wordStatService;
        this.channelProfileService = channelProfileService;
        this.tagsService = tagsService;
        this.actorSystem = actorSystem;
        this.materializer = materializer;
        this.youTubeService = youTubeService;

        // Initialize SentimentActor
        this.sentimentActor = actorSystem.actorOf(SentimentActor.props(sentimentService, httpExecutionContext));
        this.channelProfileActor = actorSystem.actorOf(ChannelProfileActor.props(this.youTubeService), "channelProfileActor");
        this.wordStatActor = actorSystem.actorOf(WordStatActor.props(this.searchService), "wordStatActor");
    }

    /**
     * Renders the index page, initializing a session ID if it doesn't exist.
     *
     * @param request The HTTP request from the client.
     * @return CompletionStage of the Result, rendering the index page.
     * @author: Zahra Rasoulifar, Hosna Habibi, Mojtaba Peyrovian, Kasra Karaji
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
     * @author: Zahra Rasoulifar, Hosna Habibi, Mojtaba Peyrovian, Kasra Karaji
     */
    public CompletionStage<Result> tags(String videoID, Http.Request request) {
        return GeneralService.tagHelper(tagsService, videoID, request);
    }

    /**
     * Performs a video search based on a keyword, storing search history and calculating sentiment.
     *
     * @param keyword The keyword to search for.
     * @param request The HTTP request from the client.
     * @return CompletionStage of the Result, rendering the search results page or an error page if an issue occurs.
     * @author: Zahra Rasoulifar, Hosna Habibi, Mojtaba Peyrovian, Kasra Karaji
     */
    public CompletionStage<Result> search(String keyword, Http.Request request) {
        return searchHelper(searchService, keyword, request);
    }

    /**
     * Renders the channel profile page for a given channel ID, including channel information and videos.
     *
     * @param channelId The ID of the YouTube channel.
     * @param request   The HTTP request from the client.
     * @return CompletionStage of the Result, rendering the channel profile page or an error page if an issue occurs.
     * @author: Zahra Rasoulifar, Hosna Habibi, Mojtaba Peyrovian, Kasra Karaji
     */
    public CompletionStage<Result> channelProfile(String channelId, Http.Request request) {
        return GeneralService.channelProfileHelper(channelProfileActor, channelId, request);
    }

    /**
     * Generates word statistics for a given search keyword and displays them on the word statistics page.
     *
     * @param keyword The keyword for which to generate word statistics.
     * @param request The HTTP request from the client.
     * @return CompletionStage of the Result, rendering the word statistics page or an error page if an issue occurs.
     * @author: Zahra Rasoulifar, Hosna Habibi, Mojtaba Peyrovian, Kasra Karaji
     */
    public CompletionStage<Result> wordStats(String keyword, Http.Request request) {
//        return GeneralService.wordStatHelper(searchService, wordStatService, keyword, request);
        return GeneralService.wordStatActorHelper(searchService, wordStatActor, keyword, request);
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
