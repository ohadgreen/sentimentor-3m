package com.acme.services;

import com.acme.model.comment.CommentDto;
import com.acme.model.comment.CommentsAnalyzeSummary;
import com.acme.model.comment.ConciseComment;
import com.acme.services.persistence.AnalysisSummaryPersistence;
import com.acme.services.persistence.CommentsPersistence;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnalysisSummaryService {
    private final static int MAX_TOP_COMMENTS = 50;
    private final AnalysisSummaryPersistence analysisSummaryPersistence;
    private final CommentsPersistence commentsPersistence;

    public AnalysisSummaryService(AnalysisSummaryPersistence analysisSummaryPersistence, CommentsPersistence commentsPersistence) {
        this.analysisSummaryPersistence = analysisSummaryPersistence;
        this.commentsPersistence = commentsPersistence;
    }

    public CommentsAnalyzeSummary getCommentsAnalysisSummary(String videoId) {
        CommentsAnalyzeSummary commentsAnalysisSummary = analysisSummaryPersistence.getCommentsAnalysisSummary(videoId);

        List<ConciseComment> topComments = commentsPersistence.getCommentsPageByVideoId(videoId, Pageable.ofSize(MAX_TOP_COMMENTS));
        List<CommentDto> topCommentsDtoList = topComments.stream().map(this::convertConciseCommentToCommentDto).toList();

        commentsAnalysisSummary.setTopRatedComments(topCommentsDtoList);

        return commentsAnalysisSummary;
    }

    private CommentDto convertConciseCommentToCommentDto(ConciseComment conciseComment) {
        return new CommentDto(
                conciseComment.getTextOriginal(),
                conciseComment.getLikeCount(),
                conciseComment.getAuthorDisplayName(),
                conciseComment.getAuthorProfileImageUrl(),
                conciseComment.getPublishedAt());
    }

}
