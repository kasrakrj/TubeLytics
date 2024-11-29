//package models.services;
//
//import models.entities.Video;
//import org.json.JSONArray;
//import org.json.JSONObject;
//import org.junit.Assert;
//import org.junit.Test;
//import org.mockito.Mockito;
//
//import java.net.http.HttpClient;
//import java.net.http.HttpResponse;
//import java.util.List;
//import java.util.concurrent.CompletableFuture;
//
//public class ChannelProfileServiceTest {
//
//    @Test
//    public void testGetChannelInfo_Success() throws Exception {
//        // Mock YouTubeService
//        YouTubeService youTubeService = Mockito.mock(YouTubeService.class);
//        Mockito.when(youTubeService.getApiUrl()).thenReturn("http://api.youtube.com");
//        Mockito.when(youTubeService.getApiKey()).thenReturn("test-api-key");
//
//        // Mock HttpClient and HttpResponse
//        HttpClient mockHttpClient = Mockito.mock(HttpClient.class);
//        HttpResponse<String> mockHttpResponse = Mockito.mock(HttpResponse.class);
//        String jsonResponse = "{\"items\":[{\"snippet\":{\"title\":\"Channel Title\",\"description\":\"Channel Description\"}}]}";
//        Mockito.when(mockHttpResponse.body()).thenReturn(jsonResponse);
//        CompletableFuture<HttpResponse<String>> futureResponse = CompletableFuture.completedFuture(mockHttpResponse);
//        Mockito.when(mockHttpClient.sendAsync(Mockito.any(), Mockito.any())).thenReturn(futureResponse);
//
//        // Create an instance of the service, overriding createHttpClient()
//        ChannelProfileService service = new ChannelProfileService(youTubeService) {
//            @Override
//            protected HttpClient createHttpClient() {
//                return mockHttpClient;
//            }
//        };
//
//        String testChannelId = "test-channel-id";
//        JSONObject result = service.getChannelInfo(testChannelId).toCompletableFuture().get();
//
//        Assert.assertEquals("Channel Title", result.getString("title"));
//        Assert.assertEquals("Channel Description", result.getString("description"));
//    }
//
//    @Test
//    public void testGetChannelInfo_Error() {
//        // Mock YouTubeService
//        YouTubeService youTubeService = Mockito.mock(YouTubeService.class);
//        Mockito.when(youTubeService.getApiUrl()).thenReturn("http://api.youtube.com");
//        Mockito.when(youTubeService.getApiKey()).thenReturn("test-api-key");
//
//        // Mock HttpClient to throw an exception
//        HttpClient mockHttpClient = Mockito.mock(HttpClient.class);
//        CompletableFuture<HttpResponse<String>> futureResponse = new CompletableFuture<>();
//        futureResponse.completeExceptionally(new RuntimeException("Network error"));
//        Mockito.when(mockHttpClient.sendAsync(Mockito.any(), Mockito.any())).thenReturn(futureResponse);
//
//        // Create an instance of the service
//        ChannelProfileService service = new ChannelProfileService(youTubeService) {
//            @Override
//            protected HttpClient createHttpClient() {
//                return mockHttpClient;
//            }
//        };
//
//        String testChannelId = "test-channel-id";
//        try {
//            service.getChannelInfo(testChannelId).toCompletableFuture().get();
//            Assert.fail("Expected an exception");
//        } catch (Exception e) {
//            Assert.assertTrue(e.getCause().getMessage().contains("Network error"));
//        }
//    }
//
//    @Test
//    public void testGetChannelVideos_Success() throws Exception {
//        // Mock YouTubeService
//        YouTubeService youTubeService = Mockito.mock(YouTubeService.class);
//        Mockito.when(youTubeService.getApiUrl()).thenReturn("http://api.youtube.com");
//        Mockito.when(youTubeService.getApiKey()).thenReturn("test-api-key");
//
//        // Assume parseVideos method returns a list of videos from a JSONArray
//        Video video1 = new Video(
//                "title1",
//                "description1",
//                "channelTitle1",
//                "thumbnailUrl1",
//                "videoId1",
//                "channelId1",
//                "videoURL1",
//                "publishedAt1"
//        );
//
//        Video video2 = new Video(
//                "title2",
//                "description2",
//                "channelTitle2",
//                "thumbnailUrl2",
//                "videoId2",
//                "channelId2",
//                "videoURL2",
//                "publishedAt2"
//        );
//
//        List<Video> mockVideos = List.of(video1, video2);
//        Mockito.when(youTubeService.parseVideos(Mockito.any(JSONArray.class))).thenReturn(mockVideos);
//
//        // Mock HttpClient and HttpResponse
//        HttpClient mockHttpClient = Mockito.mock(HttpClient.class);
//        HttpResponse<String> mockHttpResponse = Mockito.mock(HttpResponse.class);
//        String jsonResponse = "{\"items\":[{},{}]}"; // Simplified JSON
//        Mockito.when(mockHttpResponse.body()).thenReturn(jsonResponse);
//        CompletableFuture<HttpResponse<String>> futureResponse = CompletableFuture.completedFuture(mockHttpResponse);
//        Mockito.when(mockHttpClient.sendAsync(Mockito.any(), Mockito.any())).thenReturn(futureResponse);
//
//        // Create an instance of the service
//        ChannelProfileService service = new ChannelProfileService(youTubeService) {
//            @Override
//            protected HttpClient createHttpClient() {
//                return mockHttpClient;
//            }
//        };
//
//        String testChannelId = "test-channel-id";
//        int maxResults = 2;
//        List<Video> result = service.getChannelVideos(testChannelId, maxResults).toCompletableFuture().get();
//
//        Assert.assertEquals(2, result.size());
//        Video resultVideo1 = result.get(0);
//        Assert.assertEquals("videoId1", resultVideo1.getVideoId());
//        Assert.assertEquals("title1", resultVideo1.getTitle());
//        // Add more assertions as needed
//    }
//
//    @Test
//    public void testGetChannelVideos_Error() {
//        // Mock YouTubeService
//        YouTubeService youTubeService = Mockito.mock(YouTubeService.class);
//        Mockito.when(youTubeService.getApiUrl()).thenReturn("http://api.youtube.com");
//        Mockito.when(youTubeService.getApiKey()).thenReturn("test-api-key");
//
//        // Mock HttpClient to throw an exception
//        HttpClient mockHttpClient = Mockito.mock(HttpClient.class);
//        CompletableFuture<HttpResponse<String>> futureResponse = new CompletableFuture<>();
//        futureResponse.completeExceptionally(new RuntimeException("Network error"));
//        Mockito.when(mockHttpClient.sendAsync(Mockito.any(), Mockito.any())).thenReturn(futureResponse);
//
//        // Create an instance of the service
//        ChannelProfileService service = new ChannelProfileService(youTubeService) {
//            @Override
//            protected HttpClient createHttpClient() {
//                return mockHttpClient;
//            }
//        };
//
//        String testChannelId = "test-channel-id";
//        int maxResults = 2;
//        try {
//            service.getChannelVideos(testChannelId, maxResults).toCompletableFuture().get();
//            Assert.fail("Expected an exception");
//        } catch (Exception e) {
//            Assert.assertTrue(e.getCause().getMessage().contains("Network error"));
//        }
//    }
//}
