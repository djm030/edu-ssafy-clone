# ERD/SQL 이후 다음 단계 합의 계획

## Scope

- 현재 상태: 애플리케이션 코드 없음. `docs/`와 `ssafy_pages/` 캡처 기반의 문서 우선 산출물이 존재함.
- 기준 데이터 모델: `docs/revised_schema_mysql8.sql`
- 구현 전 다음 산출물: 화면 목록 정의, API 명세 초안, DB 테이블 상세 설계
- 1차 구현 범위 후보: `docs/REQUIREMENTS.md`의 우선순위 1

## Evidence

- `docs/ERD.md`는 `docs/revised_schema_mysql8.sql`을 최신 구조 검토 기준으로 명시한다.
- `docs/FUNCTIONAL_SPEC.md`는 다음 단계를 화면 목록 정의, API 명세 초안, DB 테이블 상세 설계로 제시한다.
- `docs/REQUIREMENTS.md` 우선순위 1은 로그인, 메인 대시보드, 출석현황, 레벨/포인트, 공지사항 목록, 열린 게시판 목록, 학습자료 목록, Quest/평가 목록, 설문 목록이다.
- Quest 상세, Survey 상세, eBook/PDF 내부 뷰어는 확보/검증이 부족한 보강 대상으로 남아 있다.

## RALPLAN-DR

### Principles

1. 문서 기준선을 먼저 고정한다: 구현 기준은 `revised_schema_mysql8.sql`, `FUNCTIONAL_SPEC.md`, `REQUIREMENTS.md`의 명시 범위로 제한한다.
2. 우선순위 1은 목록/조회 중심 MVP로 자른다: 상세/응답/제출/작성 기능은 별도 단계로 분리한다.
3. 화면-API-DB 추적성을 확보한다: 각 화면은 기능 ID, API 후보, 주요 테이블, 캡처 근거와 연결되어야 한다.
4. 미확보 화면은 추론으로 구현하지 않는다: Quest 상세, Survey 상세, eBook/PDF 뷰어는 리스크 항목으로 남기고 목록 수준만 우선 다룬다.
5. 코드 생성보다 계약 문서 품질을 우선한다: 앱 코드가 없으므로 다음 실행 전 인터페이스와 데이터 사용 범위를 잠근다.

### Decision Drivers

1. 구현 착수 전 불확실성 감소: 화면, API, DB 매핑이 없으면 앱 구조 선택과 테스트 기준이 흔들린다.
2. MVP 범위 통제: 우선순위 1이 넓기 때문에 목록/대시보드 중심으로 잘라야 첫 구현 단위가 작아진다.
3. 후속 실행 효율: ralph/team이 바로 실행할 수 있도록 산출물 파일명, 완료 조건, 검증 기준이 명확해야 한다.

### Options

#### Option A: 화면 목록 먼저 확정

- Pros: 캡처 기반으로 빠르게 범위를 잠글 수 있고, MVP 라우트/페이지 구성이 명확해진다.
- Pros: 구현자가 UI 라우팅과 API 필요 목록을 동시에 파악하기 쉽다.
- Cons: API/DB 제약을 늦게 발견하면 화면 목록 일부를 다시 조정해야 한다.

#### Option B: API 명세 먼저 확정

- Pros: 백엔드/프론트 계약이 빨리 생겨 구현 병렬화에 유리하다.
- Pros: 인증, 페이지네이션, 필터, 빈 상태 등 공통 규칙을 먼저 표준화할 수 있다.
- Cons: 화면 목록이 확정되지 않으면 불필요하거나 누락된 엔드포인트가 생기기 쉽다.

#### Option C: DB 테이블 상세 설계 먼저 확정

- Pros: ERD/SQL 초안의 컬럼 의미, 제약, 코드값, 샘플 데이터 기준을 정리할 수 있다.
- Pros: 이후 API 응답 필드와 시드 데이터 작성이 안정적이다.
- Cons: 사용 화면과 API 요구를 보지 않고 테이블만 깊게 파면 구현 우선순위와 동떨어질 수 있다.

## Recommendation

Option A를 첫 단계로 선택하되, 화면 목록 문서 안에서 API 후보와 DB 매핑을 얕게 함께 적는 방식이 가장 적합하다. 이후 API 명세 초안, DB 테이블 상세 설계 순서로 깊이를 올린다.

추천 순서:

1. `docs/SCREEN_LIST.md` 작성
2. `docs/API_SPEC_DRAFT.md` 작성
3. `docs/DB_TABLE_DETAILS.md` 작성
4. 세 문서 간 기능 ID/화면 ID/API ID/테이블 ID 추적성 점검

## Concrete Deliverables

1. `docs/SCREEN_LIST.md`
   - 우선순위 1 화면 목록
   - 화면 ID, 라우트 후보, 관련 기능 ID, 캡처 근거, 주요 UI 상태
   - MVP 포함/제외 판단

2. `docs/API_SPEC_DRAFT.md`
   - 로그인/세션, 대시보드 요약, 출결, 레벨/포인트, 공지 목록, 열린 게시판 목록, 학습자료 목록, Quest/평가 목록, 설문 목록 API 초안
   - 요청 파라미터, 응답 필드, 페이지네이션/필터, 인증 요구, 오류/빈 상태

3. `docs/DB_TABLE_DETAILS.md`
   - `revised_schema_mysql8.sql` 기준 주요 테이블 설명
   - 우선순위 1 화면에서 사용하는 테이블/컬럼 매핑
   - 코드성 값, 상태값, FK/인덱스, 시드 데이터 후보

4. `docs/MVP_TRACEABILITY.md`
   - Priority 1 요구사항 -> 기능 ID -> 화면 ID -> API ID -> 테이블 매핑표
   - 미확보/추론/보류 항목 표시

## Acceptance Criteria

- 모든 우선순위 1 항목이 최소 1개 화면 ID와 연결된다.
- 모든 우선순위 1 화면이 최소 1개 API 후보와 연결된다.
- 모든 API 후보가 주요 참조 테이블 또는 조회 소스와 연결된다.
- `revised_schema_mysql8.sql`이 유일한 DB 기준선으로 명시되고, 구버전 `schema.sql`, `ERD.sql`, `schema.dbml`은 참고용으로만 표기된다.
- Quest 상세, Survey 상세, eBook/PDF 내부 뷰어는 MVP 목록 범위와 분리되어 보류/리스크 항목으로 표시된다.
- 로그인/대시보드/목록 화면의 빈 상태, 권한 없음, 세션 만료, 페이지네이션 또는 필터 조건이 API 초안에 반영된다.
- 앱 코드 생성이나 프레임워크 선택은 포함하지 않는다.

## ADR

### Decision

ERD/SQL 이후에는 구현을 시작하지 않고, 화면 목록 -> API 명세 초안 -> DB 테이블 상세 설계 -> 추적성 매트릭스 순서로 문서 계약을 완성한다.

### Drivers

- 현재 저장소에는 앱 코드가 없고 문서/캡처 기반 산출물이 중심이다.
- 기능명세가 다음 단계로 화면 목록, API 명세, DB 테이블 상세 설계를 직접 제시한다.
- 우선순위 1 범위는 넓지만 대부분 목록/조회 중심이라 문서 계약을 먼저 만들면 첫 구현 단위를 안정적으로 자를 수 있다.

### Alternatives Considered

- API 명세 먼저 작성: 병렬 구현에는 유리하지만 화면 범위가 덜 잠겨 누락/과잉 API 위험이 있다.
- DB 상세 설계 먼저 작성: 데이터 품질에는 유리하지만 화면/API 우선순위와 분리될 수 있다.
- 바로 앱 코드 생성: 현재는 프레임워크, 라우트, API 계약, 시드 데이터 기준이 없어 재작업 가능성이 높다.

### Consequences

- 단기적으로 구현 착수는 늦어지지만, 첫 구현 단위의 재작업 위험이 줄어든다.
- 문서 산출물 4개가 다음 실행 모드의 직접 입력이 된다.
- 미확보 상세 화면은 의도적으로 후순위로 남는다.

### Follow-ups

- 문서 4종 작성 후 구현 계획을 다시 세워 프레임워크, 프로젝트 구조, 시드 전략, 테스트 전략을 결정한다.
- 상세 화면 캡처를 추가 확보하면 Priority 2 범위로 별도 계획을 만든다.

## Execution Mode Recommendation

- Recommended: solo
- Reason: 현재 작업은 코드 구현이 아니라 문서 산출물 4개를 일관되게 작성하는 좁은 기획/명세 작업이다. 병렬 팀을 띄우면 용어와 ID 체계가 갈라질 위험이 더 크다.
- Use ralph when: 문서 4종 작성부터 자기검토, 추적성 점검까지 한 소유자가 끝까지 반복 검증해야 할 때.
- Use team when: 이후 앱 구현 단계에서 프론트엔드, 백엔드/API, DB/시드, 테스트를 병렬로 나눌 수 있을 때.

## Available Agent Types For Later Handoff

- `planner`: 문서 범위와 실행 순서 조정
- `architect`: API/DB 경계와 도메인 구조 검토
- `executor`: 문서 작성 또는 이후 구현
- `test-engineer`: acceptance criteria와 테스트 전략 구체화
- `verifier`: 문서 간 추적성 및 누락 검증
- `critic`: 범위 과잉, 추론 남용, 기준선 불일치 검토

## Suggested Next Handoff

1. solo 또는 ralph로 `docs/SCREEN_LIST.md`부터 작성한다.
2. `SCREEN_LIST.md`가 끝나면 같은 ID 체계를 유지해 `API_SPEC_DRAFT.md`를 작성한다.
3. API 응답 필드가 정리된 뒤 `DB_TABLE_DETAILS.md`로 테이블/컬럼 근거를 연결한다.
4. 마지막에 `MVP_TRACEABILITY.md`로 누락과 보류 항목을 검증한다.
