package com.acme.repositories;

import com.acme.model.comment.SourceMode;
import com.acme.model.comment.VideoCommentsSummary;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface AnalysisSummaryRepository extends MongoRepository<VideoCommentsSummary, String> {
    VideoCommentsSummary findByVideoId(String videoId);
    void deleteByVideoId(String videoId);
    long deleteByCreateDateBefore(LocalDateTime date);
    List<VideoCommentsSummary> findTop6ByOrderByCreateDateDesc();

    // Returns ad-hoc videos: sourceMode is AD_HOC, or null (legacy documents before sourceMode existed)
    @Query("{ '$or': [ { 'sourceMode': 'AD_HOC' }, { 'sourceMode': { '$exists': false } } ] }")
    List<VideoCommentsSummary> findTop6AdHocOrderByCreateDateDesc(Sort sort);
}
