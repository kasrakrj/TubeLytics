package models.services;

import models.entities.Tag;
import models.entities.Video;

import java.util.List;
import java.util.concurrent.CompletionStage;

public interface TagsService {
    public CompletionStage<List<Tag>> getTagsByVideo(Video video);
}
