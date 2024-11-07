package models.services;

import models.entities.Search;
import models.entities.Tag;

public interface SearchService {
    public Search searchVideos(Tag tag);
    public Search searchVideos(String keyword);
}
