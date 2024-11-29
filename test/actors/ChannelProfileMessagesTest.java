package actors;

import models.entities.Video;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class ChannelProfileMessagesTest {

    @Test
    public void testGetChannelInfo() {
        String channelId = "test-channel-id";
        ChannelProfileMessages.GetChannelInfo message = new ChannelProfileMessages.GetChannelInfo(channelId);
        Assert.assertEquals(channelId, message.getChannelId());
    }

    @Test
    public void testGetChannelVideos() {
        String channelId = "test-channel-id";
        int maxResults = 5;
        ChannelProfileMessages.GetChannelVideos message = new ChannelProfileMessages.GetChannelVideos(channelId, maxResults);
        Assert.assertEquals(channelId, message.getChannelId());
        Assert.assertEquals(maxResults, message.getMaxResults());
    }

    @Test
    public void testChannelInfoResponse() {
        JSONObject channelInfo = new JSONObject();
        channelInfo.put("title", "Channel Title");
        ChannelProfileMessages.ChannelInfoResponse response = new ChannelProfileMessages.ChannelInfoResponse(channelInfo);
        Assert.assertEquals("Channel Title", response.getChannelInfo().getString("title"));
    }

    @Test
    public void testChannelVideosResponse() {
        Video video = new Video(
                "title1",
                "description1",
                "channelTitle1",
                "thumbnailUrl1",
                "videoId1",
                "channelId1",
                "videoURL1",
                "publishedAt1"
        );
        List<Video> videos = List.of(video);
        ChannelProfileMessages.ChannelVideosResponse response = new ChannelProfileMessages.ChannelVideosResponse(videos);
        Assert.assertEquals(1, response.getVideos().size());
        Assert.assertEquals("videoId1", response.getVideos().get(0).getVideoId());
    }

    @Test
    public void testChannelProfileError() {
        String errorMessage = "An error occurred";
        ChannelProfileMessages.ChannelProfileError error = new ChannelProfileMessages.ChannelProfileError(errorMessage);
        Assert.assertEquals(errorMessage, error.getErrorMessage());
    }
}
