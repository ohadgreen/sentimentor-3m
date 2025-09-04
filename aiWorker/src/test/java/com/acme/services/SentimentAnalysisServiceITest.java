package com.acme.services;

import common.model.analysisrequest.*;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("memory")
class SentimentAnalysisServiceITest {

    @Autowired
    SentimentAnalysisService sentimentAnalysisService;

    @Autowired
    private ChatModel chatModel;
    @Test
    void testSentimentAnalysisPrompt() {

        SentimentAnalysisChunkRequest sentimentReqTest = getSentimentAnalysisRequest();

        SentimentAnalysisChunkResponse response = sentimentAnalysisService.analyzeCommentsChunk(sentimentReqTest);

        assertNotNull(response, "Response should not be null");
        assertFalse(response.getCommentSentiments().isEmpty(), "Sentiments list should not be empty");

        for (CommentToAnalyze commentToAnalyze : response.getCommentSentiments()) {
            System.out.println("Comment ID: " + commentToAnalyze.getCommentId() +
                    " - sentiment: " + commentToAnalyze.getSentiment() +
                    " - reason: " + commentToAnalyze.getSentimentReason() +
                    " - likes: " + commentToAnalyze.getLikeCount()
            );
            if (commentToAnalyze.getCommentId().equals("c2")) {
                assertEquals(Sentiment.POSITIVE, commentToAnalyze.getSentiment(), "Comment c2 should be positive");
            }
        }
    }

    private static SentimentAnalysisChunkRequest getSentimentAnalysisRequest() {
        String analysisObject = "Google";
        String c1 = "Until Veo2 is released to the public, it is just fiction";
        String c2 = "Google is superior!";
        String c3 = "There's no way in hell I'm coming back to Chrome for their AI addons. I hope they come to Firefox.";
        String c4 = "Google products always getting better and better. I love it!";
        String c5 = "No worries! Sundar Pichai knows his stuff. He'll make sure everything is fine.";

        SentimentAnalysisChunkRequest sentimentAnalysisChunkRequest = new SentimentAnalysisChunkRequest(
                analysisObject,
                "Google AI DEMOLISHES OpenAI! The Ultimate AI Showdown is OVER?",
                "Google's new video generation model is called Veo 2. SORA is a video generation model created by OpenAI",
                Arrays.asList(new CommentToAnalyze("c1", c1, 10, null),
                        new CommentToAnalyze("c2", c2, 9, LocalDateTime.parse("2025-01-01T00:00:00")),
                        new CommentToAnalyze("c3", c3, 9, LocalDateTime.parse("2025-01-02T00:00:00")),
                        new CommentToAnalyze("c4", c4, 7, null),
                        new CommentToAnalyze("c5", c5, 1, null)),
                0, 0
        );

        sentimentAnalysisChunkRequest.setAnalysisChunkId(java.util.UUID.randomUUID());

        return sentimentAnalysisChunkRequest;
    }

}