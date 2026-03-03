package com.acme.model.comment;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;

public class VideoSummaryDto {
    private String videoId;
    private String videoTitle;
    private String description;
    private LinkedHashMap<String, Integer> wordsFrequency;
    private String viewCount;
    private String likeCount;
    private String commentCount;
    private String defaultThumbnailUrl;
    private List<SentimentSummaryDto> sentimentAnalyses;
    private String publishTime;
    private LocalDateTime createDate;

    public VideoSummaryDto() {
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getVideoTitle() {
        return videoTitle;
    }

    public void setVideoTitle(String videoTitle) {
        this.videoTitle = videoTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LinkedHashMap<String, Integer> getWordsFrequency() {
        return wordsFrequency;
    }

    public void setWordsFrequency(LinkedHashMap<String, Integer> wordsFrequency) {
        this.wordsFrequency = wordsFrequency;
    }

    public String getViewCount() {
        return viewCount;
    }

    public void setViewCount(String viewCount) {
        this.viewCount = viewCount;
    }

    public String getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(String likeCount) {
        this.likeCount = likeCount;
    }

    public String getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(String commentCount) {
        this.commentCount = commentCount;
    }

    public String getDefaultThumbnailUrl() {
        return defaultThumbnailUrl;
    }

    public void setDefaultThumbnailUrl(String defaultThumbnailUrl) {
        this.defaultThumbnailUrl = defaultThumbnailUrl;
    }

    public List<SentimentSummaryDto> getSentimentAnalyses() {
        return sentimentAnalyses;
    }

    public void setSentimentAnalyses(List<SentimentSummaryDto> sentimentAnalyses) {
        this.sentimentAnalyses = sentimentAnalyses;
    }

    public String getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(String publishTime) {
        this.publishTime = publishTime;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }
}
