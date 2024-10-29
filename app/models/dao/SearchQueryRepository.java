package models.dao;

import models.entities.SearchQuery;
import models.entities.Video;

import java.util.List;
import java.util.concurrent.CompletionStage;

public interface SearchQueryRepository {
    CompletionStage<SearchQuery> fetchSearchQueryAsync(String keyword, String apiKey);
    SearchQuery fetchSearchQuery(String keyword, String apiKey) throws Exception;
}

