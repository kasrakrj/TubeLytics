package controllers;

import models.entities.SearchQuery;
import models.entities.Video;
import models.services.SearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import play.mvc.Result;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static play.mvc.Http.Status.*;

class YoutubeControllerTest {

    @Mock
    private SearchService searchService;

    @InjectMocks
    private YoutubeController youtubeController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testIndex() {
        Result result = youtubeController.index();
        assertEquals(OK, result.status());
        assertEquals("text/html", result.contentType().orElse(""));
    }

    @Test
    void testSearchWithValidKeyword() {
        String keyword = "music";
        List<Video> mockVideos = List.of(new Video(
                "Video Title",
                "Description",
                "Channel Title",
                "http://thumbnail-url.com",
                "VideoId123"));
        SearchQuery mockSearchQuery = new SearchQuery(keyword, mockVideos);

        when(searchService.searchVideos(keyword)).thenReturn(CompletableFuture.completedFuture(mockSearchQuery));

        CompletionStage<Result> resultStage = youtubeController.search(keyword);
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(OK, result.status());
        verify(searchService).searchVideos(keyword);
    }

    @Test
    void testSearchWithEmptyKeyword() {
        CompletionStage<Result> resultStage = youtubeController.search("");
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(SEE_OTHER, result.status());
        assertEquals(routes.YoutubeController.index().url(), result.redirectLocation().orElse(""));
        verify(searchService, never()).searchVideos(anyString());
    }

    @Test
    void testSearchWithNullKeyword() {
        CompletionStage<Result> resultStage = youtubeController.search(null);
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(SEE_OTHER, result.status());
        assertEquals(routes.YoutubeController.index().url(), result.redirectLocation().orElse(""));
        verify(searchService, never()).searchVideos(anyString());
    }

    @Test
    void testSearchWithNoResults() {
        String keyword = "noresults";
        SearchQuery mockSearchQuery = new SearchQuery(keyword, Collections.emptyList());

        when(searchService.searchVideos(keyword)).thenReturn(CompletableFuture.completedFuture(mockSearchQuery));

        CompletionStage<Result> resultStage = youtubeController.search(keyword);
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(OK, result.status());
        verify(searchService).searchVideos(keyword);
    }

    @Test
    void testSearchWithException() {
        String keyword = "error";
        when(searchService.searchVideos(keyword))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Service failure")));

        CompletionStage<Result> resultStage = youtubeController.search(keyword);
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(INTERNAL_SERVER_ERROR, result.status());
        verify(searchService).searchVideos(keyword);
    }
}
