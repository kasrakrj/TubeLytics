package models.services;

import actors.ChannelProfileMessages;
import actors.SentimentMessages;
import actors.TagMessages;
import actors.WordStatMessages;
import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.pattern.PatternsCS;
import models.entities.Video;
import org.json.JSONObject;
import play.mvc.Http;
import play.mvc.Result;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static models.services.SessionService.addSessionId;
import static models.services.SessionService.getSessionId;
import static play.mvc.Results.*;

public class GeneralService {

    public static final int DEFAULT_NUM_OF_RESULTS = 10;
    public static final int NUM_OF_RESULTS_SENTIMENT = 50;
    public static final int NUM_OF_RESULTS_WORD_STATS = 50;

    public static boolean isKeywordValid(String keyword) {
        return keyword != null && !keyword.trim().isEmpty();
    }

    /**
     * Asynchronously retrieves video and tag information using the TagActor.
     *
     * @param tagActor The actor responsible for fetching tags.
     * @param videoId  The ID of the video.
     * @param request  The HTTP request.
     * @return A CompletionStage containing the Result to render.
     */
    public static CompletionStage<Result> tagHelper(ActorRef tagActor, String videoId, Http.Request request) {
        // Ask the actor for video information
        CompletionStage<Object> videoFuture = Patterns.ask(
                tagActor,
                new TagMessages.GetVideo(videoId),
                Duration.ofSeconds(5)
        );

        // Ask the actor for tags
        CompletionStage<Object> tagsFuture = Patterns.ask(
                tagActor,
                new TagMessages.GetTags(videoId),
                Duration.ofSeconds(5)
        );

        // Combine both futures
        return videoFuture.thenCombine(tagsFuture, (videoResponse, tagsResponse) -> {
            // Check for errors in video response
            if (videoResponse instanceof TagMessages.TagsError) {
                TagMessages.TagsError error = (TagMessages.TagsError) videoResponse;
                return internalServerError(views.html.errorPage.render(error.getErrorMessage()));
            }

            // Check for errors in tags response
            if (tagsResponse instanceof TagMessages.TagsError) {
                TagMessages.TagsError error = (TagMessages.TagsError) tagsResponse;
                return internalServerError(views.html.errorPage.render(error.getErrorMessage()));
            }

            // Check if responses are of correct types
            if (videoResponse instanceof TagMessages.GetVideoResponse && tagsResponse instanceof TagMessages.GetTagsResponse) {
                TagMessages.GetVideoResponse videoResult = (TagMessages.GetVideoResponse) videoResponse;
                TagMessages.GetTagsResponse tagsResult = (TagMessages.GetTagsResponse) tagsResponse;

                Video video = videoResult.getVideo();
                List<String> tags = tagsResult.getTags();

                return addSessionId(request, ok(views.html.tagsPage.render(video, tags)));
            } else {
                return internalServerError(views.html.errorPage.render("An unexpected error occurred."));
            }
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return internalServerError(views.html.errorPage.render("An error occurred while fetching tags."));
        });
    }


    public static CompletionStage<Result> channelProfileHelper(ActorRef channelProfileActor, String channelId, Http.Request request){
        // Ask the actor for channel info
        CompletionStage<Object> channelInfoFuture = Patterns.ask(
                channelProfileActor,
                new ChannelProfileMessages.GetChannelInfo(channelId),
                Duration.ofSeconds(5)
        );

        // Ask the actor for channel videos
        CompletionStage<Object> channelVideosFuture = Patterns.ask(
                channelProfileActor,
                new ChannelProfileMessages.GetChannelVideos(channelId, 10),
                Duration.ofSeconds(5)
        );

        // Combine both futures
        return channelInfoFuture.thenCombine(channelVideosFuture, (infoResponse, videosResponse) -> {
            if (infoResponse instanceof ChannelProfileMessages.ChannelProfileError || videosResponse instanceof ChannelProfileMessages.ChannelProfileError) {
                return internalServerError(views.html.errorPage.render("An error occurred while fetching channel profile."));
            }

            JSONObject channelInfo = ((ChannelProfileMessages.ChannelInfoResponse) infoResponse).getChannelInfo();
            List<Video> videos = ((ChannelProfileMessages.ChannelVideosResponse) videosResponse).getVideos();
            return addSessionId(request, ok(views.html.channelProfile.render(channelInfo, videos)));
        }).exceptionally(ex -> {
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

    public static CompletionStage<Result> wordStatActorHelper(SearchService searchService, ActorRef wordStatActor, String keyword, Http.Request request){
        if (!isKeywordValid(keyword)) {
            System.out.println("Keyword is not valid");
            return CompletableFuture.completedFuture(redirect(controllers.routes.YoutubeController.index()));
        }

        String standardizedKeyword = keyword.trim().toLowerCase();

        // Ask the actor to update videos
        Patterns.ask(
                wordStatActor,
                new WordStatMessages.UpdateVideos(standardizedKeyword),
                Duration.ofSeconds(5)
        );

        return Patterns.ask(wordStatActor, new WordStatMessages.GetWordStats(), Duration.ofSeconds(5))
                .thenApply(response -> {
                    Map<String, Long> wordStats = (Map<String, Long>) response;
                    return addSessionId(request, ok(views.html.wordStats.render(standardizedKeyword, wordStats)));
                }).exceptionally(ex -> {
                    ex.printStackTrace();
                    return internalServerError(views.html.errorPage.render("An error occurred while fetching word stats."));
                });
    }

    public static CompletionStage<Result> searchHelper(SearchService searchService, ActorRef sentimentActor, String keyword, Http.Request request) {
        if (!isKeywordValid(keyword)) {
            return CompletableFuture.completedFuture(
                    redirect(controllers.routes.YoutubeController.index()).withSession(request.session())
            );
        }

        String standardizedKeyword = keyword.trim().toLowerCase();
        String sessionId = getSessionId(request);

        return searchService.searchVideos(standardizedKeyword, NUM_OF_RESULTS_SENTIMENT)
                .thenCompose(videos -> {
                    // Limit to top 10 videos
                    List<Video> top10Videos = videos.stream().limit(DEFAULT_NUM_OF_RESULTS).collect(Collectors.toList());

                    // Add only top 10 videos to the search history
                    searchService.addSearchResult(sessionId, standardizedKeyword, top10Videos);

                    // Ask the sentiment actor to analyze sentiment of the videos
                    return PatternsCS.ask(sentimentActor, new SentimentMessages.AnalyzeVideos(top10Videos), java.time.Duration.ofSeconds(5))
                            .thenCompose(individualResponse -> {
                                @SuppressWarnings("unchecked")
                                Map<String, String> individualSentiments = (Map<String, String>) individualResponse;

                                // Now calculate overall sentiments for all keywords in search history
                                return searchService.calculateSentiments(sessionId)
                                        .thenApply(overallSentiment -> {
                                            // Retrieve the entire search history for the session
                                            Map<String, List<Video>> searchHistory = searchService.getSearchHistory(sessionId);

                                            // Return the search results view with sentiment analysis
                                            Result result = ok(views.html.searchResults.render(
                                                    searchHistory,
                                                    overallSentiment,
                                                    individualSentiments,
                                                    standardizedKeyword
                                            ));

                                            return addSessionId(request, result);
                                        });
                            });
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return internalServerError(views.html.errorPage.render("An error occurred while fetching search results."));
                });
    }

}
