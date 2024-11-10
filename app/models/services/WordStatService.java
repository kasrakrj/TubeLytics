package models.services;

import models.entities.Video;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service class for generating word-level statistics from a list of Video objects.
 */
public class WordStatService {
    /**
     * Creates a map of word frequencies based on the titles of the given list of videos.
     * This method splits each video title into words, normalizes them to lowercase,
     * removes special characters, and counts the occurrences of each word.
     *
     * @param videos a list of {@link Video} objects from which to generate word statistics.
     * @return a {@link Map} where keys are words (String) and values are their corresponding
     *         frequency counts (Long), sorted in descending order by frequency, with words
     *         that appear only once excluded.
     */
    public Map<String, Long> createWordStats(List<Video> videos){

        return videos.stream().map(Video::getTitle)     // Map the videos to their titles
                .map(string -> string.split(" "))   // Split the string into arrays of words
                .flatMap(Arrays::stream)    // Split the arrays into the stream
                .map(String::toLowerCase)   // Convert to lowercase
                .map(s -> s.replaceAll("[^a-zA-Z0-9]", ""))   // Remove special characters
                .filter(word -> !word.isEmpty())    // Remove empty strings
                .collect(Collectors.groupingBy(word -> word, Collectors.counting())).entrySet().stream()    // Count occurrences of each word
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())     // Sort by count in descending order
                .filter(entry -> entry.getValue() > 1)    // Filter out words that appear only once
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));   // Collect the sorted results into a LinkedHashMap

    }
}
