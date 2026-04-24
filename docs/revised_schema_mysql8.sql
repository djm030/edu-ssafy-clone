-- Revised schema DDL generated from uploaded DBML
-- Target DBMS: MySQL 8.x / InnoDB / utf8mb4
-- Key changes:
-- 1) class_groups now includes track_id; user_class_enrollments is tied to user_track_enrollments for consistency.
-- 2) learning_materials no longer stores duplicated track_id; scope is represented by content_scopes.
-- 3) survey answers support single-choice, multi-choice, and text answers with stronger question/survey consistency.
-- 4) quest_evaluations and surveys use content_scope_id instead of track_id only.
-- 5) users use soft-delete fields; historical tables avoid ON DELETE CASCADE data loss.
-- 6) code values are centralized in code_groups/codes and connected by constant code_group columns + FK.
-- 7) support tickets use message threads instead of one answer per ticket.
-- 8) attendance appeals store resolver and resolution details.
-- 9) lecture_replays uses version_no/published_at instead of is_latest.
-- 10) rank_no and has_prev_next_nav are removed; rank snapshots are separated.

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS support_ticket_message_attachments;
DROP TABLE IF EXISTS support_ticket_messages;
DROP TABLE IF EXISTS support_tickets;
DROP TABLE IF EXISTS board_post_reactions;
DROP TABLE IF EXISTS board_comments;
DROP TABLE IF EXISTS board_post_attachments;
DROP TABLE IF EXISTS board_posts;
DROP TABLE IF EXISTS board_categories;
DROP TABLE IF EXISTS boards;
DROP TABLE IF EXISTS survey_response_answer_options;
DROP TABLE IF EXISTS survey_response_answers;
DROP TABLE IF EXISTS survey_responses;
DROP TABLE IF EXISTS survey_options;
DROP TABLE IF EXISTS survey_questions;
DROP TABLE IF EXISTS surveys;
DROP TABLE IF EXISTS quest_submissions;
DROP TABLE IF EXISTS quest_evaluations;
DROP TABLE IF EXISTS learning_material_reactions;
DROP TABLE IF EXISTS learning_material_resource_attachments;
DROP TABLE IF EXISTS learning_material_resources;
DROP TABLE IF EXISTS learning_materials;
DROP TABLE IF EXISTS lecture_replays;
DROP TABLE IF EXISTS curriculum_schedules;
DROP TABLE IF EXISTS terms;
DROP TABLE IF EXISTS content_scopes;
DROP TABLE IF EXISTS notification_recipients;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS attendance_appeals;
DROP TABLE IF EXISTS attendance_records;
DROP TABLE IF EXISTS user_rank_snapshots;
DROP TABLE IF EXISTS user_level_statuses;
DROP TABLE IF EXISTS user_profile_attachments;
DROP TABLE IF EXISTS attachments;
DROP TABLE IF EXISTS user_class_enrollments;
DROP TABLE IF EXISTS user_track_enrollments;
DROP TABLE IF EXISTS class_groups;
DROP TABLE IF EXISTS tracks;
DROP TABLE IF EXISTS cohorts;
DROP TABLE IF EXISTS campuses;
DROP TABLE IF EXISTS user_profiles;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS codes;
DROP TABLE IF EXISTS code_groups;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE code_groups (
  code_group VARCHAR(50) NOT NULL,
  group_name VARCHAR(100) NOT NULL,
  description VARCHAR(500),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (code_group)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE codes (
  code_group VARCHAR(50) NOT NULL,
  code VARCHAR(50) NOT NULL,
  code_name VARCHAR(100) NOT NULL,
  description VARCHAR(500),
  sort_order INT NOT NULL DEFAULT 1,
  active_yn BOOLEAN NOT NULL DEFAULT TRUE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (code_group, code),
  CONSTRAINT fk_codes_code_group
    FOREIGN KEY (code_group) REFERENCES code_groups (code_group)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO code_groups (code_group, group_name, description) VALUES
  ('USER_ROLE', '사용자 역할', NULL),
  ('USER_STATUS', '사용자 상태', NULL),
  ('CLASS_MEMBER_ROLE', '반 구성원 역할', NULL),
  ('CONTENT_SCOPE_TYPE', '콘텐츠 적용 범위', NULL),
  ('ATTENDANCE_STATUS', '출석 상태', NULL),
  ('ATTENDANCE_APPROVAL_TYPE', '출석 승인 유형', NULL),
  ('ATTENDANCE_APPEAL_TYPE', '출석 이의신청 유형', NULL),
  ('APPROVAL_STATUS', '승인 상태', NULL),
  ('PROGRESS_STATUS', '진행 상태', NULL),
  ('CURRICULUM_TYPE', '커리큘럼 유형', NULL),
  ('MATERIAL_TYPE', '학습자료 유형', NULL),
  ('RESOURCE_TYPE', '학습자료 리소스 유형', NULL),
  ('LAUNCH_MODE', '리소스 실행 방식', NULL),
  ('REACTION_TYPE', '반응 유형', NULL),
  ('QUEST_TYPE', '퀘스트 유형', NULL),
  ('TASK_CLASSIFICATION', '과제 분류', NULL),
  ('QUEST_RESULT_STATUS', '퀘스트 결과 상태', NULL),
  ('QUEST_SUBMIT_STATUS', '퀘스트 제출 상태', NULL),
  ('SURVEY_CATEGORY', '설문 카테고리', NULL),
  ('SURVEY_QUESTION_TYPE', '설문 문항 유형', NULL),
  ('BOARD_GROUP', '게시판 그룹', NULL),
  ('ACCESS_SCOPE', '접근 범위', NULL),
  ('SUPPORT_TICKET_STATUS', '문의 상태', NULL),
  ('SUPPORT_MESSAGE_TYPE', '문의 메시지 유형', NULL),
  ('USER_PROFILE_ATTACHMENT_ROLE', '사용자 프로필 첨부 역할', NULL);

INSERT INTO codes (code_group, code, code_name, sort_order) VALUES
  ('USER_ROLE', 'student', '수강생', 1),
  ('USER_ROLE', 'instructor', '강사', 2),
  ('USER_ROLE', 'manager', '매니저', 3),
  ('USER_ROLE', 'admin', '관리자', 4),
  ('USER_STATUS', 'active', '활성', 1),
  ('USER_STATUS', 'inactive', '비활성', 2),
  ('USER_STATUS', 'withdrawn', '탈퇴', 3),
  ('USER_STATUS', 'deleted', '삭제 처리', 4),
  ('CLASS_MEMBER_ROLE', 'student', '수강생', 1),
  ('CLASS_MEMBER_ROLE', 'instructor', '강사', 2),
  ('CLASS_MEMBER_ROLE', 'mentor', '멘토', 3),
  ('CLASS_MEMBER_ROLE', 'manager', '매니저', 4),
  ('CONTENT_SCOPE_TYPE', 'all', '전체', 1),
  ('CONTENT_SCOPE_TYPE', 'campus', '캠퍼스', 2),
  ('CONTENT_SCOPE_TYPE', 'cohort', '기수', 3),
  ('CONTENT_SCOPE_TYPE', 'track', '트랙', 4),
  ('CONTENT_SCOPE_TYPE', 'track_cohort', '트랙+기수', 5),
  ('CONTENT_SCOPE_TYPE', 'class_group', '반', 6),
  ('CONTENT_SCOPE_TYPE', 'user', '사용자', 7),
  ('ATTENDANCE_STATUS', 'present', '출석', 1),
  ('ATTENDANCE_STATUS', 'late', '지각', 2),
  ('ATTENDANCE_STATUS', 'early_leave', '조퇴', 3),
  ('ATTENDANCE_STATUS', 'absent', '결석', 4),
  ('ATTENDANCE_STATUS', 'excused', '공결', 5),
  ('ATTENDANCE_APPROVAL_TYPE', 'auto', '자동', 1),
  ('ATTENDANCE_APPROVAL_TYPE', 'manual', '수동', 2),
  ('ATTENDANCE_APPROVAL_TYPE', 'appeal', '이의신청', 3),
  ('ATTENDANCE_APPEAL_TYPE', 'check_in', '입실 정정', 1),
  ('ATTENDANCE_APPEAL_TYPE', 'check_out', '퇴실 정정', 2),
  ('ATTENDANCE_APPEAL_TYPE', 'status_change', '상태 정정', 3),
  ('ATTENDANCE_APPEAL_TYPE', 'other', '기타', 99),
  ('APPROVAL_STATUS', 'requested', '요청', 1),
  ('APPROVAL_STATUS', 'approved', '승인', 2),
  ('APPROVAL_STATUS', 'rejected', '반려', 3),
  ('APPROVAL_STATUS', 'canceled', '취소', 4),
  ('PROGRESS_STATUS', 'draft', '초안', 1),
  ('PROGRESS_STATUS', 'scheduled', '예정', 2),
  ('PROGRESS_STATUS', 'in_progress', '진행 중', 3),
  ('PROGRESS_STATUS', 'completed', '완료', 4),
  ('PROGRESS_STATUS', 'closed', '종료', 5),
  ('PROGRESS_STATUS', 'archived', '보관', 6),
  ('CURRICULUM_TYPE', 'lecture', '강의', 1),
  ('CURRICULUM_TYPE', 'practice', '실습', 2),
  ('CURRICULUM_TYPE', 'project', '프로젝트', 3),
  ('CURRICULUM_TYPE', 'mentoring', '멘토링', 4),
  ('CURRICULUM_TYPE', 'exam', '시험', 5),
  ('MATERIAL_TYPE', 'document', '문서', 1),
  ('MATERIAL_TYPE', 'video', '영상', 2),
  ('MATERIAL_TYPE', 'link', '링크', 3),
  ('MATERIAL_TYPE', 'assignment', '과제', 4),
  ('MATERIAL_TYPE', 'notice', '공지', 5),
  ('RESOURCE_TYPE', 'file', '파일', 1),
  ('RESOURCE_TYPE', 'url', 'URL', 2),
  ('RESOURCE_TYPE', 'video', '영상', 3),
  ('RESOURCE_TYPE', 'external', '외부 리소스', 4),
  ('LAUNCH_MODE', 'new_tab', '새 탭', 1),
  ('LAUNCH_MODE', 'embedded', '임베드', 2),
  ('LAUNCH_MODE', 'download', '다운로드', 3),
  ('REACTION_TYPE', 'like', '좋아요', 1),
  ('REACTION_TYPE', 'bookmark', '북마크', 2),
  ('REACTION_TYPE', 'helpful', '도움됨', 3),
  ('QUEST_TYPE', 'assignment', '과제', 1),
  ('QUEST_TYPE', 'project', '프로젝트', 2),
  ('QUEST_TYPE', 'test', '테스트', 3),
  ('QUEST_TYPE', 'challenge', '챌린지', 4),
  ('TASK_CLASSIFICATION', 'required', '필수', 1),
  ('TASK_CLASSIFICATION', 'optional', '선택', 2),
  ('TASK_CLASSIFICATION', 'basic', '기본', 3),
  ('TASK_CLASSIFICATION', 'advanced', '심화', 4),
  ('QUEST_RESULT_STATUS', 'pending', '대기', 1),
  ('QUEST_RESULT_STATUS', 'pass', '통과', 2),
  ('QUEST_RESULT_STATUS', 'fail', '실패', 3),
  ('QUEST_SUBMIT_STATUS', 'not_submitted', '미제출', 1),
  ('QUEST_SUBMIT_STATUS', 'submitted', '제출', 2),
  ('QUEST_SUBMIT_STATUS', 'late', '지각 제출', 3),
  ('QUEST_SUBMIT_STATUS', 'graded', '채점 완료', 4),
  ('QUEST_SUBMIT_STATUS', 'returned', '반려', 5),
  ('SURVEY_CATEGORY', 'satisfaction', '만족도', 1),
  ('SURVEY_CATEGORY', 'course', '과정', 2),
  ('SURVEY_CATEGORY', 'lecture', '강의', 3),
  ('SURVEY_CATEGORY', 'etc', '기타', 99),
  ('SURVEY_QUESTION_TYPE', 'single_choice', '단일 선택', 1),
  ('SURVEY_QUESTION_TYPE', 'multiple_choice', '다중 선택', 2),
  ('SURVEY_QUESTION_TYPE', 'short_text', '단답형', 3),
  ('SURVEY_QUESTION_TYPE', 'long_text', '장문형', 4),
  ('SURVEY_QUESTION_TYPE', 'score', '점수형', 5),
  ('BOARD_GROUP', 'notice', '공지', 1),
  ('BOARD_GROUP', 'community', '커뮤니티', 2),
  ('BOARD_GROUP', 'qna', '질문답변', 3),
  ('BOARD_GROUP', 'faq', 'FAQ', 4),
  ('ACCESS_SCOPE', 'public', '공개', 1),
  ('ACCESS_SCOPE', 'authenticated', '인증 사용자', 2),
  ('ACCESS_SCOPE', 'class_group', '반 제한', 3),
  ('ACCESS_SCOPE', 'admin', '관리자', 4),
  ('SUPPORT_TICKET_STATUS', 'open', '접수', 1),
  ('SUPPORT_TICKET_STATUS', 'waiting_user', '사용자 응답 대기', 2),
  ('SUPPORT_TICKET_STATUS', 'answered', '답변 완료', 3),
  ('SUPPORT_TICKET_STATUS', 'closed', '종료', 4),
  ('SUPPORT_MESSAGE_TYPE', 'user_message', '사용자 메시지', 1),
  ('SUPPORT_MESSAGE_TYPE', 'admin_reply', '관리자 답변', 2),
  ('SUPPORT_MESSAGE_TYPE', 'internal_note', '내부 메모', 3),
  ('USER_PROFILE_ATTACHMENT_ROLE', 'profile_image', '프로필 이미지', 1),
  ('USER_PROFILE_ATTACHMENT_ROLE', 'id_card', '신분증', 2),
  ('USER_PROFILE_ATTACHMENT_ROLE', 'certificate', '증명서', 3),
  ('USER_PROFILE_ATTACHMENT_ROLE', 'portfolio', '포트폴리오', 4);

CREATE TABLE users (
  user_id BIGINT NOT NULL AUTO_INCREMENT,
  email VARCHAR(255) NOT NULL,
  password_hash VARCHAR(255),
  learner_no VARCHAR(50),
  name VARCHAR(100) NOT NULL,
  role_code_group VARCHAR(50) NOT NULL DEFAULT 'USER_ROLE',
  role_code VARCHAR(50) NOT NULL DEFAULT 'student',
  status_code_group VARCHAR(50) NOT NULL DEFAULT 'USER_STATUS',
  status_code VARCHAR(50) NOT NULL DEFAULT 'active',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at DATETIME NULL,
  anonymized_at DATETIME NULL,
  PRIMARY KEY (user_id),
  UNIQUE KEY uk_users_email (email),
  UNIQUE KEY uk_users_learner_no (learner_no),
  CONSTRAINT chk_users_role_code_group CHECK (role_code_group = 'USER_ROLE'),
  CONSTRAINT chk_users_status_code_group CHECK (status_code_group = 'USER_STATUS'),
  CONSTRAINT fk_users_role_code
    FOREIGN KEY (role_code_group, role_code) REFERENCES codes (code_group, code),
  CONSTRAINT fk_users_status_code
    FOREIGN KEY (status_code_group, status_code) REFERENCES codes (code_group, code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_profiles (
  user_id BIGINT NOT NULL,
  zip_code VARCHAR(20),
  address_line1 VARCHAR(255),
  address_line2 VARCHAR(255),
  mobile_phone VARCHAR(30),
  emergency_phone VARCHAR(30),
  marketing_opt_in BOOLEAN NOT NULL DEFAULT FALSE,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id),
  CONSTRAINT fk_user_profiles_user
    FOREIGN KEY (user_id) REFERENCES users (user_id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE campuses (
  campus_id BIGINT NOT NULL AUTO_INCREMENT,
  campus_name VARCHAR(100) NOT NULL,
  PRIMARY KEY (campus_id),
  UNIQUE KEY uk_campuses_name (campus_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE cohorts (
  cohort_id BIGINT NOT NULL AUTO_INCREMENT,
  cohort_name VARCHAR(100) NOT NULL,
  start_date DATE,
  end_date DATE,
  PRIMARY KEY (cohort_id),
  UNIQUE KEY uk_cohorts_name (cohort_name),
  CONSTRAINT chk_cohorts_date_range CHECK (end_date IS NULL OR start_date IS NULL OR end_date >= start_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE tracks (
  track_id BIGINT NOT NULL AUTO_INCREMENT,
  track_name VARCHAR(100) NOT NULL,
  domain_type VARCHAR(100),
  PRIMARY KEY (track_id),
  UNIQUE KEY uk_tracks_name (track_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE class_groups (
  class_group_id BIGINT NOT NULL AUTO_INCREMENT,
  campus_id BIGINT NOT NULL,
  cohort_id BIGINT NOT NULL,
  track_id BIGINT NOT NULL,
  class_name VARCHAR(100) NOT NULL,
  PRIMARY KEY (class_group_id),
  UNIQUE KEY uk_class_groups_scope (campus_id, cohort_id, track_id, class_name),
  UNIQUE KEY uk_class_groups_id_cohort_track (class_group_id, cohort_id, track_id),
  KEY idx_class_groups_cohort_track (cohort_id, track_id),
  CONSTRAINT fk_class_groups_campus
    FOREIGN KEY (campus_id) REFERENCES campuses (campus_id),
  CONSTRAINT fk_class_groups_cohort
    FOREIGN KEY (cohort_id) REFERENCES cohorts (cohort_id),
  CONSTRAINT fk_class_groups_track
    FOREIGN KEY (track_id) REFERENCES tracks (track_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_track_enrollments (
  user_track_enrollment_id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  track_id BIGINT NOT NULL,
  cohort_id BIGINT NOT NULL,
  enrolled_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_track_enrollment_id),
  UNIQUE KEY uk_user_track_enrollments_user_track_cohort (user_id, track_id, cohort_id),
  KEY idx_user_track_enrollments_track_cohort (track_id, cohort_id),
  CONSTRAINT fk_user_track_enrollments_user
    FOREIGN KEY (user_id) REFERENCES users (user_id)
    ON DELETE CASCADE,
  CONSTRAINT fk_user_track_enrollments_track
    FOREIGN KEY (track_id) REFERENCES tracks (track_id),
  CONSTRAINT fk_user_track_enrollments_cohort
    FOREIGN KEY (cohort_id) REFERENCES cohorts (cohort_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_class_enrollments (
  user_class_enrollment_id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  class_group_id BIGINT NOT NULL,
  cohort_id BIGINT NOT NULL,
  track_id BIGINT NOT NULL,
  member_role_code_group VARCHAR(50) NOT NULL DEFAULT 'CLASS_MEMBER_ROLE',
  member_role_code VARCHAR(50) NOT NULL DEFAULT 'student',
  enrolled_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_class_enrollment_id),
  UNIQUE KEY uk_user_class_enrollments_user_class (user_id, class_group_id),
  KEY idx_user_class_enrollments_user_track_cohort (user_id, track_id, cohort_id),
  CONSTRAINT chk_user_class_enrollments_member_role_group CHECK (member_role_code_group = 'CLASS_MEMBER_ROLE'),
  CONSTRAINT fk_user_class_enrollments_member_role
    FOREIGN KEY (member_role_code_group, member_role_code) REFERENCES codes (code_group, code),
  CONSTRAINT fk_user_class_enrollments_user
    FOREIGN KEY (user_id) REFERENCES users (user_id)
    ON DELETE CASCADE,
  CONSTRAINT fk_user_class_enrollments_class_scope
    FOREIGN KEY (class_group_id, cohort_id, track_id)
    REFERENCES class_groups (class_group_id, cohort_id, track_id)
    ON DELETE CASCADE,
  CONSTRAINT fk_user_class_enrollments_user_track
    FOREIGN KEY (user_id, track_id, cohort_id)
    REFERENCES user_track_enrollments (user_id, track_id, cohort_id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE attachments (
  attachment_id BIGINT NOT NULL AUTO_INCREMENT,
  original_filename VARCHAR(255) NOT NULL,
  storage_key VARCHAR(100),
  stored_path VARCHAR(500),
  mime_type VARCHAR(100),
  file_size BIGINT,
  checksum_sha256 CHAR(64),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (attachment_id),
  UNIQUE KEY uk_attachments_checksum_sha256 (checksum_sha256),
  CONSTRAINT chk_attachments_file_size CHECK (file_size IS NULL OR file_size >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_profile_attachments (
  user_id BIGINT NOT NULL,
  attachment_id BIGINT NOT NULL,
  attachment_role_code_group VARCHAR(50) NOT NULL DEFAULT 'USER_PROFILE_ATTACHMENT_ROLE',
  attachment_role_code VARCHAR(50) NOT NULL DEFAULT 'profile_image',
  PRIMARY KEY (user_id, attachment_id),
  UNIQUE KEY uk_user_profile_attachments_attachment (attachment_id),
  UNIQUE KEY uk_user_profile_attachments_user_role (user_id, attachment_role_code),
  CONSTRAINT chk_user_profile_attachments_role_group CHECK (attachment_role_code_group = 'USER_PROFILE_ATTACHMENT_ROLE'),
  CONSTRAINT fk_user_profile_attachments_role
    FOREIGN KEY (attachment_role_code_group, attachment_role_code) REFERENCES codes (code_group, code),
  CONSTRAINT fk_user_profile_attachments_user
    FOREIGN KEY (user_id) REFERENCES user_profiles (user_id)
    ON DELETE CASCADE,
  CONSTRAINT fk_user_profile_attachments_attachment
    FOREIGN KEY (attachment_id) REFERENCES attachments (attachment_id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_level_statuses (
  user_id BIGINT NOT NULL,
  level_name VARCHAR(50),
  level_no INT,
  exp INT NOT NULL DEFAULT 0,
  scholarship_point INT NOT NULL DEFAULT 0,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id),
  CONSTRAINT chk_user_level_statuses_exp CHECK (exp >= 0),
  CONSTRAINT chk_user_level_statuses_scholarship_point CHECK (scholarship_point >= 0),
  CONSTRAINT fk_user_level_statuses_user
    FOREIGN KEY (user_id) REFERENCES users (user_id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_rank_snapshots (
  user_rank_snapshot_id BIGINT NOT NULL AUTO_INCREMENT,
  snapshot_date DATE NOT NULL,
  user_id BIGINT NOT NULL,
  rank_no INT NOT NULL,
  exp INT NOT NULL DEFAULT 0,
  scholarship_point INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_rank_snapshot_id),
  UNIQUE KEY uk_user_rank_snapshots_date_user (snapshot_date, user_id),
  UNIQUE KEY uk_user_rank_snapshots_date_rank (snapshot_date, rank_no),
  CONSTRAINT chk_user_rank_snapshots_rank CHECK (rank_no > 0),
  CONSTRAINT chk_user_rank_snapshots_exp CHECK (exp >= 0),
  CONSTRAINT chk_user_rank_snapshots_scholarship_point CHECK (scholarship_point >= 0),
  CONSTRAINT fk_user_rank_snapshots_user
    FOREIGN KEY (user_id) REFERENCES users (user_id)
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE attendance_records (
  attendance_record_id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  attendance_date DATE NOT NULL,
  check_in_at TIME,
  check_out_at TIME,
  attendance_status_code_group VARCHAR(50) NOT NULL DEFAULT 'ATTENDANCE_STATUS',
  attendance_status_code VARCHAR(50) NOT NULL,
  approval_type_code_group VARCHAR(50) NOT NULL DEFAULT 'ATTENDANCE_APPROVAL_TYPE',
  approval_type_code VARCHAR(50),
  PRIMARY KEY (attendance_record_id),
  UNIQUE KEY uk_attendance_records_user_date (user_id, attendance_date),
  KEY idx_attendance_records_date_status (attendance_date, attendance_status_code),
  CONSTRAINT chk_attendance_records_status_group CHECK (attendance_status_code_group = 'ATTENDANCE_STATUS'),
  CONSTRAINT chk_attendance_records_approval_type_group CHECK (approval_type_code_group = 'ATTENDANCE_APPROVAL_TYPE'),
  CONSTRAINT fk_attendance_records_status
    FOREIGN KEY (attendance_status_code_group, attendance_status_code) REFERENCES codes (code_group, code),
  CONSTRAINT fk_attendance_records_approval_type
    FOREIGN KEY (approval_type_code_group, approval_type_code) REFERENCES codes (code_group, code),
  CONSTRAINT fk_attendance_records_user
    FOREIGN KEY (user_id) REFERENCES users (user_id)
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE attendance_appeals (
  attendance_appeal_id BIGINT NOT NULL AUTO_INCREMENT,
  attendance_record_id BIGINT NOT NULL,
  appeal_type_code_group VARCHAR(50) NOT NULL DEFAULT 'ATTENDANCE_APPEAL_TYPE',
  appeal_type_code VARCHAR(50) NOT NULL,
  reason TEXT,
  approval_status_code_group VARCHAR(50) NOT NULL DEFAULT 'APPROVAL_STATUS',
  approval_status_code VARCHAR(50) NOT NULL DEFAULT 'requested',
  requested_status_code_group VARCHAR(50) NOT NULL DEFAULT 'ATTENDANCE_STATUS',
  requested_status_code VARCHAR(50),
  resolved_status_code_group VARCHAR(50) NOT NULL DEFAULT 'ATTENDANCE_STATUS',
  resolved_status_code VARCHAR(50),
  requested_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  resolved_at DATETIME,
  resolved_by_user_id BIGINT,
  resolution_comment TEXT,
  PRIMARY KEY (attendance_appeal_id),
  KEY idx_attendance_appeals_record_status (attendance_record_id, approval_status_code),
  KEY idx_attendance_appeals_resolved_by (resolved_by_user_id),
  CONSTRAINT chk_attendance_appeals_appeal_type_group CHECK (appeal_type_code_group = 'ATTENDANCE_APPEAL_TYPE'),
  CONSTRAINT chk_attendance_appeals_approval_status_group CHECK (approval_status_code_group = 'APPROVAL_STATUS'),
  CONSTRAINT chk_attendance_appeals_requested_status_group CHECK (requested_status_code_group = 'ATTENDANCE_STATUS'),
  CONSTRAINT chk_attendance_appeals_resolved_status_group CHECK (resolved_status_code_group = 'ATTENDANCE_STATUS'),
  CONSTRAINT fk_attendance_appeals_appeal_type
    FOREIGN KEY (appeal_type_code_group, appeal_type_code) REFERENCES codes (code_group, code),
  CONSTRAINT fk_attendance_appeals_approval_status
    FOREIGN KEY (approval_status_code_group, approval_status_code) REFERENCES codes (code_group, code),
  CONSTRAINT fk_attendance_appeals_requested_status
    FOREIGN KEY (requested_status_code_group, requested_status_code) REFERENCES codes (code_group, code),
  CONSTRAINT fk_attendance_appeals_resolved_status
    FOREIGN KEY (resolved_status_code_group, resolved_status_code) REFERENCES codes (code_group, code),
  CONSTRAINT fk_attendance_appeals_attendance_record
    FOREIGN KEY (attendance_record_id) REFERENCES attendance_records (attendance_record_id)
    ON DELETE CASCADE,
  CONSTRAINT fk_attendance_appeals_resolved_by
    FOREIGN KEY (resolved_by_user_id) REFERENCES users (user_id)
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE notifications (
  notification_id BIGINT NOT NULL AUTO_INCREMENT,
  sender_user_id BIGINT,
  title VARCHAR(255) NOT NULL,
  body TEXT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (notification_id),
  KEY idx_notifications_sender_created (sender_user_id, created_at),
  CONSTRAINT fk_notifications_sender
    FOREIGN KEY (sender_user_id) REFERENCES users (user_id)
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE notification_recipients (
  notification_recipient_id BIGINT NOT NULL AUTO_INCREMENT,
  notification_id BIGINT NOT NULL,
  recipient_user_id BIGINT,
  read_at DATETIME,
  deleted_at DATETIME,
  PRIMARY KEY (notification_recipient_id),
  UNIQUE KEY uk_notification_recipients_notification_user (notification_id, recipient_user_id),
  KEY idx_notification_recipients_user_read (recipient_user_id, read_at),
  CONSTRAINT fk_notification_recipients_notification
    FOREIGN KEY (notification_id) REFERENCES notifications (notification_id)
    ON DELETE CASCADE,
  CONSTRAINT fk_notification_recipients_recipient
    FOREIGN KEY (recipient_user_id) REFERENCES users (user_id)
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE content_scopes (
  content_scope_id BIGINT NOT NULL AUTO_INCREMENT,
  scope_type_code_group VARCHAR(50) NOT NULL DEFAULT 'CONTENT_SCOPE_TYPE',
  scope_type_code VARCHAR(50) NOT NULL,
  campus_id BIGINT,
  cohort_id BIGINT,
  track_id BIGINT,
  class_group_id BIGINT,
  user_id BIGINT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (content_scope_id),
  KEY idx_content_scopes_campus (campus_id),
  KEY idx_content_scopes_cohort (cohort_id),
  KEY idx_content_scopes_track (track_id),
  KEY idx_content_scopes_class_group (class_group_id),
  KEY idx_content_scopes_user (user_id),
  CONSTRAINT chk_content_scopes_scope_group CHECK (scope_type_code_group = 'CONTENT_SCOPE_TYPE'),
  CONSTRAINT chk_content_scopes_target CHECK (
    (scope_type_code = 'all' AND campus_id IS NULL AND cohort_id IS NULL AND track_id IS NULL AND class_group_id IS NULL AND user_id IS NULL)
    OR (scope_type_code = 'campus' AND campus_id IS NOT NULL AND cohort_id IS NULL AND track_id IS NULL AND class_group_id IS NULL AND user_id IS NULL)
    OR (scope_type_code = 'cohort' AND campus_id IS NULL AND cohort_id IS NOT NULL AND track_id IS NULL AND class_group_id IS NULL AND user_id IS NULL)
    OR (scope_type_code = 'track' AND campus_id IS NULL AND cohort_id IS NULL AND track_id IS NOT NULL AND class_group_id IS NULL AND user_id IS NULL)
    OR (scope_type_code = 'track_cohort' AND campus_id IS NULL AND cohort_id IS NOT NULL AND track_id IS NOT NULL AND class_group_id IS NULL AND user_id IS NULL)
    OR (scope_type_code = 'class_group' AND campus_id IS NULL AND cohort_id IS NULL AND track_id IS NULL AND class_group_id IS NOT NULL AND user_id IS NULL)
    OR (scope_type_code = 'user' AND campus_id IS NULL AND cohort_id IS NULL AND track_id IS NULL AND class_group_id IS NULL AND user_id IS NOT NULL)
  ),
  CONSTRAINT fk_content_scopes_scope_type
    FOREIGN KEY (scope_type_code_group, scope_type_code) REFERENCES codes (code_group, code),
  CONSTRAINT fk_content_scopes_campus
    FOREIGN KEY (campus_id) REFERENCES campuses (campus_id)
    ON DELETE RESTRICT,
  CONSTRAINT fk_content_scopes_cohort
    FOREIGN KEY (cohort_id) REFERENCES cohorts (cohort_id)
    ON DELETE RESTRICT,
  CONSTRAINT fk_content_scopes_track
    FOREIGN KEY (track_id) REFERENCES tracks (track_id)
    ON DELETE RESTRICT,
  CONSTRAINT fk_content_scopes_class_group
    FOREIGN KEY (class_group_id) REFERENCES class_groups (class_group_id)
    ON DELETE RESTRICT,
  CONSTRAINT fk_content_scopes_user
    FOREIGN KEY (user_id) REFERENCES users (user_id)
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE terms (
  term_id BIGINT NOT NULL AUTO_INCREMENT,
  term_name VARCHAR(100) NOT NULL,
  progress_status_code_group VARCHAR(50) NOT NULL DEFAULT 'PROGRESS_STATUS',
  progress_status_code VARCHAR(50) NOT NULL,
  start_date DATE,
  end_date DATE,
  PRIMARY KEY (term_id),
  UNIQUE KEY uk_terms_name (term_name),
  CONSTRAINT chk_terms_progress_status_group CHECK (progress_status_code_group = 'PROGRESS_STATUS'),
  CONSTRAINT chk_terms_date_range CHECK (end_date IS NULL OR start_date IS NULL OR end_date >= start_date),
  CONSTRAINT fk_terms_progress_status
    FOREIGN KEY (progress_status_code_group, progress_status_code) REFERENCES codes (code_group, code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE curriculum_schedules (
  curriculum_schedule_id BIGINT NOT NULL AUTO_INCREMENT,
  term_id BIGINT NOT NULL,
  content_scope_id BIGINT NOT NULL,
  week_no INT,
  class_date DATE,
  start_time TIME,
  end_time TIME,
  curriculum_type_code_group VARCHAR(50) NOT NULL DEFAULT 'CURRICULUM_TYPE',
  curriculum_type_code VARCHAR(50),
  topic VARCHAR(255) NOT NULL,
  instructor_name VARCHAR(100),
  classroom VARCHAR(100),
  PRIMARY KEY (curriculum_schedule_id),
  UNIQUE KEY uk_curriculum_schedules_id_scope (curriculum_schedule_id, content_scope_id),
  KEY idx_curriculum_schedules_term_week (term_id, week_no),
  KEY idx_curriculum_schedules_scope_date (content_scope_id, class_date),
  CONSTRAINT chk_curriculum_schedules_curriculum_type_group CHECK (curriculum_type_code_group = 'CURRICULUM_TYPE'),
  CONSTRAINT chk_curriculum_schedules_time_range CHECK (end_time IS NULL OR start_time IS NULL OR end_time >= start_time),
  CONSTRAINT fk_curriculum_schedules_curriculum_type
    FOREIGN KEY (curriculum_type_code_group, curriculum_type_code) REFERENCES codes (code_group, code),
  CONSTRAINT fk_curriculum_schedules_term
    FOREIGN KEY (term_id) REFERENCES terms (term_id),
  CONSTRAINT fk_curriculum_schedules_scope
    FOREIGN KEY (content_scope_id) REFERENCES content_scopes (content_scope_id)
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE lecture_replays (
  lecture_replay_id BIGINT NOT NULL AUTO_INCREMENT,
  curriculum_schedule_id BIGINT NOT NULL,
  content_external_id VARCHAR(100),
  replay_group_key VARCHAR(255) NOT NULL DEFAULT 'default',
  title VARCHAR(255) NOT NULL,
  version_no INT NOT NULL DEFAULT 1,
  published_at DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (lecture_replay_id),
  UNIQUE KEY uk_lecture_replays_external_content (content_external_id),
  UNIQUE KEY uk_lecture_replays_schedule_group_version (curriculum_schedule_id, replay_group_key, version_no),
  KEY idx_lecture_replays_latest_lookup (curriculum_schedule_id, replay_group_key, version_no, published_at),
  CONSTRAINT chk_lecture_replays_version_no CHECK (version_no > 0),
  CONSTRAINT fk_lecture_replays_schedule
    FOREIGN KEY (curriculum_schedule_id) REFERENCES curriculum_schedules (curriculum_schedule_id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE learning_materials (
  learning_material_id BIGINT NOT NULL AUTO_INCREMENT,
  curriculum_schedule_id BIGINT,
  content_scope_id BIGINT NOT NULL,
  content_external_id VARCHAR(100),
  category_parent VARCHAR(100),
  category_child VARCHAR(100),
  material_type_code_group VARCHAR(50) NOT NULL DEFAULT 'MATERIAL_TYPE',
  material_type_code VARCHAR(50) NOT NULL,
  title VARCHAR(255) NOT NULL,
  summary TEXT,
  detail_url VARCHAR(500),
  view_count INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (learning_material_id),
  UNIQUE KEY uk_learning_materials_external_content (content_external_id),
  KEY idx_learning_materials_schedule_scope (curriculum_schedule_id, content_scope_id),
  KEY idx_learning_materials_scope_type (content_scope_id, material_type_code),
  CONSTRAINT chk_learning_materials_material_type_group CHECK (material_type_code_group = 'MATERIAL_TYPE'),
  CONSTRAINT chk_learning_materials_view_count CHECK (view_count >= 0),
  CONSTRAINT fk_learning_materials_material_type
    FOREIGN KEY (material_type_code_group, material_type_code) REFERENCES codes (code_group, code),
  CONSTRAINT fk_learning_materials_scope
    FOREIGN KEY (content_scope_id) REFERENCES content_scopes (content_scope_id)
    ON DELETE RESTRICT,
  CONSTRAINT fk_learning_materials_schedule_scope
    FOREIGN KEY (curriculum_schedule_id, content_scope_id)
    REFERENCES curriculum_schedules (curriculum_schedule_id, content_scope_id)
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE learning_material_resources (
  learning_material_resource_id BIGINT NOT NULL AUTO_INCREMENT,
  learning_material_id BIGINT NOT NULL,
  resource_type_code_group VARCHAR(50) NOT NULL DEFAULT 'RESOURCE_TYPE',
  resource_type_code VARCHAR(50) NOT NULL,
  resource_title VARCHAR(255) NOT NULL,
  launch_mode_code_group VARCHAR(50) NOT NULL DEFAULT 'LAUNCH_MODE',
  launch_mode_code VARCHAR(50) NOT NULL,
  target_url VARCHAR(500),
  external_resource_id VARCHAR(100),
  display_order INT NOT NULL DEFAULT 1,
  PRIMARY KEY (learning_material_resource_id),
  UNIQUE KEY uk_learning_material_resources_material_order (learning_material_id, display_order),
  CONSTRAINT chk_learning_material_resources_resource_type_group CHECK (resource_type_code_group = 'RESOURCE_TYPE'),
  CONSTRAINT chk_learning_material_resources_launch_mode_group CHECK (launch_mode_code_group = 'LAUNCH_MODE'),
  CONSTRAINT chk_learning_material_resources_display_order CHECK (display_order > 0),
  CONSTRAINT fk_learning_material_resources_resource_type
    FOREIGN KEY (resource_type_code_group, resource_type_code) REFERENCES codes (code_group, code),
  CONSTRAINT fk_learning_material_resources_launch_mode
    FOREIGN KEY (launch_mode_code_group, launch_mode_code) REFERENCES codes (code_group, code),
  CONSTRAINT fk_learning_material_resources_material
    FOREIGN KEY (learning_material_id) REFERENCES learning_materials (learning_material_id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE learning_material_resource_attachments (
  learning_material_resource_id BIGINT NOT NULL,
  attachment_id BIGINT NOT NULL,
  PRIMARY KEY (learning_material_resource_id, attachment_id),
  CONSTRAINT fk_learning_material_resource_attachments_resource
    FOREIGN KEY (learning_material_resource_id) REFERENCES learning_material_resources (learning_material_resource_id)
    ON DELETE CASCADE,
  CONSTRAINT fk_learning_material_resource_attachments_attachment
    FOREIGN KEY (attachment_id) REFERENCES attachments (attachment_id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE learning_material_reactions (
  learning_material_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  reaction_type_code_group VARCHAR(50) NOT NULL DEFAULT 'REACTION_TYPE',
  reaction_type_code VARCHAR(50) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (learning_material_id, user_id, reaction_type_code),
  CONSTRAINT chk_learning_material_reactions_reaction_type_group CHECK (reaction_type_code_group = 'REACTION_TYPE'),
  CONSTRAINT fk_learning_material_reactions_reaction_type
    FOREIGN KEY (reaction_type_code_group, reaction_type_code) REFERENCES codes (code_group, code),
  CONSTRAINT fk_learning_material_reactions_material
    FOREIGN KEY (learning_material_id) REFERENCES learning_materials (learning_material_id)
    ON DELETE CASCADE,
  CONSTRAINT fk_learning_material_reactions_user
    FOREIGN KEY (user_id) REFERENCES users (user_id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE quest_evaluations (
  quest_evaluation_id BIGINT NOT NULL AUTO_INCREMENT,
  content_scope_id BIGINT NOT NULL,
  external_task_id VARCHAR(100) NOT NULL,
  quest_type_code_group VARCHAR(50) NOT NULL DEFAULT 'QUEST_TYPE',
  quest_type_code VARCHAR(50) NOT NULL,
  task_classification_code_group VARCHAR(50) NOT NULL DEFAULT 'TASK_CLASSIFICATION',
  task_classification_code VARCHAR(50),
  title VARCHAR(255) NOT NULL,
  start_at DATETIME,
  end_at DATETIME,
  max_exp INT,
  progress_status_code_group VARCHAR(50) NOT NULL DEFAULT 'PROGRESS_STATUS',
  progress_status_code VARCHAR(50) NOT NULL,
  PRIMARY KEY (quest_evaluation_id),
  UNIQUE KEY uk_quest_evaluations_external_task (external_task_id),
  KEY idx_quest_evaluations_scope_period (content_scope_id, start_at, end_at),
  CONSTRAINT chk_quest_evaluations_quest_type_group CHECK (quest_type_code_group = 'QUEST_TYPE'),
  CONSTRAINT chk_quest_evaluations_task_classification_group CHECK (task_classification_code_group = 'TASK_CLASSIFICATION'),
  CONSTRAINT chk_quest_evaluations_progress_status_group CHECK (progress_status_code_group = 'PROGRESS_STATUS'),
  CONSTRAINT chk_quest_evaluations_period CHECK (end_at IS NULL OR start_at IS NULL OR end_at >= start_at),
  CONSTRAINT chk_quest_evaluations_max_exp CHECK (max_exp IS NULL OR max_exp >= 0),
  CONSTRAINT fk_quest_evaluations_scope
    FOREIGN KEY (content_scope_id) REFERENCES content_scopes (content_scope_id)
    ON DELETE RESTRICT,
  CONSTRAINT fk_quest_evaluations_quest_type
    FOREIGN KEY (quest_type_code_group, quest_type_code) REFERENCES codes (code_group, code),
  CONSTRAINT fk_quest_evaluations_task_classification
    FOREIGN KEY (task_classification_code_group, task_classification_code) REFERENCES codes (code_group, code),
  CONSTRAINT fk_quest_evaluations_progress_status
    FOREIGN KEY (progress_status_code_group, progress_status_code) REFERENCES codes (code_group, code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE quest_submissions (
  quest_submission_id BIGINT NOT NULL AUTO_INCREMENT,
  quest_evaluation_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  result_status_code_group VARCHAR(50) NOT NULL DEFAULT 'QUEST_RESULT_STATUS',
  result_status_code VARCHAR(50),
  score DECIMAL(7,2),
  submit_status_code_group VARCHAR(50) NOT NULL DEFAULT 'QUEST_SUBMIT_STATUS',
  submit_status_code VARCHAR(50) NOT NULL,
  submitted_at DATETIME,
  graded_at DATETIME,
  PRIMARY KEY (quest_submission_id),
  UNIQUE KEY uk_quest_submissions_quest_user (quest_evaluation_id, user_id),
  KEY idx_quest_submissions_user_status (user_id, submit_status_code),
  CONSTRAINT chk_quest_submissions_result_status_group CHECK (result_status_code_group = 'QUEST_RESULT_STATUS'),
  CONSTRAINT chk_quest_submissions_submit_status_group CHECK (submit_status_code_group = 'QUEST_SUBMIT_STATUS'),
  CONSTRAINT chk_quest_submissions_score CHECK (score IS NULL OR score >= 0),
  CONSTRAINT fk_quest_submissions_result_status
    FOREIGN KEY (result_status_code_group, result_status_code) REFERENCES codes (code_group, code),
  CONSTRAINT fk_quest_submissions_submit_status
    FOREIGN KEY (submit_status_code_group, submit_status_code) REFERENCES codes (code_group, code),
  CONSTRAINT fk_quest_submissions_quest
    FOREIGN KEY (quest_evaluation_id) REFERENCES quest_evaluations (quest_evaluation_id)
    ON DELETE CASCADE,
  CONSTRAINT fk_quest_submissions_user
    FOREIGN KEY (user_id) REFERENCES users (user_id)
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE surveys (
  survey_id BIGINT NOT NULL AUTO_INCREMENT,
  content_scope_id BIGINT NOT NULL,
  title VARCHAR(255) NOT NULL,
  survey_category_code_group VARCHAR(50) NOT NULL DEFAULT 'SURVEY_CATEGORY',
  survey_category_code VARCHAR(50) NOT NULL,
  required_yn BOOLEAN NOT NULL DEFAULT FALSE,
  progress_status_code_group VARCHAR(50) NOT NULL DEFAULT 'PROGRESS_STATUS',
  progress_status_code VARCHAR(50) NOT NULL,
  start_at DATETIME,
  end_at DATETIME,
  PRIMARY KEY (survey_id),
  KEY idx_surveys_scope_period (content_scope_id, start_at, end_at),
  CONSTRAINT chk_surveys_category_group CHECK (survey_category_code_group = 'SURVEY_CATEGORY'),
  CONSTRAINT chk_surveys_progress_status_group CHECK (progress_status_code_group = 'PROGRESS_STATUS'),
  CONSTRAINT chk_surveys_period CHECK (end_at IS NULL OR start_at IS NULL OR end_at >= start_at),
  CONSTRAINT fk_surveys_scope
    FOREIGN KEY (content_scope_id) REFERENCES content_scopes (content_scope_id)
    ON DELETE RESTRICT,
  CONSTRAINT fk_surveys_category
    FOREIGN KEY (survey_category_code_group, survey_category_code) REFERENCES codes (code_group, code),
  CONSTRAINT fk_surveys_progress_status
    FOREIGN KEY (progress_status_code_group, progress_status_code) REFERENCES codes (code_group, code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE survey_questions (
  survey_question_id BIGINT NOT NULL AUTO_INCREMENT,
  survey_id BIGINT NOT NULL,
  question_type_code_group VARCHAR(50) NOT NULL DEFAULT 'SURVEY_QUESTION_TYPE',
  question_type_code VARCHAR(50) NOT NULL,
  question_text TEXT NOT NULL,
  display_order INT NOT NULL,
  PRIMARY KEY (survey_question_id),
  UNIQUE KEY uk_survey_questions_survey_order (survey_id, display_order),
  UNIQUE KEY uk_survey_questions_id_survey (survey_question_id, survey_id),
  CONSTRAINT chk_survey_questions_type_group CHECK (question_type_code_group = 'SURVEY_QUESTION_TYPE'),
  CONSTRAINT chk_survey_questions_display_order CHECK (display_order > 0),
  CONSTRAINT fk_survey_questions_type
    FOREIGN KEY (question_type_code_group, question_type_code) REFERENCES codes (code_group, code),
  CONSTRAINT fk_survey_questions_survey
    FOREIGN KEY (survey_id) REFERENCES surveys (survey_id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE survey_options (
  survey_option_id BIGINT NOT NULL AUTO_INCREMENT,
  survey_question_id BIGINT NOT NULL,
  option_text VARCHAR(255) NOT NULL,
  display_order INT NOT NULL,
  PRIMARY KEY (survey_option_id),
  UNIQUE KEY uk_survey_options_question_order (survey_question_id, display_order),
  UNIQUE KEY uk_survey_options_option_question (survey_option_id, survey_question_id),
  CONSTRAINT chk_survey_options_display_order CHECK (display_order > 0),
  CONSTRAINT fk_survey_options_question
    FOREIGN KEY (survey_question_id) REFERENCES survey_questions (survey_question_id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE survey_responses (
  survey_response_id BIGINT NOT NULL AUTO_INCREMENT,
  survey_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  completed_yn BOOLEAN NOT NULL DEFAULT FALSE,
  responded_at DATETIME,
  PRIMARY KEY (survey_response_id),
  UNIQUE KEY uk_survey_responses_survey_user (survey_id, user_id),
  UNIQUE KEY uk_survey_responses_id_survey (survey_response_id, survey_id),
  KEY idx_survey_responses_user_responded (user_id, responded_at),
  CONSTRAINT fk_survey_responses_survey
    FOREIGN KEY (survey_id) REFERENCES surveys (survey_id)
    ON DELETE CASCADE,
  CONSTRAINT fk_survey_responses_user
    FOREIGN KEY (user_id) REFERENCES users (user_id)
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE survey_response_answers (
  survey_response_answer_id BIGINT NOT NULL AUTO_INCREMENT,
  survey_response_id BIGINT NOT NULL,
  survey_id BIGINT NOT NULL,
  survey_question_id BIGINT NOT NULL,
  answer_text TEXT,
  PRIMARY KEY (survey_response_answer_id),
  UNIQUE KEY uk_survey_response_answers_response_question (survey_response_id, survey_question_id),
  UNIQUE KEY uk_survey_response_answers_answer_question (survey_response_answer_id, survey_question_id),
  KEY idx_survey_response_answers_question (survey_question_id),
  CONSTRAINT fk_survey_response_answers_response_survey
    FOREIGN KEY (survey_response_id, survey_id)
    REFERENCES survey_responses (survey_response_id, survey_id)
    ON DELETE CASCADE,
  CONSTRAINT fk_survey_response_answers_question_survey
    FOREIGN KEY (survey_question_id, survey_id)
    REFERENCES survey_questions (survey_question_id, survey_id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE survey_response_answer_options (
  survey_response_answer_id BIGINT NOT NULL,
  survey_question_id BIGINT NOT NULL,
  survey_option_id BIGINT NOT NULL,
  PRIMARY KEY (survey_response_answer_id, survey_option_id),
  KEY idx_survey_response_answer_options_option (survey_option_id),
  CONSTRAINT fk_survey_response_answer_options_answer_question
    FOREIGN KEY (survey_response_answer_id, survey_question_id)
    REFERENCES survey_response_answers (survey_response_answer_id, survey_question_id)
    ON DELETE CASCADE,
  CONSTRAINT fk_survey_response_answer_options_option_question
    FOREIGN KEY (survey_option_id, survey_question_id)
    REFERENCES survey_options (survey_option_id, survey_question_id)
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE boards (
  board_id BIGINT NOT NULL AUTO_INCREMENT,
  board_code VARCHAR(50) NOT NULL,
  board_name VARCHAR(100) NOT NULL,
  board_group_code_group VARCHAR(50) NOT NULL DEFAULT 'BOARD_GROUP',
  board_group_code VARCHAR(50) NOT NULL,
  access_scope_code_group VARCHAR(50) NOT NULL DEFAULT 'ACCESS_SCOPE',
  access_scope_code VARCHAR(50) NOT NULL DEFAULT 'authenticated',
  PRIMARY KEY (board_id),
  UNIQUE KEY uk_boards_code (board_code),
  CONSTRAINT chk_boards_group_code_group CHECK (board_group_code_group = 'BOARD_GROUP'),
  CONSTRAINT chk_boards_access_scope_group CHECK (access_scope_code_group = 'ACCESS_SCOPE'),
  CONSTRAINT fk_boards_group_code
    FOREIGN KEY (board_group_code_group, board_group_code) REFERENCES codes (code_group, code),
  CONSTRAINT fk_boards_access_scope
    FOREIGN KEY (access_scope_code_group, access_scope_code) REFERENCES codes (code_group, code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE board_categories (
  board_category_id BIGINT NOT NULL AUTO_INCREMENT,
  board_id BIGINT NOT NULL,
  category_name VARCHAR(100) NOT NULL,
  sort_order INT NOT NULL DEFAULT 1,
  PRIMARY KEY (board_category_id),
  UNIQUE KEY uk_board_categories_board_name (board_id, category_name),
  UNIQUE KEY uk_board_categories_id_board (board_category_id, board_id),
  CONSTRAINT chk_board_categories_sort_order CHECK (sort_order > 0),
  CONSTRAINT fk_board_categories_board
    FOREIGN KEY (board_id) REFERENCES boards (board_id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE board_posts (
  board_post_id BIGINT NOT NULL AUTO_INCREMENT,
  board_id BIGINT NOT NULL,
  board_category_id BIGINT,
  author_user_id BIGINT,
  title VARCHAR(255) NOT NULL,
  content TEXT,
  notice_yn BOOLEAN NOT NULL DEFAULT FALSE,
  view_count INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (board_post_id),
  KEY idx_board_posts_board_created (board_id, created_at),
  KEY idx_board_posts_author_created (author_user_id, created_at),
  CONSTRAINT chk_board_posts_view_count CHECK (view_count >= 0),
  CONSTRAINT fk_board_posts_board
    FOREIGN KEY (board_id) REFERENCES boards (board_id)
    ON DELETE CASCADE,
  CONSTRAINT fk_board_posts_category_board
    FOREIGN KEY (board_category_id, board_id)
    REFERENCES board_categories (board_category_id, board_id)
    ON DELETE RESTRICT,
  CONSTRAINT fk_board_posts_author
    FOREIGN KEY (author_user_id) REFERENCES users (user_id)
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE board_post_attachments (
  board_post_id BIGINT NOT NULL,
  attachment_id BIGINT NOT NULL,
  PRIMARY KEY (board_post_id, attachment_id),
  CONSTRAINT fk_board_post_attachments_post
    FOREIGN KEY (board_post_id) REFERENCES board_posts (board_post_id)
    ON DELETE CASCADE,
  CONSTRAINT fk_board_post_attachments_attachment
    FOREIGN KEY (attachment_id) REFERENCES attachments (attachment_id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE board_comments (
  board_comment_id BIGINT NOT NULL AUTO_INCREMENT,
  board_post_id BIGINT NOT NULL,
  author_user_id BIGINT,
  parent_comment_id BIGINT,
  content TEXT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (board_comment_id),
  KEY idx_board_comments_post_created (board_post_id, created_at),
  KEY idx_board_comments_parent (parent_comment_id),
  CONSTRAINT fk_board_comments_post
    FOREIGN KEY (board_post_id) REFERENCES board_posts (board_post_id)
    ON DELETE CASCADE,
  CONSTRAINT fk_board_comments_author
    FOREIGN KEY (author_user_id) REFERENCES users (user_id)
    ON DELETE SET NULL,
  CONSTRAINT fk_board_comments_parent
    FOREIGN KEY (parent_comment_id) REFERENCES board_comments (board_comment_id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE board_post_reactions (
  board_post_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  reaction_type_code_group VARCHAR(50) NOT NULL DEFAULT 'REACTION_TYPE',
  reaction_type_code VARCHAR(50) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (board_post_id, user_id, reaction_type_code),
  CONSTRAINT chk_board_post_reactions_reaction_type_group CHECK (reaction_type_code_group = 'REACTION_TYPE'),
  CONSTRAINT fk_board_post_reactions_reaction_type
    FOREIGN KEY (reaction_type_code_group, reaction_type_code) REFERENCES codes (code_group, code),
  CONSTRAINT fk_board_post_reactions_post
    FOREIGN KEY (board_post_id) REFERENCES board_posts (board_post_id)
    ON DELETE CASCADE,
  CONSTRAINT fk_board_post_reactions_user
    FOREIGN KEY (user_id) REFERENCES users (user_id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE support_tickets (
  support_ticket_id BIGINT NOT NULL AUTO_INCREMENT,
  requester_user_id BIGINT,
  title VARCHAR(255) NOT NULL,
  status_code_group VARCHAR(50) NOT NULL DEFAULT 'SUPPORT_TICKET_STATUS',
  status_code VARCHAR(50) NOT NULL DEFAULT 'open',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  closed_at DATETIME,
  PRIMARY KEY (support_ticket_id),
  KEY idx_support_tickets_requester_status (requester_user_id, status_code),
  CONSTRAINT chk_support_tickets_status_group CHECK (status_code_group = 'SUPPORT_TICKET_STATUS'),
  CONSTRAINT fk_support_tickets_status
    FOREIGN KEY (status_code_group, status_code) REFERENCES codes (code_group, code),
  CONSTRAINT fk_support_tickets_requester
    FOREIGN KEY (requester_user_id) REFERENCES users (user_id)
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE support_ticket_messages (
  support_ticket_message_id BIGINT NOT NULL AUTO_INCREMENT,
  support_ticket_id BIGINT NOT NULL,
  sender_user_id BIGINT,
  message_type_code_group VARCHAR(50) NOT NULL DEFAULT 'SUPPORT_MESSAGE_TYPE',
  message_type_code VARCHAR(50) NOT NULL,
  content TEXT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (support_ticket_message_id),
  KEY idx_support_ticket_messages_ticket_created (support_ticket_id, created_at),
  KEY idx_support_ticket_messages_sender_created (sender_user_id, created_at),
  CONSTRAINT chk_support_ticket_messages_type_group CHECK (message_type_code_group = 'SUPPORT_MESSAGE_TYPE'),
  CONSTRAINT fk_support_ticket_messages_type
    FOREIGN KEY (message_type_code_group, message_type_code) REFERENCES codes (code_group, code),
  CONSTRAINT fk_support_ticket_messages_ticket
    FOREIGN KEY (support_ticket_id) REFERENCES support_tickets (support_ticket_id)
    ON DELETE CASCADE,
  CONSTRAINT fk_support_ticket_messages_sender
    FOREIGN KEY (sender_user_id) REFERENCES users (user_id)
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE support_ticket_message_attachments (
  support_ticket_message_id BIGINT NOT NULL,
  attachment_id BIGINT NOT NULL,
  PRIMARY KEY (support_ticket_message_id, attachment_id),
  CONSTRAINT fk_support_ticket_message_attachments_message
    FOREIGN KEY (support_ticket_message_id) REFERENCES support_ticket_messages (support_ticket_message_id)
    ON DELETE CASCADE,
  CONSTRAINT fk_support_ticket_message_attachments_attachment
    FOREIGN KEY (attachment_id) REFERENCES attachments (attachment_id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
