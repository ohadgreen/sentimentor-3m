package com.acme.model.comment;

import java.time.LocalDateTime;
import java.util.List;

public class CommentDto {
    private String commentId;
    private String text;
    private Integer likeCount;
    private String authorName;
    private String authorProfileImageUrl;
    private LocalDateTime publishedAt;
    private List<SentimentResultDto> sentimentResults;

    public CommentDto(String text, Integer likeCount, String authorName, String authorProfileImageUrl, LocalDateTime publishedAt) {
        this.text = text;
        this.likeCount = likeCount;
        this.authorName = authorName;
        this.authorProfileImageUrl = authorProfileImageUrl;
        this.publishedAt = publishedAt;
    }

    public CommentDto(String commentId, String text, Integer likeCount, String authorName,
                      String authorProfileImageUrl, LocalDateTime publishedAt,
                      List<SentimentResultDto> sentimentResults) {
        this.commentId = commentId;
        this.text = text;
        this.likeCount = likeCount;
        this.authorName = authorName;
        this.authorProfileImageUrl = authorProfileImageUrl;
        this.publishedAt = publishedAt;
        this.sentimentResults = sentimentResults;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
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

    public List<SentimentResultDto> getSentimentResults() {
        return sentimentResults;
    }

    public void setSentimentResults(List<SentimentResultDto> sentimentResults) {
        this.sentimentResults = sentimentResults;
    }
}
