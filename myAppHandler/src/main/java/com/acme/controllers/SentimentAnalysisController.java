package com.acme.controllers;

import com.acme.model.analysisreq.VideoCommentsRequest;
import com.acme.model.comment.VideoCommentsSummary;
import com.acme.model.comment.ConciseComment;
import com.acme.services.AnalysisSummaryService;
import com.acme.services.RawCommentsService;
import com.acme.services.SentimentHandlingService;
import common.model.analysisrequest.SentimentAnalysisChunkResponse;
import common.model.analysisrequest.SentimentAnalysisRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sentiment")
public class SentimentAnalysisController {

    private final Logger logger = LoggerFactory.getLogger(SentimentAnalysisController.class);

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

    @PostMapping("/analyzeRequest")
    public UUID sentimentAnalysisRequest(@RequestBody SentimentAnalysisRequest sentimentAnalysisRequest) {
        return sentimentHandlingService.handleVideoSentimentAnalysisReq(sentimentAnalysisRequest);
    }

    @PostMapping("/videocomments")
    public String getRawComments(@RequestBody VideoCommentsRequest videoCommentsRequest) {
        rawCommentsService.getRawVideoComments(videoCommentsRequest);
        return "action completed";
    }

    @PostMapping("/commentsList")
    public VideoCommentsSummary videoCommentsListRequest(@RequestBody VideoCommentsRequest videoCommentsRequest) {
        return rawCommentsService.getRawVideoComments(videoCommentsRequest);
    }

    @GetMapping("/comments/{videoId}/{commentsNum}")
    public List<ConciseComment> getConciseComments(@PathVariable String videoId, @PathVariable Integer commentsNum) {
        return rawCommentsService.getConciseCommentList(videoId, commentsNum);
    }

}
