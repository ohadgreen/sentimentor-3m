package com.acme.model.trend;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "trend_analysis_jobs")
public class TrendAnalysisJob implements Persistable<String> {

    @Id
    private String jobId;
    private String userId;
    private String searchQuery;
    private String sentimentObject;
    private int daysBack;
    private int commentsPerVideo;
    private int videosPerDay;
    private TrendJobStatus jobStatus;
    private int daysCompleted;
    private int totalDays;
    private List<DailyTrendResult> dailyResults = new ArrayList<>();
    private String errorMessage;
    @CreatedDate
    private LocalDateTime createDate;
    @LastModifiedDate
    private LocalDateTime updateDate;

    public TrendAnalysisJob() {
    }

    public TrendAnalysisJob(String jobId, String userId, String searchQuery, String sentimentObject,
                            int daysBack, int commentsPerVideo, int videosPerDay) {
        this.jobId = jobId;
        this.userId = userId;
        this.searchQuery = searchQuery;
        this.sentimentObject = sentimentObject;
        this.daysBack = daysBack;
        this.commentsPerVideo = commentsPerVideo;
        this.videosPerDay = videosPerDay;
        this.totalDays = daysBack;
        this.jobStatus = TrendJobStatus.PENDING;
    }

    @Override
    public String getId() {
        return jobId;
    }

    @Override
    public boolean isNew() {
        return createDate == null;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

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

    public TrendJobStatus getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(TrendJobStatus jobStatus) {
        this.jobStatus = jobStatus;
    }

    public int getDaysCompleted() {
        return daysCompleted;
    }

    public void setDaysCompleted(int daysCompleted) {
        this.daysCompleted = daysCompleted;
    }

    public int getTotalDays() {
        return totalDays;
    }

    public void setTotalDays(int totalDays) {
        this.totalDays = totalDays;
    }

    public List<DailyTrendResult> getDailyResults() {
        return dailyResults;
    }

    public void setDailyResults(List<DailyTrendResult> dailyResults) {
        this.dailyResults = dailyResults;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public LocalDateTime getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(LocalDateTime updateDate) {
        this.updateDate = updateDate;
    }
}
