package com.acme.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.model.analysisrequest.CommentSentiment;
import common.model.analysisrequest.SentimentAnalysisChunkRequest;
import common.model.analysisrequest.SentimentAnalysisChunkResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class SentimentAnalysisService {

    private final Logger logger = LoggerFactory.getLogger(SentimentAnalysisService.class);
    private final ChatModel chatModel;
    private final RestTemplate restTemplate;

    @Value("classpath:templates/comments-sentiment-analysis-prompt.st")
    private Resource sentimentAnalysisPrompt;
    @Value("${app.handler.base-url}")
    private String appHandlerBaseUrl;

    public SentimentAnalysisService(ChatModel chatModel, RestTemplate restTemplate) {
        this.chatModel = chatModel;
        this.restTemplate = restTemplate;
    }

    public void communicateAnalysisResponseFromRequest(SentimentAnalysisChunkRequest analysisRequest) {
        SentimentAnalysisChunkResponse chunkAnalysisResponse = analyzeCommentsChunk(analysisRequest);
        if (chunkAnalysisResponse != null) {

            String url = appHandlerBaseUrl + "/sentimentChunk/handler";
            try {
                String appHandlerResponse = restTemplate.postForObject(url, chunkAnalysisResponse, String.class);
                System.out.println("@@@ Response from chunk handler: " + appHandlerResponse);
            } catch (Exception e) {
                logger.error("Error communicating with app handler for videoId: {} - {}", analysisRequest.getVideoId(), e.getMessage());
            }

            logger.info("Sentiment analysis completed for request: {}", analysisRequest.getVideoId());
        } else {
            logger.error("Failed to analyze comments for request: {}", analysisRequest.getVideoId());
        }

    }

    public SentimentAnalysisChunkResponse analyzeCommentsChunk(SentimentAnalysisChunkRequest analysisChunkRequest) {

        String allComments = analysisChunkRequest.getComments().stream()
                .map(comment -> "\"" + comment.getCommentId() + "\": \"" + comment.getCommentText() + "\"")
                .reduce((c1, c2) -> c1 + "\n" + c2)
                .orElse("");

        System.out.println("@@@ All comments: " + allComments);

        PromptTemplate promptTemplate = new PromptTemplate(sentimentAnalysisPrompt);
        System.out.println("@@@ Prompt template: " + promptTemplate.getTemplate());

        String analysisObject = analysisChunkRequest.getAnalysisObject() != null ?
                analysisChunkRequest.getAnalysisObject() : "the content";
        String moreInfo = analysisChunkRequest.getMoreInfo() != null ?
                analysisChunkRequest.getMoreInfo() : "";
        String videoTitle = analysisChunkRequest.getVideoTitle() != null ?
                analysisChunkRequest.getVideoTitle() : "Unknown Video";

        Prompt prompt = promptTemplate.create(Map.of(
                "object", analysisObject,
                "commentsNumber", analysisChunkRequest.getComments().size(),
                "moreInfo", moreInfo,
                "videoTitle", videoTitle,
                "comments", allComments)
        );

        logger.debug("prompt: {}", prompt.toString());

        ChatResponse response = chatModel.call(prompt);
        String commentAnalysisListString = response.getResult().getOutput().getContent();

        ObjectMapper mapper = new ObjectMapper();
        try {
            List<CommentSentiment> sentiments = mapper.readValue(commentAnalysisListString, new TypeReference<>() {
            });
            return new SentimentAnalysisChunkResponse(analysisChunkRequest.getAnalysisId(), analysisChunkRequest.getAnalysisChunkId(), sentiments);

        } catch (JsonProcessingException e) {
            logger.error("Cannot parse response to json: {}", e.getMessage() );
            return null;
        }

    }

}
