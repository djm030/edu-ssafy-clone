# Infrastructure Reuse Check

Date: 2026-04-24
Worker: worker-2
Task: 19 - MySQL, Redis, RabbitMQ, ELK, Nginx가 이미 있으면 재사용한다.

## Result

Existing infrastructure is present and must be reused. No replacement services or alternate infrastructure dependencies were introduced by worker-2.

| Infrastructure | Existing evidence | Reuse decision |
| --- | --- | --- |
| MySQL | `compose.yml` service `mysql`; `compose.mysql.yml`; schema mount `docs/revised_schema_mysql8.sql`; seed mount `scripts/mysql/20-board-list-seed.sql` | Reuse existing MySQL service and schema/seed mounts. |
| Redis | `compose.yml` service `redis`; backend env `SPRING_DATA_REDIS_HOST=redis` | Reuse existing Redis service name and port. |
| RabbitMQ | `compose.yml` service `rabbitmq`; backend env `SPRING_RABBITMQ_HOST=rabbitmq`; management port configured | Reuse existing RabbitMQ service and credentials from compose env. |
| ELK | `compose.observability.yml` services `elasticsearch`, `logstash`, `kibana`, `filebeat`; configs under `infra/logstash` and `infra/filebeat` | Reuse existing observability compose file and config directories. |
| Nginx | `compose.yml` service `nginx`; reverse proxy config under `infra/nginx/conf.d`; frontend container has `frontend/nginx.conf` | Reuse existing top-level Nginx reverse proxy and frontend Nginx serving path. |

## Verification Evidence

- `docker compose config` rendered existing MySQL, Redis, and RabbitMQ services successfully.
- `docker compose -f compose.yml --profile app config` rendered backend, frontend, and Nginx app-profile wiring successfully.
- `compose.observability.yml` contains the existing ELK stack services and was inspected as source evidence.
- `npm --prefix frontend run lint` and `npm --prefix frontend run build` passed after frontend-only changes, confirming worker-2 did not add incompatible frontend dependencies.

## Guardrail

Future work should connect application code to these existing service names instead of creating duplicate local services, alternate ports, or new infrastructure packages unless the team lead explicitly changes the stack.
