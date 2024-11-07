package models.services;

import models.entities.Search;
import models.entities.Tag;
import models.entities.Video;
import models.entities.YouTube;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.net.http.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SearchServiceImpl implements SearchService {

    // Method to call the YouTube API and process the response
    public Search searchVideos(String keyword) {
        YouTubeService youTubeService = new YouTubeServiceImpl();
        YouTube youTube = youTubeService.youTubeAccess();
        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        String apiUrl = youTube.getSearchURL() + encodedKeyword + "&key=" + youTube.getApiKey();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl)).build();

        return new Search(keyword,
                client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenApply(response -> {
                            String responseBody = response.body();
                            JSONObject json = new JSONObject(responseBody);
                            JSONArray items = json.getJSONArray("items");

                            // Parse the items array properly
                            return parseVideos(items);
                        }),apiUrl
        );
    }

    public Search searchVideos(Tag tag) {
        YouTubeService youTubeService = new YouTubeServiceImpl();
        YouTube youTube = youTubeService.youTubeAccess();

        // Encode the tag to use it as a search keyword
        String encodedTag = URLEncoder.encode(tag.getName(), StandardCharsets.UTF_8);
        String apiUrl = youTube.getSearchURL() + encodedTag + "&key=" + youTube.getApiKey();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl)).build();

        // Asynchronously send the request and process the response
        return new Search(tag,
                client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenApply(response -> {
                            String responseBody = response.body();
                            JSONObject json = new JSONObject(responseBody);
                            JSONArray items = json.getJSONArray("items");
                            List<Video> videos = parseVideos(items);
                            // Parse the items array into Video objects
                            return parseVideos(items);}
                        ),apiUrl
        );// Block to get the result synchronously
    }

    // Helper method to parse the JSON array into a list of Video objects
    private List<Video> parseVideos(JSONArray items) {
        List<Video> videos = new ArrayList<>();
        String baseVideoUrl = "https://www.youtube.com/watch?v=";

        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            JSONObject snippet = item.getJSONObject("snippet");

            String title = snippet.getString("title");
            String description = snippet.getString("description");
            String channelTitle = snippet.getString("channelTitle");
            String thumbnailUrl = snippet.getJSONObject("thumbnails").getJSONObject("default").getString("url");
            String videoId = item.getJSONObject("id").getString("videoId");
            String videoUrl = baseVideoUrl + videoId;
            videos.add(new Video(title, description, channelTitle, thumbnailUrl, videoId,videoUrl));
        }

        return videos;
    }
}
