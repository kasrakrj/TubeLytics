package models.entities;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class SearchHistory {
    private LinkedHashMap<String, CompletionStage<List<Video>>> SearchHistoryMap;

    public SearchHistory(LinkedHashMap<String, CompletionStage<List<Video>>> searchHistoryMap) {
        SearchHistoryMap = searchHistoryMap;
    }

    public LinkedHashMap<String, CompletionStage<List<Video>>> getSearchHistoryMap() {
        return SearchHistoryMap;
    }

    public void setSearchHistoryMap(LinkedHashMap<String, CompletionStage<List<Video>>> searchHistoryMap) {
        SearchHistoryMap = searchHistoryMap;
    }
}
