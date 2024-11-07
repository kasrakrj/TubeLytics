package controllers;

import models.entities.Video;
import models.services.TagsServiceImpl;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class TagController extends Controller {

    private final TagsServiceImpl retrieveTagsService;

    @Inject
    public TagController(TagsServiceImpl retrieveTagsService) {
        this.retrieveTagsService = retrieveTagsService;

    }



    public Result index() {
        return ok(views.html.index.render());
    }


    public CompletionStage<Result> tags(String videoID) {
        Video video = new Video(videoID);
        return retrieveTagsService.getTagsByVideo(video)
                .thenApply(tags -> {
                    // Pass keyword, videos, and sentiment to render
                    return ok(views.html.tagsPage.render(video, tags));
                })
                .exceptionally(ex -> {
                    // Log the error and display an error message
                    ex.printStackTrace();
                    return internalServerError(views.html.errorPage.render("An error occurred while fetching tags."));
                });
    }

}

