package com.acme.services.persistence;

import com.acme.model.comment.VideoCommentsSummary;
import com.acme.repositories.AnalysisSummaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Profile("db")
public class AnalysisSummaryPersistInMongo implements AnalysisSummaryPersistence {

    @Autowired
    private AnalysisSummaryRepository analysisSummaryRepository;

    @Override
    public void saveAnalysisSummary(VideoCommentsSummary videoCommentsSummary) {
        if (videoCommentsSummary == null || videoCommentsSummary.getVideoId() == null) {
            return;
        }
        try {
            VideoCommentsSummary existingSummary = analysisSummaryRepository.findByVideoId(videoCommentsSummary.getVideoId());
            if (existingSummary != null) {
                existingSummary.setWordsFrequency(videoCommentsSummary.getWordsFrequency());
                analysisSummaryRepository.save(existingSummary);
            } else {
                analysisSummaryRepository.save(videoCommentsSummary);
            }
        } catch (DuplicateKeyException e) {
            // Another thread inserted the document concurrently — safe to ignore
        }
    }

    @Override
    public VideoCommentsSummary getCommentsAnalysisSummary(String videoId) {
        return sortWordsFrequency(analysisSummaryRepository.findByVideoId(videoId));
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

    @Override
    public List<VideoCommentsSummary> getLatestVideoSummaries() {
        return analysisSummaryRepository.findTop6ByOrderByCreateDateDesc().stream()
                .map(this::sortWordsFrequency)
                .toList();
    }

    private VideoCommentsSummary sortWordsFrequency(VideoCommentsSummary summary) {
        if (summary == null || summary.getWordsFrequency() == null) {
            return summary;
        }
        LinkedHashMap<String, Integer> sorted = summary.getWordsFrequency().entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(LinkedHashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), LinkedHashMap::putAll);
        summary.setWordsFrequency(sorted);
        return summary;
    }
}
