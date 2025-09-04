package com.acme.services.persistence;

import com.acme.model.comment.VideoCommentsSummary;
import com.acme.repositories.AnalysisSummaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("db")
public class AnalysisSummaryPersistInMongo implements AnalysisSummaryPersistence {

    @Autowired
    private AnalysisSummaryRepository analysisSummaryRepository;

    @Override
    public void saveAnalysisSummary(VideoCommentsSummary videoCommentsSummary) {
        if (videoCommentsSummary == null || videoCommentsSummary.getVideoId() == null) {
            return; // Avoid saving null or incomplete summaries
        }
        VideoCommentsSummary existingSummary = analysisSummaryRepository.findByVideoId(videoCommentsSummary.getVideoId());
        if (existingSummary != null) {
            existingSummary.setWordsFrequency(videoCommentsSummary.getWordsFrequency());
            existingSummary.setTopRatedComments(videoCommentsSummary.getTopRatedComments());
            analysisSummaryRepository.save(existingSummary);
        } else {
            analysisSummaryRepository.save(videoCommentsSummary);
        }
    }

    @Override
    public VideoCommentsSummary getCommentsAnalysisSummary(String videoId) {
        return analysisSummaryRepository.findByVideoId(videoId);
    }

    @Override
    public void updateAnalysisSummary(String videoId, VideoCommentsSummary videoCommentsSummary) {
        VideoCommentsSummary existingSummary = analysisSummaryRepository.findByVideoId(videoId);
        if (existingSummary != null) {
            existingSummary.setSentimentAnalysisStatusMap(videoCommentsSummary.getSentimentAnalysisStatusMap());
            analysisSummaryRepository.save(existingSummary);
        } else {
            analysisSummaryRepository.save(videoCommentsSummary);
        }
    }

    @Override
    public void deleteAnalysisSummary(String videoId) {
        analysisSummaryRepository.deleteByVideoId(videoId);
    }
}
