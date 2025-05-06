package com.acme.controllers;

import com.acme.services.RequestQueueService;
import common.model.analysisrequest.SentimentAnalysisRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/requestqueue")
public class RequestQueueController {

    private final RequestQueueService requestQueueService;

    public RequestQueueController(RequestQueueService requestQueueService) {
        this.requestQueueService = requestQueueService;
    }

    @PostMapping("/analyze")
    public String analyze(@RequestBody SentimentAnalysisRequest request) throws InterruptedException {
        requestQueueService.enqueue(request);
        return "Request enqueued successfully!";
    }
}
