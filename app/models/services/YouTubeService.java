package models.services;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import models.entities.Video;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class YouTubeService {
    private static final Config config = ConfigFactory.load();
    private static final String API_KEY = config.getString("youtube.api.key");
    private static final String API_URL = config.getString("youtube.api.url");

    public YouTubeService() {
    }

    public String getApiKey() {
        return API_KEY;
    }

    public String getApiUrl() {
        return API_URL;
    }
    private static final String BASE_VIDEO_URL = "https://www.youtube.com/watch?v=";

    public Video parseVideo(JSONObject item) {

        if (!item.isEmpty()) {
            JSONObject snippet = item.getJSONObject("snippet");

            // Extract video details
            String title = snippet.optString("title", "No Title");
            String description = snippet.optString("description", ""); // Optional field
            String channelTitle = snippet.optString("channelTitle", "Unknown Channel");
            String thumbnailUrl = snippet.getJSONObject("thumbnails")
                    .getJSONObject("default")
                    .optString("url", "");
            String channelId = snippet.optString("channelId", "Unknown Channel ID");

            String videoId;

            // Handle different structures of 'id'
            Object idField = item.get("id");
            if (idField instanceof JSONObject) {
                JSONObject idObject = (JSONObject) idField;
                if (idObject.has("videoId")) {
                    videoId = idObject.getString("videoId");
                } else {
                    // Handle cases where 'videoId' is not present
                    return null; // Skip this item or handle accordingly
                }
            } else if (idField instanceof String) {
                videoId = (String) idField;
            } else {
                // 'id' is neither a JSONObject nor a String
                return null; // Skip this item or handle accordingly
            }

            String videoUrl = BASE_VIDEO_URL + videoId;

            // Create and return a Video object with the extracted details
            return new Video(title, description, channelTitle, thumbnailUrl, videoId, channelId, videoUrl);
        }
        return null; // Return null if no items are found
    }
    public List<Video> parseVideos(JSONArray items) {

        List<Video> videos = new ArrayList<>();

        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            // Use parseVideo to extract Video details
            Video video = parseVideo(item);
            videos.add(video);
        }

        return videos;
    }
    // Helper method to parse the JSON array and extract tags using streams
    public List<String> parseTags(JSONArray items) {
        if (items.length() > 0) {
            JSONObject item = items.getJSONObject(0);
            JSONObject snippet = item.getJSONObject("snippet");

            if (snippet.has("tags")) {
                JSONArray tagArray = snippet.getJSONArray("tags");

                // Use streams to map each tag to its encoded URL and collect them into a list
                return IntStream.range(0, tagArray.length())
                        .mapToObj(i ->
                                tagArray.getString(i))
                        .collect(Collectors.toList());
            }
        }

        return List.of(); // Return an empty list if no tags are found
    }
}
