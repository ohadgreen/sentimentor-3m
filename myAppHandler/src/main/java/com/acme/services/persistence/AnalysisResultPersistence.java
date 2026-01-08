package com.acme.services.persistence;


import com.acme.model.comment.CommentSentimentResult;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AnalysisResultPersistence {
    void saveCommentSentimentResult(List<CommentSentimentResult> commentSentimentResults);
    CommentSentimentResult getCommentSentimentResultByCommentIdAndSentimentObject(String commentId, String sentimentObject);
    List<CommentSentimentResult> getCommentSentimentResultsByVideoIdAndSentimentObject(String videoId, String sentimentObject);
    List<CommentSentimentResult> getCommentSentimentResultsByVideoIdAndSentimentObject(String videoId, String sentimentObject, Pageable pageable);
    void deleteCommentSentimentResultByVideoId(String videoId);
}
