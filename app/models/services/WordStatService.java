package models.services;

import models.entities.Video;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WordStatService {
    public Map<String, Long> createWordStats(List<Video> videos){

        return videos.stream().map(Video::getTitle).map(string -> string.split(" ")).flatMap(Arrays::stream).map(String::toLowerCase).map(s -> s.replaceAll("[^a-zA-Z0-9]", "")).collect(Collectors.groupingBy(word -> word, Collectors.counting())).entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())      // Sort by count in descending order
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));

    }
}
