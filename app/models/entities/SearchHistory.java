package models.entities;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class SearchHistory {
    private CompletionStage<List<SearchResult>> SearchResults;
    private LinkedHashMap<String, CompletionStage<List<Video>>>  SearchMap;

    public SearchHistory(CompletionStage<List<SearchResult>> searchResults, LinkedHashMap<String, CompletionStage<List<Video>>> searchMap) {
        SearchResults = searchResults;
        SearchMap = searchMap;
    }

    public CompletionStage<List<SearchResult>> getSearchResults() {
        return SearchResults;
    }

    public void setSearchResults(CompletionStage<List<SearchResult>> searchResults) {
        SearchResults = searchResults;
    }

    public LinkedHashMap<String, CompletionStage<List<Video>>> getSearchMap() {
        return SearchMap;
    }

    public void setSearchMap(LinkedHashMap<String, CompletionStage<List<Video>>> searchMap) {
        SearchMap = searchMap;
    }
}
