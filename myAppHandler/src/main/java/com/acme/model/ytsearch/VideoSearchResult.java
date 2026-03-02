package com.acme.model.ytsearch;

import com.acme.model.ytrawcomment.PageInfo;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class VideoSearchResult {
    private String kind;
    private String etag;
    private String nextPageToken;
    private String regionCode;
    private PageInfo pageInfo;
    @JsonProperty("items")
    private List<VideoSearchItem> items;

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

    public String getNextPageToken() {
        return nextPageToken;
    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    public PageInfo getPageInfo() {
        return pageInfo;
    }

    public void setPageInfo(PageInfo pageInfo) {
        this.pageInfo = pageInfo;
    }

    public List<VideoSearchItem> getItems() {
        return items;
    }

    public void setItems(List<VideoSearchItem> items) {
        this.items = items;
    }
}