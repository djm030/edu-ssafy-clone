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
The project is now an implemented local full-stack clone surface. The stack remains fixed so ongoing UI/UX parity, verification, and production-hardening work does not re-open framework choices.

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

## Current Ownership Boundaries
- Backend/API changes live under `backend/**`.
- Frontend route/UI/client changes live under `frontend/**`.
- Runtime, Nginx, compose, smoke, and schema helper changes live under `compose*.yml`, `infra/**`, and `scripts/**`.
- Stable project facts live under `docs/**`; current UI/UX parity work is tracked in `docs/next_plan.md`.

## Current Functional Surface
- Auth/session/profile, dashboard, attendance, notifications, learning materials/resources, quests, surveys, boards, support tickets, classmate notifications, health/readiness, and Swagger/OpenAPI routes are implemented in the local clone surface.
- The generated runtime API inventory is `docs/openapi.json`.
- The schema source remains `docs/revised_schema_mysql8.sql`.

## Remaining Non-Goals For Local Clone PASS
- Pixel-perfect browser visual baseline automation.
- External deployment sign-off.
- Production secret-store integration.
- Real external SSO/JOB/Git/Meeting token issuance.

## Review Gate
Before human review, the PM must collect:

- Docker Compose startup result
- MySQL schema verification result
- Backend API smoke result
- Frontend route smoke result
- ELK service availability or documented startup risk
- Known gaps
