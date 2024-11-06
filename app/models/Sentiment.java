package models;
import java.util.Arrays;
import java.util.List;
import models.entities.Video;
/**
 * This is a class to analyze sentiment from YouTube video descriptions
 * Sentiment would be calculated for each video and the average sentiment of video streams
 * would be shown as the overall sentiment of each search query
 *
 * @author Hosna Habibi
 */
public class Sentiment {

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
    private String calculateSentiment(String description) {
        if (description == null || description.isEmpty()) {
            return ":-|"; // Neutral if description is empty
        }
        //Turning description to lowercase format.
        String LowCaseDesc = description.toLowerCase();
        //Counting happy words of the given description.
        long happyCount = HappyWords.stream()
                .filter(LowCaseDesc::contains)
                .count();
        //Counting sad words of the given description.
        long sadCount = SadWords.stream()
                .filter(LowCaseDesc::contains)
                .count();

        long totalSentimentWords = happyCount + sadCount;

        // If no sentiment words -> return neutral
        if (totalSentimentWords == 0) {
            return ":-|";
        }

        // Calculate ratios
        double happyRatio = (double) happyCount / totalSentimentWords;
        double sadRatio = (double) sadCount / totalSentimentWords;


       /* System.out.println("Description: " + description);
        System.out.println("Happy Count: " + happyCount);
        System.out.println("Sad Count: " + sadCount);
        System.out.println("Total Sentiment Words: " + totalSentimentWords);
        System.out.println("Happy Ratio: " + happyRatio);
        System.out.println("Sad Ratio: " + sadRatio);*/

        // Determine sentiment based on the ratios with given thresholds
        if (happyRatio > 0.7) {
            // System.out.println("Individual: :-)");
            return ":-)";
        } else if (sadRatio > 0.7) {
            // System.out.println("Individual: :-(");
            return ":-(";
        } else {
            // System.out.println("Individual: :-|");
            return ":-|";
        }
    }
    /**
     * Calculate the average sentiment for a list of video descriptions
     *
     * @param videos the list of videos we want to analyze their overall sentiment
     * @return a string representing the sentiment
     */
    public String AnalyzeSentiment(List<Video> videos) {
        if (videos == null || videos.isEmpty()) {
            return ":-|"; // Neutral if no videos are present
        }

        double averageScore = videos.stream()
                .map(Video::getDescription)
                .map(this::calculateSentiment)
                .mapToInt(sentiment -> {
                    switch (sentiment) {
                        case ":-)": return 1;   // Happy
                        case ":-(": return -1;  // Sad
                        default: return 0;      // Neutral
                    }
                })
                .average()
                .orElse(0); // default -> 0

        //System.out.println("Average Score: " + averageScore);

        // Determine the overall sentiment
        if (averageScore > 0) {
            //  System.out.println("Overall: :-)");
            return ":-)"; // Overall Happy
        } else if (averageScore < 0) {
            // System.out.println("Overall: :-(");
            return ":-("; // Overall Sad
        } else {
            // System.out.println("Overall: :-|");
            return ":-|"; // Overall Neutral
        }
    }
}