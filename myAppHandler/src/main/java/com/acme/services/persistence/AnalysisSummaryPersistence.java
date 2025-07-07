package com.acme.services.persistence;

import com.acme.model.comment.VideoCommentsSummary;

public interface AnalysisSummaryPersistence {
    void saveAnalysisSummary(VideoCommentsSummary videoCommentsSummary);
    VideoCommentsSummary getCommentsAnalysisSummary(String videoId);
    void updateAnalysisSummary(String videoId, VideoCommentsSummary videoCommentsSummary);
    void deleteAnalysisSummary(String videoId);
}
