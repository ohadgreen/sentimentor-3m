package com.acme.services;

import com.acme.model.comment.CommentSentimentResult;
import com.acme.model.comment.VideoCommentsSummary;
import com.acme.services.persistence.AnalysisResultPersistence;
import com.acme.services.persistence.AnalysisSummaryPersistence;
import com.acme.services.persistence.CommentsPersistence;
import common.model.analysisrequest.*;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.*;

@Service
public class SentimentHandlingService {
    private final Logger logger = getLogger(SentimentHandlingService.class);
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int PARALLEL_CHUNKS_COUNT = 2;

    private final ConcurrentHashMap<UUID, SentimentAnalysisChunkRequest> sentimentWorkerTrackMap = new ConcurrentHashMap<>();

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

        VideoCommentsSummary commentsAnalysisSummary = analysisSummaryPersistence.getCommentsAnalysisSummary(videoId);
        Map<String, Boolean> sentimentAnalysisStatus = commentsAnalysisSummary.getSentimentAnalysisStatus();
        if (sentimentAnalysisStatus.containsKey(sentimentObject)) {
            logger.warn("Sentiment analysis for object '{}' already exists for videoId: {}", sentimentObject, videoId);
            return null;
        } else {
            sentimentAnalysisStatus.put(sentimentObject, false);
            commentsAnalysisSummary.setSentimentAnalysisStatus(sentimentAnalysisStatus);
            analysisSummaryPersistence.updateAnalysisSummary(videoId, commentsAnalysisSummary);
        }

        int totalCommentsForVideo = commentsAnalysisSummary.getTotalComments();
        int totalCommentsToAnalyze = sentimentAnalysisRequest.getTotalCommentsToAnalyze();

        if (totalCommentsForVideo < totalCommentsToAnalyze) {
            sentimentAnalysisRequest.setTotalCommentsToAnalyze(totalCommentsForVideo);
        }

        SentimentAnalysisChunkRequest analysisChunkRequest = null;

        Set<UUID> processingChunkIds = new HashSet<>();

        for (int i = 0; i < PARALLEL_CHUNKS_COUNT; i++) {
            List<CommentToAnalyze> commentsPage = extractCommentsPage(videoId, i, DEFAULT_PAGE_SIZE);
            if (commentsPage.isEmpty()) {
                break;
            }
            UUID analysisChunkId = UUID.randomUUID();

            analysisChunkRequest = new SentimentAnalysisChunkRequest(
                    analysisId,
                    videoId,
                    commentsAnalysisSummary.getVideoTitle(),
                    sentimentObject,
                    sentimentAnalysisRequest.getMoreInfo(),
                    totalCommentsForVideo,
                    analysisChunkId,
                    commentsPage,
                    DEFAULT_PAGE_SIZE,
                    i
            );

            callAiWorkerForChunkAnalysis(analysisChunkRequest);
            processingChunkIds.add(analysisChunkId);
        }

        if (analysisChunkRequest != null) {
            logger.info("Sentiment analysis request created for videoId: {}, analysisId: {}", videoId, analysisId);
            analysisChunkRequest.setProcessingChunkIds(processingChunkIds);
            sentimentWorkerTrackMap.put(analysisId, analysisChunkRequest);

        } else {
            logger.warn("No comments found for videoId: {}", videoId);
        }

        return analysisId;
    }

    public void handleChunkAnalysisResponse(SentimentAnalysisChunkResponse chunkAnalysisResponse) {
        SentimentAnalysisChunkRequest analysisChunkRequest = sentimentWorkerTrackMap.get(chunkAnalysisResponse.getSentimentAnalysisId());

        if (analysisChunkRequest == null) {
            logger.error("No analysis request found for ID: {}", chunkAnalysisResponse.getSentimentAnalysisId());
            return;
        }

        Set<UUID> processingChunkIds = analysisChunkRequest.getProcessingChunkIds();
        // remove the current chunk request from the processingChunkIds list
        processingChunkIds.remove(chunkAnalysisResponse.getAnalysisChunkId());

        List<CommentSentimentResult> commentSentimentResults = convertToAnalysisResultList(chunkAnalysisResponse.getCommentSentiments(), analysisChunkRequest);
        analysisResultPersistence.saveCommentSentimentResult(commentSentimentResults);

        // check if required another chunk analysis
        int totalCommentsCount = analysisChunkRequest.getTotalCommentsToAnalyze();
        int currentProcessedCount = analysisChunkRequest.getPageNumber() * analysisChunkRequest.getPageSize() + chunkAnalysisResponse.getCommentSentiments().size();

        if (currentProcessedCount < totalCommentsCount) {
            // Prepare next chunk request
            int nextPageNumber = analysisChunkRequest.getPageNumber() + 1;
            List<CommentToAnalyze> nextCommentsPage = extractCommentsPage(analysisChunkRequest.getVideoId(), nextPageNumber, analysisChunkRequest.getPageSize());

            if (!nextCommentsPage.isEmpty()) {
                UUID newChunkId = UUID.randomUUID();
                SentimentAnalysisChunkRequest nextChunkRequest = new SentimentAnalysisChunkRequest(
                        analysisChunkRequest.getAnalysisId(),
                        analysisChunkRequest.getVideoId(),
                        analysisChunkRequest.getVideoTitle(),
                        analysisChunkRequest.getAnalysisObject(),
                        analysisChunkRequest.getMoreInfo(),
                        totalCommentsCount,
                        newChunkId,
                        nextCommentsPage,
                        analysisChunkRequest.getPageSize(),
                        nextPageNumber
                );
                callAiWorkerForChunkAnalysis(nextChunkRequest);
                processingChunkIds.add(newChunkId);
                sentimentWorkerTrackMap.put(analysisChunkRequest.getAnalysisId(), nextChunkRequest);

            } else {
                if (processingChunkIds.isEmpty()) {
                    handleCompleteAnalysis(analysisChunkRequest);
                }
            }
        } else {
            handleCompleteAnalysis(analysisChunkRequest);
        }

    }

    private void handleCompleteAnalysis(SentimentAnalysisChunkRequest analysisChunkRequest) {
        logger.info("All comments processed for videoId: {} analysis obj: {}", analysisChunkRequest.getVideoId(), analysisChunkRequest.getAnalysisObject());
        sentimentWorkerTrackMap.remove(analysisChunkRequest.getAnalysisId());
        // Update the summary status
        VideoCommentsSummary commentsSummary = analysisSummaryPersistence.getCommentsAnalysisSummary(analysisChunkRequest.getVideoId());
        commentsSummary.getSentimentAnalysisStatus().put(analysisChunkRequest.getAnalysisObject(), true);
        analysisSummaryPersistence.updateAnalysisSummary(analysisChunkRequest.getVideoId(), commentsSummary);

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

    private List<CommentSentimentResult> convertToAnalysisResultList(List<CommentSentiment> commentsSentimentAnalysisList, SentimentAnalysisChunkRequest analysisChunkRequest) {

        return commentsSentimentAnalysisList.stream()
                .map(cs -> new CommentSentimentResult(
                        cs.getCommentId(),
                        analysisChunkRequest.getVideoId(),
                        analysisChunkRequest.getAnalysisObject(),
                        cs.getSentiment(),
                        cs.getSentimentReason()
                ))
                .collect(Collectors.toList());
    }
}
