package common.model.analysisrequest;

import java.time.LocalDateTime;

public class CommentToAnalyze {
    private String commentId;
    private String commentText;
    private Integer likeCount;
    private LocalDateTime publishedAt;
    private Sentiment sentiment;
    private String sentimentReason;

    public CommentToAnalyze() {
    }

    public CommentToAnalyze(String commentId, String commentText) {
        this.commentId = commentId;
        this.commentText = commentText;
    }

    public CommentToAnalyze(String commentId, String commentText, Integer likeCount, LocalDateTime publishedAt) {
        this.commentId = commentId;
        this.commentText = commentText;
        this.likeCount = likeCount;
        this.publishedAt = publishedAt;
    }

    public String getCommentId() {
        return commentId;
    }
    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }
    public String getCommentText() {
        return commentText;
    }
    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
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

    @Override
    public String toString() {
        return "CommentToAnalyze{" +
                "commentId='" + commentId + '\'' +
                ", commentText='" + commentText + '\'' +
                '}';
    }
}
