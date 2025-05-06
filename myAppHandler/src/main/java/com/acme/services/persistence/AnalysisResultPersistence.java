package com.acme.services.persistence;


import com.acme.model.comment.CommentSentimentResult;

import java.util.List;

public interface AnalysisResultPersistence {
    void saveCommentSentimentResult(List<CommentSentimentResult> commentSentimentResults);
    CommentSentimentResult getCommentSentimentResultByCommentIdAndSentimentObject(String commentId, String sentimentObject);
    List<CommentSentimentResult> getCommentSentimentResultsByVideoIdAndSentimentObject(String videoId, String sentimentObject);
    void deleteCommentSentimentResultByVideoId(String videoId);
}
