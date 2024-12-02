package models.services;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import models.entities.Video;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Inject;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * The YouTubeService class provides utility methods to interact with the YouTube Data API,
 * parse video details, and extract metadata such as tags. It relies on configuration values
 * for API key and URL, which are loaded from an external configuration file.
 *
 * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
 */
public class YouTubeService {
    private static final Config config = ConfigFactory.load();
    private static final String API_KEY = config.getString("youtube.api.key");
    private static final String API_URL = config.getString("youtube.api.url");
    private static final String BASE_VIDEO_URL = "https://www.youtube.com/watch?v=";
    public YouTubeService(){

    }
    /**
     * Retrieves the YouTube API key from the configuration.
     *
     * @return The API key as a String.
     * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     */
    public String getApiKey() {
        return API_KEY;
    }

    /**
     * Retrieves the YouTube API URL from the configuration.
     *
     * @return The API URL as a String.
     */
    public String getApiUrl() {
        return API_URL;
    }

    /**
     * Parses a JSONObject to create a Video object, extracting details such as title, description,
     * channel information, thumbnail URL, and video URL.
     *
     * @param item The JSONObject representing a video item from the YouTube API response.
     * @return A Video object populated with the parsed details or null if the item is empty.
     * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     */
    public Video parseVideo(JSONObject item) {
        if (item.isEmpty()) {
            return null;
        }

        JSONObject snippet = item.optJSONObject("snippet");

        // Extract video details with default values where applicable
        String title = snippet.optString("title", "No Title");
        String description = snippet.optString("description", "");
        String channelTitle = snippet.optString("channelTitle", "Unknown Channel");
        String channelId = snippet.optString("channelId", "Unknown Channel ID");
        String thumbnailUrl = snippet.optJSONObject("thumbnails")
                .optJSONObject("default")
                .optString("url", "");
        String publishedAt = snippet.optString("publishedAt",
                ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT));

        // Handle different types of 'id' structures
        String videoId = null;
        Object idField = item.opt("id");
        if (idField instanceof JSONObject) {
            videoId = ((JSONObject) idField).optString("videoId", null);
        } else if (idField instanceof String) {
            videoId = (String) idField;
        }

        // Construct the video URL
        String videoUrl = BASE_VIDEO_URL + videoId;

        // Create and return the Video object
        return new Video(title, description, channelTitle, thumbnailUrl, videoId, channelId, videoUrl, publishedAt);
    }

    /**
     * Parses a JSONArray containing video items and converts each item into a Video object.
     *
     * @param items A JSONArray of video items from the YouTube API response.
     * @return A list of Video objects parsed from the JSONArray.
     * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     */
    public List<Video> parseVideos(JSONArray items) {
        return IntStream.range(0, items.length())
                .mapToObj(items::getJSONObject)
                .map(this::parseVideo)
                .collect(Collectors.toList());
    }


    /**
     * Parses a JSONArray to extract tags from the first item in the array using Java Streams.
     *
     * @param items A JSONArray of video items, with each item potentially containing tags.
     * @return A list of tags if present, otherwise an empty list.
     * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     */
    public List<String> parseTags(JSONArray items) {
        if (items.length() > 0) {
            JSONObject item = items.getJSONObject(0);

            // Check if 'snippet' exists before accessing it
            if (item.has("snippet")) {
                JSONObject snippet = item.getJSONObject("snippet");

                if (snippet.has("tags")) {
                    JSONArray tagArray = snippet.getJSONArray("tags");

                    // Use streams to map each tag to its encoded URL and collect them into a list
                    return IntStream.range(0, tagArray.length())
                            .mapToObj(tagArray::getString)
                            .collect(Collectors.toList());
                }
            }
        }

        return List.of(); // Return an empty list if no tags are found
    }

}
