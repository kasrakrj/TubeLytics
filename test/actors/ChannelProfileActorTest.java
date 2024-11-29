//package actors;
//
//import akka.actor.ActorRef;
//import akka.actor.ActorSystem;
//import akka.actor.Props;
//import akka.testkit.javadsl.TestKit;
//import models.entities.Video;
//import models.services.YouTubeService;
//import org.json.JSONArray;
//import org.json.JSONObject;
//import org.junit.*;
//import org.mockito.Mockito;
//
//import java.net.http.HttpClient;
//import java.net.http.HttpResponse;
//import java.util.List;
//import java.util.concurrent.CompletableFuture;
//
//public class ChannelProfileActorTest {
//
//    static ActorSystem system;
//
//    @BeforeClass
//    public static void setup() {
//        system = ActorSystem.create("TestSystem");
//    }
//
//    @AfterClass
//    public static void teardown() {
//        TestKit.shutdownActorSystem(system);
//        system = null;
//    }
//
//    /**
//     * Testable version of ChannelProfileActor that allows injecting a mock HttpClient.
//     */
//    public static class TestableChannelProfileActor extends ChannelProfileActor {
//
//        private final HttpClient httpClient;
//
//        public TestableChannelProfileActor(YouTubeService youTubeService, HttpClient httpClient) {
//            super(youTubeService);
//            this.httpClient = httpClient;
//        }
//
//        @Override
//        protected HttpClient createHttpClient() {
//            return httpClient;
//        }
//    }
//
//    @Test
//    public void testHandleGetChannelInfo_Success() {
//        new TestKit(system) {{
//            // Mock YouTubeService
//            YouTubeService youTubeService = Mockito.mock(YouTubeService.class);
//            Mockito.when(youTubeService.getApiUrl()).thenReturn("http://api.youtube.com");
//            Mockito.when(youTubeService.getApiKey()).thenReturn("test-api-key");
//
//            // Mock HttpClient and HttpResponse
//            HttpClient mockHttpClient = Mockito.mock(HttpClient.class);
//            HttpResponse<String> mockHttpResponse = Mockito.mock(HttpResponse.class);
//            String jsonResponse = "{\"items\":[{\"snippet\":{\"title\":\"Channel Title\",\"description\":\"Channel Description\"}}]}";
//            Mockito.when(mockHttpResponse.body()).thenReturn(jsonResponse);
//            CompletableFuture<HttpResponse<String>> futureResponse = CompletableFuture.completedFuture(mockHttpResponse);
//            Mockito.when(mockHttpClient.sendAsync(Mockito.any(), Mockito.any())).thenReturn(futureResponse);
//
//            // Create the actor
//            ActorRef actorRef = system.actorOf(Props.create(TestableChannelProfileActor.class, youTubeService, mockHttpClient));
//
//            // Send GetChannelInfo message
//            String testChannelId = "test-channel-id";
//            actorRef.tell(new ChannelProfileMessages.GetChannelInfo(testChannelId), getRef());
//
//            // Expect a ChannelInfoResponse
//            ChannelProfileMessages.ChannelInfoResponse response = expectMsgClass(ChannelProfileMessages.ChannelInfoResponse.class);
//
//            // Assert that the response contains the expected data
//            JSONObject snippet = response.getChannelInfo();
//            Assert.assertEquals("Channel Title", snippet.getString("title"));
//            Assert.assertEquals("Channel Description", snippet.getString("description"));
//        }};
//    }
//
//    @Test
//    public void testHandleGetChannelInfo_Error() {
//        new TestKit(system) {{
//            // Mock YouTubeService
//            YouTubeService youTubeService = Mockito.mock(YouTubeService.class);
//            Mockito.when(youTubeService.getApiUrl()).thenReturn("http://api.youtube.com");
//            Mockito.when(youTubeService.getApiKey()).thenReturn("test-api-key");
//
//            // Mock HttpClient to throw an exception
//            HttpClient mockHttpClient = Mockito.mock(HttpClient.class);
//            CompletableFuture<HttpResponse<String>> futureResponse = new CompletableFuture<>();
//            futureResponse.completeExceptionally(new RuntimeException("Network error"));
//            Mockito.when(mockHttpClient.sendAsync(Mockito.any(), Mockito.any())).thenReturn(futureResponse);
//
//            // Create the actor
//            ActorRef actorRef = system.actorOf(Props.create(TestableChannelProfileActor.class, youTubeService, mockHttpClient));
//
//            // Send GetChannelInfo message
//            String testChannelId = "test-channel-id";
//            actorRef.tell(new ChannelProfileMessages.GetChannelInfo(testChannelId), getRef());
//
//            // Expect a ChannelProfileError
//            ChannelProfileMessages.ChannelProfileError error = expectMsgClass(ChannelProfileMessages.ChannelProfileError.class);
//
//            // Assert that the error message is correct
//            Assert.assertEquals("Network error", error.getErrorMessage());
//        }};
//    }
//
//    @Test
//    public void testHandleGetChannelVideos_Success() {
//        new TestKit(system) {{
//            // Mock YouTubeService
//            YouTubeService youTubeService = Mockito.mock(YouTubeService.class);
//            Mockito.when(youTubeService.getApiUrl()).thenReturn("http://api.youtube.com");
//            Mockito.when(youTubeService.getApiKey()).thenReturn("test-api-key");
//
//            // Mock video parsing
//            Video video1 = new Video(
//                    "title1",        // title
//                    "description1",  // description
//                    "channelTitle1", // channelTitle
//                    "thumbnailUrl1", // thumbnailUrl
//                    "videoId1",      // videoId
//                    "channelId1",    // channelId
//                    "videoURL1",     // videoURL
//                    "publishedAt1"   // publishedAt
//            );
//
//            Video video2 = new Video(
//                    "title2",
//                    "description2",
//                    "channelTitle2",
//                    "thumbnailUrl2",
//                    "videoId2",
//                    "channelId2",
//                    "videoURL2",
//                    "publishedAt2"
//            );
//
//            List<Video> mockVideos = List.of(video1, video2);
//            Mockito.when(youTubeService.parseVideos(Mockito.any(JSONArray.class))).thenReturn(mockVideos);
//
//            // Mock HttpClient and HttpResponse
//            HttpClient mockHttpClient = Mockito.mock(HttpClient.class);
//            HttpResponse<String> mockHttpResponse = Mockito.mock(HttpResponse.class);
//            String jsonResponse = "{\"items\":[{},{}]}"; // Simplified JSON
//            Mockito.when(mockHttpResponse.body()).thenReturn(jsonResponse);
//            CompletableFuture<HttpResponse<String>> futureResponse = CompletableFuture.completedFuture(mockHttpResponse);
//            Mockito.when(mockHttpClient.sendAsync(Mockito.any(), Mockito.any())).thenReturn(futureResponse);
//
//            // Create the actor
//            ActorRef actorRef = system.actorOf(Props.create(TestableChannelProfileActor.class, youTubeService, mockHttpClient));
//
//            // Send GetChannelVideos message
//            String testChannelId = "test-channel-id";
//            int maxResults = 2;
//            actorRef.tell(new ChannelProfileMessages.GetChannelVideos(testChannelId, maxResults), getRef());
//
//            // Expect a ChannelVideosResponse
//            ChannelProfileMessages.ChannelVideosResponse response = expectMsgClass(ChannelProfileMessages.ChannelVideosResponse.class);
//
//            // Assert that the response contains the expected videos
//            Assert.assertEquals(2, response.getVideos().size());
//            Video resultVideo1 = response.getVideos().get(0);
//            Assert.assertEquals("videoId1", resultVideo1.getVideoId());
//            Assert.assertEquals("title1", resultVideo1.getTitle());
//            Assert.assertEquals("channelId1", resultVideo1.getChannelId());
//            // Add more assertions as needed
//        }};
//    }
//
//    @Test
//    public void testHandleGetChannelVideos_Error() {
//        new TestKit(system) {{
//            // Mock YouTubeService
//            YouTubeService youTubeService = Mockito.mock(YouTubeService.class);
//            Mockito.when(youTubeService.getApiUrl()).thenReturn("http://api.youtube.com");
//            Mockito.when(youTubeService.getApiKey()).thenReturn("test-api-key");
//
//            // Mock HttpClient to throw an exception
//            HttpClient mockHttpClient = Mockito.mock(HttpClient.class);
//            CompletableFuture<HttpResponse<String>> futureResponse = new CompletableFuture<>();
//            futureResponse.completeExceptionally(new RuntimeException("Network error"));
//            Mockito.when(mockHttpClient.sendAsync(Mockito.any(), Mockito.any())).thenReturn(futureResponse);
//
//            // Create the actor
//            ActorRef actorRef = system.actorOf(Props.create(TestableChannelProfileActor.class, youTubeService, mockHttpClient));
//
//            // Send GetChannelVideos message
//            String testChannelId = "test-channel-id";
//            int maxResults = 2;
//            actorRef.tell(new ChannelProfileMessages.GetChannelVideos(testChannelId, maxResults), getRef());
//
//            // Expect a ChannelProfileError
//            ChannelProfileMessages.ChannelProfileError error = expectMsgClass(ChannelProfileMessages.ChannelProfileError.class);
//
//            // Assert that the error message is correct
//            Assert.assertEquals("Network error", error.getErrorMessage());
//        }};
//    }
//
//    @Test
//    public void testUnhandledMessage() {
//        new TestKit(system) {{
//            // Mock YouTubeService
//            YouTubeService youTubeService = Mockito.mock(YouTubeService.class);
//
//            // Create the actor
//            ActorRef actorRef = system.actorOf(Props.create(ChannelProfileActor.class, youTubeService));
//
//            // Send an unhandled message
//            actorRef.tell("Unhandled message", getRef());
//
//            // Expect no message (since unhandled messages are ignored)
//            expectNoMessage();
//        }};
//    }
//}
