package com.acme.model.ytsearch;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Thumbnails {
    @JsonProperty("default")
    private VideoThumbnail defaultThumbnail;
    private VideoThumbnail medium;
    private VideoThumbnail high;

    public VideoThumbnail getDefaultThumbnail() {
        return defaultThumbnail;
    }

    public void setDefaultThumbnail(VideoThumbnail defaultThumbnail) {
        this.defaultThumbnail = defaultThumbnail;
    }

    public VideoThumbnail getMedium() {
        return medium;
    }

    public void setMedium(VideoThumbnail medium) {
        this.medium = medium;
    }

    public VideoThumbnail getHigh() {
        return high;
    }

    public void setHigh(VideoThumbnail high) {
        this.high = high;
    }
}