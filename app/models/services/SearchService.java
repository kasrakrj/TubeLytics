package models.services;

import models.entities.SearchQuery;
import java.util.concurrent.CompletionStage;

public interface SearchService {
    CompletionStage<SearchQuery> searchVideos(String keyword);
    public CompletionStage<SearchQuery> fetchSearchQueryAsync(String keyword, String apiKey);
    public SearchQuery fetchSearchQuery(String keyword, String apiKey) throws Exception;
}
