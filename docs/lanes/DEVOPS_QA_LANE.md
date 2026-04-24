# DevOps-QA Lane Contract

## 목적

PM + 3-agent 협업 하네스에서 DevOps-QA Agent는 구현 산출물이 실행, 검증, 증거 수집, 인수인계 가능한 상태인지 확인하는 품질 게이트를 소유한다. 현재 저장소는 문서와 캡처 산출물 중심이며 애플리케이션 프레임워크가 아직 확정되지 않았으므로, 이 문서는 현재 확인 가능한 근거와 향후 하네스 자리표시자 명령을 함께 정의한다.

## 책임

- 요구사항, 기능 명세, 스키마, 캡처 매니페스트를 기준으로 검증 범위를 구체화한다.
- Backend/Frontend 산출물을 통합 환경에서 실행할 수 있는 로컬/CI 체크리스트를 유지한다.
- MySQL 8 DDL 실행 가능성, 기본 코드 데이터, 파일 인코딩, 외래키/체크 제약 조건을 검증한다.
- 공지사항 목록과 자유게시판 목록 파일럿 기능의 API/UI/데이터 정합성을 검증한다.
- 회귀 테스트, 스모크 테스트, 캡처/로그/리포트 등 증거 산출물을 수집하고 PM에게 판단 가능한 형태로 전달한다.
- 실패를 구현 담당 lane에 재현 조건과 최소 증거로 되돌려 보내며, 임의로 Backend/Frontend 코드를 수정하지 않는다.

## 소유 경로

DevOps-QA Agent가 향후 생성/수정할 수 있는 경로는 PM이 별도로 승인한 경우에 한한다. 기본 소유 후보는 다음과 같다.

- `docs/lanes/DEVOPS_QA_LANE.md`
- `docs/qa/`
- `qa/`
- `tests/e2e/`
- `tests/smoke/`
- `.github/workflows/`
- `tools/qa/`
- `artifacts/qa/`

현재 작업의 실제 쓰기 범위는 `docs/lanes/DEVOPS_QA_LANE.md`뿐이다.

## 금지 경로

- Backend Agent 소유 구현 코드와 마이그레이션 파일
- Frontend Agent 소유 화면, 라우트, 스타일, 상태관리 코드
- PM 산출물 원본 요구사항/승인 문서
- `ssafy_pages/` 원본 캡처 HTML/PNG/JSON
- `docs/revised_schema_mysql8.sql` 원본 DDL
- 인증정보, 실제 사용자 계정, 외부 운영 서비스 설정

금지 경로의 결함은 수정하지 않고 재현 절차, 기대/실제 결과, 관련 로그를 담당 lane에 전달한다.

## 의존 관계

### PM

- 파일럿 범위, 우선순위, 인수 기준, 데모 시나리오를 확정한다.
- 공지사항/자유게시판 목록의 필수 컬럼과 정렬/필터/빈 상태 기준을 승인한다.
- QA가 제출한 실패 증거의 릴리스 차단 여부를 결정한다.

### Backend

- MySQL 8 DDL 적용 방식과 마이그레이션/시드 실행 진입점을 제공한다.
- `boards`, `board_categories`, `board_posts`, `board_post_attachments`, `board_comments`, `board_post_reactions` 기반 목록 API 계약을 제공한다.
- 공지사항과 자유게시판을 구분하는 `board_code` 또는 동등한 식별자를 명시한다.
- 페이지네이션, 검색어, 카테고리 필터, 권한, 빈 결과, 오류 응답 형식을 문서화한다.

### Frontend

- 공지사항 목록과 자유게시판 목록 화면의 라우트, 데이터 로딩 방식, 빈/로딩/오류 상태를 제공한다.
- 캡처 근거와 맞춘 주요 UI 텍스트, 목록 컬럼, 페이지네이션, 검색/필터 조작 지점을 제공한다.
- 테스트 가능한 selector 또는 접근성 이름을 제공한다.

## 현재 근거

- 요구사항: `docs/REQUIREMENTS.md`
- 기능 명세: `docs/FUNCTIONAL_SPEC.md`
- MySQL 8 DDL: `docs/revised_schema_mysql8.sql`
- 캡처 도구: `tools/ssafy_representative_capture.mjs`, `tools/ssafy_targeted_capture.mjs`
- 캡처 매니페스트: `ssafy_pages/capture-manifest.json`
- 타깃 캡처 요약: `ssafy_pages/targeted-capture-summary.json`
- 파일럿 캡처 키: `notice_list`, `notice_detail`, `community_free_list`, `community_free_detail`
- 확인된 원본 URL 패턴: `/edu/board/notice/list.do`, `/edu/board/notice/detail.do`, `/edu/board/free/list.do`, `/edu/board/free/detail.do`

## 파일럿 기능 검증 범위

### 공지사항 목록

- 인증 사용자가 공지사항 목록 화면 또는 API에 접근할 수 있다.
- 목록은 제목, 카테고리 또는 게시판 구분, 작성일, 조회수, 첨부 여부 등 명세에서 요구하는 메타데이터를 표시한다.
- 검색어와 카테고리 필터가 제공되는 경우 쿼리 조건이 API와 UI에 일관되게 반영된다.
- 페이지네이션은 총 건수, 현재 페이지, 다음/이전 이동, 빈 결과를 안정적으로 처리한다.
- 공지사항 상세 진입 링크는 `notice_detail` 캡처 근거와 같은 상세 흐름으로 연결된다.
- 공지사항 상세에는 자유게시판 반응 기능이 노출되지 않아야 한다는 명세 제약을 회귀 포인트로 둔다.

### 자유게시판 목록

- 인증 사용자가 자유게시판 목록 화면 또는 API에 접근할 수 있다.
- 목록은 제목, 작성자, 작성일, 조회수, 추천수, 댓글수, 첨부 여부 등 게시글 메타데이터를 표시한다.
- 카테고리 필터, 검색어, 내 글 또는 공개 여부 필터가 구현되는 경우 조건 조합을 검증한다.
- 상세 진입 링크는 `community_free_detail` 캡처 근거와 같은 `fnDetail` 계열 흐름 또는 새 라우트 계약과 일치한다.
- 권한 없는 작성/수정/삭제 버튼은 목록 검증 범위에서는 노출 여부만 확인하고, 동작 검증은 별도 CRUD 범위로 분리한다.

## MySQL 8 스키마 실행 검증

최소 검증은 깨끗한 MySQL 8 데이터베이스에서 `docs/revised_schema_mysql8.sql`이 오류 없이 실행되는지 확인하는 것이다.

현재 저장소에 DB 하네스가 없으므로 다음은 향후 자리표시자 명령이다.

```bash
# placeholder: MySQL 8 컨테이너 시작
npm run qa:mysql:up

# placeholder: DDL 적용
npm run qa:mysql:schema

# placeholder: FK/CHECK/인덱스 기본 검증
npm run qa:mysql:verify
```

검증 항목:

- `utf8mb4` 문자셋과 `utf8mb4_unicode_ci` collation 적용
- `code_groups`, `codes` 기본 코드 삽입 성공
- `BOARD_GROUP` 코드에 `notice`, `community`, `qna`, `faq` 존재
- `ACCESS_SCOPE` 코드에 `public`, `authenticated` 존재
- 게시판 관련 테이블 생성 성공: `boards`, `board_categories`, `board_posts`, `board_post_attachments`, `board_comments`, `board_post_reactions`
- `board_posts.view_count >= 0`, `board_categories.sort_order > 0` 제약 검증
- 외래키가 켜진 상태에서 잘못된 게시판/카테고리/작성자 참조 삽입 실패 확인

## 시드 및 테스트 데이터 전략

- 시드는 PM이 승인한 파일럿 데이터만 포함한다.
- 실제 SSAFY 사용자, 실제 게시글 전문, 인증 토큰, CSRF 값은 저장하지 않는다.
- 게시판 마스터 데이터는 최소 `notice`, `free` 또는 Backend가 확정한 동등한 `board_code`를 포함한다.
- 공지사항 데이터는 첨부 없음/첨부 있음, 최신/과거, 검색 대상/비대상, 빈 결과 조건을 포함한다.
- 자유게시판 데이터는 댓글/추천/첨부가 각각 0건과 1건 이상인 조합을 포함한다.
- 작성자 데이터는 학생, 운영자 또는 관리자 역할을 구분할 수 있는 최소 사용자로 구성한다.
- 시간값은 정렬 검증을 위해 고정 timestamp를 사용한다.
- 시드 재실행은 멱등이어야 하며, 실패 시 테스트 DB를 재생성할 수 있어야 한다.

향후 자리표시자 명령:

```bash
npm run qa:seed:persistent
npm run qa:seed:reset
npm run qa:seed:verify
```

## Smoke/API/UI 검증 책임

### Smoke

- 애플리케이션 부팅, DB 연결, 정적 자산 제공, 기본 라우트 응답을 확인한다.
- 인증이 필요한 화면은 미인증 접근 시 로그인 또는 401/403 계약을 확인한다.
- 캡처 산출물과 비교해야 하는 화면은 `ssafy_pages/capture-manifest.json`의 키와 링크해 증거를 남긴다.

자리표시자:

```bash
npm run smoke
npm run smoke:pilot
```

### API

- 공지사항 목록 API와 자유게시판 목록 API의 응답 스키마, 정렬, 페이지네이션, 필터를 검증한다.
- 잘못된 페이지 번호, 빈 검색어, 없는 카테고리, 권한 없음 응답을 검증한다.
- DB 시드 기준으로 응답 건수와 핵심 필드 값을 대조한다.

자리표시자:

```bash
npm run test:api -- --scope board-list
```

### UI

- 공지사항/자유게시판 목록 라우트 진입, 로딩 상태, 빈 상태, 오류 상태, 목록 렌더링을 검증한다.
- 검색/필터/페이지 이동 후 URL 또는 상태와 목록 결과가 일치하는지 확인한다.
- 상세 링크 클릭은 해당 상세 라우트 또는 API 호출이 발생하는지만 확인한다.
- 모바일/데스크톱 최소 뷰포트에서 목록이 겹치거나 잘리지 않는지 확인한다.

자리표시자:

```bash
npm run test:e2e -- --scope board-list
npm run qa:visual -- --scope board-list
```

## CI/로컬 하네스 체크리스트

로컬:

- Node/runtime 버전 확인
- 의존성 설치
- MySQL 8 시작
- DDL 적용
- 시드 초기화
- Backend 실행
- Frontend 실행
- Smoke/API/UI 파일럿 검증 실행
- 증거 산출물 저장

CI:

- 캐시된 의존성 복원
- MySQL 8 service 또는 컨테이너 준비
- DDL 적용 로그 저장
- 시드 적용 로그 저장
- 단위 테스트, API 테스트, E2E 또는 UI smoke 실행
- 실패 시 DB 로그, 서버 로그, 테스트 리포트, 스크린샷/트레이스 업로드
- PR에는 파일럿 기능의 통과/실패 요약을 첨부

현재 프레임워크가 없으므로 CI 명령은 다음 이름을 기본 계약으로 예약한다.

```bash
npm run lint
npm run typecheck
npm test
npm run test:api
npm run test:e2e
npm run qa:pilot
```

구현 lane이 다른 패키지 매니저나 프레임워크를 선택하면 DevOps-QA는 같은 의미의 명령으로 매핑표를 작성한다.

## 증거 산출물

최소 증거:

- 실행 명령과 종료 코드
- DDL 적용 로그
- 시드 적용 로그
- API 테스트 리포트
- UI 테스트 리포트
- 실패 케이스별 요청/응답 또는 스크린샷
- 캡처 근거 링크: `notice_list`, `notice_detail`, `community_free_list`, `community_free_detail`
- 릴리스 차단 여부와 담당 lane

향후 저장 위치:

- `artifacts/qa/schema/`
- `artifacts/qa/api/`
- `artifacts/qa/ui/`
- `artifacts/qa/screenshots/`
- `artifacts/qa/traces/`

## Handoff Checklist

PM에게 전달:

- 파일럿 인수 기준별 pass/fail
- 범위 밖으로 분리한 항목
- 릴리스 차단 이슈와 비차단 이슈

Backend에 전달:

- API 계약 불일치
- DDL/시드/외래키/제약 조건 실패
- 데이터 정렬, 페이지네이션, 필터 결과 불일치

Frontend에 전달:

- 화면 상태 누락
- 접근성 selector 또는 테스트 가능한 locator 누락
- 캡처 근거와 다른 목록 필드/상세 진입 흐름
- 반응형 레이아웃 문제

DevOps-QA 완료 조건:

- MySQL 8 스키마가 깨끗한 DB에서 실행됨
- 파일럿 시드가 멱등 실행됨
- 공지사항 목록과 자유게시판 목록의 API/UI smoke가 통과함
- 실패가 있으면 재현 절차, 기대/실제 결과, 담당 lane이 명확함
- 증거 산출물이 PM이 판단 가능한 형태로 보관됨

## 리스크

- 현재 저장소는 문서/캡처 중심이며 실제 Backend/Frontend 프레임워크와 테스트 러너가 확정되지 않았다.
- 기존 문서 일부는 인코딩 표시가 깨져 보일 수 있으므로 기능 ID, URL, 스키마, 캡처 키를 우선 근거로 삼아야 한다.
- 캡처 도구는 실제 SSAFY 인증 흐름과 브라우저 디버깅 포트에 의존하므로 CI 기본 경로가 아니라 수동/보조 증거 경로로 취급한다.
