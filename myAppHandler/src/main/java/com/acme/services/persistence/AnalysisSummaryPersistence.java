package com.acme.services.persistence;

import com.acme.model.comment.CommentsAnalyzeSummary;

public interface AnalysisSummaryPersistence {
    void saveAnalysisSummary(CommentsAnalyzeSummary commentsAnalyzeSummary);
    CommentsAnalyzeSummary getCommentsAnalysisSummary(String videoId);
    void updateAnalysisSummary(String videoId, CommentsAnalyzeSummary commentsAnalyzeSummary);
    void deleteAnalysisSummary(String videoId);
}
