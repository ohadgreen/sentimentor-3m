package com.acme.services;

import com.acme.model.analysisreq.VideoCommentsRequest;
import com.acme.model.comment.CommentsAnalyzeSummary;
import com.acme.model.comment.ConciseComment;
import com.acme.model.ytrawcomment.Comment;
import com.acme.model.ytrawcomment.CommentThread;
import com.acme.services.persistence.AnalysisSummaryPersistence;
import com.acme.services.persistence.CommentsPersistence;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CommentsHandlingService {
    private final GetRawCommentService getRawCommentService;
    private final CommentsPersistence commentsPersistence;
    private final AnalysisSummaryPersistence analysisSummaryPersistence;
    private final WordCountService wordCountService;
    private static final int MAX_WORDS_FREQUENCIES = 10;

    public CommentsHandlingService(GetRawCommentService getRawCommentService, CommentsPersistence commentsPersistence, AnalysisSummaryPersistence analysisSummaryPersistence, WordCountService wordCountService) {
        this.getRawCommentService = getRawCommentService;
        this.commentsPersistence = commentsPersistence;
        this.analysisSummaryPersistence = analysisSummaryPersistence;
        this.wordCountService = wordCountService;
    }

    public List<ConciseComment> getConciseCommentList(String videoId, int limit) {
        return commentsPersistence.getCommentsPageByVideoId(videoId, Pageable.ofSize(limit));
    }

    public void handleGetCommentList(VideoCommentsRequest videoCommentsRequest) {
        String videoId = videoCommentsRequest.getVideoId();
        int totalCommentsCount = 0;
        String nextPageToken = null;
        
        List<ConciseComment> allConciseComments = new ArrayList<>();

        Map<String, Integer> wordCount = new HashMap<>();
        TreeMap<Integer, List<String>> sortedCountsMap = new TreeMap<>(Collections.reverseOrder());;

        do {
            CommentThread rawCommentsFromYouTube = getRawCommentService.getRawCommentsFromYouTube(videoId, videoCommentsRequest.getCommentsInPage(), nextPageToken);
            List<Comment> comments = rawCommentsFromYouTube.getComments();

            if (comments == null || comments.isEmpty()) {
                break;
            }

            List<ConciseComment> conciseCommentList = comments.stream().map(rawComment -> getConciseCommentFromComment(rawComment, videoId)).toList();

            List<String> commentsForWordsCount = conciseCommentList.stream().map(ConciseComment::getTextDisplay).collect(Collectors.toList());
            wordCountService.wordsCount(wordCount, sortedCountsMap, commentsForWordsCount);

            totalCommentsCount += comments.size();
            allConciseComments.addAll(conciseCommentList);

            nextPageToken = rawCommentsFromYouTube.getNextPageToken();

        } while (totalCommentsCount < videoCommentsRequest.getTotalCommentsRequired() && nextPageToken != null);

        commentsPersistence.saveConciseComments(allConciseComments);
        createAndSaveAnalysisSummary(videoId, totalCommentsCount, sortedCountsMap);
    }

    protected void createAndSaveAnalysisSummary(String videoId, int totalCommentsCount, TreeMap<Integer, List<String>> sortedCountsMap) {
        LinkedHashMap<String, Integer> topWordsFrequencyMap = new LinkedHashMap<>();
        int count = 0;
        for (Map.Entry<Integer, List<String>> entry : sortedCountsMap.entrySet()) {
            for (String word : entry.getValue()) {
                topWordsFrequencyMap.put(word, entry.getKey());
                count++;
                if (count >= MAX_WORDS_FREQUENCIES) {
                    break;
                }
            }
            if (count >= MAX_WORDS_FREQUENCIES) {
                break;
            }
        }

        CommentsAnalyzeSummary commentsAnalyzeSummary = new CommentsAnalyzeSummary();
        commentsAnalyzeSummary.setVideoId(videoId);
        commentsAnalyzeSummary.setTotalComments(totalCommentsCount);
        commentsAnalyzeSummary.setWordsFrequency(topWordsFrequencyMap);

        analysisSummaryPersistence.saveAnalysisSummary(commentsAnalyzeSummary);
    }

    private ConciseComment getConciseCommentFromComment(Comment rawComment, String jobId) {
        if (rawComment.getOuterSnippet() == null || rawComment.getOuterSnippet().getTopLevelComment() == null) {
            return null;
        }

        ConciseComment conciseComment = new ConciseComment();
        conciseComment.setJobId(jobId);
        conciseComment.setCommentId(rawComment.getId());
        conciseComment.setChannelId(rawComment.getOuterSnippet().getChannelId());
        conciseComment.setVideoId(rawComment.getOuterSnippet().getVideoId());
        conciseComment.setTextDisplay(rawComment.getOuterSnippet().getTopLevelComment().getInnerSnippet().getTextDisplay());
        conciseComment.setTextOriginal(rawComment.getOuterSnippet().getTopLevelComment().getInnerSnippet().getTextOriginal());
        conciseComment.setAuthorDisplayName(rawComment.getOuterSnippet().getTopLevelComment().getInnerSnippet().getAuthorDisplayName());
        conciseComment.setAuthorProfileImageUrl(rawComment.getOuterSnippet().getTopLevelComment().getInnerSnippet().getAuthorProfileImageUrl());
        conciseComment.setCanRate(rawComment.getOuterSnippet().getTopLevelComment().getInnerSnippet().isCanRate());
        conciseComment.setLikeCount(rawComment.getOuterSnippet().getTopLevelComment().getInnerSnippet().getLikeCount());
        conciseComment.setViewerRating(rawComment.getOuterSnippet().getTopLevelComment().getInnerSnippet().getViewerRating());
        conciseComment.setPublishedAt(rawComment.getOuterSnippet().getTopLevelComment().getInnerSnippet().getPublishedAt());
        conciseComment.setUpdatedAt(rawComment.getOuterSnippet().getTopLevelComment().getInnerSnippet().getUpdatedAt());

        return conciseComment;
    }
}
