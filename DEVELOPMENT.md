# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

- **Build all modules:** `mvn clean install`
- **Build single module:** `mvn clean install -pl aiWorker` (or `myAppHandler`, `common`)
- **Run tests:** `mvn test`
- **Run single test class:** `mvn test -pl myAppHandler -Dtest=CommentsPersistInMemoryTest`
- **Run myAppHandler:** `mvn spring-boot:run -pl myAppHandler`
- **Run aiWorker:** `mvn spring-boot:run -pl aiWorker`

## Required Environment Variables

- `MONGODB_URI` - MongoDB connection string (myAppHandler)
- `YOUTUBE_API_KEY` - YouTube Data API key (myAppHandler)
- `OPENAI_API_KEY` - OpenAI API key (aiWorker)
- Optional: `AI_WORKER_PATH` (default `http://localhost:8082/api/aiworker`), `APP_HANDLER_PATH` (default `http://localhost:8081/api`), `OPENAI_CHAT_MODEL` (default `gpt-4o-mini`), `CORE_POOL_SIZE`, `POOL_MAX_SIZE`

## Architecture

This is a **multi-module Maven project** (Java 21, Spring Boot 3.4.0) for YouTube comment sentiment analysis. Three modules:

### common
Shared DTOs for inter-service communication: `SentimentAnalysisRequest`, `SentimentAnalysisChunkRequest`, `SentimentAnalysisChunkResponse`, `CommentToAnalyze`, `CommentSentiment`.

### myAppHandler (port 8081)
Orchestrator service. Fetches YouTube comments, stores them in MongoDB, and manages the sentiment analysis workflow.

- **Controllers:** `SentimentAnalysisController` (main API), `SentimentAnalysisChunkController` (receives results from aiWorker), `CommentsController` (paginated comments)
- **Core service:** `SentimentHandlingService` — orchestrates chunked analysis. Splits comments into pages (10/page), sends up to 2 parallel chunks to aiWorker, tracks in-flight chunks via `ConcurrentHashMap`, uses per-analysis-ID locks for thread safety.
- **Persistence layer uses Strategy pattern:** interfaces (`CommentsPersistence`, `AnalysisResultPersistence`, `AnalysisSummaryPersistence`) with MongoDB and in-memory implementations. Tests use the `"memory"` Spring profile to activate in-memory implementations.

### aiWorker (port 8082)
AI processing service. Consumes analysis requests from a `BlockingQueue`, calls OpenAI via Spring AI, and POSTs results back to myAppHandler.

- **Queue architecture:** `RequestQueueController` enqueues requests → `RequestQueueService` (BlockingQueue) → `AnalysisRequestConsumerService` (5 consumer threads) → `SentimentAnalysisService` (Spring AI with prompt template `comments-sentiment-analysis-prompt.st`)
- Thread pool: 3 core / 8 max threads, queue capacity 100

## Data Flow

1. Client calls myAppHandler to fetch YouTube comments → stored in MongoDB
2. Client requests sentiment analysis → `SentimentHandlingService` chunks comments, sends to aiWorker queue
3. aiWorker consumer threads process chunks via OpenAI, POST results back to myAppHandler
4. myAppHandler aggregates results, queues next chunk if needed, marks COMPLETED when done
5. Client polls ongoing analysis status endpoint for progress

## MongoDB Collections

- `video_comments_summary` — per-video summary with sentiment analysis status map
- `comment_sentiments` — individual results; compound unique index on `(commentId, sentimentObject)`
- `concise_comments` — stored YouTube comments; indexed on `videoId`
