package models.entities;

/**
 * Represents a YouTube video with associated metadata.
 */
public class Video {
    private String title;
    private String description;
    private String channelTitle;
    private String thumbnailUrl;
    private String videoId;
    private String channelId;
    private String VideoURL;

    /**
     * Default constructor.
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
     */
    public Video(String title, String description, String channelTitle, String thumbnailUrl, String videoId, String channelId, String videoURL) {
        this.title = title;
        this.description = description;
        this.channelTitle = channelTitle;
        this.thumbnailUrl = thumbnailUrl;
        this.videoId = videoId;
        this.channelId = channelId;
        this.VideoURL = videoURL;
    }

    /**
     * Returns the title of the video.
     *
     * @return the video's title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the video.
     *
     * @param title the video's title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the description of the video.
     *
     * @return the video's description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the video.
     *
     * @param description the video's description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the title of the channel that uploaded the video.
     *
     * @return the channel's title
     */
    public String getChannelTitle() {
        return channelTitle;
    }

    /**
     * Sets the title of the channel that uploaded the video.
     *
     * @param channelTitle the channel's title
     */
    public void setChannelTitle(String channelTitle) {
        this.channelTitle = channelTitle;
    }

    /**
     * Returns the URL of the video's thumbnail image.
     *
     * @return the thumbnail URL
     */
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    /**
     * Sets the URL of the video's thumbnail image.
     *
     * @param thumbnailUrl the thumbnail URL
     */
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    /**
     * Returns the unique identifier of the video.
     *
     * @return the video ID
     */
    public String getVideoId() {
        return videoId;
    }

    /**
     * Sets the unique identifier of the video.
     *
     * @param videoId the video ID
     */
    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    /**
     * Returns the unique identifier of the channel.
     *
     * @return the channel ID
     */
    public String getChannelId() {
        return channelId;
    }

    /**
     * Sets the unique identifier of the channel.
     *
     * @param channelId the channel ID
     */
    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    /**
     * Returns the URL of the video.
     *
     * @return the video URL
     */
    public String getVideoURL() {
        return VideoURL;
    }

    /**
     * Sets the URL of the video.
     *
     * @param videoURL the video URL
     */
    public void setVideoURL(String videoURL) {
        VideoURL = videoURL;
    }
}
