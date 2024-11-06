package models.services;

import models.entities.YouTube;

public class YouTubeServiceImpl implements YouTubeService {
    public YouTube youTubeAccess() {
        String API_KEY = "AIzaSyBCYzFvdDbkPslgU8WvAqX_dMk9RHMG1Ug";
        String YOUTUBE_SEARCH_URL = "https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&maxResults=10&q=";
        return new YouTube(API_KEY, YOUTUBE_SEARCH_URL);
    }

    public YouTubeServiceImpl() {
    }
}
