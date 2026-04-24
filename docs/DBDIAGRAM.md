# dbdiagram.io용 스키마 (revised schema 기준)

## 목적
이 문서는 `docs/revised_schema_mysql8.sql`을 기준으로 변환한 **dbdiagram.io 전용 DBML 안내 문서**입니다.

## 사용 방법
1. [dbdiagram.io](https://dbdiagram.io/)에 접속합니다.
2. 새 다이어그램을 생성합니다.
3. `docs/revised_schema.dbml` 파일 전체를 붙여넣습니다.
4. 도메인별 배치는 `DOMAIN_SPLIT.md`와 `ERD_BY_DOMAIN.md`를 참고합니다.

## 기준 파일
- 원본 DDL: `docs/revised_schema_mysql8.sql`
- DBML 원본: `docs/revised_schema.dbml`
- 도메인 설명: `docs/DOMAIN_SPLIT.md`
- 역할 정책: `docs/ROLE_MATRIX.md`

## 주요 반영 포인트
- 코드 테이블(`code_groups`, `codes`) 반영
- `content_scopes` 기반 타게팅 반영
- `support_ticket_messages` 기반 문의 스레드 반영
- `survey_response_answer_options` 기반 다중선택 설문 반영
- `user_rank_snapshots` 기반 랭킹 이력 반영

## 비고
기존 `docs/schema.dbml`과 `docs/DBDIAGRAM.md`는 이전 단계 `schema.sql` 기준 산출물이었습니다.
현재 구조 검토와 시각화 기준은 `revised_schema_mysql8.sql` 및 `revised_schema.dbml`입니다.
