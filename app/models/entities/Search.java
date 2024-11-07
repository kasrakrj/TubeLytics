package models.entities;

import java.util.List;
import java.util.concurrent.CompletionStage;

public class Search {
    private String Keyword;
    private String SearchURL;
    private Tag Tag;
    private CompletionStage<List<Video>> Videos;

    public Search(String keyword, CompletionStage<List<Video>> videos,String searchURL) {
        this.Keyword = keyword;
        this.Videos = videos;
        this.SearchURL = searchURL;
    }

    public models.entities.Tag getTag() {
        return Tag;
    }

    public void setTag(models.entities.Tag tag) {
        Tag = tag;
    }

    public String getSearchURL() {
        return SearchURL;
    }

    public void setSearchURL(String searchURL) {
        SearchURL = searchURL;
    }

    public Search(Tag tag, CompletionStage<List<Video>> videos, String searchURL) {
        this.Tag = tag;
        this.Videos = videos;
        this.SearchURL = searchURL;

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
