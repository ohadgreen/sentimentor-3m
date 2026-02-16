package com.acme.services.persistence;


import com.acme.model.comment.CommentSentimentResult;
import common.model.analysisrequest.Sentiment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AnalysisResultPersistence {
    void saveCommentSentimentResult(List<CommentSentimentResult> commentSentimentResults);
    CommentSentimentResult getCommentSentimentResultByCommentIdAndSentimentObject(String commentId, String sentimentObject);
    List<CommentSentimentResult> getCommentSentimentResultsByVideoIdAndSentimentObject(String videoId, String sentimentObject);
    List<CommentSentimentResult> getCommentSentimentResultsByVideoIdAndSentimentObject(String videoId, String sentimentObject, Pageable pageable);
    List<CommentSentimentResult> getCommentSentimentResultsByVideoIdAndSentimentObjectAndCommentIdIn(
            String videoId, String sentimentObject, List<String> commentIds);
    Page<CommentSentimentResult> getCommentSentimentResultsPageByVideoIdAndSentimentObjectAndSentiment(
            String videoId, String sentimentObject, Sentiment sentiment, Pageable pageable);
    List<CommentSentimentResult> getCommentSentimentResultsByVideoIdAndCommentIdIn(
            String videoId, List<String> commentIds);
    void deleteCommentSentimentResultByVideoId(String videoId);
}
