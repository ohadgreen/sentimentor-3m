package com.acme.services.persistence;

import com.acme.model.comment.ConciseComment;
import com.acme.repositories.ConciseCommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("db")
public class CommentsPersistInMongo implements CommentsPersistence {

    @Autowired
    private ConciseCommentRepository conciseCommentRepository;

    @Override
    public void saveConciseComments(List<ConciseComment> conciseCommentList) {
        List<ConciseComment> conciseComments = conciseCommentRepository.saveAll(conciseCommentList);
    }

    @Override
    public List<ConciseComment> getCommentsByVideoId(String videoId, int limit) {
        return conciseCommentRepository.findTopRatedCommentsByVideoId(videoId, PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "likeCount")));
    }

    @Override
    public void removeCommentsByVideoId(String videoId) {
        conciseCommentRepository.deleteByVideoId(videoId);
    }

}
