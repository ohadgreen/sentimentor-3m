package com.acme.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.model.analysisrequest.CommentSentiment;
import common.model.analysisrequest.SentimentAnalysisChunkRequest;
import common.model.analysisrequest.SentimentAnalysisRequest;
import common.model.analysisrequest.SentimentAnalysisResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SentimentAnalysisService {

    private final Logger logger = LoggerFactory.getLogger(SentimentAnalysisService.class);
    private final ChatModel chatModel;

    @Value("classpath:templates/comments-sentiment-analysis-prompt.st")
    private Resource sentimentAnalysisPrompt;

    public SentimentAnalysisService(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public void communicateAnalysisResponseFromRequest(SentimentAnalysisChunkRequest analysisRequest) {
        SentimentAnalysisResponse response = analyzeCommentsChunk(analysisRequest);
        if (response != null) {
            // Here you would typically send the response back to the requester or store it in a database
            logger.info("Sentiment analysis completed for request: {}", analysisRequest.getVideoId());
        } else {
            logger.error("Failed to analyze comments for request: {}", analysisRequest.getVideoId());
        }

    }

    public SentimentAnalysisResponse analyzeCommentsChunk(SentimentAnalysisChunkRequest analysisRequest) {

        String allComments = analysisRequest.getComments().stream()
                .map(comment -> "\"" + comment.getCommentId() + "\": \"" + comment.getCommentText() + "\"")
                .reduce((c1, c2) -> c1 + "\n" + c2)
                .orElse("");

        PromptTemplate promptTemplate = new PromptTemplate(sentimentAnalysisPrompt);
        Prompt prompt = promptTemplate.create(Map.of(
                "object", analysisRequest.getAnalysisObject(),
                "commentsNumber", analysisRequest.getComments().size(),
                "moreInfo", analysisRequest.getMoreInfo(),
                "videoTitle", analysisRequest.getVideoTitle(),
                "comments", allComments)
        );

        logger.debug("prompt: {}", prompt.toString());

        ChatResponse response = chatModel.call(prompt);
        String commentAnalysisListString = response.getResult().getOutput().getContent();

        ObjectMapper mapper = new ObjectMapper();
        try {
            List<CommentSentiment> sentiments = mapper.readValue(commentAnalysisListString, new TypeReference<>() {
            });
            return new SentimentAnalysisResponse(UUID.randomUUID(), sentiments);

        } catch (JsonProcessingException e) {
            logger.error("Cannot parse response to json: {}", e.getMessage() );
            return null;
        }

    }

}
