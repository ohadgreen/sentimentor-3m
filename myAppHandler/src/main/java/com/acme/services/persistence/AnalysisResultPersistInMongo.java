package com.acme.services.persistence;

import com.acme.model.comment.CommentSentimentResult;
import com.acme.repositories.CommentSentimentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("db")
public class AnalysisResultPersistInMongo implements AnalysisResultPersistence {

    @Autowired
    private CommentSentimentRepository commentSentimentRepository;

    @Override
    public void saveCommentSentimentResult(List<CommentSentimentResult> commentSentimentResults) {
        if (commentSentimentResults.isEmpty()) {
            return;
        }
        commentSentimentRepository.saveAll(commentSentimentResults);
    }

    @Override
    public CommentSentimentResult getCommentSentimentResultByCommentIdAndSentimentObject(String commentId, String sentimentObject) {
        return commentSentimentRepository.findCommentSentimentResultByCommentIdAndSentimentObject(commentId, sentimentObject);
    }

    @Override
    public List<CommentSentimentResult> getCommentSentimentResultsByVideoIdAndSentimentObject(String videoId, String sentimentObject) {
        return commentSentimentRepository.findCommentSentimentResultsByVideoIdAndSentimentObject(videoId, sentimentObject);
    }

    @Override
    public List<CommentSentimentResult> getCommentSentimentResultsByVideoIdAndSentimentObject(String videoId, String sentimentObject, Pageable pageable) {
        Sort defaultSort = Sort.by(
                Sort.Order.desc("likeCount"),
                Sort.Order.desc("publishedAt")
        );

        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort().isSorted() ? pageable.getSort() : defaultSort
        );

        return commentSentimentRepository.findCommentSentimentResultsByVideoIdAndSentimentObject(videoId, sentimentObject, sortedPageable);
    }

    @Override
    public void deleteCommentSentimentResultByVideoId(String videoId) {
        commentSentimentRepository.deleteByVideoId(videoId);
    }
}
