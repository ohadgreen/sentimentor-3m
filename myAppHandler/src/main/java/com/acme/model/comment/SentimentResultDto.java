package com.acme.model.comment;

import common.model.analysisrequest.Sentiment;

public class SentimentResultDto {
    private String sentimentObject;
    private Sentiment sentiment;
    private String sentimentReason;

    public SentimentResultDto(String sentimentObject, Sentiment sentiment, String sentimentReason) {
        this.sentimentObject = sentimentObject;
        this.sentiment = sentiment;
        this.sentimentReason = sentimentReason;
    }

    public String getSentimentObject() {
        return sentimentObject;
    }

    public Sentiment getSentiment() {
        return sentiment;
    }

    public String getSentimentReason() {
        return sentimentReason;
    }
}
