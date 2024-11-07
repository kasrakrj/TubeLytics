package models.entities;

import java.util.List;
import java.util.concurrent.CompletionStage;

public class Video {
    private String title;
    private String description;
    private String channelTitle;
    private String thumbnailUrl;
    private String videoId;
    private String VideoURL;
    private String VideoTagUrl;
    private CompletionStage<List<Tag>> tags;

    public Video(String videoId) {
        this.videoId = videoId;
    }

    public String getVideoTagUrl() {
        return VideoTagUrl;
    }

    public void setVideoTagUrl(String videoTagUrl) {
        VideoTagUrl = videoTagUrl;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setChannelTitle(String channelTitle) {
        this.channelTitle = channelTitle;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public CompletionStage<List<Tag>> getTags() {
        return tags;
    }

    public void setTags(CompletionStage<List<Tag>> tags) {
        this.tags = tags;
    }

    public Video(String title, String description, String channelTitle, String thumbnailUrl, String videoId, String videoURL) {
        this.title = title;
        this.description = description;
        this.channelTitle = channelTitle;
        this.thumbnailUrl = thumbnailUrl;
        this.videoId = videoId;
        this.VideoURL=videoURL;
    }

    public String getVideoURL() {
        return VideoURL;
    }

    public void setVideoURL(String videoURL) {
        VideoURL = videoURL;
    }

    // Getter methods
    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getChannelTitle() {
        return channelTitle;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getVideoId() {
        return videoId;
    }
}
