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

public class WordStatActor extends AbstractActor {

    private final SearchService searchService;
    private final Map<String, Long> wordStats = new LinkedHashMap<>();
    private final Set<String> processedVideoIds = new HashSet<>(); // To track processed videos

    @Inject
    public WordStatActor(SearchService searchService) {
        this.searchService = searchService;
    }

    public static Props props(SearchService searchService) {
        return Props.create(WordStatActor.class, () -> new WordStatActor(searchService));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(WordStatMessages.UpdateVideos.class, this::handleUpdateVideos)
                .match(WordStatMessages.GetWordStats.class, msg -> sender().tell(getWordStats(), self()))
                .build();
    }

    private void handleUpdateVideos(WordStatMessages.UpdateVideos message) {
        searchService.searchVideos(message.keyword, GeneralService.NUM_OF_RESULTS_WORD_STATS)
                .thenAccept(videos -> {
                    // Check if the latest videos are different
                    if (hasVideosChanged(videos)) {
                        Map<String, Long> updatedStats = createWordStats(videos);

                        // Replace the wordStats map with the new stats
                        wordStats.clear();
                        wordStats.putAll(updatedStats);

                        // Mark these videos as processed
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

    private boolean hasVideosChanged(List<Video> latestVideos) {
        // Extract IDs from the latest videos
        Set<String> latestVideoIds = latestVideos.stream()
                .map(Video::getVideoId)
                .collect(Collectors.toSet());

        // Compare with currently processed IDs
        return !processedVideoIds.equals(latestVideoIds);
    }

    private void setProcessedVideoIds(List<Video> latestVideos) {
        processedVideoIds.clear();
        processedVideoIds.addAll(
                latestVideos.stream()
                        .map(Video::getVideoId)
                        .collect(Collectors.toSet())
        );
    }

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

    private Map<String, Long> getWordStats() {
        return new LinkedHashMap<>(wordStats);
    }
}
