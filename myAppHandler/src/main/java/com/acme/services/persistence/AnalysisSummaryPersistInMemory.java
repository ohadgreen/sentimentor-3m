package com.acme.services.persistence;

import com.acme.model.comment.CommentsAnalyzeSummary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Profile("memory")
public class AnalysisSummaryPersistInMemory implements AnalysisSummaryPersistence {

    public Map<String, CommentsAnalyzeSummary> commentsAnalyzeSummaryMap = new HashMap<>();
    @Override
    public void saveAnalysisSummary(CommentsAnalyzeSummary commentsAnalyzeSummary) {
        if (commentsAnalyzeSummaryMap.containsKey(commentsAnalyzeSummary.getVideoId())) {
            commentsAnalyzeSummaryMap.replace(commentsAnalyzeSummary.getVideoId(), commentsAnalyzeSummary);
        } else {
            commentsAnalyzeSummaryMap.put(commentsAnalyzeSummary.getVideoId(), commentsAnalyzeSummary);
        }
    }

    @Override
    public CommentsAnalyzeSummary getCommentsAnalysisSummary(String videoId) {
        return commentsAnalyzeSummaryMap.get(videoId);
    }

    @Override
    public void updateAnalysisSummary(String videoId, CommentsAnalyzeSummary commentsAnalyzeSummary) {
        if (commentsAnalyzeSummaryMap.containsKey(videoId)) {
            commentsAnalyzeSummaryMap.replace(videoId, commentsAnalyzeSummary);
        } else {
            commentsAnalyzeSummaryMap.put(videoId, commentsAnalyzeSummary);
        }
    }

    @Override
    public void deleteAnalysisSummary(String videoId) {
        commentsAnalyzeSummaryMap.remove(videoId);
    }
}
