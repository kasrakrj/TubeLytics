package models.services;

import models.entities.Video;

import java.util.List;
import java.util.concurrent.CompletionStage;

public interface SearchService {
    public CompletionStage<List<Video>> searchVideos(String keyword);
}
