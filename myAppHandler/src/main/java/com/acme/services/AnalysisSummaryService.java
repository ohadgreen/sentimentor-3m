package com.acme.services;

import com.acme.model.comment.*;
import com.acme.model.ytsearch.Thumbnails;
import com.acme.model.ytsearch.VideoStatistics;
import com.acme.services.persistence.AnalysisSummaryPersistence;
import com.acme.services.persistence.CommentsPersistence;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnalysisSummaryService {
    private final static int MAX_TOP_COMMENTS = 50;
    private final AnalysisSummaryPersistence analysisSummaryPersistence;
    private final CommentsPersistence commentsPersistence;

    public AnalysisSummaryService(AnalysisSummaryPersistence analysisSummaryPersistence, CommentsPersistence commentsPersistence) {
        this.analysisSummaryPersistence = analysisSummaryPersistence;
        this.commentsPersistence = commentsPersistence;
    }

    public VideoCommentsSummary getCommentsAnalysisSummary(String videoId) {
        return analysisSummaryPersistence.getCommentsAnalysisSummary(videoId);
    }

    public List<VideoSummaryDto> getLatestVideoSummaries() {
        return analysisSummaryPersistence.getLatestVideoSummaries().stream()
                .map(this::toVideoSummaryDto)
                .collect(Collectors.toList());
    }

    private VideoSummaryDto toVideoSummaryDto(VideoCommentsSummary summary) {
        VideoSummaryDto dto = new VideoSummaryDto();
        dto.setVideoId(summary.getVideoId());
        dto.setVideoTitle(summary.getVideoTitle());
        dto.setDescription(summary.getDescription());
        dto.setWordsFrequency(summary.getWordsFrequency());
        dto.setPublishTime(summary.getPublishTime());
        dto.setCreateDate(summary.getCreateDate());

        VideoStatistics stats = summary.getStatistics();
        if (stats != null) {
            dto.setViewCount(stats.getViewCount());
            dto.setLikeCount(stats.getLikeCount());
            dto.setCommentCount(stats.getCommentCount());
        }

        Thumbnails thumbnails = summary.getThumbnails();
        if (thumbnails != null && thumbnails.getDefaultThumbnail() != null) {
            dto.setDefaultThumbnailUrl(thumbnails.getDefaultThumbnail().getUrl());
        }

        List<SentimentSummaryDto> sentimentAnalyses = summary.getSentimentAnalysisStatusMap() == null
                ? Collections.emptyList()
                : summary.getSentimentAnalysisStatusMap().values().stream()
                        .map(s -> new SentimentSummaryDto(
                                s.getSentimentObject(),
                                s.getPositiveComments(),
                                s.getNegativeComments(),
                                s.getNeutralComments(),
                                s.getTotalCommentsAnalyzed()))
                        .collect(Collectors.toList());
        dto.setSentimentAnalyses(sentimentAnalyses);

        return dto;
    }

}
