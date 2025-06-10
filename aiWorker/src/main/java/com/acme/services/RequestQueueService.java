package com.acme.services;

import common.model.analysisrequest.SentimentAnalysisChunkRequest;
import common.model.analysisrequest.SentimentAnalysisRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class RequestQueueService {

    private final BlockingQueue<SentimentAnalysisChunkRequest> queue;

    public RequestQueueService(@Value("${consumer.pool.queue-capacity}") int capacity) {
        this.queue = new LinkedBlockingQueue<>(capacity);
    }

    public void enqueue(SentimentAnalysisChunkRequest request) throws InterruptedException {
        queue.put(request);
    }

    public SentimentAnalysisChunkRequest dequeue() throws InterruptedException {
        return queue.take();
    }
}
