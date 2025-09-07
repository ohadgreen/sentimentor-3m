package com.acme.services;

import common.model.analysisrequest.*;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

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
            if (commentToAnalyze.getCommentId().contains("pos")) {
                assertEquals(Sentiment.POSITIVE, commentToAnalyze.getSentiment(), "Expected POSITIVE sentiment");
            } else if (commentToAnalyze.getCommentId().contains("neg")) {
                assertEquals(Sentiment.NEGATIVE, commentToAnalyze.getSentiment(), "Expected NEGATIVE sentiment");
            } else if (commentToAnalyze.getCommentId().contains("neu")) {
                assertEquals(Sentiment.NEUTRAL, commentToAnalyze.getSentiment(), "Expected NEUTRAL sentiment");
            } else if (commentToAnalyze.getCommentId().contains("unk")) {
                assertEquals(Sentiment.UNKNOWN, commentToAnalyze.getSentiment(), "Expected NEUTRAL sentiment for unknown");
            }
        }
    }

    private static SentimentAnalysisChunkRequest getSentimentAnalysisRequest() {
        String analysisObject = "Google";
        String neu1 = "Until Veo2 is released to the public, it is just fiction";
        String pos2 = "Google is superior!";
        String neg3 = "There's no way in hell I'm coming back to Chrome for their AI addons. I hope they come to Firefox.";
        String pos4 = "Their products always getting better and better. I love it!";
        String neu5 = "No worries! Sundar Pichai knows his stuff. He'll make sure everything is fine.";
        String unk6 = "Facebook is a wonderful platform for connecting with friends and family.";

        SentimentAnalysisChunkRequest sentimentAnalysisChunkRequest = new SentimentAnalysisChunkRequest(
                analysisObject,
                "Google AI DEMOLISHES OpenAI! The Ultimate AI Showdown is OVER?",
                "Google's new video generation model is called Veo 2. SORA is a video generation model created by OpenAI",
                Arrays.asList(new CommentToAnalyze("neu1", neu1, 10, null),
                        new CommentToAnalyze("pos2", pos2, 9, LocalDateTime.parse("2025-01-01T00:00:00")),
                        new CommentToAnalyze("neg3", neg3, 9, LocalDateTime.parse("2025-01-02T00:00:00")),
                        new CommentToAnalyze("pos4", pos4, 7, null),
                        new CommentToAnalyze("neu5", neu5, 1, null),
                        new CommentToAnalyze("unk6", unk6, 0, null)
                ),
                0, 0
        );

        sentimentAnalysisChunkRequest.setAnalysisChunkId(java.util.UUID.randomUUID());

        return sentimentAnalysisChunkRequest;
    }

}