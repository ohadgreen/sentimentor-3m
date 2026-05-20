package com.acme.model.trend;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TrendAnalysisJobDto {
    private String jobId;
    private String userId;
    private String searchQuery;
    private String sentimentObject;
    private int daysBack;
    private int commentsPerVideo;
    private int videosPerDay;
    private TrendJobStatus jobStatus;
    private int daysCompleted;
    private int totalDays;
    private List<DailyTrendResult> dailyResults;
    private String errorMessage;
    private LocalDateTime createDate;

    // Sentiment totals
    private int totalPositive;
    private int totalNegative;
    private int totalNeutral;
    private int totalAnalyzed;

    // Sentiment percentages (0–100, rounded to 1 decimal)
    private double positivePercent;
    private double negativePercent;
    private double neutralPercent;

    // Sentiment counts per date, ordered chronologically (key = "yyyy-MM-dd")
    private Map<String, DailySentimentCounts> countsByDate;

    public static TrendAnalysisJobDto from(TrendAnalysisJob job) {
        TrendAnalysisJobDto dto = new TrendAnalysisJobDto();
        dto.jobId = job.getJobId();
        dto.userId = job.getUserId();
        dto.searchQuery = job.getSearchQuery();
        dto.sentimentObject = job.getSentimentObject();
        dto.daysBack = job.getDaysBack();
        dto.commentsPerVideo = job.getCommentsPerVideo();
        dto.videosPerDay = job.getVideosPerDay();
        dto.jobStatus = job.getJobStatus();
        dto.daysCompleted = job.getDaysCompleted();
        dto.totalDays = job.getTotalDays();
        dto.dailyResults = job.getDailyResults();
        dto.errorMessage = job.getErrorMessage();
        dto.createDate = job.getCreateDate();

        List<DailyTrendResult> results = job.getDailyResults();
        dto.countsByDate = new LinkedHashMap<>();
        if (results != null) {
            for (DailyTrendResult day : results) {
                dto.totalPositive += day.getPositiveComments();
                dto.totalNegative += day.getNegativeComments();
                dto.totalNeutral  += day.getNeutralComments();
                dto.countsByDate.put(day.getDate(), new DailySentimentCounts(
                        day.getPositiveComments(),
                        day.getNegativeComments(),
                        day.getNeutralComments(),
                        day.getTotalCommentsAnalyzed()
                ));
            }
            dto.totalAnalyzed = dto.totalPositive + dto.totalNegative + dto.totalNeutral;
            if (dto.totalAnalyzed > 0) {
                dto.positivePercent = round1(100.0 * dto.totalPositive / dto.totalAnalyzed);
                dto.negativePercent = round1(100.0 * dto.totalNegative / dto.totalAnalyzed);
                dto.neutralPercent  = round1(100.0 * dto.totalNeutral  / dto.totalAnalyzed);
            }
        }

        return dto;
    }

    private static double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    public String getJobId() {
        return jobId;
    }

    public String getUserId() {
        return userId;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public String getSentimentObject() {
        return sentimentObject;
    }

    public int getDaysBack() {
        return daysBack;
    }

    public int getCommentsPerVideo() {
        return commentsPerVideo;
    }

    public int getVideosPerDay() {
        return videosPerDay;
    }

    public TrendJobStatus getJobStatus() {
        return jobStatus;
    }

    public int getDaysCompleted() {
        return daysCompleted;
    }

    public int getTotalDays() {
        return totalDays;
    }

    public List<DailyTrendResult> getDailyResults() {
        return dailyResults;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public int getTotalPositive() { return totalPositive; }
    public int getTotalNegative() { return totalNegative; }
    public int getTotalNeutral()  { return totalNeutral; }
    public int getTotalAnalyzed() { return totalAnalyzed; }
    public double getPositivePercent() { return positivePercent; }
    public double getNegativePercent() { return negativePercent; }
    public double getNeutralPercent()  { return neutralPercent; }
    public Map<String, DailySentimentCounts> getCountsByDate() { return countsByDate; }

    public static class DailySentimentCounts {
        private final int positive;
        private final int negative;
        private final int neutral;
        private final int total;

        public DailySentimentCounts(int positive, int negative, int neutral, int total) {
            this.positive = positive;
            this.negative = negative;
            this.neutral  = neutral;
            this.total    = total;
        }

        public int getPositive() { return positive; }
        public int getNegative() { return negative; }
        public int getNeutral()  { return neutral; }
        public int getTotal()    { return total; }
    }
}
