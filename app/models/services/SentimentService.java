package models.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import models.entities.Video;

/**
 * This is a service class to analyze sentiment from YouTube video descriptions.
 * Sentiment is calculated for each video description and an average sentiment
 * is provided for a list of video descriptions.
 *
 * <p>The sentiment for each description is classified as positive, negative, or neutral
 * based on the occurrence of specific words. The average sentiment is calculated
 * asynchronously, using non-blocking operations.
 *
 * @Author Hosna Habibi
 */
public class SentimentService {

    /**
     * List of keywords indicating positive sentiment.
     */
    private static final List<String> HappyWords = Arrays.asList(
            "happy", "joy", "love", "excited", "amazing", "fantastic", "wonderful",
            "awesome", "delight", "fun", "smile", "smiling", "cheerful", "great",
            "ecstatic", "grateful", "blessed", ":-)", ":)", ":D", "😊", "😃", "😍",
            "🎉", "yay", "hurray", "thrilled", "laugh", "laughing", "content",
            "pleased", "satisfied", "hopeful", "positive", "optimistic", "jubilant",
            "chipper", "blissful", "giddy", "heartwarming", "overjoyed", "vibrant",
            "uplifted", "energetic", "merry", "jovial", "sparkling", "glowing",
            "radiant", "fortunate", "fulfilled", "sunny", "peaceful", "joyful",
            "bubbly", "euphoric", "exhilarated", "lovely", "inspired", "beaming",
            "over the moon", "tickled", "elated", "in high spirits", "laughing out loud",
            "lighthearted", "dream come true", "thankful", "life is good", "cloud nine",
            "blessed and highly favored", "full of life", "rejuvenated", "carefree",
            "laughing face", "celebrate", "cheers", "good vibes", "smiley face",
            "positive energy", "feeling alive", "life is beautiful", "yay!", "happily ever after",
            "beaming with joy", "sunshine", "proud", "high on life", "floating",
            "pumped up", "smiles all around", "warm fuzzies", "cheers to that",
            "rejoicing", "truly blessed", "couldn't be happier", "heart full of joy"
    );

    /**
     * List of keywords indicating negative sentiment.
     */
    private static final List<String> SadWords = Arrays.asList(
            "sad", "unhappy", "depressed", "anxious", "alone", "heartbroken", "disappointed",
            "lonely", "miserable", "melancholy", "gloomy", "hopeless", "grief", "loss",
            "hurt", "broken", "devastated", "dismal", "distressed", "downcast", "troubled",
            "crying", "tears", "upset", "sorry", "regretful", "remorseful", "😞", "😢", "😔",
            "💔", ":( ", ":'(", "defeated", "worried", "nervous", "lost", "helpless",
            "despair", "frustrated", "dissatisfied", "anxiety", "insecure", "isolated",
            "trauma", "burdened", "overwhelmed", "hurtful", "discouraged", "vulnerable",
            "pessimistic", "rejected", "betrayed", "abandoned", "alienated", "grieving",
            "disheartened", "feeling low", "suffering", "downhearted", "agony", "misery",
            "anguish", "pained", "dejected", "crying", "blue", "distraught", "regret",
            "guilt", "sorrow", "tearful", "hopelessness", "lost cause", "broken-hearted",
            "heartache", "forsaken", "lost hope", "grieving loss", "helplessness",
            "aching", "broken spirit", "feeling empty", "isolated", "crushed", "feeling down",
            "burden", "devastation", "low spirits", "disconnected", "abandoned hope",
            "deep sadness", "mourning", "unloved", "disillusioned", "discouraged"
    );

    /**
     * Calculates the sentiment of a description.
     *
     * @param description the text description of a video
     * @return a string representing the sentiment
     */
    public String calculateSentiment(String description) {
        if (description == null || description.isEmpty()) {
            return ":-|"; // Neutral if description is empty
        }
        String LowCaseDesc = description.toLowerCase();
        long happyCount = HappyWords.stream().filter(LowCaseDesc::contains).count();
        long sadCount = SadWords.stream().filter(LowCaseDesc::contains).count();

        long totalSentimentWords = happyCount + sadCount;
        if (totalSentimentWords == 0) return ":-|";

        double happyRatio = (double) happyCount / totalSentimentWords;
        double sadRatio = (double) sadCount / totalSentimentWords;

        if (happyRatio > 0.7) return ":-)";
        else if (sadRatio > 0.7) return ":-(";
        else return ":-|";
    }

    /**
     * Analyze sentiment asynchronously for a video description.
     *
     * @param description the text description of a video
     * @return a CompletionStage<String> representing the sentiment of the video
     */
    public CompletionStage<String> analyzeAsync(String description) {
        return CompletableFuture.supplyAsync(() -> calculateSentiment(description));
    }

    /**
     * Calculate the average sentiment for a list of video descriptions asynchronously.
     *
     * @param videos the list of videos for which to analyze overall sentiment
     * @return a CompletionStage<String> representing the overall sentiment asynchronously
     */
    public CompletionStage<String> avgSentiment(List<Video> videos) {
        if (videos == null || videos.isEmpty()) {
            return CompletableFuture.completedFuture(":-|"); // Neutral if no videos are present
        }

        // Map each video's description to a future that calculates its sentiment score
        List<CompletionStage<Integer>> sentimentStages = videos.stream()
                .map(video -> CompletableFuture.supplyAsync(() -> calculateSentiment(video.getDescription())))
                .map(future -> future.thenApply(sentiment -> {
                    switch (sentiment) {
                        case ":-)": return 1; // Happy
                        case ":-(": return -1; // Sad
                        default: return 0; // Neutral
                    }
                }))
                .collect(Collectors.toList());

        // Combine all sentiment scores into a single CompletionStage
        CompletionStage<List<Integer>> allSentimentsStage = sequence(sentimentStages);

        // Calculate the average sentiment score once all futures are completed
        return allSentimentsStage.thenApply(sentiments -> {
            double averageScore = sentiments.stream()
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(0);

            if (averageScore > 0) return ":-)"; // Overall Happy
            else if (averageScore < 0) return ":-("; // Overall Sad
            else return ":-|"; // Overall Neutral
        });
    }

    /**
     * Utility method to combine a list of CompletionStage<T> into a single CompletionStage<List<T>>.
     *
     * @param stages List of CompletionStage instances
     * @param <T>    The type of result each CompletionStage produces
     * @return A CompletionStage containing a list of all results
     */
    private <T> CompletionStage<List<T>> sequence(List<CompletionStage<T>> stages) {
        // Start with an already completed empty list
        return stages.stream()
                .reduce(
                        CompletableFuture.completedFuture(new ArrayList<>()), // Initial accumulator
                        (combinedFuture, stage) -> combinedFuture.thenCombine(stage, (list, item) -> {
                            List<T> newList = new ArrayList<>(list);
                            newList.add(item);
                            return newList;
                        }),
                        (f1, f2) -> f1.thenCombine(f2, (list1, list2) -> {
                            List<T> newList = new ArrayList<>(list1);
                            newList.addAll(list2);
                            return newList;
                        })
                );
    }

}
