package com.acme.controllers;

import com.acme.model.analysisreq.VideoCommentsRequest;
import com.acme.model.comment.VideoCommentsSummary;
import com.acme.model.comment.ConciseComment;
import com.acme.services.AnalysisSummaryService;
import com.acme.services.RawCommentsService;
import common.model.analysisrequest.SentimentAnalysisChunkResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sentiment")
public class SentimentAnalysisController {

    private final Logger logger = LoggerFactory.getLogger(SentimentAnalysisController.class);

    private final RawCommentsService rawCommentsService;
    private final AnalysisSummaryService analysisSummaryService;

    public SentimentAnalysisController(RawCommentsService rawCommentsService, AnalysisSummaryService analysisSummaryService) {
        this.rawCommentsService = rawCommentsService;
        this.analysisSummaryService = analysisSummaryService;
    }

    @GetMapping("/comments/{videoId}")
    public VideoCommentsSummary getCommentsAnalysisSummary(@PathVariable String videoId) {
        return analysisSummaryService.getCommentsAnalysisSummary(videoId);
    }

    @PostMapping("/videocomments")
    public String getRawComments(@RequestBody VideoCommentsRequest videoCommentsRequest) {
        rawCommentsService.getRawVideoComments(videoCommentsRequest);
        return "action completed";
    }

    @GetMapping("/comments/{videoId}/{commentsNum}")
    public List<ConciseComment> getConciseComments(@PathVariable String videoId, @PathVariable Integer commentsNum) {
        return rawCommentsService.getConciseCommentList(videoId, commentsNum);
    }

}
