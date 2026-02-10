package com.acme.services.persistence;

import com.acme.model.comment.CommentSentimentResult;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Profile("memory")
public class AnalysisResultPersistInMemory implements AnalysisResultPersistence {

    private final Map<String, CommentSentimentResult> commentSentimentResultMap = new HashMap<>(); // Using a Map for fast lookups
    private final Map<String, Set<String>> commentIdToCommentSentimentResultMap = new HashMap<>();
    private final Map<String, Map<String, Set<String>>> videoIdToSentimentObjectToResultsMap = new HashMap<>();

    @Override
    public void saveCommentSentimentResult(List<CommentSentimentResult> commentSentimentResults) {
        if (commentSentimentResults.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        Set<String> newCommentSentimentResultSet = new HashSet<>();
        for (CommentSentimentResult result : commentSentimentResults) {
            if (result.getCreateDate() == null) {
                result.setCreateDate(now);
            }
            result.setUpdateDate(now);
            String sentimentResultId = UUID.randomUUID().toString();
            commentSentimentResultMap.put(sentimentResultId, result);
            newCommentSentimentResultSet.add(sentimentResultId);

            String commentId = result.getCommentId();
            commentIdToCommentSentimentResultMap.computeIfAbsent(commentId, k -> new HashSet<>()).add(sentimentResultId);
        }
        String videoId = commentSentimentResults.getFirst().getVideoId();
        String commentsSentimentObject = commentSentimentResults.getFirst().getSentimentObject();
        videoIdToSentimentObjectToResultsMap.computeIfAbsent(videoId, k -> new HashMap<>())
                .computeIfAbsent(commentsSentimentObject, k -> new HashSet<>()).addAll(newCommentSentimentResultSet);
    }

    @Override
    public CommentSentimentResult getCommentSentimentResultByCommentIdAndSentimentObject(String commentId, String sentimentObject) {
        Set<String> sentimentResultIds = commentIdToCommentSentimentResultMap.get(commentId);
        if (sentimentResultIds == null) {
            return null;
        }
        for (String sentimentResultId : sentimentResultIds) {
            CommentSentimentResult result = commentSentimentResultMap.get(sentimentResultId);
            if (result != null && result.getSentimentObject().equals(sentimentObject)) {
                return result;
            }
        }
        return null;
    }

    @Override
    public List<CommentSentimentResult> getCommentSentimentResultsByVideoIdAndSentimentObject(String videoId, String sentimentObject) {
        Map<String, Set<String>> sentimentObjectToResults = videoIdToSentimentObjectToResultsMap.get(videoId);
        if (sentimentObjectToResults == null) {
            return Collections.emptyList();
        }
        Set<String> resultIds = sentimentObjectToResults.get(sentimentObject);
        if (resultIds == null) {
            return Collections.emptyList();
        }
        return resultIds.stream()
                .map(commentSentimentResultMap::get)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentSentimentResult> getCommentSentimentResultsByVideoIdAndSentimentObject(String videoId, String sentimentObject, Pageable pageable) {
        // TODO: Implement pagination and sorting logic
        return new ArrayList<>();
    }

    @Override
    public void deleteCommentSentimentResultByVideoId(String videoId) {
        Map<String, Set<String>> sentimentObjectToResults = videoIdToSentimentObjectToResultsMap.remove(videoId);
        if (sentimentObjectToResults != null) {
            for (Set<String> resultIds : sentimentObjectToResults.values()) {
                for (String resultId : resultIds) {
                    commentSentimentResultMap.remove(resultId);
                }
            }
        }
        commentIdToCommentSentimentResultMap.values().forEach(resultIds -> resultIds.removeIf(commentSentimentResultMap::containsKey));
    }
}
