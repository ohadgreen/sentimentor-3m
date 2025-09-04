package com.acme.services;

import com.acme.model.analysisreq.VideoCommentsRequest;
import com.acme.model.comment.CommentDto;
import com.acme.model.comment.VideoCommentsSummary;
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
public class RawCommentsService {
    private final GetYouTubeRawComments getYouTubeRawComments;
    private final CommentsPersistence commentsPersistence;
    private final AnalysisSummaryPersistence analysisSummaryPersistence;
    private final WordCountService wordCountService;
    private static final int MAX_WORDS_FREQUENCIES = 10;
    private final static int MAX_TOP_COMMENTS = 50;

    public RawCommentsService(GetYouTubeRawComments getYouTubeRawComments, CommentsPersistence commentsPersistence, AnalysisSummaryPersistence analysisSummaryPersistence, WordCountService wordCountService) {
        this.getYouTubeRawComments = getYouTubeRawComments;
        this.commentsPersistence = commentsPersistence;
        this.analysisSummaryPersistence = analysisSummaryPersistence;
        this.wordCountService = wordCountService;
    }

    public List<ConciseComment> getConciseCommentList(String videoId, int limit) {
        return commentsPersistence.getCommentsPageByVideoId(videoId, Pageable.ofSize(limit));
    }

    public List<ConciseComment> getConciseCommentPage(String videoId, int pageNumber, int pageSize) {
        Pageable pageable = Pageable.ofSize(pageSize).withPage(pageNumber);
        return commentsPersistence.getCommentsPageByVideoId(videoId, pageable);
    }

    public VideoCommentsSummary getRawVideoComments(VideoCommentsRequest videoCommentsRequest) {
        String videoId = videoCommentsRequest.getVideoId();

        VideoCommentsSummary commentsAnalysisSummary = analysisSummaryPersistence.getCommentsAnalysisSummary(videoId);
        if (commentsAnalysisSummary != null) {
            System.out.println("Comments analysis summary already exists for videoId: " + videoId);
            return commentsAnalysisSummary;
        }

        int totalCommentsCount = 0;
        String nextPageToken = null;
        
        List<ConciseComment> allConciseComments = new ArrayList<>();

        Map<String, Integer> wordCount = new HashMap<>();
        TreeMap<Integer, List<String>> sortedWordCountsMap = new TreeMap<>(Collections.reverseOrder());

        do {
            CommentThread rawCommentsFromYouTube = getYouTubeRawComments.getRawCommentsFromYouTube(videoId, nextPageToken);
            List<Comment> comments = rawCommentsFromYouTube.getComments();

            if (comments == null || comments.isEmpty()) {
                break;
            }

            List<ConciseComment> conciseCommentList = comments.stream().map(rawComment -> getConciseCommentFromComment(rawComment, videoId)).toList();

            List<String> commentsForWordsCount = conciseCommentList.stream().map(ConciseComment::getTextDisplay).collect(Collectors.toList());
            wordCountService.wordsCount(wordCount, sortedWordCountsMap, commentsForWordsCount);

            totalCommentsCount += comments.size();
            allConciseComments.addAll(conciseCommentList);

            nextPageToken = rawCommentsFromYouTube.getNextPageToken();

        } while (totalCommentsCount < videoCommentsRequest.getTotalCommentsRequired() && nextPageToken != null);

        commentsPersistence.saveConciseComments(allConciseComments);

        List<ConciseComment> topRatedComments = allConciseComments.stream()
                .sorted(Comparator.comparingInt(ConciseComment::getLikeCount).reversed())
                .limit(MAX_TOP_COMMENTS)
                .collect(Collectors.toList());

        List<CommentDto> topCommentsDtoList = topRatedComments.stream()
                .map(conciseComment -> new CommentDto(
                        conciseComment.getTextOriginal(),
                        conciseComment.getLikeCount(),
                        conciseComment.getAuthorDisplayName(),
                        conciseComment.getAuthorProfileImageUrl(),
                        conciseComment.getPublishedAt()))
                .toList();

        VideoCommentsSummary videoCommentsSummary = new VideoCommentsSummary();
        videoCommentsSummary.setVideoId(videoId);
        videoCommentsSummary.setTotalComments(totalCommentsCount);
        videoCommentsSummary.setTopRatedComments(topCommentsDtoList);
        videoCommentsSummary.setWordsFrequency(calculateTopWordsFrequencies(sortedWordCountsMap));

        analysisSummaryPersistence.saveAnalysisSummary(videoCommentsSummary);
        return videoCommentsSummary;
    }

    protected LinkedHashMap<String, Integer> calculateTopWordsFrequencies(TreeMap<Integer, List<String>> sortedWordCountsMap) {
        LinkedHashMap<String, Integer> topWordsFrequencyMap = new LinkedHashMap<>();
        int count = 0;
        for (Map.Entry<Integer, List<String>> entry : sortedWordCountsMap.entrySet()) {
            for (String word : entry.getValue()) {
                topWordsFrequencyMap.put(word, entry.getKey());
                count++;
                if (count >= MAX_WORDS_FREQUENCIES) {
                    break;
                }
            }
            if (count == MAX_WORDS_FREQUENCIES) {
                break;
            }
        }

//        VideoCommentsSummary videoCommentsSummary = new VideoCommentsSummary();
//        videoCommentsSummary.setVideoId(videoId);
//        videoCommentsSummary.setTotalComments(totalCommentsCount);
//        videoCommentsSummary.setWordsFrequency(topWordsFrequencyMap);
//
//        analysisSummaryPersistence.saveAnalysisSummary(videoCommentsSummary);

        return topWordsFrequencyMap;
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
