package models.services;

import models.dao.SearchQueryRepositoryImpl;
import models.entities.SearchQuery;

import javax.inject.Inject;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletionStage;

public class SearchServiceImpl implements SearchService {

    private final SearchQueryRepositoryImpl searchQueryRepository;
    private static final String API_KEY = "AIzaSyBCYzFvdDbkPslgU8WvAqX_dMk9RHMG1Ug";
    // Encode the keyword to handle spaces and special characters

    @Inject
    public SearchServiceImpl(SearchQueryRepositoryImpl searchQueryRepository) {
        this.searchQueryRepository = searchQueryRepository;
    }


    @Override
    public CompletionStage<SearchQuery> searchVideos(String keyword) {
        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        return searchQueryRepository.fetchSearchQueryAsync(keyword, API_KEY);
    }
}
