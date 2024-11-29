package actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.google.inject.Inject;
import models.entities.Video;
import models.services.GeneralService;
import models.services.SearchService;

import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * Actor responsible for managing and calculating word statistics from video titles.
 * The actor interacts with the {@link SearchService} to fetch video data and computes word frequencies.
 *
 * Messages handled by this actor:
 * - {@link WordStatMessages.UpdateVideos}: Updates word statistics based on new videos.
 * - {@link WordStatMessages.GetWordStats}: Retrieves the current word statistics.
 */
public class WordStatActor extends AbstractActor {

    private final SearchService searchService;
    private final Map<String, Long> wordStats = new LinkedHashMap<>();
    private final Set<String> processedVideoIds = new HashSet<>(); // To track processed videos

    /**
     * Constructs a {@code WordStatActor} with the specified {@link SearchService}.
     *
     * @param searchService the service used to fetch video data
     */
    @Inject
    public WordStatActor(SearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * Creates a {@link Props} instance for this actor.
     *
     * @param searchService the {@link SearchService} instance to use
     * @return a {@link Props} instance for creating a {@code WordStatActor}
     */
    public static Props props(SearchService searchService) {
        return Props.create(WordStatActor.class, () -> new WordStatActor(searchService));
    }

    /**
     * Defines the behavior of the actor.
     *
     * Supported messages:
     * - {@link WordStatMessages.UpdateVideos}: Triggers an update of word statistics.
     * - {@link WordStatMessages.GetWordStats}: Returns the current word statistics.
     *
     * @return the actor's behavior
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(WordStatMessages.UpdateVideos.class, this::handleUpdateVideos)
                .match(WordStatMessages.GetWordStats.class, msg -> sender().tell(getWordStats(), self()))
                .build();
    }

    /**
     * Handles the {@link WordStatMessages.UpdateVideos} message.
     * Fetches videos for the given keyword and updates word statistics if the video data has changed.
     *
     * @param message the {@link WordStatMessages.UpdateVideos} message containing the keyword
     */
    private void handleUpdateVideos(WordStatMessages.UpdateVideos message) {
        searchService.searchVideos(message.keyword, GeneralService.NUM_OF_RESULTS_WORD_STATS)
                .thenAccept(videos -> {
                    if (hasVideosChanged(videos)) {
                        Map<String, Long> updatedStats = createWordStats(videos);
                        wordStats.clear();
                        wordStats.putAll(updatedStats);
                        setProcessedVideoIds(videos);
                        System.out.println("Updated word statistics for keyword: " + message.keyword);
                    } else {
                        System.out.println("No changes in the latest videos for keyword: " + message.keyword);
                    }
                })
                .exceptionally(ex -> {
                    System.err.println("Error fetching videos for keyword '" + message.keyword + "': " + ex.getMessage());
                    return null;
                });
    }

    /**
     * Checks if the videos retrieved from the search service differ from previously processed videos.
     *
     * @param latestVideos the list of videos retrieved from the search service
     * @return {@code true} if the videos are different, {@code false} otherwise
     */
    private boolean hasVideosChanged(List<Video> latestVideos) {
        Set<String> latestVideoIds = latestVideos.stream()
                .map(Video::getVideoId)
                .collect(Collectors.toSet());
        return !processedVideoIds.equals(latestVideoIds);
    }

    /**
     * Updates the set of processed video IDs with the IDs of the latest videos.
     *
     * @param latestVideos the list of videos retrieved from the search service
     */
    private void setProcessedVideoIds(List<Video> latestVideos) {
        processedVideoIds.clear();
        processedVideoIds.addAll(
                latestVideos.stream()
                        .map(Video::getVideoId)
                        .collect(Collectors.toSet())
        );
    }

    /**
     * Creates a map of word statistics based on the titles of the given videos.
     * Filters out words with a frequency of 1 and sorts the map by frequency in descending order.
     *
     * @param videos the list of videos whose titles are analyzed
     * @return a sorted map of word frequencies
     */
    public Map<String, Long> createWordStats(List<Video> videos) {
        return videos.stream()
                .map(Video::getTitle)
                .map(title -> title.split(" "))
                .flatMap(Arrays::stream)
                .map(String::toLowerCase)
                .map(word -> word.replaceAll("[^a-zA-Z0-9]", ""))
                .filter(word -> !word.isEmpty())
                .collect(Collectors.groupingBy(word -> word, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .filter(entry -> entry.getValue() > 1)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    /**
     * Retrieves the current word statistics.
     *
     * @return a copy of the word statistics map
     */
    private Map<String, Long> getWordStats() {
        return new LinkedHashMap<>(wordStats);
    }
}
