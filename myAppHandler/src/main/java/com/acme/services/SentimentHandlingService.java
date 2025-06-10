package com.acme.services;

import com.acme.model.comment.CommentsAnalyzeSummary;
import com.acme.services.persistence.AnalysisResultPersistence;
import com.acme.services.persistence.AnalysisSummaryPersistence;
import com.acme.services.persistence.CommentsPersistence;
import common.model.analysisrequest.CommentToAnalyze;
import common.model.analysisrequest.SentimentAnalysisChunkRequest;
import common.model.analysisrequest.SentimentAnalysisRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SentimentHandlingService {
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int PARALLEL_CHUNKS_COUNT = 2;

    private final ConcurrentHashMap<UUID, SentimentAnalysisRequest> sentimentWorkerTrackMap = new ConcurrentHashMap<>();

    @Value("${app.aiWorker.base-url}")
    private String aiWorkerBaseUrl;

    private final AnalysisSummaryPersistence analysisSummaryPersistence;
    private final CommentsPersistence commentsPersistence;
    private final AnalysisResultPersistence analysisResultPersistence;
    private final RestTemplate restTemplate;

    public SentimentHandlingService(AnalysisSummaryPersistence analysisSummaryPersistence, CommentsPersistence commentsPersistence, AnalysisResultPersistence analysisResultPersistence, RestTemplate restTemplate) {
        this.analysisSummaryPersistence = analysisSummaryPersistence;
        this.commentsPersistence = commentsPersistence;
        this.analysisResultPersistence = analysisResultPersistence;
        this.restTemplate = restTemplate;
    }

    public UUID handleVideoSentimentAnalysisReq(SentimentAnalysisRequest sentimentAnalysisRequest) {
        // Extract videoId and commentId from the request
        String videoId = sentimentAnalysisRequest.getVideoId();
        String sentimentObject = sentimentAnalysisRequest.getAnalysisObject();
        UUID analysisId = UUID.randomUUID();

        sentimentAnalysisRequest.setAnalysisId(analysisId);

        CommentsAnalyzeSummary commentsAnalysisSummary = analysisSummaryPersistence.getCommentsAnalysisSummary(videoId);
        int totalComments = commentsAnalysisSummary.getTotalComments();
        int requestedCommentCount = sentimentAnalysisRequest.getCommentCount();


        if (sentimentAnalysisRequest.getCommentCount() > 0 && totalComments > requestedCommentCount) {
            // If the requested comment count is greater than available comments, adjust it
            sentimentAnalysisRequest.setCommentCount(totalComments);
        }

        for (int i = 0; i < PARALLEL_CHUNKS_COUNT; i++) {
            // Extract comments for the current page
            List<CommentToAnalyze> commentsPage = extractCommentsPage(videoId, i, sentimentAnalysisRequest.getPageSize() > 0 ? sentimentAnalysisRequest.getPageSize() : DEFAULT_PAGE_SIZE);

        }

        return UUID.randomUUID();
    }

    private List<CommentToAnalyze> extractCommentsPage(String videoId, int pageNumber, int pageSize) {

        Pageable pageable = Pageable.ofSize(pageSize).withPage(pageNumber);
        return commentsPersistence.getCommentsPageByVideoId(videoId, pageable)
                .stream()
                .map(comment -> new CommentToAnalyze(comment.getCommentId(), comment.getTextDisplay()))
                .toList();
    }

    private void callAiWorkerForChunkAnalysis(SentimentAnalysisChunkRequest analysisChunkRequest) {

        String url = aiWorkerBaseUrl + "/queue";
        try {
            String response = restTemplate.postForObject(url, analysisChunkRequest, String.class);
            System.out.println("@@@ Response from AI Worker: " + response);
        } catch (Exception e) {
            // Handle the exception (e.g., log it, retry, etc.)
            e.printStackTrace();
        }


    }
}
