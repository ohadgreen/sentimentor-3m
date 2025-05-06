package com.acme.model.comment;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;

@Document(collection = "analyze_summary")
public class CommentsAnalyzeSummary implements Serializable {
    @Id
    private String videoId;
    private String jobId;
    private int totalComments;
    private boolean isCompleted;
    private LinkedHashMap<String, Integer> wordsFrequency;
    private List<CommentDto> topRatedComments;

    public CommentsAnalyzeSummary() {
    }

    public CommentsAnalyzeSummary(String jobId, String videoId, boolean isCompleted, int totalComments, LinkedHashMap<String, Integer> wordsFrequency, List<CommentDto> topRatedComments) {
        this.jobId = jobId;
        this.videoId = videoId;
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
}
