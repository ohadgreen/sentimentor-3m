package com.acme.services.persistence;

import com.acme.model.comment.ConciseComment;
import com.acme.repositories.ConciseCommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Profile("db")
public class CommentsPersistInMongo implements CommentsPersistence {

    @Autowired
    private ConciseCommentRepository conciseCommentRepository;

    @Override
    public void saveConciseComments(List<ConciseComment> conciseCommentList) {
        if (conciseCommentList.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        conciseCommentList.forEach(comment -> {
            if (comment.getCreateDate() == null) {
                comment.setCreateDate(now);
            }
            comment.setUpdateDate(now);
        });
        conciseCommentRepository.saveAll(conciseCommentList);
    }

    @Override
    public Page<ConciseComment> getCommentsPageByVideoId(String videoId, Pageable pageable) {
        Sort defaultSort = Sort.by(
                Sort.Order.desc("likeCount"),
                Sort.Order.desc("publishedAt")
        );

        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort().isSorted() ? pageable.getSort() : defaultSort
        );

        return conciseCommentRepository.findConciseCommentByVideoId(videoId, sortedPageable);
    }

    @Override
    public List<ConciseComment> findByVideoIdAndCommentIdIn(String videoId, List<String> commentIds) {
        return conciseCommentRepository.findByVideoIdAndCommentIdIn(videoId, commentIds);
    }

    @Override
    public Page<ConciseComment> getCommentsPageByVideoIdAndKeyword(String videoId, String keyword, Pageable pageable) {
        Sort defaultSort = Sort.by(
                Sort.Order.desc("likeCount"),
                Sort.Order.desc("publishedAt")
        );

        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort().isSorted() ? pageable.getSort() : defaultSort
        );

        return conciseCommentRepository.findByVideoIdAndWords(videoId, keyword.toLowerCase(), sortedPageable);
    }

    @Override
    public List<ConciseComment> findByVideoIdAndCommentIdInAndKeyword(String videoId, List<String> commentIds, String keyword) {
        return conciseCommentRepository.findByVideoIdAndCommentIdInAndWords(videoId, commentIds, keyword.toLowerCase());
    }

    @Override
    public void removeCommentsByVideoId(String videoId) {
        conciseCommentRepository.deleteByVideoId(videoId);
    }

}
