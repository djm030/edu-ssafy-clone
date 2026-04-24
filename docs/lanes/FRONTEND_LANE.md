# Frontend Lane Contract

## 목적

이 문서는 PM+3-agent 협업 하네스에서 Frontend Agent가 소유하는 책임, 경계, 의존성, 파일 범위, 파일럿 기능 범위, 검증 기준을 정의한다. 레거시 SSAFY `.do` URL과 캡처 산출물은 화면/상태/정보 구조를 확인하기 위한 증거로만 사용하며, 구현은 PM 문서와 Backend API 계약에 정의된 도메인 라우트와 응답 모델에 의존한다.

## 근거 문서

- `docs/REQUIREMENTS.md`
- `docs/FUNCTIONAL_SPEC.md`
- `ssafy_pages/capture-manifest.json`
- `ssafy_pages/targeted-capture-summary.json`
- 대표 캡처: `main_home`, `dashboard_attendance`, `lecture_curriculum`, `lecture_openlearning`, `quest_list`, `survey_list`, `community_free_list`, `community_free_detail`, `notice_list`, `notice_detail`

## Frontend Agent 책임

- PM이 확정한 화면 범위와 우선순위를 라우트, 화면 상태, 컴포넌트 구조로 변환한다.
- Backend가 제공하는 API 계약에 맞춰 목록 조회, 필터, 검색, 페이지네이션, 오류/빈 상태를 구현한다.
- DevOps-QA가 실행 가능한 smoke/visual 체크 지점을 화면별로 제공한다.
- 공통 내비게이션, 인증 필요 화면 보호, 목록 UI 패턴, 날짜/상태/카운트 표현을 일관되게 유지한다.
- 캡처된 레거시 UI의 정보 구조는 참고하되 레거시 URL, hidden form field, `_csrf`, `fnDetail(...)` 같은 구현 방식은 새 프론트엔드 계약으로 끌어오지 않는다.

## Owned Paths

실제 프론트엔드 스캐폴드가 생성된 뒤 Frontend Agent가 소유한다.

- `frontend/**`
- `apps/web/**`
- `src/pages/**`
- `src/routes/**`
- `src/components/**`
- `src/features/**`
- `src/lib/api/**` 중 브라우저 클라이언트 어댑터
- `src/styles/**`
- `tests/e2e/**` 중 화면 smoke/visual 테스트
- `docs/lanes/FRONTEND_LANE.md`

현재 문서 작성 작업의 쓰기 범위는 `docs/lanes/FRONTEND_LANE.md`뿐이다.

## Forbidden Paths

Frontend Agent는 별도 PM 승인 없이 아래 경로를 수정하지 않는다.

- Backend 서버, DB, 마이그레이션, 시드, API 구현 경로
- DevOps, CI/CD, Docker, 배포 설정 경로
- PM 산출물 원본: `docs/REQUIREMENTS.md`, `docs/FUNCTIONAL_SPEC.md`
- 캡처 증거 원본: `ssafy_pages/**`
- 다른 lane 계약 문서
- 인증/권한 정책의 서버 측 판정 로직

## 협업 의존성

### PM 의존성

- 도메인 라우트 이름, IA, 메뉴 라벨, MVP 우선순위를 확정한다.
- 파일럿 범위에서 공지사항과 자유게시판 목록의 필수 컬럼, 카테고리명, 빈 상태 문구, 권한별 버튼 노출 정책을 확정한다.
- 레거시 캡처와 다르게 구현할 UX 차이가 있으면 PM 문서에 명시한다.

### Backend 의존성

- 목록 API, 검색/필터/페이지네이션 파라미터, 정렬 기본값, 응답 스키마를 제공한다.
- 인증 만료, 권한 없음, 빈 결과, 서버 오류의 표준 에러 형태를 제공한다.
- 게시판 카테고리 코드와 표시명 매핑을 도메인 API로 제공한다.
- 프론트엔드는 레거시 `brdItmSeq`, `searchBrdItmCdVal` 명칭을 직접 의존하지 않고, API 문서의 `id`, `categoryId` 같은 도메인 필드를 따른다.

### DevOps-QA 의존성

- 로컬 실행 명령, 테스트 명령, 기준 브라우저/뷰포트, 스크린샷 저장 위치를 제공한다.
- smoke 테스트가 호출할 mock/staging API 베이스 URL과 인증 세션 준비 방식을 제공한다.
- PR 게이트에 포함될 lint/typecheck/test/visual 기준을 확정한다.

## 파일럿 기능 범위

파일럿은 공지사항 목록과 자유게시판 목록을 먼저 구현한다.

### 공지사항 목록

- 도메인 라우트: `/help/notice`
- 레거시 증거: `notice_list` 캡처는 탭형 카테고리, 번호/제목/등록일, 공지 고정 행, 검색, 페이지네이션을 보여준다.
- 화면 책임: 카테고리 필터, 검색어 입력, 목록 렌더링, 고정 공지 표시, 페이지 이동, 상세 라우트 진입.
- 상세 라우트는 링크만 연결 가능해야 하며, 상세 화면 구현은 파일럿 외 범위일 수 있다.

### 자유게시판 목록

- 도메인 라우트: `/community/free`
- 레거시 증거: `community_free_list` 캡처는 카테고리 필터, 글쓰기 버튼, 제목/작성자/등록일, 조회/추천/댓글/찜 카운트, 첨부 여부, 새 글 표시, 검색, 페이지네이션을 보여준다.
- 화면 책임: 카테고리 필터, 검색어 입력, 목록 카드/행 렌더링, 메타데이터 표시, 글쓰기 버튼의 권한 기반 노출, 페이지 이동, 상세 라우트 진입.
- 글쓰기/상세/댓글/추천/찜 동작은 파일럿에서는 진입 지점과 비활성/준비 상태까지로 제한할 수 있다.

## Route and Screen Responsibilities

| 영역 | 라우트 | Frontend 책임 | 비고 |
|---|---|---|---|
| 메인 | `/` 또는 `/dashboard` | 개인 요약, 주요 모듈 링크, 공통 내비게이션 패턴 반영 | `main_home`은 정보 배치 참고용 |
| 마이캠퍼스 | `/my-campus/attendance`, `/my-campus/points`, `/my-campus/notifications` | 출석/포인트/알림 화면의 상태 표현 패턴 정의 | 파일럿 외 |
| 강의실 | `/lectures/curriculum`, `/lectures/replay`, `/lectures/materials` | 커리큘럼, 다시보기, 학습자료 목록 패턴 정의 | 파일럿 외 |
| Quest/평가 | `/quests` | 상태 배지, 기간, 점수/PASS 요약 패턴 정의 | 파일럿 외 |
| 설문 | `/surveys` | 진행/예정/종료, 필수 여부, 완료 여부 표시 패턴 정의 | 파일럿 외 |
| 커뮤니티 | `/community/free` | 자유게시판 목록 파일럿 구현 대상 | 상세는 후속 |
| HELP DESK | `/help/notice` | 공지사항 목록 파일럿 구현 대상 | 상세는 후속 |

## API Contract Assumptions

Frontend는 다음 형태의 도메인 계약을 가정한다. 실제 필드명은 Backend API 문서를 최종 기준으로 삼는다.

### 공통 목록 요청

- `page`: 1부터 시작하는 페이지 번호
- `size`: 페이지 크기
- `categoryId`: 선택 카테고리, 전체는 생략 또는 `all`
- `keyword`: 검색어
- `sort`: 기본값은 최신순

### 공통 목록 응답

- `items`: 화면에 표시할 행 배열
- `page`: `{ page, size, totalItems, totalPages }`

### Notice Item

- `id`
- `categoryLabel`
- `title`
- `publishedAt`
- `isPinned`
- `hasAttachment`

### Free Board Item

- `id`
- `categoryLabel`
- `title`
- `authorDisplayName`
- `publishedAt`
- `viewCount`
- `reactionCount`
- `commentCount`
- `bookmarkCount`
- `hasAttachment`
- `isNew`

### Error Handling

- `401`: 세션 만료 안내 후 로그인 라우트로 이동
- `403`: 권한 없음 상태 표시
- `404`: 상세 진입 시 대상 없음 상태 표시
- `5xx` 또는 네트워크 오류: 재시도 가능한 오류 상태 표시

## UI States

모든 파일럿 목록 화면은 아래 상태를 구현 대상으로 둔다.

- Initial loading: 첫 목록 로딩 중 스켈레톤 또는 로딩 표시
- Refresh loading: 필터/검색/페이지 변경 중 기존 레이아웃 유지
- Loaded: 카테고리, 검색, 목록, 페이지네이션 표시
- Empty: 검색/필터 결과 없음 문구와 필터 초기화 동선
- Error: 오류 메시지와 재시도 동선
- Unauthorized: 세션 만료 또는 로그인 필요 안내
- Forbidden: 접근 권한 없음 안내
- Disabled action: 글쓰기/상세 진입 권한이 없을 때 버튼 숨김 또는 비활성

## Acceptance Criteria

- 공지사항 목록은 카테고리별 건수, 고정 공지, 제목, 등록일, 검색, 페이지네이션을 표시한다.
- 자유게시판 목록은 카테고리별 건수, 제목, 작성자, 등록일, 조회/추천/댓글/찜, 첨부 여부, 새 글 상태, 검색, 페이지네이션을 표시한다.
- 목록 행 클릭은 PM/API 문서의 도메인 상세 라우트로 이동하며 레거시 `.do` URL을 사용하지 않는다.
- 검색과 카테고리 변경은 URL query 또는 라우터 상태로 재현 가능해야 한다.
- 빈 상태, 오류 상태, 인증/권한 상태가 목록 화면을 깨뜨리지 않는다.
- 모바일과 데스크톱에서 공통 내비게이션과 목록 콘텐츠가 겹치지 않는다.
- 레거시 캡처에서 확인된 정보 구조를 반영하되 레거시 form field, onclick 함수, CSRF hidden input에 직접 결합하지 않는다.

## Visual and Smoke Checks

DevOps-QA와 합의해 최소 아래 체크를 실행한다.

- Desktop viewport: 공지사항 목록 기본, 카테고리 필터 적용, 검색 결과, 빈 결과
- Desktop viewport: 자유게시판 목록 기본, 카테고리 필터 적용, 검색 결과, 빈 결과
- Mobile viewport: 상단 내비게이션, 목록 행 줄바꿈, 페이지네이션, 검색 입력이 겹치지 않는지 확인
- Smoke: `/help/notice` 진입 후 목록 API 호출 성공, 행 클릭 시 도메인 상세 라우트 생성
- Smoke: `/community/free` 진입 후 목록 API 호출 성공, 글쓰기 버튼 권한별 노출 확인
- Error smoke: API 401/403/500 mock 응답에서 대응 상태 표시 확인

## Handoff Checklist

- PM: 파일럿 라우트, 화면명, 메뉴 위치, 카테고리 라벨, 권한별 버튼 노출 정책 확정
- Backend: 공지사항/자유게시판 목록 API 문서, 예시 응답, 에러 응답, 페이지네이션 규칙 제공
- Frontend: route map, UI 상태표, 컴포넌트 책임, mock data, smoke 시나리오 제공
- DevOps-QA: 실행 명령, 테스트 명령, 브라우저/뷰포트, visual baseline 저장 정책 제공
- Cross-lane: 레거시 SSAFY URL은 증거 링크로만 남기고 제품 라우트와 API 계약은 도메인 명명으로 통일

## Risks

- `docs/REQUIREMENTS.md`와 `docs/FUNCTIONAL_SPEC.md`는 현재 콘솔에서 일부 Korean text가 깨져 보일 수 있어, 기능 ID와 캡처 JSON의 정상 UTF-8 텍스트를 함께 근거로 삼아야 한다.
- 상세 화면, 글쓰기, 댓글/추천/찜은 파일럿 목록 범위 밖이므로 PM이 MVP 포함 여부를 별도 결정해야 한다.
- 실제 프론트엔드 스캐폴드 경로가 아직 확정되지 않았다면 Owned Paths는 구현 시작 전에 PM/DevOps-QA와 재확정해야 한다.
