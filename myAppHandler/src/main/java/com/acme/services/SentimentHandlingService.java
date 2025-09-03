package com.acme.services;

import com.acme.model.comment.AnalysisStatus;
import com.acme.model.comment.CommentSentimentResult;
import com.acme.model.comment.CommentSentimentSummary;
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
    private static final int DEFAULT_PAGE_SIZE = 5;
    private static final int PARALLEL_CHUNKS_COUNT = 2;

    private final ConcurrentHashMap<UUID, CommentSentimentSummary> sentimentWorkerTrackMap2 = new ConcurrentHashMap<>();
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

    public CommentSentimentSummary getCommentSentimentSummary(String videoId, UUID analysisId) {
        CommentSentimentSummary commentSentimentSummary = sentimentWorkerTrackMap2.get(analysisId);
        if (commentSentimentSummary != null) {
            return commentSentimentSummary;
        }

        VideoCommentsSummary commentsAnalysisSummary = analysisSummaryPersistence.getCommentsAnalysisSummary(videoId);
        if (commentsAnalysisSummary == null) {
            logger.error("No comments summary found for videoId: {}", videoId);
            return null;
        }
        Map<UUID, CommentSentimentSummary> sentimentAnalysisStatusMap = commentsAnalysisSummary.getSentimentAnalysisStatusMap();
        return sentimentAnalysisStatusMap.get(analysisId);
    }

    public UUID handleVideoSentimentAnalysisReq(SentimentAnalysisRequest sentimentAnalysisRequest) {
        String videoId = sentimentAnalysisRequest.getVideoId();
        String sentimentObject = sentimentAnalysisRequest.getAnalysisObject();
        UUID analysisId = UUID.randomUUID();

        sentimentAnalysisRequest.setAnalysisId(analysisId);

        VideoCommentsSummary commentsAnalysisSummary = analysisSummaryPersistence.getCommentsAnalysisSummary(videoId);
        if (commentsAnalysisSummary == null) {
            logger.error("No comments summary found for videoId: {}", videoId);
            return null;
        }
        if (commentsAnalysisSummary.getTotalComments() == 0) {
            logger.error("No comments found for videoId: {}", videoId);
            return null;
        }

        Map<UUID, CommentSentimentSummary> sentimentAnalysisStatusMap = commentsAnalysisSummary.getSentimentAnalysisStatusMap();
        Set<String> existingSentimentObjects = sentimentAnalysisStatusMap.values()
                .stream()
                .map(CommentSentimentSummary::getSentimentObject)
                .collect(Collectors.toSet());
        if (existingSentimentObjects.contains(sentimentObject)) {
            logger.warn("Sentiment analysis summary for object '{}' already exists for videoId: {}", sentimentObject, videoId);
            return null;
        }

        int totalCommentsForVideo = commentsAnalysisSummary.getTotalComments();
        int totalCommentsToAnalyze = Math.min(sentimentAnalysisRequest.getTotalCommentsToAnalyze(), totalCommentsForVideo);

        // calculate how many chunks we need to process
        int chunksNeeded = (int) Math.ceil((double) totalCommentsToAnalyze / DEFAULT_PAGE_SIZE);
        // calculate how many chunks we can process in parallel
        int chunksToProcess = Math.min(chunksNeeded, PARALLEL_CHUNKS_COUNT);

        Set<UUID> processingChunkIds = new HashSet<>();

        for (int i = 0; i < chunksToProcess; i++) {
            List<CommentToAnalyze> commentsPage = extractCommentsPage(videoId, i, DEFAULT_PAGE_SIZE);
            UUID analysisChunkId = UUID.randomUUID();

            SentimentAnalysisChunkRequest analysisChunkRequest = new SentimentAnalysisChunkRequest(
                    analysisId,
                    videoId,
                    commentsAnalysisSummary.getVideoTitle(),
                    sentimentObject,
                    sentimentAnalysisRequest.getMoreInfo(),
                    totalCommentsToAnalyze,
                    analysisChunkId,
                    commentsPage,
                    DEFAULT_PAGE_SIZE,
                    i
            );

            callAiWorkerForChunkAnalysis(analysisChunkRequest);
            processingChunkIds.add(analysisChunkId);
        }

        logger.info("Sentiment analysis request created for videoId: {}, analysisId: {}, chunks count: {}", videoId, analysisId, processingChunkIds.size());

        CommentSentimentSummary commentSentimentSummary =
                new CommentSentimentSummary(videoId, analysisId, commentsAnalysisSummary.getVideoTitle(), sentimentObject, sentimentAnalysisRequest.getMoreInfo());
        commentSentimentSummary.setAnalysisStatus(AnalysisStatus.IN_PROGRESS);
        commentSentimentSummary.setTotalCommentsToAnalyze(totalCommentsToAnalyze);
        commentSentimentSummary.setProcessingChunkIds(processingChunkIds);
        commentSentimentSummary.setCurrentPage(chunksToProcess - 1);
        commentSentimentSummary.setPageSize(DEFAULT_PAGE_SIZE);
        sentimentWorkerTrackMap2.put(analysisId, commentSentimentSummary);

        sentimentAnalysisStatusMap.put(analysisId, commentSentimentSummary);
        commentsAnalysisSummary.setSentimentAnalysisStatusMap(sentimentAnalysisStatusMap);
        analysisSummaryPersistence.updateAnalysisSummary(videoId, commentsAnalysisSummary);

        return analysisId;
    }

    public void handleChunkAnalysisResponse(SentimentAnalysisChunkResponse chunkAnalysisResponse) {

        CommentSentimentSummary commentSentimentSummary = sentimentWorkerTrackMap2.get(chunkAnalysisResponse.getSentimentAnalysisId());

        if (commentSentimentSummary == null) {
            logger.error("No analysis request found for ID: {}", chunkAnalysisResponse.getSentimentAnalysisId());
            return;
        }

        Set<UUID> processingChunkIds = commentSentimentSummary.getProcessingChunkIds();

        boolean removeProcessedChunkFromSet = processingChunkIds.remove(chunkAnalysisResponse.getAnalysisChunkId());
        if (!removeProcessedChunkFromSet) {
            logger.warn("Processed chunk ID: {} was not found in the processing set for analysis ID: {}", chunkAnalysisResponse.getAnalysisChunkId(), chunkAnalysisResponse.getSentimentAnalysisId());
        } else {
            logger.info("Processed chunk ID: {} removed from processing set for analysis ID: {}", chunkAnalysisResponse.getAnalysisChunkId(), chunkAnalysisResponse.getSentimentAnalysisId());
        }

        List<CommentSentimentResult> commentSentimentResults = convertToAnalysisResultList(chunkAnalysisResponse.getCommentSentiments(), commentSentimentSummary.getVideoId(), commentSentimentSummary.getSentimentObject());
        analysisResultPersistence.saveCommentSentimentResult(commentSentimentResults);
        updateCommentSentimentCounts(commentSentimentSummary, commentSentimentResults);

        // check if required another chunk analysis
        int totalCommentsToAnalyze = commentSentimentSummary.getTotalCommentsToAnalyze();
        int currentProcessedCount = commentSentimentSummary.getTotalCommentsAnalyzed() + commentSentimentResults.size();

        System.out.println("@@@ currentProcessedCount: " + currentProcessedCount + " - total to analyze: " + totalCommentsToAnalyze);
        if (currentProcessedCount < totalCommentsToAnalyze) {
            // Prepare next chunk request
            int nextPageNumber = commentSentimentSummary.getCurrentPage() + 1;
            int nextPageSize = Math.min((totalCommentsToAnalyze - currentProcessedCount), commentSentimentSummary.getPageSize());

            List<CommentToAnalyze> nextCommentsPage = extractCommentsPage(commentSentimentSummary.getVideoId(), nextPageNumber, nextPageSize);

            if (!nextCommentsPage.isEmpty()) {
                UUID newChunkId = UUID.randomUUID();
                SentimentAnalysisChunkRequest nextChunkRequest = new SentimentAnalysisChunkRequest(
                        commentSentimentSummary.getAnalysisId(),
                        commentSentimentSummary.getVideoId(),
                        commentSentimentSummary.getVideoTitle(),
                        commentSentimentSummary.getSentimentObject(),
                        commentSentimentSummary.getMoreInfo(),
                        totalCommentsToAnalyze,
                        newChunkId,
                        nextCommentsPage,
                        commentSentimentSummary.getPageSize(),
                        nextPageNumber
                );
                callAiWorkerForChunkAnalysis(nextChunkRequest);
                processingChunkIds.add(newChunkId);

                commentSentimentSummary.setCurrentPage(nextPageNumber);
                commentSentimentSummary.setProcessingChunkIds(processingChunkIds);
                commentSentimentSummary.setTotalCommentsAnalyzed(currentProcessedCount);
                sentimentWorkerTrackMap2.put(commentSentimentSummary.getAnalysisId(), commentSentimentSummary);

            } else {
                if (processingChunkIds.isEmpty()) {
                    handleCompleteAnalysis(commentSentimentSummary);
                }
            }
        } else {
            handleCompleteAnalysis(commentSentimentSummary);
        }
    }

    private void updateCommentSentimentCounts(CommentSentimentSummary commentSentimentSummary, List<CommentSentimentResult> newResults) {
        int positiveCount = commentSentimentSummary.getPositiveComments();
        int negativeCount = commentSentimentSummary.getNegativeComments();
        int neutralCount = commentSentimentSummary.getNeutralComments();

        for (CommentSentimentResult result : newResults) {
            switch (result.getSentiment()) {
                case Sentiment.POSITIVE -> positiveCount++;
                case Sentiment.NEGATIVE -> negativeCount++;
                case Sentiment.NEUTRAL -> neutralCount++;
            }
        }
        commentSentimentSummary.setPositiveComments(positiveCount);
        commentSentimentSummary.setNegativeComments(negativeCount);
        commentSentimentSummary.setNeutralComments(neutralCount);
    }

    private void handleCompleteAnalysis(CommentSentimentSummary commentSentimentSummary) {
        logger.info("All comments processed for videoId: {} analysis obj: {}", commentSentimentSummary.getVideoId(), commentSentimentSummary.getSentimentObject());
        sentimentWorkerTrackMap2.remove(commentSentimentSummary.getAnalysisId());
        // Update the summary status
        commentSentimentSummary.setAnalysisStatus(AnalysisStatus.COMPLETED);
        VideoCommentsSummary commentsSummary = analysisSummaryPersistence.getCommentsAnalysisSummary(commentSentimentSummary.getVideoId());
        Map<UUID, CommentSentimentSummary> sentimentAnalysisStatusMap = commentsSummary.getSentimentAnalysisStatusMap();
        sentimentAnalysisStatusMap.put(commentSentimentSummary.getAnalysisId(), commentSentimentSummary);
        analysisSummaryPersistence.updateAnalysisSummary(commentSentimentSummary.getVideoId(), commentsSummary);
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
            logger.error("Error communicating with AI Worker for videoId: {} - {}", analysisChunkRequest.getVideoId(), e.getMessage());
        }
    }

    private List<CommentSentimentResult> convertToAnalysisResultList(List<CommentSentiment> commentsSentimentAnalysisList, String videoId, String analysisObject) {

        return commentsSentimentAnalysisList.stream()
                .map(cs -> new CommentSentimentResult(
                        cs.getCommentId(),
                        videoId,
                        analysisObject,
                        cs.getSentiment(),
                        cs.getSentimentReason()
                ))
                .collect(Collectors.toList());
    }
}
