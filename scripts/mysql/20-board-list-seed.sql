SET NAMES utf8mb4;

INSERT INTO campuses (campus_name)
VALUES ('Seoul')
ON DUPLICATE KEY UPDATE campus_name = VALUES(campus_name);

INSERT INTO cohorts (cohort_name, start_date, end_date)
VALUES ('12th', '2026-01-01', '2026-12-31')
ON DUPLICATE KEY UPDATE start_date = VALUES(start_date), end_date = VALUES(end_date);

INSERT INTO tracks (track_name, domain_type)
VALUES ('Java', 'backend')
ON DUPLICATE KEY UPDATE domain_type = VALUES(domain_type);

INSERT INTO users (email, password_hash, learner_no, name, role_code)
VALUES
  ('student@ssafy.com', '{sha256}5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8', 'SSAFY-12-0001', 'Demo Student', 'student'),
  ('manager@ssafy.com', '{sha256}5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8', 'SSAFY-12-9001', 'Demo Manager', 'manager'),
  ('classmate@ssafy.com', '{sha256}5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8', 'SSAFY-12-0002', 'Demo Classmate', 'student')
ON DUPLICATE KEY UPDATE
  password_hash = VALUES(password_hash),
  name = VALUES(name),
  role_code = VALUES(role_code),
  status_code = 'active';

INSERT IGNORE INTO user_profiles (user_id, mobile_phone, marketing_opt_in)
SELECT user_id, '010-0000-0000', FALSE
FROM users
WHERE email IN ('student@ssafy.com', 'manager@ssafy.com', 'classmate@ssafy.com');

SET @campus_id := (SELECT campus_id FROM campuses WHERE campus_name = 'Seoul');
SET @cohort_id := (SELECT cohort_id FROM cohorts WHERE cohort_name = '12th');
SET @track_id := (SELECT track_id FROM tracks WHERE track_name = 'Java');
SET @student_id := (SELECT user_id FROM users WHERE email = 'student@ssafy.com');
SET @manager_id := (SELECT user_id FROM users WHERE email = 'manager@ssafy.com');
SET @classmate_id := (SELECT user_id FROM users WHERE email = 'classmate@ssafy.com');

INSERT INTO class_groups (campus_id, cohort_id, track_id, class_name)
VALUES (@campus_id, @cohort_id, @track_id, 'Seoul Java 1')
ON DUPLICATE KEY UPDATE class_name = VALUES(class_name);

SET @class_group_id := (
  SELECT class_group_id
  FROM class_groups
  WHERE campus_id = @campus_id
    AND cohort_id = @cohort_id
    AND track_id = @track_id
    AND class_name = 'Seoul Java 1'
);

INSERT IGNORE INTO user_track_enrollments (user_id, track_id, cohort_id)
VALUES
  (@student_id, @track_id, @cohort_id),
  (@manager_id, @track_id, @cohort_id),
  (@classmate_id, @track_id, @cohort_id);

INSERT IGNORE INTO user_class_enrollments (user_id, class_group_id, cohort_id, track_id, member_role_code)
VALUES
  (@student_id, @class_group_id, @cohort_id, @track_id, 'student'),
  (@manager_id, @class_group_id, @cohort_id, @track_id, 'manager'),
  (@classmate_id, @class_group_id, @cohort_id, @track_id, 'student');

INSERT INTO content_scopes (scope_type_code, class_group_id)
SELECT 'class_group', @class_group_id
WHERE NOT EXISTS (
  SELECT 1 FROM content_scopes
  WHERE scope_type_code = 'class_group'
    AND class_group_id = @class_group_id
);

SET @class_scope_id := (
  SELECT content_scope_id
  FROM content_scopes
  WHERE scope_type_code = 'class_group'
    AND class_group_id = @class_group_id
  ORDER BY content_scope_id
  LIMIT 1
);

INSERT INTO terms (term_name, progress_status_code, start_date, end_date)
VALUES ('2026 Priority 1 Term', 'in_progress', '2026-04-01', '2026-04-30')
ON DUPLICATE KEY UPDATE
  progress_status_code = VALUES(progress_status_code),
  start_date = VALUES(start_date),
  end_date = VALUES(end_date);

SET @term_id := (SELECT term_id FROM terms WHERE term_name = '2026 Priority 1 Term');

INSERT INTO user_level_statuses (user_id, level_name, level_no, exp, scholarship_point)
VALUES (@student_id, 'Level 5', 5, 4200, 85)
ON DUPLICATE KEY UPDATE
  level_name = VALUES(level_name),
  level_no = VALUES(level_no),
  exp = VALUES(exp),
  scholarship_point = VALUES(scholarship_point);

INSERT INTO user_rank_snapshots (snapshot_date, user_id, rank_no, exp, scholarship_point)
VALUES ('2026-04-24', @student_id, 12, 4200, 85)
ON DUPLICATE KEY UPDATE exp = VALUES(exp), scholarship_point = VALUES(scholarship_point);

INSERT INTO attendance_records (user_id, attendance_date, check_in_at, check_out_at, attendance_status_code, approval_type_code)
VALUES
  (@student_id, '2026-04-22', '08:55:00', '18:01:00', 'present', 'auto'),
  (@student_id, '2026-04-23', '09:08:00', '18:03:00', 'late', 'auto'),
  (@student_id, '2026-04-24', '08:58:00', NULL, 'present', 'auto')
ON DUPLICATE KEY UPDATE
  check_in_at = VALUES(check_in_at),
  check_out_at = VALUES(check_out_at),
  attendance_status_code = VALUES(attendance_status_code),
  approval_type_code = VALUES(approval_type_code);

INSERT INTO attendance_appeals (attendance_record_id, appeal_type_code, reason, requested_status_code)
SELECT attendance_record_id, 'status_change', 'Seed appeal for attendance screen smoke checks.', 'present'
FROM attendance_records
WHERE user_id = @student_id
  AND attendance_date = '2026-04-23'
  AND NOT EXISTS (
    SELECT 1
    FROM attendance_appeals a
    WHERE a.attendance_record_id = attendance_records.attendance_record_id
  );

INSERT INTO notifications (sender_user_id, title, body)
SELECT @manager_id, seed.title, seed.body
FROM (
  SELECT 'Welcome to eduSSAFY' AS title, 'Dashboard notification seed.' AS body
  UNION ALL SELECT 'Weekly survey is open', 'Survey list notification seed.'
  UNION ALL SELECT 'Quest deadline reminder', 'Quest list notification seed.'
) seed
WHERE NOT EXISTS (SELECT 1 FROM notifications n WHERE n.title = seed.title);

INSERT IGNORE INTO notification_recipients (notification_id, recipient_user_id, read_at)
SELECT notification_id, @student_id, CASE WHEN title = 'Welcome to eduSSAFY' THEN CURRENT_TIMESTAMP ELSE NULL END
FROM notifications
WHERE title IN ('Welcome to eduSSAFY', 'Weekly survey is open', 'Quest deadline reminder');

INSERT INTO curriculum_schedules (term_id, content_scope_id, week_no, class_date, start_time, end_time, curriculum_type_code, topic, instructor_name, classroom)
SELECT @term_id, @class_scope_id, 4, '2026-04-24', '09:00:00', '18:00:00', 'lecture', 'Spring Boot REST API', 'Demo Instructor', 'Seoul 1'
WHERE NOT EXISTS (
  SELECT 1 FROM curriculum_schedules
  WHERE content_scope_id = @class_scope_id
    AND class_date = '2026-04-24'
    AND topic = 'Spring Boot REST API'
);

INSERT INTO curriculum_schedules (term_id, content_scope_id, week_no, class_date, start_time, end_time, curriculum_type_code, topic, instructor_name, classroom)
SELECT @term_id, @class_scope_id, 4, '2026-04-20', '09:00:00', '12:00:00', 'lecture', 'Java Collections Review', 'Demo Instructor', 'Seoul 1'
WHERE NOT EXISTS (
  SELECT 1 FROM curriculum_schedules
  WHERE content_scope_id = @class_scope_id
    AND class_date = '2026-04-20'
    AND topic = 'Java Collections Review'
);

INSERT INTO curriculum_schedules (term_id, content_scope_id, week_no, class_date, start_time, end_time, curriculum_type_code, topic, instructor_name, classroom)
SELECT @term_id, @class_scope_id, 4, '2026-04-22', '13:00:00', '18:00:00', 'practice', 'MySQL Schema Practice', 'Demo Instructor', 'Seoul Lab'
WHERE NOT EXISTS (
  SELECT 1 FROM curriculum_schedules
  WHERE content_scope_id = @class_scope_id
    AND class_date = '2026-04-22'
    AND topic = 'MySQL Schema Practice'
);

INSERT INTO curriculum_schedules (term_id, content_scope_id, week_no, class_date, start_time, end_time, curriculum_type_code, topic, instructor_name, classroom)
SELECT @term_id, @class_scope_id, 5, '2026-04-26', '10:00:00', '12:00:00', 'project', '주차별 프로젝트 코칭', 'Demo Coach', 'Seoul 1'
WHERE NOT EXISTS (
  SELECT 1 FROM curriculum_schedules
  WHERE content_scope_id = @class_scope_id
    AND class_date = '2026-04-26'
    AND topic = '주차별 프로젝트 코칭'
);

SET @schedule_id := (
  SELECT curriculum_schedule_id
  FROM curriculum_schedules
  WHERE content_scope_id = @class_scope_id
    AND class_date = '2026-04-24'
    AND topic = 'Spring Boot REST API'
  LIMIT 1
);

INSERT INTO lecture_replays (curriculum_schedule_id, content_external_id, replay_group_key, title, version_no, published_at)
VALUES (@schedule_id, 'replay-priority1-rest-api', 'rest-api', 'Spring Boot REST API Replay', 1, '2026-04-24 18:30:00')
ON DUPLICATE KEY UPDATE title = VALUES(title), published_at = VALUES(published_at);

SET @practice_schedule_id := (
  SELECT curriculum_schedule_id
  FROM curriculum_schedules
  WHERE content_scope_id = @class_scope_id
    AND class_date = '2026-04-22'
    AND topic = 'MySQL Schema Practice'
  LIMIT 1
);

INSERT INTO lecture_replays (curriculum_schedule_id, content_external_id, replay_group_key, title, version_no, published_at)
VALUES (@practice_schedule_id, 'replay-priority2-schema-practice', 'schema-practice', 'MySQL Schema Practice Replay', 1, '2026-04-22 18:30:00')
ON DUPLICATE KEY UPDATE title = VALUES(title), published_at = VALUES(published_at);

SET @rest_replay_id := (
  SELECT lecture_replay_id
  FROM lecture_replays
  WHERE content_external_id = 'replay-priority1-rest-api'
  LIMIT 1
);

INSERT INTO lecture_replay_watch_logs (lecture_replay_id, user_id, watched_at)
SELECT @rest_replay_id, @student_id, '2026-04-25 10:00:00'
WHERE NOT EXISTS (
  SELECT 1
  FROM lecture_replay_watch_logs
  WHERE lecture_replay_id = @rest_replay_id
    AND user_id = @student_id
);

INSERT INTO learning_materials (curriculum_schedule_id, content_scope_id, content_external_id, category_parent, category_child, material_type_code, title, summary, detail_url, view_count)
VALUES
  (@schedule_id, @class_scope_id, 'material-priority1-rest-api-doc', 'Backend', 'REST API', 'document', 'REST API Workbook', 'Seed document for learning materials list.', '/materials/rest-api-workbook.pdf', 12),
  (@schedule_id, @class_scope_id, 'material-priority1-rest-api-video', 'Backend', 'REST API', 'video', 'REST API Walkthrough', 'Seed video for type filter checks.', '/materials/rest-api-video', 7),
  (NULL, @class_scope_id, 'material-priority1-community-link', 'Community', 'Guide', 'link', 'Community Board Guide', 'Seed link for keyword checks.', '/materials/community-board-guide', 3)
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  summary = VALUES(summary),
  detail_url = VALUES(detail_url),
  view_count = VALUES(view_count);

INSERT IGNORE INTO learning_material_resources (learning_material_id, resource_type_code, resource_title, launch_mode_code, target_url, display_order)
SELECT learning_material_id, 'url', title, 'new_tab', detail_url, 1
FROM learning_materials
WHERE content_external_id IN (
  'material-priority1-rest-api-doc',
  'material-priority1-rest-api-video',
  'material-priority1-community-link'
);

INSERT INTO elearning_courses (content_scope_id, course_external_id, title, category, thumbnail_url, provider, description, total_lessons, total_duration_seconds, active_yn)
VALUES
  (@class_scope_id, 'elearning-java-oop', 'Java 객체지향 이러닝', 'Java', '/assets/elearning/java-oop.png', 'SSAFY e-Learning', '객체지향 핵심 개념을 복습하는 온라인 과정입니다.', 6, 14400, TRUE),
  (@class_scope_id, 'elearning-spring-rest', 'Spring REST API 이러닝', 'Backend', '/assets/elearning/spring-rest.png', 'SSAFY e-Learning', 'Spring Boot 기반 REST API 설계를 실습합니다.', 5, 10800, TRUE)
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  category = VALUES(category),
  thumbnail_url = VALUES(thumbnail_url),
  provider = VALUES(provider),
  description = VALUES(description),
  total_lessons = VALUES(total_lessons),
  total_duration_seconds = VALUES(total_duration_seconds),
  active_yn = VALUES(active_yn);

SET @oop_course_id := (SELECT elearning_course_id FROM elearning_courses WHERE course_external_id = 'elearning-java-oop' LIMIT 1);
SET @rest_course_id := (SELECT elearning_course_id FROM elearning_courses WHERE course_external_id = 'elearning-spring-rest' LIMIT 1);

INSERT INTO elearning_lessons (elearning_course_id, lesson_no, title, duration_seconds)
SELECT @oop_course_id, seed.lesson_no, seed.title, seed.duration_seconds
FROM (
  SELECT 1 AS lesson_no, '클래스와 객체', 2400 AS duration_seconds
  UNION ALL SELECT 2, '상속과 다형성', 2400
  UNION ALL SELECT 3, '인터페이스 설계', 2400
  UNION ALL SELECT 4, '예외 처리 전략', 2400
  UNION ALL SELECT 5, '컬렉션 활용', 2400
  UNION ALL SELECT 6, '객체지향 리팩터링', 2400
) seed
WHERE NOT EXISTS (
  SELECT 1
  FROM elearning_lessons el
  WHERE el.elearning_course_id = @oop_course_id
    AND el.lesson_no = seed.lesson_no
);

INSERT INTO elearning_lessons (elearning_course_id, lesson_no, title, duration_seconds)
SELECT @rest_course_id, seed.lesson_no, seed.title, seed.duration_seconds
FROM (
  SELECT 1 AS lesson_no, 'REST 컨트롤러 설계', 2160 AS duration_seconds
  UNION ALL SELECT 2, 'DTO와 Validation', 2160
  UNION ALL SELECT 3, 'Service/Repository 분리', 2160
  UNION ALL SELECT 4, '예외 응답 표준화', 2160
  UNION ALL SELECT 5, 'API 테스트 자동화', 2160
) seed
WHERE NOT EXISTS (
  SELECT 1
  FROM elearning_lessons el
  WHERE el.elearning_course_id = @rest_course_id
    AND el.lesson_no = seed.lesson_no
);

INSERT INTO learner_elearning_progress (user_id, elearning_course_id, progress_percent, completed_lessons, last_lesson_title, last_learning_at, status_code, resume_url)
VALUES
  (@student_id, @oop_course_id, 50, 3, '인터페이스 설계', '2026-04-25 10:15:00', 'in_progress', CONCAT('/mycampus/elearning/', @oop_course_id)),
  (@student_id, @rest_course_id, 100, 5, 'API 테스트 자동화', '2026-04-24 16:40:00', 'completed', CONCAT('/mycampus/elearning/', @rest_course_id))
ON DUPLICATE KEY UPDATE
  progress_percent = VALUES(progress_percent),
  completed_lessons = VALUES(completed_lessons),
  last_lesson_title = VALUES(last_lesson_title),
  last_learning_at = VALUES(last_learning_at),
  status_code = VALUES(status_code),
  resume_url = VALUES(resume_url);

INSERT INTO learner_elearning_lesson_progress (user_id, elearning_lesson_id, completed_at)
SELECT @student_id, el.elearning_lesson_id, TIMESTAMPADD(HOUR, el.lesson_no, '2026-04-25 09:00:00')
FROM elearning_lessons el
WHERE el.elearning_course_id = @oop_course_id
  AND el.lesson_no <= 3
ON DUPLICATE KEY UPDATE completed_at = VALUES(completed_at);

INSERT INTO learner_elearning_lesson_progress (user_id, elearning_lesson_id, completed_at)
SELECT @student_id, el.elearning_lesson_id, TIMESTAMPADD(HOUR, el.lesson_no, '2026-04-24 10:00:00')
FROM elearning_lessons el
WHERE el.elearning_course_id = @rest_course_id
ON DUPLICATE KEY UPDATE completed_at = VALUES(completed_at);

INSERT INTO learner_bookmarks (user_id, target_type_code, target_id, title_snapshot, description_snapshot, thumbnail_url, target_url)
SELECT @student_id, 'material', lm.learning_material_id, lm.title, lm.summary, NULL, CONCAT('/learning/materials/', lm.learning_material_id)
FROM learning_materials lm
WHERE lm.content_external_id = 'material-priority1-rest-api-doc'
ON DUPLICATE KEY UPDATE
  title_snapshot = VALUES(title_snapshot),
  description_snapshot = VALUES(description_snapshot),
  target_url = VALUES(target_url);

INSERT INTO learner_bookmarks (user_id, target_type_code, target_id, title_snapshot, description_snapshot, thumbnail_url, target_url)
SELECT @student_id, 'elearning', ec.elearning_course_id, ec.title, ec.description, ec.thumbnail_url, CONCAT('/mycampus/elearning/', ec.elearning_course_id)
FROM elearning_courses ec
WHERE ec.course_external_id = 'elearning-java-oop'
ON DUPLICATE KEY UPDATE
  title_snapshot = VALUES(title_snapshot),
  description_snapshot = VALUES(description_snapshot),
  thumbnail_url = VALUES(thumbnail_url),
  target_url = VALUES(target_url);

INSERT INTO document_requests (title, description, category, required_yn, allowed_extensions, max_file_size_bytes, starts_at, due_at, active_yn, created_by)
VALUES
  ('신분증 사본 제출', '본인 확인을 위한 신분증 사본을 PDF 또는 이미지 파일로 제출합니다.', 'identity', TRUE, '.pdf,.jpg,.jpeg,.png', 2097152, '2026-04-01 09:00:00', '2026-05-10 18:00:00', TRUE, @manager_id),
  ('통장 사본 제출', '장학 포인트 정산 계좌 확인용 통장 사본을 제출합니다.', 'scholarship', FALSE, '.pdf,.jpg,.jpeg,.png', 2097152, '2026-04-01 09:00:00', '2026-05-17 18:00:00', TRUE, @manager_id)
ON DUPLICATE KEY UPDATE
  description = VALUES(description),
  category = VALUES(category),
  required_yn = VALUES(required_yn),
  allowed_extensions = VALUES(allowed_extensions),
  max_file_size_bytes = VALUES(max_file_size_bytes),
  starts_at = VALUES(starts_at),
  due_at = VALUES(due_at),
  active_yn = VALUES(active_yn);

SET @identity_doc_id := (SELECT document_request_id FROM document_requests WHERE title = '신분증 사본 제출' LIMIT 1);

INSERT INTO learner_document_submissions (document_request_id, user_id, status_code, submitted_at)
VALUES (@identity_doc_id, @student_id, 'submitted', '2026-04-25 14:30:00')
ON DUPLICATE KEY UPDATE
  status_code = VALUES(status_code),
  submitted_at = VALUES(submitted_at),
  reviewed_at = NULL,
  review_comment = NULL;

SET @identity_submission_id := (
  SELECT document_submission_id
  FROM learner_document_submissions
  WHERE document_request_id = @identity_doc_id
    AND user_id = @student_id
  LIMIT 1
);

INSERT INTO attachments (original_filename, storage_key, stored_path, mime_type, file_size, checksum_sha256)
VALUES ('identity-sample.pdf', 'seed/documents/identity-sample.pdf', '/seed/documents/identity-sample.pdf', 'application/pdf', 1024, SHA2('identity-sample.pdf', 256))
ON DUPLICATE KEY UPDATE original_filename = VALUES(original_filename);

SET @identity_attachment_id := (SELECT attachment_id FROM attachments WHERE checksum_sha256 = SHA2('identity-sample.pdf', 256) LIMIT 1);

INSERT IGNORE INTO learner_document_attachments (learner_document_submission_id, attachment_id)
VALUES (@identity_submission_id, @identity_attachment_id);

INSERT INTO pledge_documents (title, content, version, required_yn, starts_at, due_at, active_yn)
VALUES
  ('교육생 기본 서약서', 'SSAFY 교육과정의 학습 규칙, 보안 수칙, 출결 정책을 준수합니다.', '2026.1', TRUE, '2026-04-01 09:00:00', '2026-05-10 18:00:00', TRUE),
  ('개인정보 이용 동의서', '교육 운영과 학습 이력 관리를 위한 개인정보 이용 항목을 확인했습니다.', '2026.1', TRUE, '2026-04-01 09:00:00', '2026-05-10 18:00:00', TRUE)
ON DUPLICATE KEY UPDATE
  content = VALUES(content),
  required_yn = VALUES(required_yn),
  starts_at = VALUES(starts_at),
  due_at = VALUES(due_at),
  active_yn = VALUES(active_yn);

SET @pledge_id := (
  SELECT pledge_document_id
  FROM pledge_documents
  WHERE title = '교육생 기본 서약서'
    AND version = '2026.1'
  LIMIT 1
);

INSERT INTO learner_pledge_agreements (
  pledge_document_id,
  user_id,
  agreed_yn,
  agreed_at,
  agreement_ip_hash,
  user_agent_hash,
  version_snapshot
)
VALUES (
  @pledge_id,
  @student_id,
  TRUE,
  '2026-04-25 15:00:00',
  SHA2('seed-ip', 256),
  SHA2('seed-user-agent', 256),
  '2026.1'
)
ON DUPLICATE KEY UPDATE
  agreed_yn = VALUES(agreed_yn),
  agreed_at = VALUES(agreed_at),
  version_snapshot = VALUES(version_snapshot);

INSERT INTO quest_evaluations (content_scope_id, external_task_id, quest_type_code, task_classification_code, title, start_at, end_at, max_exp, progress_status_code)
VALUES
  (@class_scope_id, 'quest-priority1-board-api', 'assignment', 'required', 'Implement board API', '2026-04-24 09:00:00', '2026-04-25 18:00:00', 100, 'in_progress'),
  (@class_scope_id, 'quest-priority1-dashboard-smoke', 'challenge', 'optional', 'Dashboard smoke check', '2026-04-24 09:00:00', '2026-04-24 23:59:00', 50, 'completed')
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  progress_status_code = VALUES(progress_status_code),
  end_at = VALUES(end_at);

INSERT INTO quest_submissions (quest_evaluation_id, user_id, result_status_code, score, submit_status_code, submitted_at, graded_at)
SELECT quest_evaluation_id, @student_id, 'pending', NULL, 'submitted', '2026-04-24 17:30:00', NULL
FROM quest_evaluations
WHERE external_task_id = 'quest-priority1-board-api'
ON DUPLICATE KEY UPDATE submit_status_code = VALUES(submit_status_code), submitted_at = VALUES(submitted_at);

INSERT INTO surveys (content_scope_id, title, survey_category_code, required_yn, progress_status_code, start_at, end_at)
SELECT @class_scope_id, 'Weekly satisfaction survey', 'satisfaction', TRUE, 'in_progress', '2026-04-24 09:00:00', '2026-04-26 18:00:00'
WHERE NOT EXISTS (SELECT 1 FROM surveys WHERE title = 'Weekly satisfaction survey');

SET @survey_id := (SELECT survey_id FROM surveys WHERE title = 'Weekly satisfaction survey' LIMIT 1);

INSERT INTO survey_questions (survey_id, question_type_code, question_text, display_order)
SELECT @survey_id, 'single_choice', 'How was this week?', 1
WHERE NOT EXISTS (
  SELECT 1 FROM survey_questions
  WHERE survey_id = @survey_id
    AND display_order = 1
);

SET @survey_question_id := (
  SELECT survey_question_id
  FROM survey_questions
  WHERE survey_id = @survey_id
    AND display_order = 1
  LIMIT 1
);

INSERT INTO survey_options (survey_question_id, option_text, display_order)
SELECT @survey_question_id, seed.option_text, seed.display_order
FROM (
  SELECT 'Good' AS option_text, 1 AS display_order
  UNION ALL SELECT 'Needs support', 2
) seed
WHERE NOT EXISTS (
  SELECT 1 FROM survey_options
  WHERE survey_question_id = @survey_question_id
    AND display_order = seed.display_order
);

SET @survey_option_id := (
  SELECT survey_option_id
  FROM survey_options
  WHERE survey_question_id = @survey_question_id
    AND display_order = 1
  LIMIT 1
);

INSERT INTO survey_responses (survey_id, user_id, completed_yn, responded_at)
VALUES (@survey_id, @student_id, TRUE, '2026-04-24 17:45:00')
ON DUPLICATE KEY UPDATE
  completed_yn = VALUES(completed_yn),
  responded_at = VALUES(responded_at);

SET @survey_response_id := (
  SELECT survey_response_id
  FROM survey_responses
  WHERE survey_id = @survey_id
    AND user_id = @student_id
  LIMIT 1
);

INSERT INTO survey_response_answers (survey_response_id, survey_id, survey_question_id, answer_text)
SELECT @survey_response_id, @survey_id, @survey_question_id, 'Seed survey response for submit smoke checks.'
WHERE NOT EXISTS (
  SELECT 1 FROM survey_response_answers
  WHERE survey_response_id = @survey_response_id
    AND survey_question_id = @survey_question_id
);

SET @survey_answer_id := (
  SELECT survey_response_answer_id
  FROM survey_response_answers
  WHERE survey_response_id = @survey_response_id
    AND survey_question_id = @survey_question_id
  LIMIT 1
);

INSERT IGNORE INTO survey_response_answer_options (survey_response_answer_id, survey_question_id, survey_option_id)
VALUES (@survey_answer_id, @survey_question_id, @survey_option_id);

INSERT INTO support_tickets (requester_user_id, title, status_code)
SELECT @student_id, 'Password check help', 'open'
WHERE NOT EXISTS (
  SELECT 1 FROM support_tickets
  WHERE requester_user_id = @student_id
    AND title = 'Password check help'
);

SET @support_ticket_id := (
  SELECT support_ticket_id
  FROM support_tickets
  WHERE requester_user_id = @student_id
    AND title = 'Password check help'
  LIMIT 1
);

INSERT INTO support_ticket_messages (support_ticket_id, sender_user_id, message_type_code, content)
SELECT @support_ticket_id, @student_id, 'user_message', 'Seed QNA support ticket message.'
WHERE NOT EXISTS (
  SELECT 1 FROM support_ticket_messages
  WHERE support_ticket_id = @support_ticket_id
);

INSERT INTO boards (board_code, board_name, board_group_code, access_scope_code)
VALUES
  ('notice', 'Notice', 'notice', 'public'),
  ('free', 'Free Board', 'community', 'authenticated'),
  ('anonymous', 'Anonymous Board', 'community', 'authenticated'),
  ('faq', 'FAQ', 'faq', 'public'),
  ('qna', 'Q&A', 'qna', 'authenticated')
ON DUPLICATE KEY UPDATE
  board_name = VALUES(board_name),
  board_group_code = VALUES(board_group_code),
  access_scope_code = VALUES(access_scope_code);

INSERT INTO board_categories (board_id, category_name, sort_order)
SELECT b.board_id, seed.category_name, seed.sort_order
FROM boards b
JOIN (
  SELECT 'notice' AS board_code, 'General' AS category_name, 1 AS sort_order
  UNION ALL
  SELECT 'free', 'General', 1
  UNION ALL
  SELECT 'free', 'Study', 2
  UNION ALL
  SELECT 'anonymous', 'General', 1
  UNION ALL
  SELECT 'faq', 'General', 1
  UNION ALL
  SELECT 'qna', 'General', 1
) seed ON seed.board_code = b.board_code
ON DUPLICATE KEY UPDATE
  sort_order = VALUES(sort_order);

INSERT INTO board_posts (board_id, board_category_id, author_user_id, title, content, notice_yn, view_count)
SELECT
  b.board_id,
  c.board_category_id,
  @student_id,
  seed.title,
  seed.content,
  seed.notice_yn,
  seed.view_count
FROM boards b
JOIN (
  SELECT 'notice' AS board_code, 'General' AS category_name, 'Welcome notice' AS title, 'Initial notice board seed post.' AS content, TRUE AS notice_yn, 10 AS view_count
  UNION ALL
  SELECT 'free', 'General', 'First free-board post', 'Initial free board seed post.', FALSE, 8
  UNION ALL
  SELECT 'free', 'Study', 'REST API study notes', 'Searchable board seed for keyword checks.', FALSE, 5
  UNION ALL
  SELECT 'anonymous', 'General', '익명 학습 고민 공유', '작성자는 DB에 저장되지만 API 응답에는 익명으로 표시되는 게시글입니다.', FALSE, 4
  UNION ALL
  SELECT 'faq', 'General', 'How do I open learning materials?', 'Use the learning materials menu.', TRUE, 3
  UNION ALL
  SELECT 'qna', 'General', 'Attendance appeal question', 'Seed Q&A post for common board API coverage.', FALSE, 2
) seed ON seed.board_code = b.board_code
JOIN board_categories c
  ON c.board_id = b.board_id
 AND c.category_name = seed.category_name
WHERE NOT EXISTS (
  SELECT 1
  FROM board_posts existing
  WHERE existing.board_id = b.board_id
    AND existing.title = seed.title
);

INSERT INTO attachments (original_filename, storage_key, stored_path, mime_type, file_size, checksum_sha256)
VALUES ('rest-api-study.pdf', 'seed/rest-api-study.pdf', '/seed/rest-api-study.pdf', 'application/pdf', 1024, SHA2('rest-api-study.pdf', 256))
ON DUPLICATE KEY UPDATE original_filename = VALUES(original_filename);

SET @free_post_id := (SELECT board_post_id FROM board_posts WHERE title = 'REST API study notes' LIMIT 1);
SET @anonymous_post_id := (SELECT board_post_id FROM board_posts WHERE title = '익명 학습 고민 공유' LIMIT 1);
SET @attachment_id := (SELECT attachment_id FROM attachments WHERE checksum_sha256 = SHA2('rest-api-study.pdf', 256) LIMIT 1);

INSERT IGNORE INTO board_post_attachments (board_post_id, attachment_id)
VALUES (@free_post_id, @attachment_id);

INSERT INTO board_comments (board_post_id, author_user_id, content)
SELECT @free_post_id, @manager_id, 'Seed comment for board list counts.'
WHERE NOT EXISTS (SELECT 1 FROM board_comments WHERE board_post_id = @free_post_id);

INSERT INTO board_comments (board_post_id, author_user_id, content)
SELECT @anonymous_post_id, @classmate_id, '익명 댓글도 작성자 식별자 없이 표시됩니다.'
WHERE NOT EXISTS (SELECT 1 FROM board_comments WHERE board_post_id = @anonymous_post_id);

INSERT IGNORE INTO board_post_reactions (board_post_id, user_id, reaction_type_code)
VALUES
  (@free_post_id, @student_id, 'bookmark'),
  (@free_post_id, @manager_id, 'like'),
  (@anonymous_post_id, @student_id, 'like');

-- SSAFY e-book demo data for My Campus e-book flow.
INSERT INTO ssafy_ebooks (title, description, thumbnail_url, category, external_url, active_yn) VALUES
  ('SSAFY Java e-book', 'Java 트랙 교육생을 위한 핵심 문법 e-book입니다.', '/assets/ebooks/java.png', 'Java', 'https://edu.ssafy.local/ebooks/java', TRUE),
  ('SSAFY 알고리즘 e-book', '알고리즘 문제 해결 전략과 예제 풀이를 제공합니다.', '/assets/ebooks/algorithm.png', 'Algorithm', 'https://edu.ssafy.local/ebooks/algorithm', TRUE),
  ('비공개 e-book', '비활성 e-book은 API 목록에서 제외되어야 합니다.', NULL, 'Archive', 'https://edu.ssafy.local/ebooks/private', FALSE);

INSERT INTO ssafy_ebook_access_logs (ebook_id, user_id, accessed_at)
SELECT e.ebook_id, 1, CURRENT_TIMESTAMP - INTERVAL 1 DAY
FROM ssafy_ebooks e
WHERE e.title = 'SSAFY Java e-book';

-- Required study demo data for classroom mandatory learning flow.
INSERT INTO required_studies (
  title,
  description,
  category,
  required_for_track,
  due_at,
  content_type,
  content_url,
  active_yn
)
SELECT seed.title, seed.description, seed.category, seed.required_for_track, seed.due_at, seed.content_type, seed.content_url, seed.active_yn
FROM (
  SELECT 'Java 보안 필수학습' AS title,
         '웹 애플리케이션 보안 기본 원칙과 SSAFY 프로젝트 보안 체크리스트를 학습합니다.' AS description,
         'Security' AS category,
         'Java' AS required_for_track,
         CURRENT_TIMESTAMP + INTERVAL 7 DAY AS due_at,
         'url' AS content_type,
         'https://edu.ssafy.local/required-studies/java-security' AS content_url,
         TRUE AS active_yn
  UNION ALL
  SELECT '프로젝트 협업 필수학습',
         '팀 프로젝트를 시작하기 전 Git 브랜치 전략과 코드 리뷰 규칙을 확인합니다.',
         'Collaboration',
         NULL,
         CURRENT_TIMESTAMP - INTERVAL 1 DAY,
         'url',
         'https://edu.ssafy.local/required-studies/collaboration',
         TRUE
  UNION ALL
  SELECT '비활성 필수학습',
         '비활성 필수학습은 API 목록에서 제외되어야 합니다.',
         'Archive',
         'Java',
         CURRENT_TIMESTAMP + INTERVAL 30 DAY,
         'url',
         'https://edu.ssafy.local/required-studies/private',
         FALSE
) seed
WHERE NOT EXISTS (
  SELECT 1
  FROM required_studies rs
  WHERE rs.title = seed.title
);

SET @java_security_study_id := (
  SELECT required_study_id
  FROM required_studies
  WHERE title = 'Java 보안 필수학습'
  LIMIT 1
);
SET @collaboration_study_id := (
  SELECT required_study_id
  FROM required_studies
  WHERE title = '프로젝트 협업 필수학습'
  LIMIT 1
);

INSERT INTO learner_required_study_progress (user_id, required_study_id, status_code, progress_percent)
VALUES
  (@student_id, @java_security_study_id, 'in_progress', 40)
ON DUPLICATE KEY UPDATE
  status_code = VALUES(status_code),
  progress_percent = VALUES(progress_percent);

INSERT INTO learner_required_study_progress (user_id, required_study_id, status_code, progress_percent, completed_at)
VALUES
  (@classmate_id, @collaboration_study_id, 'completed', 100, CURRENT_TIMESTAMP - INTERVAL 2 DAY)
ON DUPLICATE KEY UPDATE
  status_code = VALUES(status_code),
  progress_percent = VALUES(progress_percent),
  completed_at = VALUES(completed_at);

-- Live session demo data for classroom live shortcut flow.
INSERT INTO live_sessions (title, track, cohort, class_room, starts_at, ends_at, join_url, active_yn)
SELECT seed.title, seed.track, seed.cohort, seed.class_room, seed.starts_at, seed.ends_at, seed.join_url, seed.active_yn
FROM (
  SELECT 'Java 라이브 알고리즘 코칭' AS title,
         'Java' AS track,
         '12th' AS cohort,
         'Seoul Java 1' AS class_room,
         CURRENT_TIMESTAMP - INTERVAL 30 MINUTE AS starts_at,
         CURRENT_TIMESTAMP + INTERVAL 90 MINUTE AS ends_at,
         'https://edu.ssafy.local/live/java-algorithm' AS join_url,
         TRUE AS active_yn
  UNION ALL
  SELECT 'Spring 프로젝트 라이브 Q&A',
         'Java',
         '12th',
         'Seoul Java 1',
         CURRENT_TIMESTAMP + INTERVAL 3 HOUR,
         CURRENT_TIMESTAMP + INTERVAL 5 HOUR,
         'https://edu.ssafy.local/live/spring-qna',
         TRUE
  UNION ALL
  SELECT '비활성 라이브',
         'Java',
         '12th',
         'Seoul Java 1',
         CURRENT_TIMESTAMP,
         CURRENT_TIMESTAMP + INTERVAL 1 HOUR,
         'https://edu.ssafy.local/live/private',
         FALSE
) seed
WHERE NOT EXISTS (
  SELECT 1
  FROM live_sessions ls
  WHERE ls.title = seed.title
);

SET @live_algorithm_id := (
  SELECT live_session_id
  FROM live_sessions
  WHERE title = 'Java 라이브 알고리즘 코칭'
  LIMIT 1
);

INSERT INTO live_session_join_logs (live_session_id, user_id, joined_at)
SELECT @live_algorithm_id, @student_id, CURRENT_TIMESTAMP - INTERVAL 10 MINUTE
WHERE NOT EXISTS (
  SELECT 1
  FROM live_session_join_logs
  WHERE live_session_id = @live_algorithm_id
    AND user_id = @student_id
);
