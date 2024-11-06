package models.services;

import models.entities.Search;

public interface SearchService {
    public Search searchVideos(String keyword);
}
