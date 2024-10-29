package models.entities;

import java.util.List;

public class SearchQuery {
    private String keyword;
    private List<Video> videos;

    public SearchQuery(String keyword, List<Video> videos) {
        this.keyword = keyword;
        this.videos = videos;
    }

    // Getters and Setters
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public List<Video> getVideos() { return videos; }
    public void setVideos(List<Video> videos) { this.videos = videos; }
}
