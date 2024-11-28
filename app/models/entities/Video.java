package models.entities;

import java.util.Objects;

/**
 * Represents a YouTube video with associated metadata.
 * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
 */
public class Video {
    private String title;
    private String description;
    private String channelTitle;
    private String thumbnailUrl;
    private String videoId;
    private String channelId;
    private String VideoURL;

    private String publishedAt;

    /**
     * Default constructor.
     * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     */
    public Video() {
    }

    /**
     * Constructs a new {@code Video} with the specified details.
     *
     * @param title         the title of the video
     * @param description   the description of the video
     * @param channelTitle  the title of the channel that uploaded the video
     * @param thumbnailUrl  the URL of the video's thumbnail image
     * @param videoId       the unique identifier of the video
     * @param channelId     the unique identifier of the channel
     * @param videoURL      the URL of the video
     * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     */
    public Video(String title, String description, String channelTitle, String thumbnailUrl, String videoId,
                 String channelId, String videoURL, String publishedAt) {
        this.title = title;
        this.description = description;
        this.channelTitle = channelTitle;
        this.thumbnailUrl = thumbnailUrl;
        this.videoId = videoId;
        this.channelId = channelId;
        this.VideoURL = videoURL;
        this.publishedAt = publishedAt;
    }

    /**
     * Returns the title of the video.
     *
     * @return the video's title
     * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the video.
     *
     * @param title the video's title
     * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the description of the video.
     *
     * @return the video's description
     * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the video.
     *
     * @param description the video's description
     * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the title of the channel that uploaded the video.
     *
     * @return the channel's title
     * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     */
    public String getChannelTitle() {
        return channelTitle;
    }

    /**
     * Sets the title of the channel that uploaded the video.
     *
     * @param channelTitle the channel's title
     * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     */
    public void setChannelTitle(String channelTitle) {
        this.channelTitle = channelTitle;
    }

    /**
     * Returns the URL of the video's thumbnail image.
     *
     * @return the thumbnail URL
     * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     */
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    /**
     * Sets the URL of the video's thumbnail image.
     *
     * @param thumbnailUrl the thumbnail URL
     * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     */
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    /**
     * Returns the unique identifier of the video.
     *
     * @return the video ID
     * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     */
    public String getVideoId() {
        return videoId;
    }

    /**
     * Sets the unique identifier of the video.
     *
     * @param videoId the video ID
     * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     */
    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    /**
     * Returns the unique identifier of the channel.
     *
     * @return the channel ID
     * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     */
    public String getChannelId() {
        return channelId;
    }

    /**
     * Sets the unique identifier of the channel.
     *
     * @param channelId the channel ID
     * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     */
    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    /**
     * Returns the URL of the video.
     *
     * @return the video URL
     * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     */
    public String getVideoURL() {
        return VideoURL;
    }

    /**
     * Sets the URL of the video.
     *
     * @param videoURL the video URL
     * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     */
    public void setVideoURL(String videoURL) {
        VideoURL = videoURL;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Video video = (Video) o;
        return videoId.equals(video.videoId) && channelId.equals(video.channelId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(videoId, channelId);
    }

    @Override
    public String toString() {
        return "Video{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", channelTitle='" + channelTitle + '\'' +
                ", thumbnailUrl='" + thumbnailUrl + '\'' +
                ", videoId='" + videoId + '\'' +
                ", channelId='" + channelId + '\'' +
                ", VideoURL='" + VideoURL + '\'' +
                ", publishedAt='" + publishedAt + '\'' +
                '}';
    }
}
