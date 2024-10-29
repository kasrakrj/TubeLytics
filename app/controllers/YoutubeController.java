package controllers;

import models.entities.SearchQuery;
import models.entities.Video;
import models.services.SearchService;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YoutubeController extends Controller {

    private static final Logger logger = LoggerFactory.getLogger(YoutubeController.class);
    private final SearchService searchService;

    @Inject
    public YoutubeController(SearchService searchService) {
        this.searchService = searchService;
    }

    public Result index() {
        return ok(views.html.index.render());
    }

    public CompletionStage<Result> search(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return CompletableFuture.completedFuture(redirect(routes.YoutubeController.index()));
        }

        // Call the SearchService to fetch and process video results
        return searchService.searchVideos(keyword)
                .thenApplyAsync(searchQuery -> {
                    List<Video> videos = searchQuery.getVideos();
                    if (videos == null || videos.isEmpty()) {
                        return ok(views.html.noResults.render(keyword));  // Handle empty search results
                    }

                    // Pass the Java List directly to the Scala template
                    return ok(views.html.searchResults.render(keyword, videos));
                })
                .exceptionally(ex -> {
                    // Log the error and display an error message
                    logger.error("An error occurred while fetching search results.", ex);
                    return internalServerError(
                            views.html.errorPage.render("An error occurred while fetching search results.")
                    );
                });
    }
}
