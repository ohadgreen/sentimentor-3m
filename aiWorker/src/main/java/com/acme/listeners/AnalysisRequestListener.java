package com.acme.listeners;

import com.acme.services.SentimentAnalysisService;
import common.model.analysisrequest.SentimentAnalysisChunkRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class AnalysisRequestListener {

    private final Logger logger = LoggerFactory.getLogger(AnalysisRequestListener.class);
    private final SentimentAnalysisService sentimentAnalysisService;

    public AnalysisRequestListener(SentimentAnalysisService sentimentAnalysisService) {
        this.sentimentAnalysisService = sentimentAnalysisService;
    }

    @JmsListener(destination = "${jms.queues.analysis-request}", containerFactory = "jmsListenerContainerFactory")
    public void onMessage(SentimentAnalysisChunkRequest request) {
        logger.info("Received analysis request for videoId: {}", request.getVideoId());
        sentimentAnalysisService.communicateAnalysisResponseFromRequest(request);
    }
}
