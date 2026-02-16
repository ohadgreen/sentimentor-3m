package com.acme.services;

import com.acme.model.analysisreq.VideoCommentsRequest;
import com.acme.model.comment.CommentDto;
import com.acme.model.comment.CommentSentimentResult;
import com.acme.model.comment.SentimentResultDto;
import com.acme.model.comment.VideoCommentsSummary;
import com.acme.model.comment.ConciseComment;
import com.acme.model.ytrawcomment.Comment;
import com.acme.model.ytrawcomment.CommentThread;
import com.acme.services.persistence.AnalysisResultPersistence;
import com.acme.services.persistence.AnalysisSummaryPersistence;
import com.acme.services.persistence.CommentsPersistence;
import common.model.analysisrequest.Sentiment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.Objects;

@Service
public class RawCommentsService {
    private final GetYouTubeRawComments getYouTubeRawComments;
    private final CommentsPersistence commentsPersistence;
    private final AnalysisSummaryPersistence analysisSummaryPersistence;
    private final WordCountService wordCountService;
    private final AnalysisResultPersistence analysisResultPersistence;
    private static final int MAX_WORDS_FREQUENCIES = 10;
    private final static int MAX_TOP_COMMENTS = 50;
    private final static int MAX_WORDS_IN_COMMENT = 50;


    public RawCommentsService(GetYouTubeRawComments getYouTubeRawComments, CommentsPersistence commentsPersistence, AnalysisSummaryPersistence analysisSummaryPersistence, WordCountService wordCountService, AnalysisResultPersistence analysisResultPersistence) {
        this.getYouTubeRawComments = getYouTubeRawComments;
        this.commentsPersistence = commentsPersistence;
        this.analysisSummaryPersistence = analysisSummaryPersistence;
        this.wordCountService = wordCountService;
        this.analysisResultPersistence = analysisResultPersistence;
    }

    public Page<ConciseComment> getConciseCommentList(String videoId, int limit) {
        return commentsPersistence.getCommentsPageByVideoId(videoId, Pageable.ofSize(limit));
    }

    public Page<ConciseComment> getConciseCommentPage(String videoId, int pageNumber, int pageSize) {
        Pageable pageable = Pageable.ofSize(pageSize).withPage(pageNumber);
        return commentsPersistence.getCommentsPageByVideoId(videoId, pageable);
    }

    public Page<CommentDto> getConciseCommentPage(String videoId, int pageNumber, int pageSize, String sentimentObject, Sentiment sentiment) {
        Pageable pageable = Pageable.ofSize(pageSize).withPage(pageNumber);

        if (sentimentObject != null && sentiment != null) {
            Page<CommentSentimentResult> sentimentPage = analysisResultPersistence
                    .getCommentSentimentResultsPageByVideoIdAndSentimentObjectAndSentiment(videoId, sentimentObject, sentiment, pageable);

            List<String> commentIds = sentimentPage.getContent().stream()
                    .map(CommentSentimentResult::getCommentId)
                    .collect(Collectors.toList());

            Map<String, ConciseComment> commentMap = commentsPersistence.findByVideoIdAndCommentIdIn(videoId, commentIds).stream()
                    .collect(Collectors.toMap(ConciseComment::getCommentId, c -> c));

            Map<String, List<SentimentResultDto>> sentimentsByCommentId = buildSentimentsByCommentId(videoId, commentIds);

            List<CommentDto> mappedList = sentimentPage.getContent().stream()
                    .map(sentimentResult -> {
                        ConciseComment conciseComment = commentMap.get(sentimentResult.getCommentId());
                        if (conciseComment == null) return null;
                        return new CommentDto(
                                conciseComment.getCommentId(),
                                conciseComment.getTextOriginal(),
                                conciseComment.getLikeCount(),
                                conciseComment.getAuthorDisplayName(),
                                conciseComment.getAuthorProfileImageUrl(),
                                conciseComment.getPublishedAt(),
                                sentimentsByCommentId.getOrDefault(conciseComment.getCommentId(), List.of())
                        );
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            return new PageImpl<>(mappedList, pageable, sentimentPage.getTotalElements());
        }

        Page<ConciseComment> page = commentsPersistence.getCommentsPageByVideoId(videoId, pageable);

        List<String> commentIds = page.getContent().stream()
                .map(ConciseComment::getCommentId)
                .collect(Collectors.toList());

        Map<String, List<SentimentResultDto>> sentimentsByCommentId = buildSentimentsByCommentId(videoId, commentIds);

        List<CommentDto> mappedList = page.getContent().stream()
                .map(conciseComment -> new CommentDto(
                        conciseComment.getCommentId(),
                        conciseComment.getTextOriginal(),
                        conciseComment.getLikeCount(),
                        conciseComment.getAuthorDisplayName(),
                        conciseComment.getAuthorProfileImageUrl(),
                        conciseComment.getPublishedAt(),
                        sentimentsByCommentId.getOrDefault(conciseComment.getCommentId(), List.of())
                ))
                .collect(Collectors.toList());

        return new PageImpl<>(mappedList, pageable, page.getTotalElements());
    }

    private Map<String, List<SentimentResultDto>> buildSentimentsByCommentId(String videoId, List<String> commentIds) {
        List<CommentSentimentResult> allSentiments = analysisResultPersistence
                .getCommentSentimentResultsByVideoIdAndCommentIdIn(videoId, commentIds);
        Map<String, List<SentimentResultDto>> result = new HashMap<>();
        for (CommentSentimentResult r : allSentiments) {
            result.computeIfAbsent(r.getCommentId(), k -> new ArrayList<>())
                    .add(new SentimentResultDto(r.getSentimentObject(), r.getSentiment(), r.getSentimentReason()));
        }
        return result;
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
                .toList();

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
        conciseComment.setWords(splitCommentTextIntoWords(rawComment.getOuterSnippet().getTopLevelComment().getInnerSnippet().getTextOriginal()));
        conciseComment.setViewerRating(rawComment.getOuterSnippet().getTopLevelComment().getInnerSnippet().getViewerRating());
        conciseComment.setPublishedAt(rawComment.getOuterSnippet().getTopLevelComment().getInnerSnippet().getPublishedAt());
        conciseComment.setUpdatedAt(rawComment.getOuterSnippet().getTopLevelComment().getInnerSnippet().getUpdatedAt());

        return conciseComment;
    }

    private Set<String> splitCommentTextIntoWords(String commentText) {
        if (commentText == null || commentText.isEmpty()) {
            return Collections.emptySet();
        }
        String[] wordsArray = commentText.toLowerCase().split("\\W+");
        return Arrays.stream(wordsArray)
                .filter(word -> !word.isEmpty())
                .limit(MAX_WORDS_IN_COMMENT)
                .collect(Collectors.toSet());
    }
}
