package com.acme.listeners;

import com.acme.services.SentimentHandlingService;
import common.model.analysisrequest.SentimentAnalysisChunkResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class AnalysisResponseListener {

    private final Logger logger = LoggerFactory.getLogger(AnalysisResponseListener.class);
    private final SentimentHandlingService sentimentHandlingService;

    public AnalysisResponseListener(SentimentHandlingService sentimentHandlingService) {
        this.sentimentHandlingService = sentimentHandlingService;
    }

    @JmsListener(destination = "${jms.queues.analysis-response}", containerFactory = "jmsListenerContainerFactory")
    public void onMessage(SentimentAnalysisChunkResponse response) {
        logger.info("Received analysis response for analysisId: {}", response.getAnalysisId());
        sentimentHandlingService.handleChunkAnalysisResponse(response);
    }
}
