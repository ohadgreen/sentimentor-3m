package com.acme.model.comment;

public class SentimentSummaryDto {
    private String sentimentObject;
    private int positiveComments;
    private int negativeComments;
    private int neutralComments;
    private int totalCommentsAnalyzed;

    public SentimentSummaryDto(String sentimentObject, int positiveComments, int negativeComments, int neutralComments, int totalCommentsAnalyzed) {
        this.sentimentObject = sentimentObject;
        this.positiveComments = positiveComments;
        this.negativeComments = negativeComments;
        this.neutralComments = neutralComments;
        this.totalCommentsAnalyzed = totalCommentsAnalyzed;
    }

    public String getSentimentObject() {
        return sentimentObject;
    }

    public int getPositiveComments() {
        return positiveComments;
    }

    public int getNegativeComments() {
        return negativeComments;
    }

    public int getNeutralComments() {
        return neutralComments;
    }

    public int getTotalCommentsAnalyzed() {
        return totalCommentsAnalyzed;
    }
}
