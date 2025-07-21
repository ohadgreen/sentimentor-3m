package com.acme.controllers;

import com.acme.services.SentimentHandlingService;
import common.model.analysisrequest.SentimentAnalysisChunkResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sentimentChunk")
public class SentimentAnalysisChunkController {

    private final SentimentHandlingService sentimentHandlingService;

    public SentimentAnalysisChunkController(SentimentHandlingService sentimentHandlingService) {
        this.sentimentHandlingService = sentimentHandlingService;
    }

    @PostMapping("/handler")
    public String getRawComments(@RequestBody SentimentAnalysisChunkResponse response) {
        sentimentHandlingService.handleChunkAnalysisResponse(response);
        return "action completed";
    }

}
