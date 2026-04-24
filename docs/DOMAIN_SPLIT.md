# revised schema 기준 최종 도메인 분리표

## 문서 목적
이 문서는 `docs/revised_schema_mysql8.sql`을 기준으로 현재 데이터 구조를 **개발/설계 관점에서 어떤 도메인으로 나누는 것이 적절한지** 정리한 최종 분리표입니다.

## 설계 판단 기준
- 전역 재사용 자원과 업무 엔티티를 분리한다.
- 사용자/소속/범위(scope)를 콘텐츠/평가/커뮤니케이션보다 먼저 독립시킨다.
- 동일한 생명주기와 책임을 갖는 테이블끼리 묶는다.
- revised schema에서 추가된 `code_groups/codes`, `content_scopes`, `support_ticket_messages`, `user_rank_snapshots`를 반영한다.

## 권장 최종 도메인

| 도메인 | 목적 | 핵심 테이블 | 분리 이유 |
|---|---|---|---|
| `core` | 전역 기준정보/공통 자원 | `code_groups`, `codes`, `attachments` | 모든 도메인이 재사용하는 공통 자원 |
| `identity` | 계정/프로필/사용자 상태 | `users`, `user_profiles`, `user_profile_attachments` | 로그인 주체와 개인정보 책임 분리 |
| `organization` | 교육 조직/소속 | `campuses`, `cohorts`, `tracks`, `class_groups`, `user_track_enrollments`, `user_class_enrollments` | 교육 편성 구조와 사용자 소속 무결성 유지 |
| `scope` | 콘텐츠 적용 범위 | `content_scopes` | 학습자료/퀘스트/설문의 공통 타게팅 허브 |
| `learning` | 학기/커리큘럼/강의/학습자료 | `terms`, `curriculum_schedules`, `lecture_replays`, `learning_materials`, `learning_material_resources`, `learning_material_resource_attachments`, `learning_material_reactions` | 수업 운영 흐름을 하나의 축으로 관리 |
| `assessment` | 평가/설문/응답 | `quest_evaluations`, `quest_submissions`, `surveys`, `survey_questions`, `survey_options`, `survey_responses`, `survey_response_answers`, `survey_response_answer_options` | 제출/응답/채점/수집 흐름 통합 |
| `communication` | 공지/게시판/알림/문의 | `notifications`, `notification_recipients`, `boards`, `board_categories`, `board_posts`, `board_post_attachments`, `board_comments`, `board_post_reactions`, `support_tickets`, `support_ticket_messages`, `support_ticket_message_attachments` | 사용자 커뮤니케이션 채널 통합 |
| `operations` | 출결/소명/레벨/랭크 | `user_level_statuses`, `user_rank_snapshots`, `attendance_records`, `attendance_appeals` | 운영/행정성 데이터 분리 |

## 도메인별 상세 설명

### 1. core
- `code_groups`, `codes`: 코드성 값 중앙 관리
- `attachments`: 모든 도메인이 참조하는 파일 메타데이터
- 특징: revised schema의 다수 컬럼이 코드 FK를 참조하므로 가장 먼저 초기화되어야 함

### 2. identity
- `users`: 전역 사용자 주체, 역할/상태/soft delete 정보 포함
- `user_profiles`: 개인정보와 연락처
- `user_profile_attachments`: 프로필 관련 첨부
- 특징: `deleted_at`, `anonymized_at`가 있어 계정 생명주기 책임이 명확함

### 3. organization
- `campuses`, `cohorts`, `tracks`: 교육 편성 기준 축
- `class_groups`: 반 단위 편성(`campus + cohort + track + class_name`)
- `user_track_enrollments`, `user_class_enrollments`: 소속 무결성 유지
- 특징: `user_class_enrollments`가 `user_track_enrollments`를 참조하여 반 소속과 트랙 소속의 일관성을 강제함

### 4. scope
- `content_scopes`: `all / campus / cohort / track / track_cohort / class_group / user` 범위를 공통 추상화
- 특징: revised schema의 핵심 허브이며 콘텐츠/평가 배포 범위를 일관된 방식으로 표현함

### 5. learning
- `terms`, `curriculum_schedules`: 학기/수업 운영 정보
- `lecture_replays`: 다시보기 버전 관리(`version_no`, `published_at`)
- `learning_materials`: 학습자료 본체
- `learning_material_resources`: eBook/PDF/URL 등 실행 리소스
- 특징: 콘텐츠 본체와 실행 리소스를 분리해 확장성이 높음

### 6. assessment
- `quest_evaluations`, `quest_submissions`: 과제/평가/제출
- `surveys`, `survey_questions`, `survey_options`, `survey_responses`, `survey_response_answers`, `survey_response_answer_options`: 설문 및 다중선택 응답 구조
- 특징: `content_scope_id`를 통해 반/트랙/개인 단위 배포 가능

### 7. communication
- `notifications`, `notification_recipients`: 발송/수신 상태 분리
- `boards`, `board_posts`, `board_comments`, `board_post_reactions`: 게시판 계열
- `support_tickets`, `support_ticket_messages`, `support_ticket_message_attachments`: 1:1 문의 스레드
- 특징: revised schema는 **문의를 단일 답변형이 아니라 메시지 스레드형**으로 확장함

### 8. operations
- `attendance_records`, `attendance_appeals`: 출결과 소명 처리
- `user_level_statuses`, `user_rank_snapshots`: 현재 상태와 스냅샷 분리
- 특징: 랭크를 현재값과 이력값으로 나누어 분석/운영 요구를 함께 수용함

## 패키지/모듈 분리 추천

```text
core
identity
organization
scope
learning
assessment
communication
operations
```

## 구현 관점 추천
- API는 위 8개 도메인 기준으로 나누는 것이 가장 명확하다.
- `scope`는 별도 공통 서비스로 두고 `learning`, `assessment`, `communication`에서 재사용하는 방식이 좋다.
- `identity`와 `organization`은 분리하되, 인증 이후 조회 API에서는 함께 조합하는 경우가 많다.
- `communication`과 `operations`는 운영 화면에서 자주 결합되지만 저장 모델은 분리하는 편이 안전하다.

## 결론
현재 revised schema는 기존보다 훨씬 명확하게 **공통자원 / 사용자·소속 / 스코프 / 학습 / 평가 / 커뮤니케이션 / 운영**의 경계가 잡혀 있다. 실제 개발 구조는 위 8개 도메인 분리를 기본으로 두는 것이 가장 자연스럽다.
