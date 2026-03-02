package com.acme.model.ytsearch;

public class VideoDetailsItem {
    private String id;
    private VideoStatistics statistics;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public VideoStatistics getStatistics() {
        return statistics;
    }

    public void setStatistics(VideoStatistics statistics) {
        this.statistics = statistics;
    }
}
