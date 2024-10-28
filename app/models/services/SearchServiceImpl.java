package models.services;

import models.dao.VideoRepository;
import models.dao.VideoRepositoryImpl;
import models.entities.Video;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class SearchServiceImpl {

    private static final String API_KEY = "AIzaSyBCYzFvdDbkPslgU8WvAqX_dMk9RHMG1Ug";
    private static final String YOUTUBE_SEARCH_URL = "https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&maxResults=10&q=";

    private final VideoRepository videoRepository;

    public SearchServiceImpl() {
        this.videoRepository = new VideoRepositoryImpl();
    }

    // Method to call the YouTube API and process the response
    public CompletionStage<List<Video>> searchVideos(String keyword) {
        // Encode the keyword to handle spaces and special characters
        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        String apiUrl = YOUTUBE_SEARCH_URL + encodedKeyword + "&key=" + API_KEY;

        return videoRepository.fetchVideosFromApiAsync(apiUrl);
    }
}
