package com.acme.model.comment;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

@Document(collection = "video_comments_summary")
public class VideoCommentsSummary implements Serializable, Persistable<String> {
    @Id
    private String videoId;
    private String videoTitle;
    private String jobId;
    private int totalComments;
    private LinkedHashMap<String, Integer> wordsFrequency;
    private Map<UUID, CommentSentimentSummary> sentimentAnalysisStatusMap = new HashMap<>();
    @CreatedDate
    private LocalDateTime createDate;
    @LastModifiedDate
    private LocalDateTime updateDate;

    public VideoCommentsSummary() {
    }

    public VideoCommentsSummary(String jobId, String videoId, String videoTitle, int totalComments, LinkedHashMap<String, Integer> wordsFrequency) {
        this.jobId = jobId;
        this.videoId = videoId;
        this.videoTitle = videoTitle;
        this.totalComments = totalComments;
        this.wordsFrequency = wordsFrequency;
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
    public String getVideoTitle() {
        return videoTitle;
    }
    public void setVideoTitle(String videoTitle) {
        this.videoTitle = videoTitle;
    }

    public int getTotalComments() {
        return totalComments;
    }

    public void setTotalComments(int totalComments) {
        this.totalComments = totalComments;
    }

    public LinkedHashMap<String, Integer> getWordsFrequency() {
        return wordsFrequency;
    }

    public void setWordsFrequency(LinkedHashMap<String, Integer> wordsFrequency) {
        this.wordsFrequency = wordsFrequency;
    }

    public Map<UUID, CommentSentimentSummary> getSentimentAnalysisStatusMap() {
        return sentimentAnalysisStatusMap;
    }
    public void setSentimentAnalysisStatusMap(Map<UUID, CommentSentimentSummary> sentimentAnalysisStatusMap) {
        this.sentimentAnalysisStatusMap = sentimentAnalysisStatusMap;
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

    @Override
    public String getId() {
        return videoId;
    }

    @Override
    public boolean isNew() {
        return createDate == null;
    }
}
