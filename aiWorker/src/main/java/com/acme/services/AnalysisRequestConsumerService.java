package com.acme.services;

import common.model.analysisrequest.SentimentAnalysisRequest;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executor;

@Service
public class AnalysisRequestConsumerService {
    private final RequestQueueService queueService;
    private final SentimentAnalysisService analysisService;
    private final Executor consumerExecutor;
    private static final int CONSUMER_COUNT = 5;

    public AnalysisRequestConsumerService(RequestQueueService queueService,
                                          SentimentAnalysisService analysisService,
                                          @Qualifier("sentimentConsumerExecutor") Executor consumerExecutor) {
        this.queueService = queueService;
        this.analysisService = analysisService;
        this.consumerExecutor = consumerExecutor;
    }

    @PostConstruct
    public void startConsumers() {
        for (int i = 0; i < CONSUMER_COUNT; i++) {
            consumerExecutor.execute(this::consume);
        }
    }

    private void consume() {
        while (true) {
            try {
                SentimentAnalysisRequest request = queueService.dequeue();
                analysisService.analyzeCommentsChunk(request);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
