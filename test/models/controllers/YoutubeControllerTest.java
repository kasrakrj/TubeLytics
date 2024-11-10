package models.controllers;

import controllers.YoutubeController;
import controllers.routes;
import models.entities.Video;
import models.services.*;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import play.mvc.Result;
import views.html.index;

import javax.validation.constraints.AssertTrue;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static play.mvc.Http.Status.OK;
import static play.mvc.Http.Status.SEE_OTHER;
import static play.test.Helpers.contentAsString;

public class YoutubeControllerTest {

    @Mock
    private SearchService searchService;
    @Mock
    private SentimentService sentimentService;
    @Mock
    private WordStatService wordStatService;
    @Mock
    private ChannelProfileService channelProfileService;
    @Mock
    private TagsService tagsService;

    private YoutubeController youtubeController;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        youtubeController = new YoutubeController(searchService, sentimentService, wordStatService, channelProfileService, tagsService);
    }

    // Test for index status
    @Test
    public void testIndex() throws ExecutionException, InterruptedException {
        CompletionStage<Result> resultCompletionStage = youtubeController.index();

        Result result = resultCompletionStage.toCompletableFuture().get();

        assertEquals(OK, result.status());

        // Assert that the rendered content matches the expected content
        assertEquals(contentAsString(index.render()), contentAsString(result));
    }

    // Test for tags
    @Test
    public void testTags() throws ExecutionException, InterruptedException {
        when(tagsService.getTagsByVideo(any())).thenReturn(CompletableFuture.completedFuture(List.of("tag1", "tag2", "tag3")));

        when(tagsService.getVideoByVideoId(any())).thenReturn(CompletableFuture.completedFuture(new Video("title1 title2", "description", "channel title", "ThumbnailURL", "id", "channelId", "videoURL")));

        CompletionStage<Result> resultCompletionStage = youtubeController.tags("videoID");

        Result result = resultCompletionStage.toCompletableFuture().get();

        assertEquals(OK, result.status());

        assertTrue(contentAsString(result).contains("tag1"));
    }

    // Test for search with empty keyword
    @Test
    public void testSearchWithEmptyKeyword() throws ExecutionException, InterruptedException {
        CompletionStage<Result> resultCompletionStage = youtubeController.search("");

        Result result = resultCompletionStage.toCompletableFuture().get();

        //redirect status code
        assertEquals(SEE_OTHER, result.status());


        // Assert that the redirected page is index.html
        assertEquals(routes.YoutubeController.index().url(), result.redirectLocation().orElse(null));
    }

    // Test for search with null keyword
    @Test
    public void testSearchWithNullKeyword() throws ExecutionException, InterruptedException {
        CompletionStage<Result> resultCompletionStage = youtubeController.search(null);

        Result result = resultCompletionStage.toCompletableFuture().get();

        //redirect status code
        assertEquals(SEE_OTHER, result.status());

        // Assert that the redirected page is index.html
        assertEquals(routes.YoutubeController.index().url(), result.redirectLocation().orElse(null));
    }

    // Test if one search result is stored in the search history
    @Test
    public void testSearchWithValidKeyword() throws ExecutionException, InterruptedException {
        String keyword = "music";
        List<Video> mockVideos = List.of(new Video(
                "Title",
                "Description",
                "Channel title",
                "ThumbnailURL",
                "VideoID",
                "ChannelID",
                "videoURL"));

        when(searchService.searchVideos(keyword, youtubeController.DEFAULT_NUM_OF_RESULTS)).thenReturn(CompletableFuture.completedFuture(mockVideos));
        // Return a dummy CompletableFuture with any value, just to satisfy the function's dependency
        when(sentimentService.avgSentiment(any())).thenReturn(CompletableFuture.completedFuture("dummy value"));
        CompletionStage<Result> resultCompletionStage = youtubeController.search(keyword);

        Result result = resultCompletionStage.toCompletableFuture().get();

        assertEquals(OK, result.status());

        // Verify that the search history contains the keyword
        assertTrue(youtubeController.getSearchHistory().containsKey(keyword));
    }

    // Test if the search result is stored for 10 searches
    @Test
    public void testSearchHistoryLimit() throws ExecutionException, InterruptedException {
        // Fill the search history with 10 searches
        List<Video> mockVideos = new ArrayList<>();
        for (int i = 1; i <= YoutubeController.getMaxSearches(); i++) {
            String keyword = "keyword" + i;
            mockVideos.add(new Video(
                    "Title for " + keyword,
                    "Description for " + keyword,
                    "Channel title for " + keyword,
                    "ThumbnailURL",
                    "VideoID" + i,
                    "ChannelID" + i,
                    "videoURL"
            ));

            when(searchService.searchVideos(keyword, youtubeController.DEFAULT_NUM_OF_RESULTS)).thenReturn(CompletableFuture.completedFuture(mockVideos));
            // Return a dummy CompletableFuture with any value, just to satisfy the function's dependency
            when(sentimentService.avgSentiment(any())).thenReturn(CompletableFuture.completedFuture("sentiment" + i));
            CompletionStage<Result> resultCompletionStage = youtubeController.search(keyword);

            resultCompletionStage.toCompletableFuture().join();
            assertTrue(youtubeController.getSearchHistory().containsKey(keyword));
            assertTrue(youtubeController.getIndividualSentiments().containsKey(keyword));
            assertEquals("sentiment" + i, youtubeController.getIndividualSentiments().get(keyword));

        }

    }

    // Test if the search result  for 11 searches first one should be deleted
    @Test
    public void testSearchHistoryOverLimit() {
        // Fill the search history with 10 searches
        List<Video> mockVideos = new ArrayList<>();
        for (int i = 1; i <= YoutubeController.getMaxSearches()+1; i++) {
            String keyword = "keyword" + i;
            mockVideos.add(new Video(
                    "Title for " + keyword,
                    "Description for " + keyword,
                    "Channel title for " + keyword,
                    "ThumbnailURL",
                    "VideoID" + i,
                    "ChannelID" + i,
                    "videoURL"
            ));

            when(searchService.searchVideos(keyword, youtubeController.DEFAULT_NUM_OF_RESULTS)).thenReturn(CompletableFuture.completedFuture(mockVideos));
            // Return a dummy CompletableFuture with any value, just to satisfy the function's dependency
            when(sentimentService.avgSentiment(any())).thenReturn(CompletableFuture.completedFuture("sentiment" + i));
            CompletionStage<Result> resultCompletionStage = youtubeController.search(keyword);
            resultCompletionStage.toCompletableFuture().join();
        }

        assertFalse(youtubeController.getSearchHistory().containsKey("keyword1"));
        assertFalse(youtubeController.getIndividualSentiments().containsKey("keyword1"));

    }

    // Test for channel profile
    @Test
    public void testChannelProfile() throws ExecutionException, InterruptedException {
        String channelId = "channelID";
        when(channelProfileService.getChannelInfo(channelId)).thenReturn(CompletableFuture.completedFuture(new JSONObject("{ \"title\": \"Channel Title\", \"description\": \"Channel Description\"}")));
        when(channelProfileService.getChannelVideos(channelId, 10)).thenReturn(CompletableFuture.completedFuture(List.of(new Video(
                "Title",
                "Description",
                "Channel title",
                "ThumbnailURL",
                "VideoID",
                "ChannelID",
                "videoURL"))));

        CompletionStage<Result> resultCompletionStage = youtubeController.channelProfile(channelId);

        Result result = resultCompletionStage.toCompletableFuture().get();

        assertEquals(OK, result.status());
    }

    // Test for word stats
//    @Test
//    public void testWordStats() throws ExecutionException, InterruptedException {
//        String keyword = "keyword";
//        List<Video> mockVideos = List.of(new Video(
//                "Title",
//                "Description",
//                "Channel title",
//                "ThumbnailURL",
//                "VideoID",
//                "ChannelID",
//                "videoURL"));
//
//        when(searchService.searchVideos(keyword, youtubeController.NUM_OF_RESULTS_WORD_STATS)).thenReturn(CompletableFuture.completedFuture(mockVideos));
//        when(wordStatService.createWordStats(mockVideos)).thenReturn(Map.of("word1", 2L, "word2", 3L));
//
//        CompletionStage<Result> resultCompletionStage = youtubeController.wordStats(keyword);
//
//        Result result = resultCompletionStage.toCompletableFuture().get();
//
//        assertEquals(OK, result.status());
//
//        assertTrue(result.body().toString().matches(".*word1.*2.*word2.*3.*"));
//    }






}
