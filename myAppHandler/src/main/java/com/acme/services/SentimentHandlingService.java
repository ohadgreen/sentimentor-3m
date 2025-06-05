package com.acme.services;

import com.acme.services.persistence.AnalysisResultPersistence;
import com.acme.services.persistence.AnalysisSummaryPersistence;
import com.acme.services.persistence.CommentsPersistence;
import common.model.analysisrequest.CommentToAnalyze;
import common.model.analysisrequest.SentimentAnalysisRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SentimentHandlingService {
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int PARALLEL_CHUNKS_COUNT = 2;

    private final ConcurrentHashMap<UUID, SentimentAnalysisRequest> sentimentWorkerTrackMap = new ConcurrentHashMap<>();

    private final AnalysisSummaryPersistence analysisSummaryPersistence;
    private final CommentsPersistence commentsPersistence;
    private final AnalysisResultPersistence analysisResultPersistence;

    public SentimentHandlingService(AnalysisSummaryPersistence analysisSummaryPersistence, CommentsPersistence commentsPersistence, AnalysisResultPersistence analysisResultPersistence) {
        this.analysisSummaryPersistence = analysisSummaryPersistence;
        this.commentsPersistence = commentsPersistence;
        this.analysisResultPersistence = analysisResultPersistence;
    }

    public UUID handleVideoSentimentAnalysisReq(SentimentAnalysisRequest sentimentAnalysisRequest) {
        // Extract videoId and commentId from the request
        String videoId = sentimentAnalysisRequest.getVideoId();
        String sentimentObject = sentimentAnalysisRequest.getAnalysisObject();
        UUID analysisId = UUID.randomUUID();

        return UUID.randomUUID();
    }

    private List<CommentToAnalyze> extractCommentsPage(SentimentAnalysisRequest sentimentAnalysisRequest) {
        int pageNumber = sentimentAnalysisRequest.getPageNumber();
        int pageSize = sentimentAnalysisRequest.getPageSize() > 0 ? sentimentAnalysisRequest.getPageSize() : DEFAULT_PAGE_SIZE;

        Pageable pageable = Pageable.ofSize(pageSize).withPage(pageNumber);
        // Fetch comments for the given videoId and page
        return commentsPersistence.getCommentsPageByVideoId(sentimentAnalysisRequest.getVideoId(), pageable)
                .stream()
                .map(comment -> new CommentToAnalyze(comment.getCommentId(), comment.getTextDisplay()))
                .toList();
    }
}
