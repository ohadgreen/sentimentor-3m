package com.acme.model.trend;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TrendSummaryDto {

    private String jobId;
    private String searchQuery;
    private String sentimentObject;
    private TrendJobStatus jobStatus;

    // Overall totals
    private int totalPositive;
    private int totalNegative;
    private int totalNeutral;
    private int totalAnalyzed;

    // Overall percentages (0–100, rounded to 1 decimal)
    private double positivePercent;
    private double negativePercent;
    private double neutralPercent;

    // Sentiment counts per date, ordered chronologically (key = "yyyy-MM-dd")
    private Map<String, DailySentimentCounts> countsByDate;

    public static TrendSummaryDto from(TrendAnalysisJob job) {
        TrendSummaryDto dto = new TrendSummaryDto();
        dto.jobId = job.getJobId();
        dto.searchQuery = job.getSearchQuery();
        dto.sentimentObject = job.getSentimentObject();
        dto.jobStatus = job.getJobStatus();

        List<DailyTrendResult> results = job.getDailyResults();
        if (results == null || results.isEmpty()) {
            dto.countsByDate = new LinkedHashMap<>();
            return dto;
        }

        int totalPos = 0, totalNeg = 0, totalNeu = 0;
        Map<String, DailySentimentCounts> countsByDate = new LinkedHashMap<>();

        for (DailyTrendResult day : results) {
            totalPos += day.getPositiveComments();
            totalNeg += day.getNegativeComments();
            totalNeu += day.getNeutralComments();
            countsByDate.put(day.getDate(), new DailySentimentCounts(
                    day.getPositiveComments(),
                    day.getNegativeComments(),
                    day.getNeutralComments(),
                    day.getTotalCommentsAnalyzed()
            ));
        }

        dto.totalPositive = totalPos;
        dto.totalNegative = totalNeg;
        dto.totalNeutral = totalNeu;
        dto.totalAnalyzed = totalPos + totalNeg + totalNeu;
        dto.countsByDate = countsByDate;

        if (dto.totalAnalyzed > 0) {
            dto.positivePercent = round1(100.0 * totalPos / dto.totalAnalyzed);
            dto.negativePercent = round1(100.0 * totalNeg / dto.totalAnalyzed);
            dto.neutralPercent  = round1(100.0 * totalNeu / dto.totalAnalyzed);
        }

        return dto;
    }

    private static double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    // --- Nested record for per-day counts ---

    public static class DailySentimentCounts {
        private final int positive;
        private final int negative;
        private final int neutral;
        private final int total;

        public DailySentimentCounts(int positive, int negative, int neutral, int total) {
            this.positive = positive;
            this.negative = negative;
            this.neutral = neutral;
            this.total = total;
        }

        public int getPositive() { return positive; }
        public int getNegative() { return negative; }
        public int getNeutral()  { return neutral; }
        public int getTotal()    { return total; }
    }

    // --- Getters ---

    public String getJobId() { return jobId; }
    public String getSearchQuery() { return searchQuery; }
    public String getSentimentObject() { return sentimentObject; }
    public TrendJobStatus getJobStatus() { return jobStatus; }
    public int getTotalPositive() { return totalPositive; }
    public int getTotalNegative() { return totalNegative; }
    public int getTotalNeutral() { return totalNeutral; }
    public int getTotalAnalyzed() { return totalAnalyzed; }
    public double getPositivePercent() { return positivePercent; }
    public double getNegativePercent() { return negativePercent; }
    public double getNeutralPercent() { return neutralPercent; }
    public Map<String, DailySentimentCounts> getCountsByDate() { return countsByDate; }
}
