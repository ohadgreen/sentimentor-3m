package com.acme.controllers;

import com.acme.model.trend.TrendAnalysisJobDto;
import com.acme.model.trend.TrendAnalysisRequest;
import com.acme.repositories.TrendAnalysisJobRepository;
import com.acme.services.TrendAnalysisOrchestrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trend")
public class TrendAnalysisController {

    private final TrendAnalysisOrchestrationService orchestrationService;
    private final TrendAnalysisJobRepository trendJobRepository;

    public TrendAnalysisController(TrendAnalysisOrchestrationService orchestrationService,
                                   TrendAnalysisJobRepository trendJobRepository) {
        this.orchestrationService = orchestrationService;
        this.trendJobRepository = trendJobRepository;
    }

    @PostMapping("/start")
    public ResponseEntity<Map<String, String>> startTrendAnalysis(@RequestBody TrendAnalysisRequest request,
                                                                   Authentication auth) {
        String jobId = orchestrationService.startTrendJob(request, auth.getName());
        return ResponseEntity.ok(Map.of("jobId", jobId));
    }

    @GetMapping("/latest")
    public List<TrendAnalysisJobDto> getLatestTrendJobs(Authentication auth) {
        return trendJobRepository.findTop10ByUserIdOrderByCreateDateDesc(auth.getName())
                .stream()
                .map(TrendAnalysisJobDto::from)
                .toList();
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<TrendAnalysisJobDto> getTrendJob(@PathVariable String jobId) {
        return trendJobRepository.findById(jobId)
                .map(TrendAnalysisJobDto::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/summary")
    public List<TrendAnalysisJobDto> getTrendSummary(Authentication auth) {
        return trendJobRepository.findTop10ByUserIdOrderByCreateDateDesc(auth.getName())
                .stream()
                .map(TrendAnalysisJobDto::from)
                .toList();
    }
}
