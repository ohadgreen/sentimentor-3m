package com.acme.services.persistence;

import com.acme.model.comment.ConciseComment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Profile("memory")
public class CommentsPersistInMemory implements CommentsPersistence {

    private final Logger logger = LoggerFactory.getLogger(CommentsPersistInMemory.class);
    public Map<String, List<ConciseComment>> conciseCommentsMap = new HashMap<>();

    @Override
    public void saveConciseComments(List<ConciseComment> conciseCommentList) {
        if (conciseCommentList.isEmpty()) {
            return;
        }
        String videoId = conciseCommentList.getFirst().getVideoId();
        if (conciseCommentsMap.containsKey(videoId)) {
            logger.warn("videoId exists in map");
        } else {
            conciseCommentsMap.put(videoId, conciseCommentList);
            logger.info("saved {} comments for videoId {}", conciseCommentList.size(), videoId);
        }
    }

    @Override
    public List<ConciseComment> getCommentsPageByVideoId(String videoId, Pageable pageable) {
        List<ConciseComment> conciseCommentList = conciseCommentsMap.get(videoId);

        if (conciseCommentList == null || conciseCommentList.isEmpty()) {
            return Collections.emptyList();
        }
        int pageSize = pageable.getPageSize();
        int limit = pageSize;
        if (limit <= 0) {
            limit = conciseCommentList.size();
        }
        if (limit > conciseCommentList.size()) {
            limit = conciseCommentList.size();
        }

        conciseCommentList.sort(Comparator.comparingInt(ConciseComment::getLikeCount).reversed()
                .thenComparing(ConciseComment::getPublishedAt));

        int pageNumber = pageable.getPageNumber();
        int start = pageNumber * pageSize;
        int end = Math.min(start + limit, conciseCommentList.size());
        if (start >= conciseCommentList.size()) {
            return Collections.emptyList();
        }
        List<ConciseComment> sortedComments = conciseCommentList.subList(start, end);
        logger.info("retrieved {} comments for videoId {}", sortedComments.size(), videoId);
        return sortedComments;
    }

    @Override
    public void removeCommentsByVideoId(String videoId) {
        conciseCommentsMap.remove(videoId);
    }
}
