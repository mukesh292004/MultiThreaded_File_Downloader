package com.example;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class DownloadHistory {
    private final SimpleStringProperty url;
    private final SimpleStringProperty progress;

    public DownloadHistory(String url, String progress) {
        this.url = new SimpleStringProperty(url);
        this.progress = new SimpleStringProperty(progress);
    }

    public String getUrl() {
        return url.get();
    }

    public StringProperty urlProperty() {
        return url;
    }

    public String getProgress() {
        return progress.get();
    }

    public StringProperty progressProperty() {
        return progress;
    }
}

