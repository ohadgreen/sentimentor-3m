package com.acme.model.ytsearch;

public class VideoDetailsItem {
    private String id;
    private VideoStatistics statistics;
    private VideoSnippet snippet;

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

    public VideoSnippet getSnippet() {
        return snippet;
    }

    public void setSnippet(VideoSnippet snippet) {
        this.snippet = snippet;
    }
}
