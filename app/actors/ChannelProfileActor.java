package actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import models.entities.Video;
import models.services.YouTubeService;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Inject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static akka.pattern.Patterns.pipe;

/**
 * Akka Actor for handling channel profile operations.
 */
public class ChannelProfileActor extends AbstractActor {

    private final YouTubeService youTubeService;

    // Props method for creating the actor
    public static Props props(YouTubeService youTubeService) {
        return Props.create(ChannelProfileActor.class, () -> new ChannelProfileActor(youTubeService));
    }

    @Inject
    public ChannelProfileActor(YouTubeService youTubeService) {
        this.youTubeService = youTubeService;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ChannelProfileMessages.GetChannelInfo.class, this::handleGetChannelInfo)
                .match(ChannelProfileMessages.GetChannelVideos.class, this::handleGetChannelVideos)
                .build();
    }

    private void handleGetChannelInfo(ChannelProfileMessages.GetChannelInfo message) {
        String channelId = message.getChannelId();
        String youtubeChannelUrl = youTubeService.getApiUrl() + "/search?part=snippet&type=video&";
        String apiUrl = youtubeChannelUrl + "channelId=" + channelId + "&key=" + youTubeService.getApiKey();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl)).build();

        CompletionStage<Object> futureResponse = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .handle((response, ex) -> {
                    if (ex != null) {
                        return new ChannelProfileMessages.ChannelProfileError(ex.getMessage());
                    } else {
                        String responseBody = response.body();
                        JSONObject json = new JSONObject(responseBody);
                        JSONObject snippet = json.getJSONArray("items").getJSONObject(0).getJSONObject("snippet");
                        return new ChannelProfileMessages.ChannelInfoResponse(snippet);
                    }
                });

        pipe(futureResponse, getContext().dispatcher()).to(sender());
    }

    private void handleGetChannelVideos(ChannelProfileMessages.GetChannelVideos message) {
        String channelId = message.getChannelId();
        int maxResults = message.getMaxResults();
        String youtubeChannelVideosUrl = youTubeService.getApiUrl() + "/search?part=snippet&order=date&type=video";
        String apiUrl = youtubeChannelVideosUrl + "channelId=" + channelId + "&maxResults=" + maxResults + "&key=" + youTubeService.getApiKey();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl)).build();

        CompletionStage<Object> futureResponse = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .handle((response, ex) -> {
                    if (ex != null) {
                        return new ChannelProfileMessages.ChannelProfileError(ex.getMessage());
                    } else {
                        String responseBody = response.body();
                        JSONObject json = new JSONObject(responseBody);
                        JSONArray items = json.getJSONArray("items");
                        List<Video> videos = youTubeService.parseVideos(items);
                        return new ChannelProfileMessages.ChannelVideosResponse(videos);
                    }
                });

        pipe(futureResponse, getContext().dispatcher()).to(sender());
    }
}
