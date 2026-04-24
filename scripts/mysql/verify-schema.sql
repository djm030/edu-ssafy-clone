SELECT 'mysql_version' AS check_name, VERSION() AS result;

SELECT 'table_count' AS check_name, COUNT(*) AS result
FROM information_schema.tables
WHERE table_schema = DATABASE();

SELECT 'required_board_tables' AS check_name, COUNT(*) AS result
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN (
    'boards',
    'board_categories',
    'board_posts',
    'board_post_attachments',
    'board_comments',
    'board_post_reactions'
  );

SELECT 'board_group_codes' AS check_name, COUNT(*) AS result
FROM codes
WHERE code_group = 'BOARD_GROUP'
  AND code IN ('notice', 'community', 'faq', 'qna');

SELECT 'access_scope_codes' AS check_name, COUNT(*) AS result
FROM codes
WHERE code_group = 'ACCESS_SCOPE'
  AND code IN ('public', 'authenticated');

SELECT 'invalid_notices_table' AS check_name, COUNT(*) AS result
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN ('notices', 'notice_posts', 'notice_categories');

SELECT 'board_posts_columns' AS check_name, COUNT(*) AS result
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name = 'board_posts'
  AND column_name IN (
    'board_post_id',
    'board_id',
    'board_category_id',
    'author_user_id',
    'title',
    'content',
    'view_count',
    'notice_yn',
    'created_at'
  );

SELECT 'seeded_priority1_user' AS check_name, COUNT(*) AS result
FROM users
WHERE email = 'student@ssafy.com'
  AND role_code = 'student';

SELECT 'seeded_profile_password_read_path' AS check_name, COUNT(*) AS result
FROM users u
JOIN user_profiles p ON p.user_id = u.user_id
WHERE u.email = 'student@ssafy.com'
  AND u.password_hash IS NOT NULL
  AND u.status_code = 'active';

SELECT 'seeded_classmates' AS check_name, COUNT(*) AS result
FROM user_class_enrollments student_enrollment
JOIN user_class_enrollments classmates
  ON classmates.class_group_id = student_enrollment.class_group_id
JOIN users u ON u.user_id = classmates.user_id
WHERE student_enrollment.user_id = (SELECT user_id FROM users WHERE email = 'student@ssafy.com')
  AND u.email IN ('student@ssafy.com', 'classmate@ssafy.com');

SELECT 'seeded_dashboard_level' AS check_name, COUNT(*) AS result
FROM user_level_statuses
WHERE user_id = (SELECT user_id FROM users WHERE email = 'student@ssafy.com')
  AND level_no = 5;

SELECT 'seeded_dashboard_rank' AS check_name, COUNT(*) AS result
FROM user_rank_snapshots
WHERE user_id = (SELECT user_id FROM users WHERE email = 'student@ssafy.com')
  AND snapshot_date = '2026-04-24'
  AND rank_no = 12;

SELECT 'seeded_attendance_records' AS check_name, COUNT(*) AS result
FROM attendance_records
WHERE user_id = (SELECT user_id FROM users WHERE email = 'student@ssafy.com');

SELECT 'seeded_attendance_appeals' AS check_name, COUNT(*) AS result
FROM attendance_appeals a
JOIN attendance_records r ON r.attendance_record_id = a.attendance_record_id
JOIN users u ON u.user_id = r.user_id
WHERE u.email = 'student@ssafy.com';

SELECT 'seeded_attendance_appeal_submit_ready' AS check_name, COUNT(*) AS result
FROM attendance_records r
JOIN users u ON u.user_id = r.user_id
WHERE u.email = 'student@ssafy.com'
  AND r.attendance_status_code = 'late';

SELECT 'seeded_notifications' AS check_name, COUNT(*) AS result
FROM notification_recipients nr
JOIN users u ON u.user_id = nr.recipient_user_id
WHERE u.email = 'student@ssafy.com'
  AND nr.deleted_at IS NULL;

SELECT 'seeded_curriculum_today' AS check_name, COUNT(*) AS result
FROM curriculum_schedules
WHERE class_date = '2026-04-24'
  AND topic = 'Spring Boot REST API';

SELECT 'seeded_weekly_curriculum' AS check_name, COUNT(*) AS result
FROM curriculum_schedules
WHERE week_no = 4
  AND class_date BETWEEN '2026-04-20' AND '2026-04-24';

SELECT 'seeded_lecture_replays' AS check_name, COUNT(*) AS result
FROM lecture_replays
WHERE content_external_id IN (
  'replay-priority1-rest-api',
  'replay-priority2-schema-practice'
);

SELECT 'seeded_learning_materials' AS check_name, COUNT(*) AS result
FROM learning_materials
WHERE content_external_id IN (
  'material-priority1-rest-api-doc',
  'material-priority1-rest-api-video',
  'material-priority1-community-link'
);

SELECT 'seeded_learning_material_detail_resources' AS check_name, COUNT(*) AS result
FROM learning_materials lm
JOIN learning_material_resources r ON r.learning_material_id = lm.learning_material_id
WHERE lm.content_external_id IN (
  'material-priority1-rest-api-doc',
  'material-priority1-rest-api-video',
  'material-priority1-community-link'
)
  AND lm.summary IS NOT NULL
  AND lm.detail_url IS NOT NULL
  AND r.target_url IS NOT NULL;

SELECT 'seeded_quests' AS check_name, COUNT(*) AS result
FROM quest_evaluations
WHERE external_task_id IN (
  'quest-priority1-board-api',
  'quest-priority1-dashboard-smoke'
);

SELECT 'seeded_quest_detail_submission' AS check_name, COUNT(*) AS result
FROM quest_evaluations q
LEFT JOIN quest_submissions s ON s.quest_evaluation_id = q.quest_evaluation_id
WHERE q.external_task_id = 'quest-priority1-board-api'
  AND q.start_at IS NOT NULL
  AND q.end_at IS NOT NULL
  AND q.max_exp IS NOT NULL
  AND s.user_id = (SELECT user_id FROM users WHERE email = 'student@ssafy.com');

SELECT 'seeded_surveys' AS check_name, COUNT(*) AS result
FROM surveys
WHERE title = 'Weekly satisfaction survey';

SELECT 'seeded_survey_questions' AS check_name, COUNT(*) AS result
FROM survey_questions q
JOIN surveys s ON s.survey_id = q.survey_id
WHERE s.title = 'Weekly satisfaction survey';

SELECT 'seeded_survey_detail_options' AS check_name, COUNT(*) AS result
FROM surveys s
JOIN survey_questions q ON q.survey_id = s.survey_id
JOIN survey_options o ON o.survey_question_id = q.survey_question_id
WHERE s.title = 'Weekly satisfaction survey'
  AND q.question_text IS NOT NULL;

SELECT 'seeded_survey_response_submission' AS check_name, COUNT(*) AS result
FROM survey_responses r
JOIN survey_response_answers a ON a.survey_response_id = r.survey_response_id
JOIN survey_response_answer_options o ON o.survey_response_answer_id = a.survey_response_answer_id
JOIN surveys s ON s.survey_id = r.survey_id
JOIN users u ON u.user_id = r.user_id
WHERE s.title = 'Weekly satisfaction survey'
  AND u.email = 'student@ssafy.com'
  AND r.completed_yn = TRUE;

SELECT 'seeded_support_tickets' AS check_name, COUNT(*) AS result
FROM support_tickets t
JOIN support_ticket_messages m ON m.support_ticket_id = t.support_ticket_id
JOIN users u ON u.user_id = t.requester_user_id
WHERE u.email = 'student@ssafy.com'
  AND t.title = 'Password check help';

SELECT 'seeded_boards' AS check_name, COUNT(*) AS result
FROM boards
WHERE board_code IN ('notice', 'free', 'faq', 'qna');

SELECT 'seeded_board_categories' AS check_name, COUNT(*) AS result
FROM board_categories c
JOIN boards b ON b.board_id = c.board_id
WHERE b.board_code IN ('notice', 'free', 'faq', 'qna');

SELECT 'seeded_board_posts' AS check_name, COUNT(*) AS result
FROM board_posts p
JOIN boards b ON b.board_id = p.board_id
WHERE b.board_code IN ('notice', 'free', 'faq', 'qna');

SELECT 'seeded_board_detail_posts' AS check_name, COUNT(DISTINCT b.board_code) AS result
FROM board_posts p
JOIN boards b ON b.board_id = p.board_id
WHERE b.board_code IN ('notice', 'free', 'faq', 'qna')
  AND p.content IS NOT NULL
  AND p.author_user_id IS NOT NULL;

SELECT 'seeded_board_write_relations' AS check_name, COUNT(*) AS result
FROM board_posts p
JOIN board_comments c ON c.board_post_id = p.board_post_id
JOIN board_post_reactions r ON r.board_post_id = p.board_post_id
WHERE p.title = 'REST API study notes';

SELECT 'seeded_board_engagement' AS check_name, COUNT(*) AS result
FROM board_posts p
LEFT JOIN board_comments c ON c.board_post_id = p.board_post_id
LEFT JOIN board_post_reactions r ON r.board_post_id = p.board_post_id
LEFT JOIN board_post_attachments a ON a.board_post_id = p.board_post_id
WHERE p.title = 'REST API study notes'
  AND (c.board_comment_id IS NOT NULL OR r.user_id IS NOT NULL OR a.attachment_id IS NOT NULL);
