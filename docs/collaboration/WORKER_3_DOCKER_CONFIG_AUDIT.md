# Worker 3 Docker Configuration Audit

## Task 15 — 기존 Docker 설정 파일을 먼저 확인한다

- Status: existing Docker/runtime files inspected before any Docker-related implementation changes.
- Scope inspected: `compose.yml`, `compose.mysql.yml`, `compose.observability.yml`, `backend/Dockerfile`, `frontend/Dockerfile`, `frontend/nginx.conf`, and `frontend/.dockerignore`.
- No Docker configuration files were modified for this task.

## Existing Docker files

| File | Observed purpose |
|---|---|
| `compose.yml` | Main local runtime: MySQL, Redis, RabbitMQ by default; backend/frontend/nginx behind `app` profile. |
| `compose.mysql.yml` | MySQL-only schema/seed verification profile with docs/scripts mounted read-only. |
| `compose.observability.yml` | Elasticsearch, Logstash, Kibana, and Filebeat observability stack. |
| `backend/Dockerfile` | Multi-stage Maven build on `maven:3.9.9-eclipse-temurin-21`, runtime on `eclipse-temurin:21-jre`. |
| `frontend/Dockerfile` | Node 24 Alpine build followed by Nginx 1.27 Alpine static serving. |
| `frontend/nginx.conf` | SPA fallback via `try_files`; `/api/` returns 502 inside the standalone frontend image. |
| `frontend/.dockerignore` | Excludes `node_modules`, `dist`, `.vite`, `coverage`, and logs. |

## Service inventory verified

- `docker compose -f compose.yml config --services` -> `mysql`, `rabbitmq`, `redis`.
- `docker compose -f compose.yml --profile app config --services` -> `mysql`, `rabbitmq`, `redis`, `backend`, `frontend`, `nginx`.
- `docker compose -f compose.mysql.yml config --services` -> `mysql`.
- `docker compose -f compose.observability.yml config --services` -> `elasticsearch`, `logstash`, `filebeat`, `kibana`.

## Guardrail for future Docker work

- Preserve these existing files and profiles unless a later task explicitly assigns a Docker edit.
- Prefer additive documentation or profile-specific changes over replacing the current compose structure.
- Treat Docker syntax validation as necessary but not sufficient for full feature completion.
