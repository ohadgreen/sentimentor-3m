package com.acme.controllers;

import com.acme.services.SentimentAnalysisService;
import common.model.analysisrequest.SentimentAnalysisChunkRequest;
import common.model.analysisrequest.SentimentAnalysisChunkResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/aianalysis")
public class SentimentAnalysisController {
    private final SentimentAnalysisService sentimentAnalysisService;

    public SentimentAnalysisController(SentimentAnalysisService sentimentAnalysisService) {
        this.sentimentAnalysisService = sentimentAnalysisService;
    }

    @PostMapping("/comments")
    public SentimentAnalysisChunkResponse analyzeCommentsSentiment(@RequestBody SentimentAnalysisChunkRequest sentimentAnalysisRequest) {
        return sentimentAnalysisService.analyzeCommentsChunk(sentimentAnalysisRequest);
    }
}
