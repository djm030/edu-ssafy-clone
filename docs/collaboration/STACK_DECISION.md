# Stack Decision

## Decision
SSAFY clone implementation uses a Docker Compose based monorepo with these fixed lanes:

- Frontend: React + Vite + TypeScript
- Backend: Spring Boot + Java 21
- Gateway/static serving: Nginx
- Database: MySQL 8
- Cache/session support: Redis
- Messaging: RabbitMQ
- Observability for operations: ELK stack

## Rationale
The project is moving from documentation and schema design into implementation. The stack must be fixed before multi-agent coding starts so Backend, Frontend, and DevOps-QA can work independently without negotiating framework choices during implementation.

## Architecture
```text
Browser
  |
Nginx
  |-- /api/* -> Spring Boot backend
  |-- /*      -> React static app

Spring Boot backend
  |-- MySQL 8
  |-- Redis
  |-- RabbitMQ

Operations
  |-- Elasticsearch
  |-- Logstash
  |-- Kibana
```

## Docker Compose Rules
- All runtime services start through Docker Compose.
- Infrastructure services must be separated from app rebuilds.
- MySQL, Redis, RabbitMQ, and ELK use image pulls and named volumes.
- Spring Boot and React are the primary frequently rebuilt images.
- Backend and frontend Dockerfiles must cache dependency layers before copying source code.
- Environment-specific secrets stay in `.env`, not in git.
- Hand-written code/config files should stay under 500 lines; split files before they exceed that limit.

## Caching Strategy
### Backend
- Copy `pom.xml` first.
- Resolve Maven dependencies before copying `src`.
- Copy source only after dependency cache layer.
- Runtime image should contain only the built jar and JRE.

### Frontend
- Copy `package.json` and lockfile first.
- Run dependency install before copying `src`.
- Build static assets.
- Serve final assets from Nginx.

### Infrastructure
- Use versioned upstream images and named volumes.
- Do not rebuild infra containers when only Spring Boot or React code changes.
- Use separate compose layers for base runtime and observability where possible.

## Required Services
### Base Runtime
- `mysql`
- `redis`
- `rabbitmq`
- `backend`
- `frontend` or `nginx`

### Operations Profile
- `elasticsearch`
- `logstash`
- `kibana`
- optional log shipper if needed

## Work Ownership
### PM
- Maintains stack decision and work contract docs.
- Resolves cross-lane contract changes.

### Backend Agent
- Owns `backend/**`.
- Implements Spring Boot API and backend Dockerfile.

### Frontend Agent
- Owns `frontend/**`.
- Implements React app and frontend Dockerfile.

### DevOps-QA Agent
- Owns compose files, `infra/**`, and `scripts/**`.
- Implements Docker Compose, Nginx, MySQL seed/verify, Redis/RabbitMQ/ELK wiring, and QA scripts.

## Initial Functional Pilot
The first implementation pilot remains the board-list flow:

- `GET /api/boards/{boardCode}/categories`
- `GET /api/boards/{boardCode}/posts`
- `/help/notice`
- `/community/free`

The schema source remains `docs/revised_schema_mysql8.sql`.

## Non-Goals For First Pass
- Full auth/session implementation
- Full CRUD for boards
- Quest/survey submission flows
- Production-grade secret management
- External deployment

## Review Gate
Before human review, the PM must collect:

- Docker Compose startup result
- MySQL schema verification result
- Backend API smoke result
- Frontend route smoke result
- ELK service availability or documented startup risk
- Known gaps
