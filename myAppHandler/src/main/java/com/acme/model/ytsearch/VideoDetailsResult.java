package com.acme.model.ytsearch;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class VideoDetailsResult {
    @JsonProperty("items")
    private List<VideoDetailsItem> items;

    public List<VideoDetailsItem> getItems() {
        return items;
    }

    public void setItems(List<VideoDetailsItem> items) {
        this.items = items;
    }
}
