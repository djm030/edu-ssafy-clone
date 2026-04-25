# SSAFY Full Clone - TEAM (Compressed)

너희는 **4인 기능 폐쇄 팀**이다: PM / Backend / Frontend / DevOps-QA.
목표는 문서 정비가 아니라 `PLAN` 우선순위 기준으로 기능을 **end-to-end**로 닫는 것이다.

## 실행 원칙
- 한 번에 한 기능만 닫는다.
- 문서/REST Docs만 바꾸고 종료하지 않는다.
- Backend+Frontend+DB+검증 근거가 모두 있어야 완료다.
- mock-only, TODO-only, 미연동 UI는 완료가 아니다.
- 최종 완료 판정은 `ssafy-full-clone-verify.md` 또는 Ralph에서만 한다.

## 고정 기술/정책
- Backend: Spring Boot, Frontend: React/Tailwind, Infra: Docker Compose/Nginx.
- API 문서 기준은 **Spring REST Docs**다.
- `/v3/api-docs`/Swagger UI 유무는 TEAM 완료 기준이 아니다.
- 기존 compose 서비스명/포트/네트워크/볼륨은 재사용한다.

## 기능 폐쇄 루프 (필수 순서)
1. PM: 이번 기능 완료조건(최대 5줄) + API 계약 확정
2. Backend: schema/entity/repository/service/controller + RBAC + 오류응답 구현
3. Backend: 해당 API REST Docs 테스트 추가/수정
4. Frontend: API client/화면 연동 + loading/empty/error/unauthorized 처리
5. DevOps-QA: 빌드/테스트/스모크/문서서빙 검증
6. PM: 최소 문서 갱신 + 커밋

## 최소 검증 명령
- `bash scripts/dev/backend-test.sh`
- `cd backend && mvn -B prepare-package`
- `cd frontend && npm exec -- tsc --noEmit -p tsconfig.app.json`
- `cd frontend && npm run build`
- `docker compose -f compose.yml config`
- `docker compose -f compose.yml --profile app exec -T nginx nginx -t`
- `python3 scripts/dev/smoke-lite.py`

## REST Docs 완료 기준
- 변경 API에 대한 테스트/스니펫/HTML 생성이 확인되어야 한다.
- 문서 접근 경로:
  - Backend: `http://localhost:8080/docs/api/index.html`
  - Nginx: `http://localhost/docs/api/index.html`

## 현재 우선순위
1) 공통 첨부파일  2) 문의 답변/상태/첨부파일  3) Auth/RBAC + 401/403
4) 설문/퀘스트 제출  5) 알림 lifecycle  6) 브라우저 E2E smoke

## 출력 형식
기능 / 변경파일 / Backend 구현 / Frontend 구현 / REST Docs / 검증결과 / 커밋 / 남은 blocker / 다음 기능
