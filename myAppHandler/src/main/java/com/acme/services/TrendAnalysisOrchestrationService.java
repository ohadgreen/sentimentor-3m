package com.acme.services;

import com.acme.model.analysisreq.VideoCommentsRequest;
import com.acme.model.comment.AnalysisStatus;
import com.acme.model.comment.CommentSentimentSummary;
import com.acme.model.comment.VideoCommentsSummary;
import com.acme.model.trend.DailyTrendResult;
import com.acme.model.trend.TrendAnalysisJob;
import com.acme.model.trend.TrendAnalysisRequest;
import com.acme.model.trend.TrendJobStatus;
import com.acme.model.ytsearch.VideoSearchItem;
import com.acme.model.ytsearch.VideoSearchResult;
import com.acme.repositories.TrendAnalysisJobRepository;
import com.acme.services.persistence.AnalysisSummaryPersistence;
import common.model.analysisrequest.SentimentAnalysisRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TrendAnalysisOrchestrationService {

    private static final Logger logger = LoggerFactory.getLogger(TrendAnalysisOrchestrationService.class);
    private static final long POLL_INTERVAL_MS = 2000;
    private static final long VIDEO_ANALYSIS_TIMEOUT_MS = 5 * 60 * 1000;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final TrendAnalysisJobRepository trendJobRepository;
    private final GetYouTubeVideoSnippets getYouTubeVideoSnippets;
    private final RawCommentsService rawCommentsService;
    private final SentimentHandlingService sentimentHandlingService;
    private final AnalysisSummaryPersistence analysisSummaryPersistence;

    @Lazy
    @Autowired
    private TrendAnalysisOrchestrationService self;

    public TrendAnalysisOrchestrationService(TrendAnalysisJobRepository trendJobRepository,
                                             GetYouTubeVideoSnippets getYouTubeVideoSnippets,
                                             RawCommentsService rawCommentsService,
                                             SentimentHandlingService sentimentHandlingService,
                                             AnalysisSummaryPersistence analysisSummaryPersistence) {
        this.trendJobRepository = trendJobRepository;
        this.getYouTubeVideoSnippets = getYouTubeVideoSnippets;
        this.rawCommentsService = rawCommentsService;
        this.sentimentHandlingService = sentimentHandlingService;
        this.analysisSummaryPersistence = analysisSummaryPersistence;
    }

    public String startTrendJob(TrendAnalysisRequest request, String userId) {
        String jobId = UUID.randomUUID().toString();
        TrendAnalysisJob job = new TrendAnalysisJob(jobId, userId, request.getSearchQuery(),
                request.getSentimentObject(), request.getDaysBack(),
                request.getCommentsPerVideo(), request.getVideosPerDay());
        trendJobRepository.save(job);
        logger.info("Created trend job: {}", jobId);
        self.runTrendJob(jobId);  // called via proxy so @Async takes effect
        return jobId;
    }

    @Async("trendAnalysisExecutor")
    public void runTrendJob(String jobId) {
        TrendAnalysisJob job = trendJobRepository.findById(jobId).orElse(null);
        if (job == null) {
            logger.error("Trend job not found: {}", jobId);
            return;
        }

        job.setJobStatus(TrendJobStatus.IN_PROGRESS);
        job.setTotalDays(job.getDaysBack() - 1);
        trendJobRepository.save(job);

        try {
            LocalDate today = LocalDate.now(ZoneOffset.UTC);

            for (int i = job.getDaysBack() - 1; i >= 1; i--) {
                LocalDate day = today.minusDays(i);
                Instant dayStart = day.atStartOfDay(ZoneOffset.UTC).toInstant();
                Instant dayEnd = day.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
                String dateLabel = day.format(DATE_FORMATTER);

                logger.info("Trend job {}: processing day {}", jobId, dateLabel);

                DailyTrendResult dayResult = processSingleDay(job, dayStart, dayEnd, dateLabel);
                job.getDailyResults().add(dayResult);
                job.setDaysCompleted(job.getDaysCompleted() + 1);
                trendJobRepository.save(job);
            }

            job.setJobStatus(TrendJobStatus.COMPLETED);
            trendJobRepository.save(job);
            logger.info("Trend job {} completed", jobId);

        } catch (Exception e) {
            logger.error("Trend job {} failed", jobId, e);
            job.setJobStatus(TrendJobStatus.FAILED);
            job.setErrorMessage(e.getMessage());
            trendJobRepository.save(job);
        }
    }

    private DailyTrendResult processSingleDay(TrendAnalysisJob job, Instant dayStart, Instant dayEnd, String dateLabel) {
        DailyTrendResult dayResult = new DailyTrendResult(dateLabel);
        List<String> videoIds = new ArrayList<>();

        VideoSearchResult searchResult = getYouTubeVideoSnippets.searchVideos(
                job.getSearchQuery(), null, dayStart, dayEnd);

        if (searchResult == null || searchResult.getItems() == null || searchResult.getItems().isEmpty()) {
            logger.info("No videos found for day {}", dateLabel);
            dayResult.setVideoIds(videoIds);
            return dayResult;
        }

        List<VideoSearchItem> videos = searchResult.getItems().stream()
                .filter(v -> hasEnoughComments(v, 100))
                .limit(job.getVideosPerDay())
                .toList();

        for (VideoSearchItem video : videos) {
            String videoId = video.getId() != null ? video.getId().getVideoId() : null;
            if (videoId == null) continue;

            videoIds.add(videoId);
            try {
                processVideoForDay(job, videoId, dayResult);
            } catch (Exception e) {
                logger.warn("Failed to process video {} for day {}: {}", videoId, dateLabel, e.getMessage());
            }
        }

        dayResult.setVideoIds(videoIds);
        dayResult.setVideoCount(videoIds.size());
        return dayResult;
    }

    private void processVideoForDay(TrendAnalysisJob job, String videoId, DailyTrendResult dayResult)
            throws InterruptedException {
        VideoCommentsRequest commentsRequest = new VideoCommentsRequest();
        commentsRequest.setVideoId(videoId);
        commentsRequest.setTotalCommentsRequired(job.getCommentsPerVideo());
        rawCommentsService.getRawVideoComments(commentsRequest);
        analysisSummaryPersistence.markAsTrend(videoId);

        SentimentAnalysisRequest analysisRequest = new SentimentAnalysisRequest();
        analysisRequest.setVideoId(videoId);
        analysisRequest.setAnalysisObject(job.getSentimentObject());
        analysisRequest.setTotalCommentsToAnalyze(job.getCommentsPerVideo());

        UUID analysisId = sentimentHandlingService.handleVideoSentimentAnalysisReq(analysisRequest);

        if (analysisId == null) {
            // Duplicate guard hit — find the existing completed analysis
            analysisId = findExistingAnalysisId(videoId, job.getSentimentObject());
        }

        if (analysisId == null) {
            logger.warn("Could not get analysis ID for video {}", videoId);
            return;
        }

        CommentSentimentSummary summary = pollUntilComplete(videoId, analysisId);
        if (summary != null) {
            dayResult.setPositiveComments(dayResult.getPositiveComments() + summary.getPositiveComments());
            dayResult.setNegativeComments(dayResult.getNegativeComments() + summary.getNegativeComments());
            dayResult.setNeutralComments(dayResult.getNeutralComments() + summary.getNeutralComments());
            dayResult.setTotalCommentsAnalyzed(dayResult.getTotalCommentsAnalyzed() + summary.getTotalCommentsAnalyzed());
        }
    }

    private UUID findExistingAnalysisId(String videoId, String sentimentObject) {
        VideoCommentsSummary summary = analysisSummaryPersistence.getCommentsAnalysisSummary(videoId);
        if (summary == null) return null;

        return summary.getSentimentAnalysisStatusMap().entrySet().stream()
                .filter(e -> sentimentObject.equals(e.getValue().getSentimentObject()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    private CommentSentimentSummary pollUntilComplete(String videoId, UUID analysisId)
            throws InterruptedException {
        long deadline = System.currentTimeMillis() + VIDEO_ANALYSIS_TIMEOUT_MS;

        while (System.currentTimeMillis() < deadline) {
            CommentSentimentSummary summary = sentimentHandlingService.getOngoingSentimentAnalysis(videoId, analysisId);
            if (summary != null && AnalysisStatus.COMPLETED.equals(summary.getAnalysisStatus())) {
                return summary;
            }
            Thread.sleep(POLL_INTERVAL_MS);
        }

        logger.warn("Timed out waiting for analysis {} on video {}", analysisId, videoId);
        return null;
    }

    private boolean hasEnoughComments(VideoSearchItem video, int minComments) {
        try {
            return video.getStatistics() != null
                    && Integer.parseInt(video.getStatistics().getCommentCount()) >= minComments;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
