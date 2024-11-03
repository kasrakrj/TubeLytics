package models;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import models.entities.Video;

public class Sentiment {

    // Happy and Sad keywords lists
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

    // Method to calculate the sentiment of a single video description
    private String CalculateSentiment(String description) {
        long happyCount = HappyWords.stream()
                .filter(description::contains)
                .count();
        long sadCount = SadWords.stream()
                .filter(description::contains)
                .count();
        long totalCount = happyCount + sadCount;

        if (totalCount == 0) {
            return ":-|";
        }

        double happyRatio = (double) happyCount / totalCount;
        double sadRatio = (double) sadCount / totalCount;

        if (happyRatio > 0.7) {
            return ":-)";
        } else if (sadRatio > 0.7) {
            return ":-(";
        } else {
            return ":-|"; // Neutral sentiment
        }
    }

    // Method to calculate the average sentiment for a list of video descriptions
    public String AnalyzeSentiment(List<Video> videos) {
        List<String> sentiments = videos.stream()
                .map(Video::getDescription)
                .map(this::CalculateSentiment)
                .collect(Collectors.toList());

        long happyCount = sentiments.stream().filter(":-)"::equals).count();
        long sadCount = sentiments.stream().filter(":-("::equals).count();
        long totalCount = sentiments.size();
        //System.out.println("Happy Count: " + happyCount);
        //System.out.println("Sad Count: " + sadCount);
        //System.out.println("Total Count: " + totalCount);
        if ((double) happyCount / totalCount > 0.7) {
            return ":-)";
        } else if ((double) sadCount / totalCount > 0.7) {
            return ":-(";
        } else {
            return ":-|";
        }
    }
}
