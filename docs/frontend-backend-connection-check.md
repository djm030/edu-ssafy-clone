# Frontend / Backend Connection Check

Date: 2026-04-24
Worker: worker-2
Task: 49 - Backend와 Frontend가 실제로 연결되어 있다.

## Result

Frontend and backend are connected through real HTTP API paths and the Docker/Nginx reverse proxy wiring. The frontend API clients call relative `/api/...` URLs, the backend exposes matching Spring MVC mappings, and the top-level Nginx service proxies `/api/` to the `backend` service while serving the `frontend` service for page routes.

## Connection Evidence

| Layer | Evidence | Status |
| --- | --- | --- |
| Frontend API client | `frontend/src/api/app.ts` and `frontend/src/api/boards.ts` call `/api/auth/login`, `/api/dashboard/summary`, `/api/attendance/records`, `/api/notifications`, `/api/learning/*`, `/api/quests`, `/api/surveys`, `/api/profile`, `/api/community/classmates`, `/api/support/tickets`, and `/api/boards/*`. | Connected to backend path space. |
| Backend controllers | Spring controllers expose matching routes under `backend/src/main/java/com/edussafy/backend/**`: auth, profile, dashboard, attendance, notification, learning, quest/survey, community, support, and board endpoints. | Matching server endpoints exist. |
| Reverse proxy | `infra/nginx/conf.d/default.conf` has `location /api/` and `location /actuator/` proxying to `backend:8080`, and `location /` proxying to `frontend:80`. | Browser traffic is routed to the correct service. |
| Docker app profile | `compose.yml` defines `backend`, `frontend`, and `nginx`; `frontend` depends on healthy `backend`, and `nginx` depends on both `frontend` and `backend`. | Runtime service graph is wired. |

## Verification Evidence

- `rg` frontend API call inspection found relative `/api/...` calls in both API client modules.
- `rg` backend mapping inspection found matching `@RequestMapping` / method mappings for frontend-called domains.
- `rg` Nginx inspection confirmed `/api/` -> `backend:8080` and `/` -> `frontend:80`.
- `docker compose -f compose.yml --profile app config` -> PASS.
- `npm --prefix frontend run build` -> PASS (`tsc -b && vite build`, 65 modules transformed).

## Limitation

This worker verified static route wiring and build/config correctness. Live browser-to-backend smoke execution was not run here; that remains covered by smoke/QA tasks such as Tasks 55, 88, and 129.
