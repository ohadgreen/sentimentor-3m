package com.acme.services;

import com.acme.model.ytrawcomment.CommentThread;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

@Service
public class GetYouTubeRawComments {
    Logger logger = LoggerFactory.getLogger(GetYouTubeRawComments.class);

    @Value("${youtube.api.key}")
    private String API_KEY;
    @Value("${youtube.api.base-url}")
    private String COMMENT_THREADS_BASE_API;
    private static final int COMMENTS_PER_PAGE = 50;

    public CommentThread getRawCommentsFromYouTube(String videoId, @Nullable String nextPageToken) {
        logger.info("get raw comments videoId: {}, nextPageToken: {}", videoId, nextPageToken);

        String commentThreadsUri = COMMENT_THREADS_BASE_API + "?part=snippet&videoId="+ videoId + "&key=" + API_KEY + "&maxResults=" + COMMENTS_PER_PAGE;
        if (nextPageToken != null) {
            commentThreadsUri += "&pageToken=" + nextPageToken;
        }

        // get comment threads for a video
        try {
            URL url = new URI(commentThreadsUri).toURL();
            logger.info("comment threads url: {}", url);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            return objectMapper.readValue(connection.getInputStream(), CommentThread.class);

        } catch (IOException | URISyntaxException e) {
            logger.error("Error getting comment threads for videoId: {}", videoId, e);
            return null;
        }
    }
}
