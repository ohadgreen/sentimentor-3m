package com.acme.controllers;

import com.acme.model.ytsearch.VideoSearchResult;
import com.acme.services.GetYouTubeVideoSnippets;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/videos")
public class VideoSearchController {

    private final GetYouTubeVideoSnippets getYouTubeVideoSnippets;

    public VideoSearchController(GetYouTubeVideoSnippets getYouTubeVideoSnippets) {
        this.getYouTubeVideoSnippets = getYouTubeVideoSnippets;
    }

    @GetMapping("/search")
    public VideoSearchResult searchVideos(@RequestParam String q,
                                          @RequestParam(required = false) String pageToken) {
        return getYouTubeVideoSnippets.searchVideos(q, pageToken);
    }
}