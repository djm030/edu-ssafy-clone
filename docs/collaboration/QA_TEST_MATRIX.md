# QA Test Matrix

## Static Gates
| Gate | Command | Owner |
|---|---|---|
| line count | `scripts/dev/smoke.ps1 -SkipHttp` | DevOps-QA |
| frontend lint | `npm run lint` in `frontend` | Frontend |
| frontend build | `npm run build` in `frontend` | Frontend |
| compose config | `scripts/dev/verify-compose.ps1 -App` | DevOps-QA |
| encoding scan | PM shell scan over `frontend/src`, `docs/collaboration`, `backend/src` | PM |

## Backend Gates
| Gate | Command | Owner |
|---|---|---|
| compile | Maven `compile` with writable local repo | Backend |
| test compile | Maven `test-compile` with writable local repo | Backend |
| unit tests | Maven `test` | Backend |
| API smoke | `scripts/dev/smoke.ps1` with app running | DevOps-QA |

Known environment blockers:
- sandbox may not write `C:\Users\CodexSandboxOffline\.m2`
- Docker pipe may deny access to `npipe:////./pipe/docker_engine`
- Docker config may warn on `C:\Users\kwanyeol\.docker\config.json`

## Runtime Gates
| Gate | Command | Success |
|---|---|---|
| base infra | `docker compose up -d mysql redis rabbitmq` | all healthy |
| app stack | `docker compose --profile app up -d --build` | nginx/backend/frontend healthy |
| observability | `docker compose -f compose.yml -f compose.observability.yml up -d` | kibana/logstash/elasticsearch healthy |
| HTTP smoke | `scripts/dev/smoke.ps1` | P1 APIs and routes return expected status |

## Failure Record Template
```text
Round:
Gate:
Command:
Result:
Failure owner:
Root cause:
Fix:
Retest:
Status:
```

## Stable Service Definition
서비스가 안정적이라고 말하려면 다음 조건을 모두 만족해야 한다.

- Frontend lint/build 통과
- Backend compile/test-compile 통과
- Compose config validation 통과
- app profile 컨테이너 기동 또는 환경 권한 차단 사유 명시
- P1 HTTP smoke 통과
- 실패 항목이 `WORK_TRACKER.md`에 owner와 다음 조치로 남아 있음

