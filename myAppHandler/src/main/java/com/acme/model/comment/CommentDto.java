package com.acme.model.comment;

import common.model.analysisrequest.Sentiment;

import java.time.LocalDateTime;

public class CommentDto {
    private String text;
    private Integer likeCount;
    private String authorName;
    private String authorProfileImageUrl;
    private LocalDateTime publishedAt;
    private String sentimentObject;
    private Sentiment sentiment;
    private String sentimentReason;

    public CommentDto(String text, Integer likeCount, String authorName, String authorProfileImageUrl, LocalDateTime publishedAt) {
        this.text = text;
        this.likeCount = likeCount;
        this.authorName = authorName;
        this.authorProfileImageUrl = authorProfileImageUrl;
        this.publishedAt = publishedAt;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorProfileImageUrl() {
        return authorProfileImageUrl;
    }

    public void setAuthorProfileImageUrl(String authorProfileImageUrl) {
        this.authorProfileImageUrl = authorProfileImageUrl;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getSentimentObject() {
        return sentimentObject;
    }

    public void setSentimentObject(String sentimentObject) {
        this.sentimentObject = sentimentObject;
    }

    public Sentiment getSentiment() {
        return sentiment;
    }

    public void setSentiment(Sentiment sentiment) {
        this.sentiment = sentiment;
    }

    public String getSentimentReason() {
        return sentimentReason;
    }

    public void setSentimentReason(String sentimentReason) {
        this.sentimentReason = sentimentReason;
    }
}
