package models.services;

import models.entities.Video;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class WordStatServiceTest {
    private WordStatService wordStatService;

    @Before
    public void setUp() {
        wordStatService = new WordStatService();
    }

    // test to see if the titles are being split correctly
    @Test
    public void testSplitTitle(){
        List<Video> videoList = new ArrayList<>();

        Video video1 = new Video("title1 title2", "description", "channel title", "ThumbnailURL", "id", "channelId", "videoURL");
        Video video2 = new Video("title2 title3", "description", "channel title", "ThumbnailURL", "id", "channelId", "videoURL");

        videoList.add(video1);
        videoList.add(video2);

        Map<String, Long> statMap =  wordStatService.createWordStats(videoList);

        assertEquals("title2", statMap.entrySet().iterator().next().getKey());
    }

    // test to see if the output size is correct
    @Test
    public void testOutputSize(){
        List<Video> videoList = new ArrayList<>();

        Video video1 = new Video("title1 title2", "description", "channel title", "ThumbnailURL", "id", "channelId", "videoURL");
        Video video2 = new Video("title1 title2", "description", "channel title", "ThumbnailURL", "id", "channelId", "videoURL");

        videoList.add(video1);
        videoList.add(video2);

        Map<String, Long> statMap =  wordStatService.createWordStats(videoList);

        assertEquals(2, statMap.size());
    }

    // test to see if single occurrences are removed
    @Test
    public void testSingleOccurrence(){
        List<Video> videoList = new ArrayList<>();

        Video video1 = new Video("title1", "description", "channel title", "ThumbnailURL", "id", "channelId", "videoURL");
        Video video2 = new Video("title2", "description", "channel title", "ThumbnailURL", "id", "channelId", "videoURL");

        videoList.add(video1);
        videoList.add(video2);

        Map<String, Long> statMap =  wordStatService.createWordStats(videoList);

        assertEquals(0, statMap.size());
    }

    // test to see if non-alphanumeric characters are removed
    @Test
    public void testNonAlphaNumericCharacters(){
        List<Video> videoList = new ArrayList<>();

        Video video1 = new Video("(title1)", "description", "channel title", "ThumbnailURL", "id", "channelId", "videoURL");
        Video video2 = new Video("[title1]", "description", "channel title", "ThumbnailURL", "id", "channelId", "videoURL");

        videoList.add(video1);
        videoList.add(video2);

        Map<String, Long> statMap =  wordStatService.createWordStats(videoList);

        assertEquals("title1", statMap.entrySet().iterator().next().getKey());
    }

    // test to see if empty strings are removed
    @Test
    public void testEmptyStrings(){
        List<Video> videoList = new ArrayList<>();

        Video video1 = new Video(" ) ", "description", "channel title", "ThumbnailURL", "id", "channelId", "videoURL");
        Video video2 = new Video(" ) ", "description", "channel title", "ThumbnailURL", "id", "channelId", "videoURL");

        videoList.add(video1);
        videoList.add(video2);

        Map<String, Long> statMap =  wordStatService.createWordStats(videoList);

        assertEquals(0, statMap.size());
    }

    // test to see if the output is sorted correctly
    @Test
    public void testSortedOutput() {
        List<Video> videoList = new ArrayList<>();

        Video video1 = new Video("title1 title2", "description", "channel title", "ThumbnailURL", "id", "channelId", "videoURL");
        Video video2 = new Video("title2 title1", "description", "channel title", "ThumbnailURL", "id", "channelId", "videoURL");
        Video video3 = new Video("title2 title3", "description", "channel title", "ThumbnailURL", "id", "channelId", "videoURL");

        videoList.add(video1);
        videoList.add(video2);
        videoList.add(video3);

        Map<String, Long> statMap =  wordStatService.createWordStats(videoList);

        Iterator<Map.Entry<String, Long>> iterator = statMap.entrySet().iterator();

        assertEquals("title2", iterator.next().getKey());
        assertEquals("title1", iterator.next().getKey());

    }



}

