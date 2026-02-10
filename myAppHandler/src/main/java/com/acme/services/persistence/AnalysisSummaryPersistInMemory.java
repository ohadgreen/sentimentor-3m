package com.acme.services.persistence;

import com.acme.model.comment.VideoCommentsSummary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Profile("memory")
public class AnalysisSummaryPersistInMemory implements AnalysisSummaryPersistence {

    public Map<String, VideoCommentsSummary> commentsAnalyzeSummaryMap = new HashMap<>();
    @Override
    public void saveAnalysisSummary(VideoCommentsSummary videoCommentsSummary) {
        LocalDateTime now = LocalDateTime.now();
        if (commentsAnalyzeSummaryMap.containsKey(videoCommentsSummary.getVideoId())) {
            videoCommentsSummary.setUpdateDate(now);
            commentsAnalyzeSummaryMap.replace(videoCommentsSummary.getVideoId(), videoCommentsSummary);
        } else {
            videoCommentsSummary.setCreateDate(now);
            videoCommentsSummary.setUpdateDate(now);
            commentsAnalyzeSummaryMap.put(videoCommentsSummary.getVideoId(), videoCommentsSummary);
        }
    }

    @Override
    public VideoCommentsSummary getCommentsAnalysisSummary(String videoId) {
        return commentsAnalyzeSummaryMap.get(videoId);
    }

    @Override
    public void updateAnalysisSummary(String videoId, VideoCommentsSummary videoCommentsSummary) {
        LocalDateTime now = LocalDateTime.now();
        videoCommentsSummary.setUpdateDate(now);
        if (commentsAnalyzeSummaryMap.containsKey(videoId)) {
            commentsAnalyzeSummaryMap.replace(videoId, videoCommentsSummary);
        } else {
            videoCommentsSummary.setCreateDate(now);
            commentsAnalyzeSummaryMap.put(videoId, videoCommentsSummary);
        }
    }

    @Override
    public void deleteAnalysisSummary(String videoId) {
        commentsAnalyzeSummaryMap.remove(videoId);
    }
}
