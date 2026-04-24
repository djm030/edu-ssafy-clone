# PM + 3-Agent Collaboration Harness

## 목적
SSAFY 클론 코딩을 `설계노트 -> 구현계획 -> 코드작성 -> 테스트 -> 사람리뷰` 순서로 진행하기 위한 협업 하네스다.

현재 저장소는 ERD, SQL, 캡처, 협업 문서를 기반으로 구현을 시작하는 단계다. 바로 전체 기능을 병렬 구현하지 않고, 작은 파일럿 기능으로 계약과 검증 루프를 먼저 고정한다.

## 운영 원칙
- PM은 구현자가 아니라 계약 관리자다.
- Backend, Frontend, DevOps-QA는 PM이 승인한 문서 범위 밖 기능을 만들지 않는다.
- 공유 계약 문서는 PM이 최종 수정한다.
- 각 에이전트는 자기 소유 경로만 수정한다.
- legacy SSAFY URL은 근거 자료로만 사용하고, 구현 계약은 도메인 API 기준으로 작성한다.
- DB 기준은 `docs/revised_schema_mysql8.sql`이다.
- 공지와 자유게시판은 별도 `notices` 테이블을 만들지 않고 `boards`, `board_categories`, `board_posts` 계열로 모델링한다.
- 직접 작성하는 코드 파일은 500줄을 넘기지 않는다. 넘을 가능성이 있으면 컴포넌트, 서비스, 설정 파일을 분리한다.

## 역할 구조
```text
Human Reviewers
      |
PM / Orchestrator
      |
+----------------+----------------+----------------+
| Backend Agent  | Frontend Agent | DevOps-QA Agent |
+----------------+----------------+----------------+
```

## PM 책임
- `docs/collaboration/DESIGN_NOTES.md` 유지
- `docs/collaboration/IMPLEMENTATION_PLAN.md` 유지
- `docs/collaboration/API_SPEC_DRAFT.md` 최종 승인
- `docs/collaboration/SCREEN_LIST.md` 최종 승인
- `docs/collaboration/TEST_SPEC.md` 최종 승인
- lane 산출물 충돌 조정
- 최종 사람리뷰 체크리스트 작성

## 에이전트 책임
### Backend Agent
- Spring Boot API 구현
- DB/API 계약 반영
- schema 실행 가능성 검증 지원
- seed data shape 제안
- 서비스, 리포지토리, API 테스트 작성

### Frontend Agent
- React 화면, 라우트, 상태, UI 계약 구현
- API 응답과 화면 상태 매핑
- 캡처 기반 UI 검증 포인트 정리

### DevOps-QA Agent
- Docker Compose 기반 실행 하네스 유지
- MySQL 8, Redis, RabbitMQ, Nginx, ELK 구성
- schema 검증, smoke test, 운영 스크립트 관리
- 검증 증거 수집

## 단계 게이트
### 1. 설계노트
완료 조건:
- 파일럿 기능의 FR/FS ID가 명시되어 있다.
- 관련 화면 캡처와 스키마 테이블이 연결되어 있다.
- 추론과 확인된 사실이 분리되어 있다.

### 2. 구현계획
완료 조건:
- 각 작업에 담당 lane, 입력, 출력, 완료 조건, 테스트가 있다.
- 공유 파일 수정 권한이 명시되어 있다.
- DB/API/UI 변경 순서가 정해져 있다.

### 3. 코드작성
완료 조건:
- PM 승인 범위의 작업만 수행한다.
- 각 lane은 자기 소유 경로만 수정한다.
- 직접 작성 코드 파일은 500줄 이하로 유지한다.
- API 계약 변경은 PM 문서 갱신 후 반영한다.

### 4. 테스트
완료 조건:
- SQL 실행 검증 또는 명시된 미검증 사유가 있다.
- API contract check가 있다.
- 화면 smoke check가 있다.
- 실패 시 담당 lane과 수정 루프가 기록된다.

### 5. 사람리뷰
완료 조건:
- PM이 변경 요약, 테스트 증거, 남은 리스크를 제공한다.
- 리뷰어가 기능, 화면, 데이터 관점에서 승인 또는 수정 요청을 남긴다.

## 첫 파일럿 범위
게시판 목록 공통 모델로 진행한다.

- 공지사항 목록: `FR-NOTI-001`, `FS-NOTI-001`
- 자유게시판 목록: `FR-COMM-001`, `FS-COMM-001`
- 게시글 메타데이터 표시: `FR-COMM-002`, `FS-COMM-002`

선택 이유:
- 우선순위 1 범위에 포함된다.
- 공지와 자유게시판이 같은 DB 모델을 공유하므로 Backend/API 계약을 검증하기 좋다.
- 목록, 검색, 카테고리, 페이지네이션, 빈 상태 등 공통 UI 패턴을 검증할 수 있다.

## OMX 사용 방식
계획 검토:
```bash
omx plan --consensus "SSAFY clone PM+3-agent harness 설계와 파일럿 구현계획 검토"
```

병렬 실행:
```bash
omx team 3:executor "execute docs/collaboration/IMPLEMENTATION_PLAN.md with Backend, Frontend, DevOps-QA lanes"
```

최종 검증:
```bash
omx ralph "verify implementation against docs/collaboration/TEST_SPEC.md and summarize risks for human review"
```

## 변경 제어
- PM 문서 변경 후에는 변경 이유를 `Decision Log`에 남긴다.
- Backend가 API 응답 shape를 바꾸면 `API_SPEC_DRAFT.md`가 먼저 바뀌어야 한다.
- Frontend가 화면 요구사항을 바꾸면 `SCREEN_LIST.md`가 먼저 바뀌어야 한다.
- DevOps-QA가 테스트 기준을 바꾸면 `TEST_SPEC.md`가 먼저 바뀌어야 한다.

## Decision Log
- 2026-04-24: PM + Backend + Frontend + DevOps-QA 구조를 기본 작업 하네스로 채택했다.
- 2026-04-24: 첫 파일럿을 공지사항 목록과 자유게시판 목록으로 고정했다.
- 2026-04-24: 직접 작성 코드 파일은 500줄 이하로 유지하는 규칙을 추가했다.
