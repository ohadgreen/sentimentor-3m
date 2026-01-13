package com.acme.model.comment;

import java.util.Set;
import java.util.UUID;

public class CommentSentimentSummary {
    private String videoId;
    private UUID analysisId;
    private Set<UUID> processingChunkIds;
    private String videoTitle;
    private String sentimentObject;
    private String moreInfo;
    private int totalCommentsToAnalyze;
    private int totalCommentsAnalyzed;
    private int currentPage;
    private int pageSize;
    private AnalysisStatus analysisStatus;
    private int positiveComments;
    private int negativeComments;
    private int neutralComments;

    public CommentSentimentSummary(String videoId, UUID analysisId, String videoTitle, String sentimentObject, String moreInfo) {
        this.videoId = videoId;
        this.analysisId = analysisId;
        this.videoTitle = videoTitle;
        this.sentimentObject = sentimentObject;
        this.moreInfo = moreInfo;
    }

    public CommentSentimentSummary(String videoId, UUID analysisId, AnalysisStatus analysisStatus) {
        this.videoId = videoId;
        this.analysisId = analysisId;
        this.analysisStatus = analysisStatus;
    }

    public CommentSentimentSummary() {
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }
    public UUID getAnalysisId() {
        return analysisId;
    }
    public void setAnalysisId(UUID analysisId) {
        this.analysisId = analysisId;
    }

    public Set<UUID> getProcessingChunkIds() {
        return processingChunkIds;
    }
    public void setProcessingChunkIds(Set<UUID> processingChunkIds) {
        this.processingChunkIds = processingChunkIds;
    }

    public String getVideoTitle() {
        return videoTitle;
    }

    public void setVideoTitle(String videoTitle) {
        this.videoTitle = videoTitle;
    }

    public String getSentimentObject() {
        return sentimentObject;
    }

    public void setSentimentObject(String sentimentObject) {
        this.sentimentObject = sentimentObject;
    }

    public String getMoreInfo() {
        return moreInfo;
    }

    public void setMoreInfo(String moreInfo) {
        this.moreInfo = moreInfo;
    }

    public int getTotalCommentsAnalyzed() {
        return totalCommentsAnalyzed;
    }

    public int getTotalCommentsToAnalyze() {
        return totalCommentsToAnalyze;
    }

    public void setTotalCommentsToAnalyze(int totalCommentsToAnalyze) {
        this.totalCommentsToAnalyze = totalCommentsToAnalyze;
    }

    public void setTotalCommentsAnalyzed(int totalCommentsAnalyzed) {
        this.totalCommentsAnalyzed = totalCommentsAnalyzed;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPositiveComments() {
        return positiveComments;
    }

    public AnalysisStatus getAnalysisStatus() {
        return analysisStatus;
    }

    public void setAnalysisStatus(AnalysisStatus analysisStatus) {
        this.analysisStatus = analysisStatus;
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
}
