package com.acme.controllers;

import com.acme.model.analysisreq.VideoCommentsRequest;
import com.acme.model.comment.CommentSentimentSummary;
import com.acme.model.comment.VideoCommentsSummary;
import com.acme.model.comment.ConciseComment;
import com.acme.services.AnalysisSummaryService;
import com.acme.services.RawCommentsService;
import com.acme.services.SentimentHandlingService;
import common.model.analysisrequest.SentimentAnalysisRequest;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sentiment")
public class SentimentAnalysisController {

    private final RawCommentsService rawCommentsService;
    private final AnalysisSummaryService analysisSummaryService;
    private final SentimentHandlingService sentimentHandlingService;

    public SentimentAnalysisController(RawCommentsService rawCommentsService, AnalysisSummaryService analysisSummaryService, SentimentHandlingService sentimentHandlingService) {
        this.rawCommentsService = rawCommentsService;
        this.analysisSummaryService = analysisSummaryService;
        this.sentimentHandlingService = sentimentHandlingService;
    }

    @GetMapping("/comments/{videoId}")
    public VideoCommentsSummary getCommentsAnalysisSummary(@PathVariable String videoId) {
        return analysisSummaryService.getCommentsAnalysisSummary(videoId);
    }
    @GetMapping("/sentimentOngoingAnalysis/{videoId}/{analysisId}")
    public CommentSentimentSummary getOnGoingSentimentAnalysisSummary(
            @PathVariable String videoId,
            @PathVariable UUID analysisId)  {
        return sentimentHandlingService.getOngoingSentimentAnalysis(videoId, analysisId);
    }

    @PostMapping("/analyzeRequest")
    public UUID sentimentAnalysisRequest(@RequestBody SentimentAnalysisRequest sentimentAnalysisRequest) {
        return sentimentHandlingService.handleVideoSentimentAnalysisReq(sentimentAnalysisRequest);
    }

    @PostMapping("/getRawVideoComments")
    public VideoCommentsSummary getRawVideoComments(@RequestBody VideoCommentsRequest videoCommentsRequest) {
        return rawCommentsService.getRawVideoComments(videoCommentsRequest);
    }

}
