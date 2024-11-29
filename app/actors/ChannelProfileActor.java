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
 *
 * <p>This actor interacts with the YouTube Data API to fetch channel information and videos. It uses
 * {@link YouTubeService} for API configuration and handling the response data.</p>
 *
 * <p>Messages handled by this actor include:
 * <ul>
 *   <li>{@link ChannelProfileMessages.GetChannelInfo} - To retrieve information about a specific channel.</li>
 *   <li>{@link ChannelProfileMessages.GetChannelVideos} - To fetch a list of videos from a specific channel.</li>
 * </ul>
 * </p>
 *
 * @author Zahra Rasoulifar
 */
public class ChannelProfileActor extends AbstractActor {

    private final YouTubeService youTubeService;

    /**
     * Factory method for creating an actor's {@link Props}.
     *
     * @param youTubeService the YouTube service providing API configurations
     * @return the {@link Props} for creating a {@link ChannelProfileActor}
     */
    public static Props props(YouTubeService youTubeService) {
        return Props.create(ChannelProfileActor.class, () -> new ChannelProfileActor(youTubeService));
    }

    /**
     * Constructs a new {@code ChannelProfileActor}.
     *
     * @param youTubeService the YouTube service providing API configurations
     */
    @Inject
    public ChannelProfileActor(YouTubeService youTubeService) {
        this.youTubeService = youTubeService;
    }

    /**
     * Defines the actor's behavior by specifying how it handles incoming messages.
     *
     * @return a {@link Receive} object defining the behavior for the actor
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ChannelProfileMessages.GetChannelInfo.class, this::handleGetChannelInfo)
                .match(ChannelProfileMessages.GetChannelVideos.class, this::handleGetChannelVideos)
                .build();
    }

    /**
     * Handles the {@link ChannelProfileMessages.GetChannelInfo} message.
     *
     * <p>This method sends a request to the YouTube Data API to fetch information about the specified channel.
     * The result is sent back to the sender as either {@link ChannelProfileMessages.ChannelInfoResponse}
     * or {@link ChannelProfileMessages.ChannelProfileError}.</p>
     *
     * @param message the {@link ChannelProfileMessages.GetChannelInfo} message containing the channel ID
     */
    private void handleGetChannelInfo(ChannelProfileMessages.GetChannelInfo message) {
        String channelId = message.getChannelId();
        String youtubeChannelUrl = youTubeService.getApiUrl() + "/channels?part=snippet&id=";
        String apiUrl = youtubeChannelUrl + channelId + "&key=" + youTubeService.getApiKey();
        HttpClient client = createHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl)).build();

        CompletionStage<Object> futureResponse = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .handle((response, ex) -> {
                    if (ex != null) {
                        return new ChannelProfileMessages.ChannelProfileError(ex.getMessage());
                    } else {
                        try {
                            String responseBody = response.body();
                            JSONObject json = new JSONObject(responseBody);
                            JSONObject snippet = json.getJSONArray("items").getJSONObject(0).getJSONObject("snippet");
                            return new ChannelProfileMessages.ChannelInfoResponse(snippet);
                        } catch (Exception e) {
                            return new ChannelProfileMessages.ChannelProfileError(e.getMessage());
                        }
                    }
                });

        pipe(futureResponse, getContext().dispatcher()).to(sender());
    }

    /**
     * Handles the {@link ChannelProfileMessages.GetChannelVideos} message.
     *
     * <p>This method sends a request to the YouTube Data API to fetch a list of videos from the specified channel.
     * The result is sent back to the sender as either {@link ChannelProfileMessages.ChannelVideosResponse}
     * or {@link ChannelProfileMessages.ChannelProfileError}.</p>
     *
     * @param message the {@link ChannelProfileMessages.GetChannelVideos} message containing the channel ID
     *                and the maximum number of videos to retrieve
     */
    private void handleGetChannelVideos(ChannelProfileMessages.GetChannelVideos message) {
        String channelId = message.getChannelId();
        int maxResults = message.getMaxResults();
        String youtubeChannelVideosUrl = youTubeService.getApiUrl() + "/search?part=snippet&order=date&type=video&";
        String apiUrl = youtubeChannelVideosUrl + "channelId=" + channelId + "&maxResults=" + maxResults + "&key=" + youTubeService.getApiKey();
        HttpClient client = createHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl)).build();

        CompletionStage<Object> futureResponse = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .handle((response, ex) -> {
                    if (ex != null) {
                        return new ChannelProfileMessages.ChannelProfileError(ex.getMessage());
                    } else {
                        try {
                            String responseBody = response.body();
                            JSONObject json = new JSONObject(responseBody);
                            JSONArray items = json.getJSONArray("items");
                            List<Video> videos = youTubeService.parseVideos(items);
                            return new ChannelProfileMessages.ChannelVideosResponse(videos);
                        } catch (Exception e) {
                            return new ChannelProfileMessages.ChannelProfileError(e.getMessage());
                        }
                    }
                });

        pipe(futureResponse, getContext().dispatcher()).to(sender());
    }

    /**
     * Creates a new {@link HttpClient} instance.
     *
     * <p>This method provides a way to create an {@link HttpClient} for making HTTP requests.</p>
     *
     * @return a new {@link HttpClient}
     */
    protected HttpClient createHttpClient() {
        return HttpClient.newHttpClient();
    }
}
