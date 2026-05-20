package com.acme.model.trend;

public class TrendAnalysisRequest {
    private String searchQuery;
    private String sentimentObject;
    private int daysBack;
    private int commentsPerVideo;
    private int videosPerDay;

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public String getSentimentObject() {
        return sentimentObject;
    }

    public void setSentimentObject(String sentimentObject) {
        this.sentimentObject = sentimentObject;
    }

    public int getDaysBack() {
        return daysBack;
    }

    public void setDaysBack(int daysBack) {
        this.daysBack = daysBack;
    }

    public int getCommentsPerVideo() {
        return commentsPerVideo;
    }

    public void setCommentsPerVideo(int commentsPerVideo) {
        this.commentsPerVideo = commentsPerVideo;
    }

    public int getVideosPerDay() {
        return videosPerDay;
    }

    public void setVideosPerDay(int videosPerDay) {
        this.videosPerDay = videosPerDay;
    }
}
