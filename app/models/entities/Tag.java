package models.entities;

import java.util.List;
import java.util.concurrent.CompletionStage;

public class Tag {
    private String Name;
    private String tagURL;

    public Tag(String name, String tagURL) {
        Name = name;
        this.tagURL = tagURL;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getTagURL() {
        return tagURL;
    }

    public void setTagURL(String tagURL) {
        this.tagURL = tagURL;
    }
}
