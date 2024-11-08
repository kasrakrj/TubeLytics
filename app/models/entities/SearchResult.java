package models.entities;

import java.util.List;
import java.util.concurrent.CompletionStage;

public class SearchResult {
    private String Keyword;
    private CompletionStage<List<Video>> Videos;

    public SearchResult(String keyword, CompletionStage<List<Video>> videos) {
        Keyword = keyword;
        Videos = videos;
    }

    public String getKeyword() {
        return Keyword;
    }

    public void setKeyword(String keyword) {
        Keyword = keyword;
    }

    public CompletionStage<List<Video>> getVideos() {
        return Videos;
    }

    public void setVideos(CompletionStage<List<Video>> videos) {
        Videos = videos;
    }
}
