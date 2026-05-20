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

- **Controllers:** `SentimentAnalysisController` (main API), `SentimentAnalysisChunkController` (receives results from aiWorker), `CommentsController` (paginated comments), `TrendAnalysisController` (trend jobs)
- **Core service:** `SentimentHandlingService` — orchestrates chunked analysis. Splits comments into pages (10/page), sends up to 2 parallel chunks to aiWorker, tracks in-flight chunks via `ConcurrentHashMap`, uses per-analysis-ID locks for thread safety.
- **Persistence layer uses Strategy pattern:** interfaces (`CommentsPersistence`, `AnalysisResultPersistence`, `AnalysisSummaryPersistence`) with MongoDB and in-memory implementations. Tests use the `"memory"` Spring profile to activate in-memory implementations.
- **Async config:** `AsyncConfig` enables `@Async` and defines a `trendAnalysisExecutor` thread pool (2 core / 5 max / 50 queue, prefix `"trend-"`) used by trend analysis jobs.

### aiWorker (port 8082)
AI processing service. Listens on an ActiveMQ JMS queue, calls OpenAI via Spring AI, and sends results back to myAppHandler via a JMS response queue.

- **JMS listener:** `AnalysisRequestListener` (`@JmsListener` on `sentiment.analysis.request`) → `SentimentAnalysisService` (Spring AI with prompt template `comments-sentiment-analysis-prompt.st`) → publishes `SentimentAnalysisChunkResponse` to `sentiment.analysis.response`
- Listener concurrency: `1-5` threads (configurable via `jms.listener.concurrency`)

## Data Flow

### Ad-hoc Analysis
1. Client calls myAppHandler to fetch YouTube comments → stored in MongoDB
2. Client requests sentiment analysis → `SentimentHandlingService` chunks comments (10/page), sends up to 2 parallel `SentimentAnalysisChunkRequest` messages to the `sentiment.analysis.request` JMS queue
3. aiWorker `AnalysisRequestListener` picks up each chunk, processes via OpenAI, publishes `SentimentAnalysisChunkResponse` to the `sentiment.analysis.response` queue
4. myAppHandler `AnalysisResponseListener` receives responses, aggregates results, sends next chunk if needed, marks COMPLETED when done
5. Client polls the analysis status endpoint for progress

### Trend Analysis
1. Client POSTs to `/api/trend/start` with search query, sentiment object, and time range
2. `TrendAnalysisOrchestrationService.startTrendJob()` creates a `TrendAnalysisJob` (PENDING), saves to MongoDB, and immediately returns the `jobId`
3. `@Async runTrendJob()` executes on `trendAnalysisExecutor` thread pool, sets status to IN_PROGRESS
4. For each day in the range (most recent first): searches YouTube with date filters, selects videos with ≥100 comments, fetches comments, triggers sentiment analysis, polls until COMPLETED, accumulates daily counts
5. After each day `daysCompleted` is incremented and job is persisted (fault tolerant)
6. Job reaches COMPLETED or FAILED; client polls `/api/trend/{jobId}` every 10 s for progress

## JMS / ActiveMQ

Both modules communicate via **Apache ActiveMQ Classic** (OpenWire on port 61616, web console on 8161).

| Queue | Direction | Message type |
|---|---|---|
| `sentiment.analysis.request` | myAppHandler → aiWorker | `SentimentAnalysisChunkRequest` |
| `sentiment.analysis.response` | aiWorker → myAppHandler | `SentimentAnalysisChunkResponse` |

- **Serialization:** `MappingJackson2MessageConverter` (JSON text messages, type hint in `_type` header, `JavaTimeModule` for `LocalDateTime`)
- **myAppHandler listener concurrency:** 1–3 threads; **aiWorker listener concurrency:** 1–5 threads
- **Broker URL:** `tcp://localhost:61616` (override with `ACTIVEMQ_BROKER_URL` env var), credentials `admin:admin`
- Docker service uses image `apache/activemq-classic` with custom config from `monitoring/activemq.xml`

## MongoDB Collections

- `video_comments_summary` — per-video summary with sentiment analysis status map; includes `sourceMode` (AD_HOC / TREND) and optional `jobId`
- `comment_sentiments` — individual results; compound unique index on `(commentId, sentimentObject)`
- `concise_comments` — stored YouTube comments; indexed on `videoId`
- `trend_analysis_jobs` — trend job documents with status, daily results, and progress counters

## Rate Limiting

Bucket4j token-bucket rate limits per IP (configured in `application.yml`):

| Endpoint | Limit |
|---|---|
| `/api/analyze` | 4 req/min |
| `/api/comments` | 10 req/min |
| `/api/trend/start` | 2 req/min |

## SourceMode

`SourceMode` enum (`AD_HOC` / `TREND`) is stored on `VideoCommentsSummary` to distinguish user-initiated searches from batch trend jobs. The latest-videos query excludes TREND-sourced entries so the home screen only shows ad-hoc analyses.

## Trend Analysis Data Model

| Class | Role |
|---|---|
| `TrendAnalysisRequest` | API input DTO (searchQuery, sentimentObject, daysBack 1-30, commentsPerVideo 10-500, videosPerDay 1-10) |
| `TrendAnalysisJob` | MongoDB document (`trend_analysis_jobs`) tracking job state and accumulated `dailyResults` |
| `DailyTrendResult` | Per-day aggregation: positive/negative/neutral counts, videoCount, videoIds, date |
| `TrendJobStatus` | Enum: PENDING → IN_PROGRESS → COMPLETED / FAILED |
| `TrendAnalysisJobDto` | Full job response DTO (factory `from(TrendAnalysisJob)`) |
| `TrendSummaryDto` | Aggregated totals + percentages + per-date `LinkedHashMap` breakdown |

## Frontend

React SPA (port 3000). Key components:

- **TubeSurfMain** — root component; renders either `TrendAnalysisResult` (when a trend job is active), `VideoDisplayWrapper` (ad-hoc video details), or the home screen (two-column: ad-hoc left / trend right)
- **TrendAnalysisForm** — form to submit trend jobs (POST `/api/trend/start`); calls `onJobStarted(jobId, params)` on success
- **TrendAnalysisResult** — polls job status every 10 s; shows progress bar while IN_PROGRESS, Chart.js line chart + per-date table when COMPLETED, error badge on FAILED
