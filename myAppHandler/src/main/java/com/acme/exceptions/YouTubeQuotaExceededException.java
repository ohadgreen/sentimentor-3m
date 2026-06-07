package com.acme.exceptions;

public class YouTubeQuotaExceededException extends RuntimeException {
    public YouTubeQuotaExceededException() {
        super("YouTube API daily quota exceeded");
    }
}