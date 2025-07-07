package com.acme.model.comment;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Document(collection = "video_comments_summary")
public class VideoCommentsSummary implements Serializable {
    @Id
    private String videoId;
    private String videoTitle;
    private String jobId;
    private int totalComments;
    private boolean isCompleted;
    private LinkedHashMap<String, Integer> wordsFrequency;
    private List<CommentDto> topRatedComments;
    private Map<String, Boolean> sentimentAnalysisStatus = new HashMap<>();

    public VideoCommentsSummary() {
    }

    public VideoCommentsSummary(String jobId, String videoId, String videoTitle, boolean isCompleted, int totalComments, LinkedHashMap<String, Integer> wordsFrequency, List<CommentDto> topRatedComments) {
        this.jobId = jobId;
        this.videoId = videoId;
        this.videoTitle = videoTitle;
        this.isCompleted = isCompleted;
        this.totalComments = totalComments;
        this.wordsFrequency = wordsFrequency;
        this.topRatedComments = topRatedComments;
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

    public List<CommentDto> getTopRatedComments() {
        return topRatedComments;
    }

    public void setTopRatedComments(List<CommentDto> topRatedComments) {
        this.topRatedComments = topRatedComments;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public Map<String, Boolean> getSentimentAnalysisStatus() {
        return sentimentAnalysisStatus;
    }
    public void setSentimentAnalysisStatus(Map<String, Boolean> sentimentAnalysisStatus) {
        this.sentimentAnalysisStatus = sentimentAnalysisStatus;
    }
}
