package controllers;

import actors.*;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.stream.Materializer;
import models.services.*;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.streams.ActorFlow;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.WebSocket;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static models.services.SessionService.*;

/**
 * The YoutubeController class provides the main entry points for handling user interactions
 * with the YouTube API, including searching for videos, viewing tags, generating word statistics,
 * and retrieving channel profiles. It manages session data and handles asynchronous requests.
 *
 * <p><strong>Key Enhancements:</strong></p>
 * <ul>
 *     <li><strong>Dependency Injection of Actors:</strong> Allows injection of mock actors during testing.</li>
 *     <li><strong>Loose Coupling:</strong> Removes internal actor creation, promoting better testability and maintainability.</li>
 *     <li><strong>Comprehensive Documentation:</strong> Enhances readability and understanding of controller responsibilities.</li>
 * </ul>
 *
 * <p><strong>Authors:</strong> Zahra Rasoulifar, Hosna Habibi, Mojtaba Peyrovian, Kasra Karaji</p>
 */
public class YoutubeController extends Controller {

    // Service Dependencies
    private final SearchService searchService;
    private final WordStatService wordStatService;
    private final ChannelProfileService channelProfileService;
    private final TagsService tagsService;
    private final YouTubeService youTubeService;
    private final SentimentService sentimentService;
    private final HttpExecutionContext httpExecutionContext;

    // Actor Dependencies
    private final ActorRef sentimentActor;
    private final ActorRef channelProfileActor;
    private final ActorRef wordStatActor;
    private final ActorRef tagActor;

    // Actor System and Materializer for WebSocket
    private final ActorSystem actorSystem;
    private final Materializer materializer;

    /**
     * Constructs a YoutubeController with injected dependencies, including ActorRefs for actors.
     * This constructor facilitates dependency injection of mock actors during testing.
     *
     * @param searchService          The service for searching YouTube videos.
     * @param wordStatService        The service for generating word statistics from video descriptions.
     * @param channelProfileService  The service for retrieving YouTube channel profiles.
     * @param tagsService            The service for retrieving tags associated with videos.
     * @param youTubeService         The service for YouTube API interactions.
     * @param sentimentService       The service for sentiment analysis.
     * @param httpExecutionContext   Execution context for asynchronous operations.
     * @param sentimentActor         ActorRef for sentiment analysis.
     * @param channelProfileActor    ActorRef for channel profiling.
     * @param wordStatActor          ActorRef for word statistics.
     * @param tagActor               ActorRef for tag retrieval.
     * @param actorSystem            ActorSystem for creating actors and managing WebSocket connections.
     * @param materializer           Materializer for stream processing, required for WebSocket.
     */
    @Inject
    public YoutubeController(
            SearchService searchService,
            WordStatService wordStatService,
            ChannelProfileService channelProfileService,
            TagsService tagsService,
            YouTubeService youTubeService,
            SentimentService sentimentService,
            HttpExecutionContext httpExecutionContext,
            @Named("sentimentActor") ActorRef sentimentActor,
            @Named("channelProfileActor") ActorRef channelProfileActor,
            @Named("wordStatActor") ActorRef wordStatActor,
            @Named("tagActor") ActorRef tagActor,
            ActorSystem actorSystem,
            Materializer materializer
    ) {
        this.searchService = searchService;
        this.wordStatService = wordStatService;
        this.channelProfileService = channelProfileService;
        this.tagsService = tagsService;
        this.youTubeService = youTubeService;
        this.sentimentService = sentimentService;
        this.httpExecutionContext = httpExecutionContext;
        this.sentimentActor = sentimentActor;
        this.channelProfileActor = channelProfileActor;
        this.wordStatActor = wordStatActor;
        this.tagActor = tagActor;
        this.actorSystem = actorSystem;
        this.materializer = materializer;
    }

    /**
     * Renders the index page, initializing a session ID if it doesn't exist.
     *
     * @param request The HTTP request from the client.
     * @return A CompletionStage containing the Result, rendering the index page.
     */
    public CompletionStage<Result> index(Http.Request request) {
        return CompletableFuture.completedFuture(addSessionId(request, ok(views.html.index.render())));
    }

    /**
     * Renders the tags page for a specific video by its ID.
     *
     * @param videoID The ID of the video for which to retrieve tags.
     * @param request The HTTP request from the client.
     * @return A CompletionStage containing the Result, rendering the tags page or an error page if an issue occurs.
     */
    public CompletionStage<Result> tags(String videoID, Http.Request request) {
        return GeneralService.tagHelper(tagActor, videoID, request);
    }

    /**
     * Performs a video search based on a keyword, storing search history and calculating sentiment.
     *
     * @param keyword The keyword to search for.
     * @param request The HTTP request from the client.
     * @return A CompletionStage containing the Result, rendering the search results page or an error page if an issue occurs.
     */
    public CompletionStage<Result> search(String keyword, Http.Request request) {
        return GeneralService.searchHelper(searchService, sentimentActor, keyword, request);
    }

    /**
     * Renders the channel profile page for a given channel ID, including channel information and videos.
     *
     * @param channelId The ID of the YouTube channel.
     * @param request   The HTTP request from the client.
     * @return A CompletionStage containing the Result, rendering the channel profile page or an error page if an issue occurs.
     */
    public CompletionStage<Result> channelProfile(String channelId, Http.Request request) {
        return GeneralService.channelProfileHelper(channelProfileActor, channelId, request);
    }

    /**
     * Generates word statistics for a given search keyword and displays them on the word statistics page.
     *
     * @param keyword The keyword for which to generate word statistics.
     * @param request The HTTP request from the client.
     * @return A CompletionStage containing the Result, rendering the word statistics page or an error page if an issue occurs.
     */
    public CompletionStage<Result> wordStats(String keyword, Http.Request request) {
        return GeneralService.wordStatActorHelper(searchService, wordStatActor, keyword, request);
    }

    /**
     * Provides a WebSocket endpoint for real-time interactions.
     *
     * @return A WebSocket for client connections.
     */
    public WebSocket ws() {
        return WebSocket.Text.accept(request -> {
            String sessionId = getSessionIdByHeader(request);
            return ActorFlow.actorRef(
                    out -> UserActor.props(out, searchService, sentimentActor, sessionId),
                    actorSystem,
                    materializer
            );
        });
    }
}
