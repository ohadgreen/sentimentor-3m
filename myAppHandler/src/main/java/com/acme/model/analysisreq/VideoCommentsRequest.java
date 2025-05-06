package com.acme.model.analysisreq;

public class VideoCommentsRequest {
    private String userId;
    private String jobId;
    private String videoId;
    private int totalCommentsRequired;
    private int commentsInPage;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public int getTotalCommentsRequired() {
        return totalCommentsRequired;
    }

    public void setTotalCommentsRequired(int totalCommentsRequired) {
        this.totalCommentsRequired = totalCommentsRequired;
    }

    public int getCommentsInPage() {
        return commentsInPage;
    }

    public void setCommentsInPage(int commentsInPage) {
        this.commentsInPage = commentsInPage;
    }
}
