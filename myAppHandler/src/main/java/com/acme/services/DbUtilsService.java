package com.acme.services;

import com.acme.repositories.AnalysisSummaryRepository;
import com.acme.repositories.CommentSentimentRepository;
import com.acme.repositories.ConciseCommentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.LinkedHashMap;

@Service
@Profile("db")
public class DbUtilsService {

    private static final Logger log = LoggerFactory.getLogger(DbUtilsService.class);

    private final AnalysisSummaryRepository analysisSummaryRepository;
    private final CommentSentimentRepository commentSentimentRepository;
    private final ConciseCommentRepository conciseCommentRepository;

    public DbUtilsService(AnalysisSummaryRepository analysisSummaryRepository,
                          CommentSentimentRepository commentSentimentRepository,
                          ConciseCommentRepository conciseCommentRepository) {
        this.analysisSummaryRepository = analysisSummaryRepository;
        this.commentSentimentRepository = commentSentimentRepository;
        this.conciseCommentRepository = conciseCommentRepository;
    }

    public long deleteRecordsOlderThanDays(int days) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        log.info("Deleting records created before {}", cutoff);

        long summariesDeleted = analysisSummaryRepository.deleteByCreateDateBefore(cutoff);
        log.info("Deleted {} records from video_comments_summary", summariesDeleted);

        long sentimentsDeleted = commentSentimentRepository.deleteByCreateDateBefore(cutoff);
        log.info("Deleted {} records from comment_sentiments", sentimentsDeleted);

        long commentsDeleted = conciseCommentRepository.deleteByCreateDateBefore(cutoff);
        log.info("Deleted {} records from concise_comments", commentsDeleted);

        long total = summariesDeleted + sentimentsDeleted + commentsDeleted;
        log.info("Total records deleted: {}", total);
        return total;
    }

    public Map<String, Long> countDocumentsPerCollection() {
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("video_comments_summary", analysisSummaryRepository.count());
        counts.put("comment_sentiments", commentSentimentRepository.count());
        counts.put("concise_comments", conciseCommentRepository.count());
        return counts;
    }
}
