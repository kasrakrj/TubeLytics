package controllers;

import models.entities.Video;
import models.services.SearchServiceImpl;
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
    private SearchServiceImpl youTubeService;

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
                "http://video-url.com",
                "http://thumbnail-url.com",
                "VideoId123"));
        when(youTubeService.searchVideos(keyword)).thenReturn(CompletableFuture.completedFuture(mockVideos));

        CompletionStage<Result> resultStage = youtubeController.search(keyword);
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(OK, result.status());
        verify(youTubeService).searchVideos(keyword);
    }

    @Test
    void testSearchWithEmptyKeyword() {
        CompletionStage<Result> resultStage = youtubeController.search("");
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(SEE_OTHER, result.status());
        assertEquals(routes.YoutubeController.index().url(), result.redirectLocation().orElse(""));
        verify(youTubeService, never()).searchVideos(anyString());
    }

    @Test
    void testSearchWithNullKeyword() {
        CompletionStage<Result> resultStage = youtubeController.search(null);
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(SEE_OTHER, result.status());
        assertEquals(routes.YoutubeController.index().url(), result.redirectLocation().orElse(""));
        verify(youTubeService, never()).searchVideos(anyString());
    }

    @Test
    void testSearchWithNoResults() {
        String keyword = "noresults";
        when(youTubeService.searchVideos(keyword)).thenReturn(CompletableFuture.completedFuture(Collections.emptyList()));

        CompletionStage<Result> resultStage = youtubeController.search(keyword);
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(OK, result.status());
        verify(youTubeService).searchVideos(keyword);
    }

    @Test
    void testSearchWithException() {
        String keyword = "error";
        when(youTubeService.searchVideos(keyword)).thenReturn(CompletableFuture.failedFuture(new RuntimeException("Service failure")));

        CompletionStage<Result> resultStage = youtubeController.search(keyword);
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(INTERNAL_SERVER_ERROR, result.status());
        verify(youTubeService).searchVideos(keyword);
    }
}
