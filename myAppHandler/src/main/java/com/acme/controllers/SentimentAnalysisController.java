package com.acme.controllers;

import com.acme.model.analysisreq.VideoCommentsRequest;
import com.acme.model.comment.CommentsAnalyzeSummary;
import com.acme.model.comment.ConciseComment;
import com.acme.services.AnalysisSummaryService;
import com.acme.services.CommentsHandlingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sentiment")
public class SentimentAnalysisController {

    private final Logger logger = LoggerFactory.getLogger(SentimentAnalysisController.class);

    private final CommentsHandlingService commentsHandlingService;
    private final AnalysisSummaryService analysisSummaryService;

    public SentimentAnalysisController(CommentsHandlingService commentsHandlingService, AnalysisSummaryService analysisSummaryService) {
        this.commentsHandlingService = commentsHandlingService;
        this.analysisSummaryService = analysisSummaryService;
    }

    @GetMapping("/comments/{videoId}")
    public CommentsAnalyzeSummary getCommentsAnalysisSummary(@PathVariable String videoId) {
        return analysisSummaryService.getCommentsAnalysisSummary(videoId);
    }

    @PostMapping("/videocomments")
    public String getRawComments(@RequestBody VideoCommentsRequest videoCommentsRequest) {
        commentsHandlingService.handleGetCommentList(videoCommentsRequest);
        return "action completed";
    }

    @GetMapping("/comments/{videoId}/{commentsNum}")
    public List<ConciseComment> getConciseComments(@PathVariable String videoId, @PathVariable Integer commentsNum) {
        return commentsHandlingService.getConciseCommentList(videoId, commentsNum);
    }

}
