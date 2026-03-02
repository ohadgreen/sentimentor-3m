package com.acme.services;

import com.acme.model.ytsearch.VideoDetailsResult;
import com.acme.model.ytsearch.VideoSearchResult;
import com.acme.model.ytsearch.VideoStatistics;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GetYouTubeVideoSnippets {

    private static final Logger logger = LoggerFactory.getLogger(GetYouTubeVideoSnippets.class);
    private static final int MAX_RESULTS = 20;
    private static final int PUBLISHED_AFTER_DAYS = 30;
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .withZone(ZoneOffset.UTC);

    @Value("${youtube.api.key}")
    private String apiKey;

    @Value("${youtube.api.search-url}")
    private String searchBaseUrl;

    @Value("${youtube.api.videos-url}")
    private String videosBaseUrl;

    public VideoSearchResult searchVideos(String searchTerm, String pageToken) {
        String publishedAfter = ISO_FORMATTER.format(Instant.now().minusSeconds(PUBLISHED_AFTER_DAYS * 24L * 60 * 60));
        String searchUri = searchBaseUrl
                + "?key=" + apiKey
                + "&part=snippet"
                + "&type=video"
                + "&maxResults=" + MAX_RESULTS
                + "&q=" + encodeSearchTerm(searchTerm)
                + "&publishedAfter=" + publishedAfter
                + "&order=viewCount"
                + (pageToken != null && !pageToken.isBlank() ? "&pageToken=" + pageToken : "");

        logger.info("Searching YouTube videos: term={}, publishedAfter={}", searchTerm, publishedAfter);

        try {
            URL url = new URI(searchUri).toURL();
            logger.info("YouTube search url: {}", url);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            VideoSearchResult result = objectMapper.readValue(connection.getInputStream(), VideoSearchResult.class);

            if (result != null && result.getItems() != null && !result.getItems().isEmpty()) {
                enrichWithStatistics(result, objectMapper);
            }

            return result;

        } catch (IOException | URISyntaxException e) {
            logger.error("Error searching YouTube videos for term: {}", searchTerm, e);
            return null;
        }
    }

    private void enrichWithStatistics(VideoSearchResult result, ObjectMapper objectMapper) {
        List<String> videoIds = result.getItems().stream()
                .filter(item -> item.getId() != null && item.getId().getVideoId() != null)
                .map(item -> item.getId().getVideoId())
                .collect(Collectors.toList());

        String ids = String.join(",", videoIds);
        String videosUri = videosBaseUrl
                + "?key=" + apiKey
                + "&part=statistics"
                + "&id=" + ids;

        try {
            URL url = new URI(videosUri).toURL();
            logger.info("YouTube videos statistics url: {}", url);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            VideoDetailsResult detailsResult = objectMapper.readValue(connection.getInputStream(), VideoDetailsResult.class);

            if (detailsResult == null || detailsResult.getItems() == null) {
                return;
            }

            Map<String, VideoStatistics> statsById = detailsResult.getItems().stream()
                    .collect(Collectors.toMap(
                            item -> item.getId(),
                            item -> item.getStatistics()
                    ));

            result.getItems().forEach(item -> {
                if (item.getId() != null) {
                    VideoStatistics stats = statsById.get(item.getId().getVideoId());
                    if (stats != null) {
                        item.setStatistics(stats);
                    }
                }
            });

        } catch (IOException | URISyntaxException e) {
            logger.error("Error fetching video statistics for ids: {}", ids, e);
        }
    }

    private String encodeSearchTerm(String searchTerm) {
        return searchTerm.replace(" ", "+");
    }
}