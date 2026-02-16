package com.acme.repositories;

import com.acme.model.comment.CommentSentimentResult;
import common.model.analysisrequest.Sentiment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentSentimentRepository extends MongoRepository<CommentSentimentResult, String>  {
    CommentSentimentResult findCommentSentimentResultByCommentIdAndSentimentObject(String commentId, String sentimentObject);
    List<CommentSentimentResult> findCommentSentimentResultsByVideoIdAndSentimentObject(String videoId, String sentimentObject);
//    List<CommentSentimentResult> findCommentSentimentResultsByCommentIds(List<String> commentId);
    List<CommentSentimentResult> findCommentSentimentResultsByVideoIdAndSentimentObject(String videoId, String sentimentObject, Pageable pageable);
    List<CommentSentimentResult> findByVideoIdAndSentimentObjectAndCommentIdIn(
            String videoId, String sentimentObject, List<String> commentIds);
    Page<CommentSentimentResult> findByVideoIdAndSentimentObjectAndSentiment(
            String videoId, String sentimentObject, Sentiment sentiment, Pageable pageable);
    List<CommentSentimentResult> findByVideoIdAndCommentIdIn(String videoId, List<String> commentIds);
    void deleteByVideoId(String videoId);
    long deleteByCreateDateBefore(LocalDateTime date);
}
