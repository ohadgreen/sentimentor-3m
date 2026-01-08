package com.acme.controllers;

import com.acme.model.comment.ConciseComment;
import com.acme.services.RawCommentsService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
public class CommentsController {

    private final RawCommentsService rawCommentsService;
    public CommentsController(RawCommentsService rawCommentsService) {
        this.rawCommentsService = rawCommentsService;
    }

    @GetMapping("/page")
    public Page<ConciseComment> getCommentsByVideoIdWithPagination(
            @RequestParam String videoId,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize) {
        return rawCommentsService.getConciseCommentPage(videoId, pageNumber, pageSize);
    }


}
