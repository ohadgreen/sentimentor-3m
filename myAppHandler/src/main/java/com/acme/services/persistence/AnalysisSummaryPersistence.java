package com.acme.services.persistence;

import com.acme.model.comment.VideoCommentsSummary;

import java.util.List;

public interface AnalysisSummaryPersistence {
    void saveAnalysisSummary(VideoCommentsSummary videoCommentsSummary);
    VideoCommentsSummary getCommentsAnalysisSummary(String videoId);
    void updateAnalysisSummary(String videoId, VideoCommentsSummary videoCommentsSummary);
    void markAsTrend(String videoId);
    void deleteAnalysisSummary(String videoId);
    List<VideoCommentsSummary> getLatestVideoSummaries();
}
