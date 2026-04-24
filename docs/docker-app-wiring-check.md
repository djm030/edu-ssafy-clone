# Docker / Application Wiring Check

Date: 2026-04-24
Worker: worker-2
Task: 61 - Docker 설정과 실제 애플리케이션 설정이 연결되어 있다.

## Result

Docker Compose and the actual backend/frontend application settings are connected. The app profile renders the full service graph (`redis`, `mysql`, `rabbitmq`, `backend`, `frontend`, `nginx`), backend environment variables point to Docker service names, the frontend image builds the Vite app into Nginx, and the top-level Nginx reverse proxy routes browser/API traffic to the proper services.

## Wiring Evidence

| Boundary | Evidence | Status |
| --- | --- | --- |
| Compose app graph | `docker compose -f compose.yml --profile app config --services` returns `redis`, `mysql`, `rabbitmq`, `backend`, `frontend`, `nginx`. | PASS |
| Backend -> MySQL | `compose.yml` sets `SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/...`; `application.yml` fallback also uses `mysql`. | PASS |
| Backend -> Redis | `compose.yml` sets `SPRING_DATA_REDIS_HOST=redis`; `application.yml` fallback also uses `redis`. | PASS |
| Backend -> RabbitMQ | `compose.yml` sets `SPRING_RABBITMQ_HOST=rabbitmq`; `application.yml` fallback also uses `rabbitmq`. | PASS |
| Frontend container | `frontend/Dockerfile` runs `npm ci`, `npm run build`, then serves `/app/dist` with Nginx. | PASS |
| Browser/API proxy | `infra/nginx/conf.d/default.conf` proxies `/api/` and `/actuator/` to `backend:8080`, and `/` to `frontend:80`. | PASS |
| Frontend API paths | API clients use relative `/api/...` paths, so browser requests flow through the top-level Nginx proxy. | PASS |

## Verification Evidence

- `docker compose -f compose.yml --profile app config --services` -> PASS, rendered expected app services.
- `docker compose -f compose.yml --profile app config` -> PASS.
- `npm --prefix frontend run build` -> PASS (`tsc -b && vite build`, 65 modules transformed).
- Static inspection confirmed backend env fallbacks and Nginx upstreams use the existing Docker service names.

## Classification Notes

This check passes for static/build/config wiring. It does not claim all full-clone domains are PASS: remaining PARTIAL items such as durable auth/RBAC, attachments, domain depth, and live E2E are tracked by follow-up tasks 117-130 and final verification tasks.
