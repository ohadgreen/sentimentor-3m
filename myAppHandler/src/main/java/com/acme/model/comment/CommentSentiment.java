package com.acme.model.comment;

import common.model.analysisrequest.Sentiment;

public class CommentSentiment {
    private String commentId;
    private Sentiment sentiment;
    private String sentimentReason;

    public CommentSentiment() {
    }

    public CommentSentiment(String commentId, Sentiment sentiment, String sentimentReason) {
        this.commentId = commentId;
        this.sentiment = sentiment;
        this.sentimentReason = sentimentReason;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
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
