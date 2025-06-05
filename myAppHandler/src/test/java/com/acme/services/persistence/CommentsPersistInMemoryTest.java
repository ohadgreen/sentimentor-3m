package com.acme.services.persistence;

import com.acme.model.comment.ConciseComment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CommentsPersistInMemoryTest {

    private CommentsPersistInMemory commentsPersistInMemory;

    @BeforeEach
    void setUp() {
        commentsPersistInMemory = new CommentsPersistInMemory();

        // Create test comments
        ConciseComment comment1 = new ConciseComment();
        comment1.setCommentId("c1");
        comment1.setVideoId("video123");
        comment1.setLikeCount(10);
        comment1.setPublishedAt(LocalDateTime.of(2023, 1, 1, 10, 0));

        ConciseComment comment2 = new ConciseComment();
        comment2.setCommentId("c2");
        comment2.setVideoId("video123");
        comment2.setLikeCount(15);
        comment2.setPublishedAt(LocalDateTime.of(2023, 1, 2, 10, 0));

        ConciseComment comment3 = new ConciseComment();
        comment3.setCommentId("c3");
        comment3.setVideoId("video123");
        comment3.setLikeCount(15); // same likeCount as comment2, but older date
        comment3.setPublishedAt(LocalDateTime.of(2023, 1, 1, 9, 0));

        commentsPersistInMemory.conciseCommentsMap.put("video123", Arrays.asList(comment1, comment2, comment3));
    }

    @Test
    void shouldReturnSortedAndPagedComments() {
        Pageable pageable = PageRequest.of(0, 2); // First page, 2 items

        List<ConciseComment> result = commentsPersistInMemory.getCommentsPageByVideoId("video123", pageable);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCommentId()).isEqualTo("c3"); // same likeCount as c2 but earlier date
        assertThat(result.get(1).getCommentId()).isEqualTo("c2");
    }

    @Test
    void shouldReturnEmptyListIfVideoIdNotFound() {
        Pageable pageable = PageRequest.of(0, 10);

        List<ConciseComment> result = commentsPersistInMemory.getCommentsPageByVideoId("nonexistent", pageable);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnSecondPageCorrectly() {
        Pageable pageable = PageRequest.of(1, 2); // Second page

        List<ConciseComment> result = commentsPersistInMemory.getCommentsPageByVideoId("video123", pageable);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCommentId()).isEqualTo("c1");
    }
}