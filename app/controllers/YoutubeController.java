package controllers;

import models.entities.Video;
import models.services.SearchService;
import models.services.SearchServiceImpl;
import play.mvc.Controller;
import play.mvc.Result;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpClient;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class YoutubeController extends Controller {

    private static final Logger logger = LoggerFactory.getLogger(YoutubeController.class);
    private final SearchService searchService;

    // Constructor without dependency injection
    public YoutubeController() {
        // Instantiate HttpClient manually and pass it to SearchServiceImpl
        HttpClient httpClient = HttpClient.newHttpClient();
        this.searchService = new SearchServiceImpl(httpClient);
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
