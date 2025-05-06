package com.acme.repositories;

import com.acme.model.comment.CommentsAnalyzeSummary;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AnalysisSummaryRepository extends MongoRepository<CommentsAnalyzeSummary, String> {
    CommentsAnalyzeSummary findByVideoId(String videoId);
    void deleteByVideoId(String videoId);
}
