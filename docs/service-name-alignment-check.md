# Backend/Frontend Service Name Alignment Check

Date: 2026-04-24
Worker: worker-2
Task: 21 - Backend/Frontend 설정이 기존 Docker 서비스명과 맞지 않을 때만 수정한다.

## Result

Backend and frontend settings already align with the existing Docker service names. No corrective source or compose changes were required.

## Backend Alignment

| Setting | Current value | Docker service match |
| --- | --- | --- |
| MySQL URL | `jdbc:mysql://mysql:3306/...` in `compose.yml`; fallback `jdbc:mysql://mysql:3306/...` in `backend/src/main/resources/application.yml` | `mysql` service exists in `compose.yml`. |
| Redis host | `SPRING_DATA_REDIS_HOST=redis`; fallback `redis` in `application.yml` | `redis` service exists in `compose.yml`. |
| RabbitMQ host | `SPRING_RABBITMQ_HOST=rabbitmq`; fallback `rabbitmq` in `application.yml` | `rabbitmq` service exists in `compose.yml`. |

## Frontend / Reverse Proxy Alignment

| Setting | Current value | Docker service match |
| --- | --- | --- |
| Frontend API calls | Relative `/api/...` paths in `frontend/src/api/app.ts` and `frontend/src/api/boards.ts` | Top-level Nginx owns `/api/` routing. |
| Nginx backend upstream | `server backend:8080` in `infra/nginx/conf.d/default.conf` | `backend` service exists in `compose.yml`. |
| Nginx frontend upstream | `server frontend:80` in `infra/nginx/conf.d/default.conf` | `frontend` service exists in `compose.yml`. |
| Frontend container Nginx | `frontend/nginx.conf` serves the SPA and intentionally does not proxy API directly | API proxy remains centralized in the top-level `nginx` service. |

## Verification Evidence

- `docker compose -f compose.yml --profile app config` -> PASS.
- `rg "['\`]/api/" frontend/src/api frontend/src` -> frontend API calls use relative `/api/...` paths.
- `grep`/inspection confirmed backend env and fallback hosts use `mysql`, `redis`, and `rabbitmq`.
- `npm --prefix frontend run lint` -> PASS.

## Decision

No changes were made to service names because the current application settings already match Docker Compose. Future changes should preserve these names unless the compose services are intentionally renamed in the same change set.
