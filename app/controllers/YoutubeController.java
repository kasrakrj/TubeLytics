package controllers;

import models.services.SearchServiceImpl;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class YoutubeController extends Controller {

    private final SearchServiceImpl youTubeService;

    @Inject
    public YoutubeController(SearchServiceImpl youTubeService) {
        this.youTubeService = youTubeService;
    }

    public Result index() {
        return ok(views.html.index.render());
    }

    public CompletionStage<Result> search(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return CompletableFuture.completedFuture(redirect(routes.YoutubeController.index()));
        }

        // Call the YouTubeService to fetch and process video results
        return youTubeService.searchVideos(keyword)
                .thenApply(videos -> {
                    if (videos.isEmpty()) {
                        return ok(views.html.noResults.render(keyword));  // Handle empty search results
                    }
                    return ok(views.html.searchResults.render(keyword, videos));
                })
                .exceptionally(ex -> {
                    // Log the error and display an error message
                    ex.printStackTrace();
                    return internalServerError(views.html.errorPage.render("An error occurred while fetching search results."));
                });
    }
}
