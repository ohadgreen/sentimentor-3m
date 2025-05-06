package com.acme.services.persistence;

import com.acme.model.comment.ConciseComment;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommentsPersistence {
    void saveConciseComments(List<ConciseComment> conciseCommentList);
    List<ConciseComment> getCommentsByVideoId(String videoId, Pageable pageable);
    void removeCommentsByVideoId(String videoId);
}
