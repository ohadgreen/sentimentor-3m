package com.acme.controllers;

import com.acme.services.RequestQueueService;
import common.model.analysisrequest.SentimentAnalysisChunkRequest;
import common.model.analysisrequest.SentimentAnalysisRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/aiworker")
public class RequestQueueController {

    private final RequestQueueService requestQueueService;

    public RequestQueueController(RequestQueueService requestQueueService) {
        this.requestQueueService = requestQueueService;
    }

    @PostMapping("/queue")
    public String analyze(@RequestBody SentimentAnalysisChunkRequest request) throws InterruptedException {
        requestQueueService.enqueue(request);
        return "Request chunk enqueued successfully!";
    }
}
