package com.acme.model.trend;

import java.util.List;

public class DailyTrendResult {
    private String date; // yyyy-MM-dd
    private int positiveComments;
    private int negativeComments;
    private int neutralComments;
    private int totalCommentsAnalyzed;
    private int videoCount;
    private List<String> videoIds;

    public DailyTrendResult() {
    }

    public DailyTrendResult(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getPositiveComments() {
        return positiveComments;
    }

    public void setPositiveComments(int positiveComments) {
        this.positiveComments = positiveComments;
    }

    public int getNegativeComments() {
        return negativeComments;
    }

    public void setNegativeComments(int negativeComments) {
        this.negativeComments = negativeComments;
    }

    public int getNeutralComments() {
        return neutralComments;
    }

    public void setNeutralComments(int neutralComments) {
        this.neutralComments = neutralComments;
    }

    public int getTotalCommentsAnalyzed() {
        return totalCommentsAnalyzed;
    }

    public void setTotalCommentsAnalyzed(int totalCommentsAnalyzed) {
        this.totalCommentsAnalyzed = totalCommentsAnalyzed;
    }

    public int getVideoCount() {
        return videoCount;
    }

    public void setVideoCount(int videoCount) {
        this.videoCount = videoCount;
    }

    public List<String> getVideoIds() {
        return videoIds;
    }

    public void setVideoIds(List<String> videoIds) {
        this.videoIds = videoIds;
    }
}
