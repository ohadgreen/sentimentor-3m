package com.acme.controllers;

import com.acme.exceptions.YouTubeQuotaExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(YouTubeQuotaExceededException.class)
    public ResponseEntity<Map<String, String>> handleQuotaExceeded() {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Map.of(
                        "error", "quota_exceeded",
                        "message", "YouTube API quota exceeded. Please try again tomorrow."
                ));
    }
}