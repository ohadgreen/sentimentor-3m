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
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.*;

@Service
public class SentimentHandlingService {
    private final Logger logger = getLogger(SentimentHandlingService.class);
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int PARALLEL_CHUNKS_COUNT = 2;

    private final ConcurrentHashMap<UUID, CommentSentimentSummary> sentimentWorkerTrackMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Object> analysisLocks = new ConcurrentHashMap<>();

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

    public CommentSentimentSummary getOngoingSentimentAnalysis(String videoId, UUID analysisId) {
        logger.info("Fetching ongoing sentiment analysis for videoId: {}, analysisId: {}", videoId, analysisId);

        // if exists in local tracking map, return it
        CommentSentimentSummary trackedSummary = sentimentWorkerTrackMap.get(analysisId);
        if (trackedSummary != null) {
            return trackedSummary;
        } else {
            // else check in persistence
            VideoCommentsSummary commentsAnalysisSummary = analysisSummaryPersistence.getCommentsAnalysisSummary(videoId);
            if (commentsAnalysisSummary != null) {
                Map<UUID, CommentSentimentSummary> sentimentAnalysisStatusMap = commentsAnalysisSummary.getSentimentAnalysisStatusMap();
                return sentimentAnalysisStatusMap.get(analysisId);
            } else {
                return new CommentSentimentSummary(videoId, analysisId, AnalysisStatus.IN_PROGRESS);
            }
        }
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

        logger.info("@@@ Sentiment analysis request created for videoId: {}, analysisId: {}, chunks count: {}", videoId, analysisId, processingChunkIds.size());

        CommentSentimentSummary commentSentimentSummary =
                new CommentSentimentSummary(videoId, analysisId, commentsAnalysisSummary.getVideoTitle(), sentimentObject, sentimentAnalysisRequest.getMoreInfo());
        commentSentimentSummary.setAnalysisStatus(AnalysisStatus.IN_PROGRESS);
        commentSentimentSummary.setTotalCommentsToAnalyze(totalCommentsToAnalyze);
        commentSentimentSummary.setProcessingChunkIds(processingChunkIds);
        commentSentimentSummary.setCurrentPage(chunksToProcess - 1);
        commentSentimentSummary.setPageSize(DEFAULT_PAGE_SIZE);

        // Create lock object for this analysis
        analysisLocks.put(analysisId, new Object());
        sentimentWorkerTrackMap.put(analysisId, commentSentimentSummary);

        sentimentAnalysisStatusMap.put(analysisId, commentSentimentSummary);
        commentsAnalysisSummary.setSentimentAnalysisStatusMap(sentimentAnalysisStatusMap);
        analysisSummaryPersistence.updateAnalysisSummary(videoId, commentsAnalysisSummary);

        return analysisId;
    }

    public void handleChunkAnalysisResponse(SentimentAnalysisChunkResponse chunkAnalysisResponse) {
        UUID analysisId = chunkAnalysisResponse.getAnalysisId();

        // Get or create a lock object for this specific analysis
        Object lock = analysisLocks.get(analysisId);
        if (lock == null) {
            logger.error("No lock found for analysis ID: {} - analysis may have completed or never started", analysisId);
            return;
        }

        // Synchronize only on this specific analysis
        synchronized (lock) {
            handleChunkAnalysisResponseInternal(chunkAnalysisResponse);
        }
    }

    private void handleChunkAnalysisResponseInternal(SentimentAnalysisChunkResponse chunkAnalysisResponse) {
        CommentSentimentSummary commentSentimentSummary = sentimentWorkerTrackMap.get(chunkAnalysisResponse.getAnalysisId());

        if (commentSentimentSummary == null) {
            logger.error("No analysis request found for ID: {}", chunkAnalysisResponse.getAnalysisId());
            return;
        }

        Set<UUID> processingChunkIds = commentSentimentSummary.getProcessingChunkIds();

        boolean removeProcessedChunkFromSet = processingChunkIds.remove(chunkAnalysisResponse.getAnalysisChunkId());
        if (!removeProcessedChunkFromSet) {
            logger.warn("Processed chunk ID: {} was not found in the processing set for analysis ID: {}", chunkAnalysisResponse.getAnalysisChunkId(), chunkAnalysisResponse.getAnalysisId());
            return; // Don't process this chunk if it wasn't expected
        }

        List<CommentSentimentResult> commentSentimentResults = convertToAnalysisResultList(
                chunkAnalysisResponse.getCommentSentiments(),
                commentSentimentSummary.getVideoId(),
                commentSentimentSummary.getSentimentObject()
        );

        analysisResultPersistence.saveCommentSentimentResult(commentSentimentResults);
        updateCommentSentimentCounts(commentSentimentSummary, commentSentimentResults);

        // Update total analyzed count
        int currentTotalAnalyzed = commentSentimentSummary.getTotalCommentsAnalyzed();
        int newTotalAnalyzed = currentTotalAnalyzed + commentSentimentResults.size();
        commentSentimentSummary.setTotalCommentsAnalyzed(newTotalAnalyzed);

        int totalCommentsToAnalyze = commentSentimentSummary.getTotalCommentsToAnalyze();

        logger.info("@@@ Chunk analysis internal. Chunk ID: {}. Comments in chunk: {}. Total analyzed so far: {}/{}",
                chunkAnalysisResponse.getAnalysisChunkId(),
                commentSentimentResults.size(),
                newTotalAnalyzed,
                totalCommentsToAnalyze);

        // Calculate how many comments are potentially being processed by in-flight chunks
        int chunksInFlight = processingChunkIds.size();
        int potentiallyProcessingComments = chunksInFlight * commentSentimentSummary.getPageSize();
        int estimatedTotal = newTotalAnalyzed + potentiallyProcessingComments;

        logger.info("@@@ Estimated total with in-flight chunks: {} (analyzed: {} + in-flight estimate: {})",
                estimatedTotal, newTotalAnalyzed, potentiallyProcessingComments);

        // Only queue more chunks if we haven't reached the limit AND there aren't already enough chunks in flight
        if (newTotalAnalyzed < totalCommentsToAnalyze && estimatedTotal < totalCommentsToAnalyze) {
            int nextPageNumber = commentSentimentSummary.getCurrentPage() + 1;
            int remainingComments = totalCommentsToAnalyze - newTotalAnalyzed;
            int nextPageSize = Math.min(remainingComments, commentSentimentSummary.getPageSize());

            logger.info("@@@ Queueing next chunk - page number: {}, page size: {}", nextPageNumber, nextPageSize);

            List<CommentToAnalyze> nextCommentsPage = extractCommentsPage(
                    commentSentimentSummary.getVideoId(),
                    nextPageNumber,
                    nextPageSize
            );

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
                sentimentWorkerTrackMap.put(commentSentimentSummary.getAnalysisId(), commentSentimentSummary);
            }
        } else {
            // Check if all chunks are completed
            if (processingChunkIds.isEmpty()) {
                logger.info("@@@ All chunks processed for analysis ID: {} positive: {} negative: {} neutral: {} total: {}",
                        commentSentimentSummary.getAnalysisId(),
                        commentSentimentSummary.getPositiveComments(),
                        commentSentimentSummary.getNegativeComments(),
                        commentSentimentSummary.getNeutralComments(),
                        commentSentimentSummary.getTotalCommentsAnalyzed());
                commentSentimentSummary.setCurrentPage(999);
                commentSentimentSummary.setProcessingChunkIds(null);
                handleCompleteAnalysis(commentSentimentSummary);
            }
        }
    }

    private synchronized void updateCommentSentimentCounts(CommentSentimentSummary commentSentimentSummary, List<CommentSentimentResult> newResults) {
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
        logger.info("@@@ Handling complete analysis for videoId: {} analysis obj: {}",
                commentSentimentSummary.getVideoId(),
                commentSentimentSummary.getSentimentObject());

        // Clean up tracking structures
        sentimentWorkerTrackMap.remove(commentSentimentSummary.getAnalysisId());
        analysisLocks.remove(commentSentimentSummary.getAnalysisId());

        // Update the summary status
        commentSentimentSummary.setAnalysisStatus(AnalysisStatus.COMPLETED);
        VideoCommentsSummary commentsSummary = analysisSummaryPersistence.getCommentsAnalysisSummary(commentSentimentSummary.getVideoId());
        Map<UUID, CommentSentimentSummary> sentimentAnalysisStatusMap = commentsSummary.getSentimentAnalysisStatusMap();
        sentimentAnalysisStatusMap.put(commentSentimentSummary.getAnalysisId(), commentSentimentSummary);
        commentsSummary.setSentimentAnalysisStatusMap(sentimentAnalysisStatusMap);
        analysisSummaryPersistence.updateAnalysisSummary(commentSentimentSummary.getVideoId(), commentsSummary);
    }

    private List<CommentToAnalyze> extractCommentsPage(String videoId, int pageNumber, int pageSize) {
        Pageable pageable = Pageable.ofSize(pageSize).withPage(pageNumber);
        return commentsPersistence.getCommentsPageByVideoId(videoId, pageable)
                .stream()
                .map(comment -> new CommentToAnalyze(
                        comment.getCommentId(),
                        comment.getTextDisplay(),
                        comment.getLikeCount(),
                        comment.getPublishedAt()
                ))
                .toList();
    }

    private void callAiWorkerForChunkAnalysis(SentimentAnalysisChunkRequest analysisChunkRequest) {
        String url = aiWorkerBaseUrl + "/queue";
        try {
            String response = restTemplate.postForObject(url, analysisChunkRequest, String.class);
            logger.info("@@@ AI Worker: {} ", response);
        } catch (Exception e) {
            logger.error("Error communicating with AI Worker for videoId: {} - {}",
                    analysisChunkRequest.getVideoId(), e.getMessage());
        }
    }

    private List<CommentSentimentResult> convertToAnalysisResultList(List<CommentToAnalyze> commentsSentimentAnalysisList, String videoId, String sentimentObject) {
        return commentsSentimentAnalysisList.stream()
                .map(cs -> new CommentSentimentResult(
                        cs.getCommentId(),
                        videoId,
                        cs.getLikeCount(),
                        cs.getPublishedAt(),
                        sentimentObject,
                        cs.getSentiment(),
                        cs.getSentimentReason()
                ))
                .collect(Collectors.toList());
    }
}