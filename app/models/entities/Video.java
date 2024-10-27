package models.entities;

public class Video {
    public String title;
    public String description;
    public String channelTitle;
    public String thumbnailUrl;
    public String videoId;

    public Video(String title, String description, String channelTitle, String thumbnailUrl, String videoId) {
        this.title = title;
        this.description = description;
        this.channelTitle = channelTitle;
        this.thumbnailUrl = thumbnailUrl;
        this.videoId = videoId;
    }
}
