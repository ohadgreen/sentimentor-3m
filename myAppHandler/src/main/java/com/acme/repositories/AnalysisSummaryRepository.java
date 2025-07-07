package com.acme.repositories;

import com.acme.model.comment.VideoCommentsSummary;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AnalysisSummaryRepository extends MongoRepository<VideoCommentsSummary, String> {
    VideoCommentsSummary findByVideoId(String videoId);
    void deleteByVideoId(String videoId);
}
