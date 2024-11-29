//package models.services;
//
//import models.entities.Video;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.util.*;
//
//import static org.junit.Assert.assertEquals;
//
///**
// * Unit tests for the WordStatService class.
// * This test class verifies the functionality of word frequency analysis
// * from a list of Video objects, ensuring proper handling of various edge cases.
// * @author: Kasra Karaji
// */
//public class WordStatServiceTest {
//    private WordStatService wordStatService;
//
//    /**
//     * Sets up the WordStatService instance before each test.
//     * @author Kasra Karaji
//     */
//    @Before
//    public void setUp() {
//        wordStatService = new WordStatService();
//    }
//
//    /**
//     * Tests whether the createWordStats method correctly splits video titles
//     * into individual words and identifies common words across multiple titles.
//     * @author Kasra Karaji
//     */
//    @Test
//    public void testSplitTitle() {
//        List<Video> videoList = new ArrayList<>();
//
//        Video video1 = new Video("title1 title2", "description", "channel title", "ThumbnailURL", "id", "channelId", "videoURL");
//        Video video2 = new Video("title2 title3", "description", "channel title", "ThumbnailURL", "id", "channelId", "videoURL");
//
//        videoList.add(video1);
//        videoList.add(video2);
//
//        Map<String, Long> statMap = wordStatService.createWordStats(videoList);
//
//        assertEquals("title2", statMap.entrySet().iterator().next().getKey());
//    }
//
//    /**
//     * Tests whether the createWordStats method produces the correct number
//     * of unique word counts in the output.
//     * @author Kasra Karaji
//     */
//    @Test
//    public void testOutputSize() {
//        List<Video> videoList = new ArrayList<>();
//
//        Video video1 = new Video("title1 title2", "description", "channel title", "ThumbnailURL", "id", "channelId", "videoURL");
//        Video video2 = new Video("title1 title2", "description", "channel title", "ThumbnailURL", "id", "channelId", "videoURL");
//
//        videoList.add(video1);
//        videoList.add(video2);
//
//        Map<String, Long> statMap = wordStatService.createWordStats(videoList);
//
//        assertEquals(2, statMap.size());
//    }
//
//    /**
//     * Tests whether single occurrences of words across multiple titles
//     * are correctly excluded from the output.
//     *  @author Kasra Karaji
//     */
//    @Test
//    public void testSingleOccurrence() {
//        List<Video> videoList = new ArrayList<>();
//
//        Video video1 = new Video("title1", "description", "channel title", "ThumbnailURL", "id", "channelId", "videoURL");
//        Video video2 = new Video("title2", "description", "channel title", "ThumbnailURL", "id", "channelId", "videoURL");
//
//        videoList.add(video1);
//        videoList.add(video2);
//
//        Map<String, Long> statMap = wordStatService.createWordStats(videoList);
//
//        assertEquals(0, statMap.size());
//    }
//
//    /**
//     * Tests whether the createWordStats method correctly removes non-alphanumeric
//     * characters from words in video titles.
//     * @author Kasra Karaji
//     */
//    @Test
//    public void testNonAlphaNumericCharacters() {
//        List<Video> videoList = new ArrayList<>();
//
//        Video video1 = new Video("(title1)", "description", "channel title", "ThumbnailURL", "id", "channelId", "videoURL");
//        Video video2 = new Video("[title1]", "description", "channel title", "ThumbnailURL", "id", "channelId", "videoURL");
//
//        videoList.add(video1);
//        videoList.add(video2);
//
//        Map<String, Long> statMap = wordStatService.createWordStats(videoList);
//
//        assertEquals("title1", statMap.entrySet().iterator().next().getKey());
//    }
//
//    /**
//     * Tests whether empty strings are excluded from the output of the createWordStats method.
//     * @author Kasra Karaji
//     */
//    @Test
//    public void testEmptyStrings() {
//        List<Video> videoList = new ArrayList<>();
//
//        Video video1 = new Video(" ) ", "description", "channel title", "ThumbnailURL", "id", "channelId", "videoURL");
//        Video video2 = new Video(" ) ", "description", "channel title", "ThumbnailURL", "id", "channelId", "videoURL");
//
//        videoList.add(video1);
//        videoList.add(video2);
//
//        Map<String, Long> statMap = wordStatService.createWordStats(videoList);
//
//        assertEquals(0, statMap.size());
//    }
//
//    /**
//     * Tests whether the output of the createWordStats method is sorted by frequency
//     * in descending order.
//     * @author Kasra Karaji
//     */
//    @Test
//    public void testSortedOutput() {
//        List<Video> videoList = new ArrayList<>();
//
//        Video video1 = new Video("title1 title2", "description", "channel title", "ThumbnailURL", "id", "channelId", "videoURL");
//        Video video2 = new Video("title2 title1", "description", "channel title", "ThumbnailURL", "id", "channelId", "videoURL");
//        Video video3 = new Video("title2 title3", "description", "channel title", "ThumbnailURL", "id", "channelId", "videoURL");
//
//        videoList.add(video1);
//        videoList.add(video2);
//        videoList.add(video3);
//
//        Map<String, Long> statMap = wordStatService.createWordStats(videoList);
//
//        Iterator<Map.Entry<String, Long>> iterator = statMap.entrySet().iterator();
//
//        assertEquals("title2", iterator.next().getKey());
//        assertEquals("title1", iterator.next().getKey());
//    }
//}
