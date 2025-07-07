package com.acme.services.persistence;

import com.acme.model.comment.VideoCommentsSummary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Profile("memory")
public class AnalysisSummaryPersistInMemory implements AnalysisSummaryPersistence {

    public Map<String, VideoCommentsSummary> commentsAnalyzeSummaryMap = new HashMap<>();
    @Override
    public void saveAnalysisSummary(VideoCommentsSummary videoCommentsSummary) {
        if (commentsAnalyzeSummaryMap.containsKey(videoCommentsSummary.getVideoId())) {
            commentsAnalyzeSummaryMap.replace(videoCommentsSummary.getVideoId(), videoCommentsSummary);
        } else {
            commentsAnalyzeSummaryMap.put(videoCommentsSummary.getVideoId(), videoCommentsSummary);
        }
    }

    @Override
    public VideoCommentsSummary getCommentsAnalysisSummary(String videoId) {
        return commentsAnalyzeSummaryMap.get(videoId);
    }

    @Override
    public void updateAnalysisSummary(String videoId, VideoCommentsSummary videoCommentsSummary) {
        if (commentsAnalyzeSummaryMap.containsKey(videoId)) {
            commentsAnalyzeSummaryMap.replace(videoId, videoCommentsSummary);
        } else {
            commentsAnalyzeSummaryMap.put(videoId, videoCommentsSummary);
        }
    }

    @Override
    public void deleteAnalysisSummary(String videoId) {
        commentsAnalyzeSummaryMap.remove(videoId);
    }
}
