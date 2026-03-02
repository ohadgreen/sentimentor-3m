package com.acme.model.ytsearch;

public class VideoSearchItem {
    private String kind;
    private String etag;
    private VideoSearchId id;
    private VideoSnippet snippet;
    private VideoStatistics statistics;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public VideoSearchId getId() {
        return id;
    }

    public void setId(VideoSearchId id) {
        this.id = id;
    }

    public VideoSnippet getSnippet() {
        return snippet;
    }

    public void setSnippet(VideoSnippet snippet) {
        this.snippet = snippet;
    }

    public VideoStatistics getStatistics() {
        return statistics;
    }

    public void setStatistics(VideoStatistics statistics) {
        this.statistics = statistics;
    }
}