package models.services;

import models.entities.Tag;
import models.entities.Video;
import models.entities.YouTube;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class TagsServiceImpl {
    public TagsServiceImpl() {
    }

    public CompletionStage<List<Tag>> getTagsByVideo(Video video) {
        YouTubeService youTubeService = new YouTubeServiceImpl();
        YouTube youTube = youTubeService.youTubeAccess();
        String encodedVideoId = URLEncoder.encode(video.getVideoId(), StandardCharsets.UTF_8);
        String apiUrl = video.getVideoURL() + encodedVideoId + "&key=" + youTube.getApiKey();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl)).build();

        // Asynchronously send the request and process the response to get tags
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    String responseBody = response.body();
                    JSONObject json = new JSONObject(responseBody);
                    JSONArray items = json.getJSONArray("items");
                    SearchServiceImpl searchByTagService =  new SearchServiceImpl();
                    List<Tag> tags = new ArrayList<>();
                    tags = parseTags(items);
                    for(var t: tags){
                        t.setTagURL(searchByTagService.searchVideos(t).getSearchURL());
                    }
                    // Parse the JSON response to extract tags
                    return parseTags(items);
                });

    }

    // Helper method to parse the JSON array and extract tags
    private List<Tag> parseTags(JSONArray items) {
        List<Tag> tags = new ArrayList<>();

        if (items.length() > 0) {
            JSONObject item = items.getJSONObject(0);
            JSONObject snippet = item.getJSONObject("snippet");

            if (snippet.has("tags")) {
                JSONArray tagArray = snippet.getJSONArray("tags");
                for (int i = 0; i < tagArray.length(); i++) {
                    String tagName = tagArray.getString(i);
                    String tagURL = "http://localhost:9000/?tag=" + URLEncoder.encode(tagName, StandardCharsets.UTF_8);
                    tags.add(new Tag(tagURL, tagName));
                }
            }
        }

        return tags;
    }

}
