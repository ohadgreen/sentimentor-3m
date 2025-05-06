package com.acme.repositories;

import com.acme.model.comment.ConciseComment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ConciseCommentRepository extends MongoRepository<ConciseComment, String> {
    ConciseComment findConciseCommentByCommentId(String id);
    List<ConciseComment> findConciseCommentByVideoId(String videoId);
    List<ConciseComment> findConciseCommentByVideoId(String videoId, Pageable pageable);
    List<ConciseComment> findTopRatedCommentsByVideoId(String videoId, Pageable pageable);
    void deleteByVideoId(String videoId);
}
