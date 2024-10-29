package models.services;

import models.entities.SearchQuery;
import java.util.concurrent.CompletionStage;

public interface SearchService {
    CompletionStage<SearchQuery> searchVideos(String keyword);
}
