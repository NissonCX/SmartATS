# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**SmartATS** is an intelligent recruitment management system for HR professionals. The system enables batch resume uploads, AI-powered automatic parsing of structured information, and RAG semantic talent search.

**Current State**: This is a greenfield project. No Java source code exists yet. The repository contains comprehensive design documentation in the `docs/` folder.

**Reference Documentation**:
- `docs/SmartATS-Design-Document.md` - Complete technical specification, database schema, API definitions, architecture diagrams
- `docs/SmartATS-从0到1开发教学手册.md` - Step-by-step development tutorial for Java beginners (Chinese)

## Technology Stack

| Component | Technology | Purpose |
|-----------|-----------|---------|
| Core Framework | Spring Boot 3.x | Application framework |
| ORM | MyBatis-Plus | Database operations |
| Database | MySQL 8.0 | Business data persistence |
| Cache/Rate Limit/Lock | Redis + Redisson | High-performance caching, distributed locks, rate limiting |
| Message Queue | RabbitMQ | Async task decoupling |
| AI Integration | Spring AI | LLM calls, Embedding, RAG |
| Vector Database | Milvus / PgVector | Resume vector storage and retrieval |

## Development Environment Setup

### Required Software
1. JDK 21
2. Maven 3.9+
3. Docker Desktop
4. IntelliJ IDEA (Community Edition works)
5. Postman or Apifox (for API testing)
6. DBeaver / DataGrip (for database viewing)

### Starting Infrastructure

The project requires Docker Compose services. Create `docker-compose.yml` with:
- MySQL 8.0
- Redis 7
- RabbitMQ (with management UI)
- Milvus or PgVector (for vector storage)
- MinIO (for resume file storage)

```bash
# Start all infrastructure services
docker-compose up -d
```

### Build and Run

```bash
# Build the project (once pom.xml exists)
mvn clean install

# Run the application
mvn spring-boot:run

# Or run the JAR directly
java -jar target/smartats-*.jar
```

### Testing

```bash
# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=ResumeServiceTest

# Run a specific test method
mvn test -Dtest=ResumeServiceTest#testUploadResume
```

## Architecture Overview

The system consists of **four main processing chains**:

1. **Authentication Chain**: Registration/Login/JWT validation
2. **Upload Chain**: Upload resume → Save to DB → Send to MQ → Async parsing
3. **Search Chain**: Keyword/semantic search for candidates
4. **Recruitment Chain**: Job management → Applications → Interview records

### Critical Design Patterns

**Async Processing Flow** (Resume Upload):
```
Upload → MD5 Hash → Deduplication Check (Redis) → File Storage → DB Record → Task Status (Redis) → MQ Message → Return taskId
                                                          ↓
                                      Consumer picks up → Distributed Lock → AI Parse → DB Update → Vector Store → Task Complete
```

**Caching Strategy**:
- Read: Check Redis first, fallback to MySQL, populate cache
- Write: Update MySQL, then delete corresponding cache key
- Hot data: Use ZSet for rankings (e.g., hot jobs)

**Rate Limiting**: Implemented via `@RateLimiter` annotation with AOP + Redis Lua scripts for atomicity

**Distributed Locking**: Redisson with Watchdog mechanism for long-running operations

## Module Structure

The recommended code organization:

```
src/main/java/com/yourcompany/smartats/
├── common/                 # Shared utilities
│   ├── result/            # Unified Result wrapper, ResultCode
│   ├── exception/         # BusinessException, GlobalExceptionHandler
│   ├── annotation/        # Custom annotations (@RateLimiter)
│   └── aspect/            # AOP aspects
├── config/                # Configuration classes
│   ├── RedisConfig.java
│   ├── RabbitMQConfig.java
│   ├── SecurityConfig.java
│   └── SpringAIConfig.java
├── module/                # Business modules
│   ├── auth/             # Authentication (register/login/JWT)
│   ├── resume/           # Resume upload, parsing, status tracking
│   ├── candidate/        # Candidate CRUD, search
│   ├── job/              # Job management
│   └── ai/               # AI services (extraction, embedding, search)
└── infrastructure/        # Infrastructure services
    ├── redis/            # RedisService wrapper
    ├── mq/               # MessagePublisher
    └── storage/          # FileStorageService (local or MinIO)
```

## Database Schema

Core tables (in order of dependency):
1. `users` - User accounts with roles (ADMIN, HR, INTERVIEWER)
2. `jobs` - Job postings with requirements and status
3. `resumes` - Resume files with MD5 hash for deduplication, status tracking
4. `candidates` - AI-extracted structured candidate data with JSON fields
5. `job_applications` - Application tracking with match scores
6. `interview_records` - Interview feedback and scheduling

**Important**: `candidates.resume_id` references `resumes.id` (1:1 relationship). Resumes are deduplicated by MD5 hash (`file_hash` column has unique index).

## Redis Key Patterns

| Pattern | Type | Purpose | TTL |
|---------|------|---------|-----|
| `task:resume:{taskId}` | Hash | Resume parsing task status | 24h |
| `rate:ai:{userId}:{date}` | String | AI call daily quota | 24h |
| `rate:upload:{userId}` | String | Upload frequency limit | 1min |
| `lock:resume:{fileHash}` | String | Resume parsing distributed lock | 10min |
| `cache:job:{jobId}` | String | Job detail cache | 30min |
| `cache:job:hot` | ZSet | Hot job ranking | 10min |
| `cache:candidate:{id}` | String | Candidate info cache | 30min |
| `dedup:resume:{fileHash}` | String | File deduplication mark | 7d |

## RabbitMQ Topology

- **Exchange**: `smartats.exchange` (Direct)
- **Queue**: `resume.parse.queue` (main processing queue)
- **DLX**: `smartats.dlx` (dead letter exchange)
- **DLQ**: `resume.parse.dlq` (failed tasks)
- **Routing Key**: `resume.parse`

**Message Flow**:
```
Producer → smartats.exchange → resume.parse.queue → Consumer
                                        ↓ (fail after retry)
                                  smartats.dlx → resume.parse.dlq
```

## Development Workflow Recommendations

### Phased Development Approach

**Do not start with AI/RAG features**. Follow this order:

1. **Phase 1**: Project skeleton, unified response, global exception handler
2. **Phase 2**: Auth module (register/login/JWT)
3. **Phase 3**: Job CRUD with caching
4. **Phase 4**: Resume upload (sync) → MQ consumer (async parsing)
5. **Phase 5**: Candidate module with structured data display
6. **Phase 6**: AI extraction, vector storage, semantic search

### Common Implementation Gotchas

- **File Deduplication**: Always use MD5 hash, check Redis before DB insert, use distributed lock
- **Async Task Status**: Store in Redis first, update as task progresses. Front-end polls by taskId
- **MQ Idempotency**: Check if message was already processed before starting work
- **Token-based Auth**: Store refresh tokens in Redis for revocation support
- **Cache Invalidation**: Delete cache on update, don't update cache directly
- **JSON Fields**: Use MyBatis-Plus `JSONTypeHandler` for JSON columns

### Error Code Standards

All APIs return standardized format:
```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1704067200000
}
```

Common error codes:
- `40101`: Not logged in
- `40301`: No permission
- `42901`: AI quota exceeded
- `50002`: AI service unavailable
