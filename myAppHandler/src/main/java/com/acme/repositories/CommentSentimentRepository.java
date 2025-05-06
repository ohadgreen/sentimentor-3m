package com.acme.repositories;

import com.acme.model.comment.CommentSentimentResult;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CommentSentimentRepository extends MongoRepository<CommentSentimentResult, String>  {
    CommentSentimentResult findCommentSentimentResultByCommentIdAndSentimentObject(String commentId, String sentimentObject);
    List<CommentSentimentResult> findCommentSentimentResultsByVideoIdAndSentimentObject(String videoId, String sentimentObject);
    void deleteByVideoId(String videoId);
}
