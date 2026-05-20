package com.acme.repositories;

import com.acme.model.trend.TrendAnalysisJob;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TrendAnalysisJobRepository extends MongoRepository<TrendAnalysisJob, String> {
    List<TrendAnalysisJob> findTop10ByUserIdOrderByCreateDateDesc(String userId);
}
