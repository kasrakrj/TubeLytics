package utils;

import models.entities.Video;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class VideoSorter {

    public static void sortVideosByPublishedAt(List<Video> videos) {
        Collections.sort(videos, new Comparator<Video>() {
            @Override
            public int compare(Video video1, Video video2) {
                ZonedDateTime dateTime1 = ZonedDateTime.parse(video1.getPublishedAt(), DateTimeFormatter.ISO_ZONED_DATE_TIME);
                ZonedDateTime dateTime2 = ZonedDateTime.parse(video2.getPublishedAt(), DateTimeFormatter.ISO_ZONED_DATE_TIME);
                return dateTime2.compareTo(dateTime1); // Newer videos first
            }
        });
    }
}

