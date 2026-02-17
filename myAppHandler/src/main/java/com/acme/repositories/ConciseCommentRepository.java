package com.acme.repositories;

import com.acme.model.comment.ConciseComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ConciseCommentRepository extends MongoRepository<ConciseComment, String> {
    ConciseComment findConciseCommentByCommentId(String id);
    List<ConciseComment> findConciseCommentByVideoId(String videoId);
    Page<ConciseComment> findConciseCommentByVideoId(String videoId, Pageable pageable);
    List<ConciseComment> findByVideoIdAndCommentIdIn(String videoId, List<String> commentIds);
    Page<ConciseComment> findByVideoIdAndWords(String videoId, String keyword, Pageable pageable);
    List<ConciseComment> findByVideoIdAndCommentIdInAndWords(String videoId, List<String> commentIds, String keyword);
    void deleteByVideoId(String videoId);
    long deleteByCreateDateBefore(LocalDateTime date);
}
