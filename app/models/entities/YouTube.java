package models.entities;

public class YouTube {
    private String ApiKey;
    private String SearchURL;

    public YouTube(String apiKey, String searchURL) {
        ApiKey = apiKey;
        SearchURL = searchURL;
    }

    public String getApiKey() {
        return ApiKey;
    }

    public void setApiKey(String apiKey) {
        ApiKey = apiKey;
    }

    public String getSearchURL() {
        return SearchURL;
    }

    public void setSearchURL(String searchURL) {
        SearchURL = searchURL;
    }
}
