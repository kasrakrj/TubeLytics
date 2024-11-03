package models.entities;

public class Video {
    private String title;
    private String description;
    private String channelTitle;
    private String thumbnailUrl;
    private String videoId;

    public Video(String title, String description, String channelTitle, String thumbnailUrl, String videoId) {
        this.title = title;
        this.description = description;
        this.channelTitle = channelTitle;
        this.thumbnailUrl = thumbnailUrl;
        this.videoId = videoId;
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
