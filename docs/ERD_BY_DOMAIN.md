# 영역별 Mermaid ERD (revised schema 기준)

## 문서 목적
이 문서는 `docs/revised_schema_mysql8.sql`을 기준으로 **영역별로 분리한 Mermaid `erDiagram`** 모음입니다.
전체 스키마를 한 장에 넣지 않고, 도메인별로 읽기 쉽게 나눈 발표/문서화용 버전입니다.

## 작성 원칙
- 기준 스키마: `docs/revised_schema_mysql8.sql`
- 공통 엔티티(`users`, `tracks`, `attachments`, `content_scopes`)는 필요한 영역에 중복 표기합니다.
- 각 다이어그램은 해당 영역 이해에 필요한 관계만 포함합니다.

## 1. Core / Identity / Organization
```mermaid
erDiagram
    CODE_GROUPS ||--o{ CODES : contains
    USERS ||--o| USER_PROFILES : profile
    USER_PROFILES ||--o{ USER_PROFILE_ATTACHMENTS : profile_image_link
    ATTACHMENTS ||--o| USER_PROFILE_ATTACHMENTS : attachment
    CAMPUSES ||--o{ CLASS_GROUPS : campus_classes
    COHORTS ||--o{ CLASS_GROUPS : cohort_classes
    TRACKS ||--o{ CLASS_GROUPS : track_classes
    USERS ||--o{ USER_TRACK_ENROLLMENTS : track_enrollment
    USERS ||--o{ USER_CLASS_ENROLLMENTS : class_enrollment
    CLASS_GROUPS ||--o{ USER_CLASS_ENROLLMENTS : members
    USER_TRACK_ENROLLMENTS ||--o{ USER_CLASS_ENROLLMENTS : validates_scope
```

## 2. Scope / Learning
```mermaid
erDiagram
    CONTENT_SCOPES ||--o{ CURRICULUM_SCHEDULES : schedule_scope
    TERMS ||--o{ CURRICULUM_SCHEDULES : term_schedules
    CURRICULUM_SCHEDULES ||--o{ LECTURE_REPLAYS : replay_versions
    CONTENT_SCOPES ||--o{ LEARNING_MATERIALS : material_scope
    CURRICULUM_SCHEDULES ||--o{ LEARNING_MATERIALS : schedule_material
    LEARNING_MATERIALS ||--o{ LEARNING_MATERIAL_RESOURCES : resources
    LEARNING_MATERIAL_RESOURCES ||--o{ LEARNING_MATERIAL_RESOURCE_ATTACHMENTS : resource_files
    ATTACHMENTS ||--o{ LEARNING_MATERIAL_RESOURCE_ATTACHMENTS : attachment
    LEARNING_MATERIALS ||--o{ LEARNING_MATERIAL_REACTIONS : reactions
    USERS ||--o{ LEARNING_MATERIAL_REACTIONS : reacts
```

## 3. Assessment
```mermaid
erDiagram
    CONTENT_SCOPES ||--o{ QUEST_EVALUATIONS : quest_scope
    QUEST_EVALUATIONS ||--o{ QUEST_SUBMISSIONS : submissions
    USERS ||--o{ QUEST_SUBMISSIONS : submits

    CONTENT_SCOPES ||--o{ SURVEYS : survey_scope
    SURVEYS ||--o{ SURVEY_QUESTIONS : questions
    SURVEY_QUESTIONS ||--o{ SURVEY_OPTIONS : options
    SURVEYS ||--o{ SURVEY_RESPONSES : responses
    USERS ||--o{ SURVEY_RESPONSES : answers
    SURVEY_RESPONSES ||--o{ SURVEY_RESPONSE_ANSWERS : answer_items
    SURVEY_RESPONSE_ANSWERS ||--o{ SURVEY_RESPONSE_ANSWER_OPTIONS : selected_options
    SURVEY_OPTIONS ||--o{ SURVEY_RESPONSE_ANSWER_OPTIONS : option_link
```

## 4. Communication
```mermaid
erDiagram
    USERS ||--o{ NOTIFICATIONS : sends
    NOTIFICATIONS ||--o{ NOTIFICATION_RECIPIENTS : delivered_to
    USERS ||--o{ NOTIFICATION_RECIPIENTS : receives

    BOARDS ||--o{ BOARD_CATEGORIES : categories
    BOARDS ||--o{ BOARD_POSTS : posts
    BOARD_CATEGORIES ||--o{ BOARD_POSTS : category_posts
    USERS ||--o{ BOARD_POSTS : writes
    BOARD_POSTS ||--o{ BOARD_POST_ATTACHMENTS : post_file
    ATTACHMENTS ||--o{ BOARD_POST_ATTACHMENTS : attachment
    BOARD_POSTS ||--o{ BOARD_COMMENTS : comments
    USERS ||--o{ BOARD_COMMENTS : writes_comment
    BOARD_COMMENTS ||--o{ BOARD_COMMENTS : replies
    BOARD_POSTS ||--o{ BOARD_POST_REACTIONS : reactions
    USERS ||--o{ BOARD_POST_REACTIONS : reacts

    USERS ||--o{ SUPPORT_TICKETS : opens
    SUPPORT_TICKETS ||--o{ SUPPORT_TICKET_MESSAGES : messages
    USERS ||--o{ SUPPORT_TICKET_MESSAGES : sends_message
    SUPPORT_TICKET_MESSAGES ||--o{ SUPPORT_TICKET_MESSAGE_ATTACHMENTS : message_files
    ATTACHMENTS ||--o{ SUPPORT_TICKET_MESSAGE_ATTACHMENTS : attachment
```

## 5. Operations
```mermaid
erDiagram
    USERS ||--o| USER_LEVEL_STATUSES : current_level
    USERS ||--o{ USER_RANK_SNAPSHOTS : rank_history
    USERS ||--o{ ATTENDANCE_RECORDS : attendance
    ATTENDANCE_RECORDS ||--o{ ATTENDANCE_APPEALS : appeal
    USERS ||--o{ ATTENDANCE_APPEALS : resolved_by
```

## 활용 가이드
- 발표 자료에는 각 섹션을 별도 슬라이드로 분리하는 것이 좋습니다.
- 전체 구조 설명은 `ERD.md`, 도메인 경계 설명은 `DOMAIN_SPLIT.md`, 역할 설명은 `ROLE_MATRIX.md`와 함께 사용하면 좋습니다.
