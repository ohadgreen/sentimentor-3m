package com.acme.repositories;

import com.acme.model.comment.CommentSentimentResult;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CommentSentimentRepository extends MongoRepository<CommentSentimentResult, String>  {
    CommentSentimentResult findCommentSentimentResultByCommentIdAndSentimentObject(String commentId, String sentimentObject);
    List<CommentSentimentResult> findCommentSentimentResultsByVideoIdAndSentimentObject(String videoId, String sentimentObject);
//    List<CommentSentimentResult> findCommentSentimentResultsByCommentIds(List<String> commentId);
    List<CommentSentimentResult> findCommentSentimentResultsByVideoIdAndSentimentObject(String videoId, String sentimentObject, Pageable pageable);
    void deleteByVideoId(String videoId);
}
