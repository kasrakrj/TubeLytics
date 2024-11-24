package models.services;

public class ContorllerHelper {
    public static boolean isKeywordValid(String keyword) {
        return keyword != null && !keyword.trim().isEmpty();
    }
}
