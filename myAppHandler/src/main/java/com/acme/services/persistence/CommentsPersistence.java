package com.acme.services.persistence;

import com.acme.model.comment.ConciseComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommentsPersistence {
    void saveConciseComments(List<ConciseComment> conciseCommentList);
    Page<ConciseComment> getCommentsPageByVideoId(String videoId, Pageable pageable);
    List<ConciseComment> findByVideoIdAndCommentIdIn(String videoId, List<String> commentIds);
    Page<ConciseComment> getCommentsPageByVideoIdAndKeyword(String videoId, String keyword, Pageable pageable);
    List<ConciseComment> findByVideoIdAndCommentIdInAndKeyword(String videoId, List<String> commentIds, String keyword);
    void removeCommentsByVideoId(String videoId);
}
