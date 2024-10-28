package models.dao;

import models.entities.Video;

import java.util.List;
import java.util.concurrent.CompletionStage;

public interface VideoRepository {
    public CompletionStage<List<Video>> fetchVideosFromApiAsync(String apiUrl);
}

