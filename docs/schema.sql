-- SSAFY clone coding application schema
-- Source of truth for normalized relational structure
-- Migration style: ordered CREATE TABLE statements without destructive DROP statements
-- Focus: 최소 중복, 무결성 보장, 핵심 비즈니스 요구사항 반영
-- Dialect: MySQL 8.x

SET NAMES utf8mb4;


CREATE TABLE `users` (
  `user_id` BIGINT NOT NULL AUTO_INCREMENT,
  `email` VARCHAR(255) NOT NULL,
  `password_hash` VARCHAR(255) NULL,
  `learner_no` VARCHAR(50) NULL,
  `name` VARCHAR(100) NOT NULL,
  `role_code` VARCHAR(30) NOT NULL DEFAULT 'STUDENT',
  `status_code` VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_users_email` (`email`),
  UNIQUE KEY `uk_users_learner_no` (`learner_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `user_profiles` (
  `user_id` BIGINT NOT NULL,
  `zip_code` VARCHAR(20) NULL,
  `address_line1` VARCHAR(255) NULL,
  `address_line2` VARCHAR(255) NULL,
  `mobile_phone` VARCHAR(30) NULL,
  `emergency_phone` VARCHAR(30) NULL,
  `marketing_opt_in` BOOLEAN NOT NULL DEFAULT FALSE,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  CONSTRAINT `fk_user_profiles_user`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
      ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `campuses` (
  `campus_id` BIGINT NOT NULL AUTO_INCREMENT,
  `campus_name` VARCHAR(100) NOT NULL,
  PRIMARY KEY (`campus_id`),
  UNIQUE KEY `uk_campuses_name` (`campus_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `cohorts` (
  `cohort_id` BIGINT NOT NULL AUTO_INCREMENT,
  `cohort_name` VARCHAR(100) NOT NULL,
  `start_date` DATE NULL,
  `end_date` DATE NULL,
  PRIMARY KEY (`cohort_id`),
  UNIQUE KEY `uk_cohorts_name` (`cohort_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `tracks` (
  `track_id` BIGINT NOT NULL AUTO_INCREMENT,
  `track_name` VARCHAR(100) NOT NULL,
  `domain_type` VARCHAR(100) NULL,
  PRIMARY KEY (`track_id`),
  UNIQUE KEY `uk_tracks_name` (`track_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `class_groups` (
  `class_group_id` BIGINT NOT NULL AUTO_INCREMENT,
  `campus_id` BIGINT NOT NULL,
  `cohort_id` BIGINT NOT NULL,
  `class_name` VARCHAR(100) NOT NULL,
  PRIMARY KEY (`class_group_id`),
  UNIQUE KEY `uk_class_groups_scope` (`campus_id`, `cohort_id`, `class_name`),
  CONSTRAINT `fk_class_groups_campus`
    FOREIGN KEY (`campus_id`) REFERENCES `campuses` (`campus_id`),
  CONSTRAINT `fk_class_groups_cohort`
    FOREIGN KEY (`cohort_id`) REFERENCES `cohorts` (`cohort_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `user_class_enrollments` (
  `user_class_enrollment_id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `class_group_id` BIGINT NOT NULL,
  `member_role_code` VARCHAR(30) NOT NULL DEFAULT 'STUDENT',
  `enrolled_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_class_enrollment_id`),
  UNIQUE KEY `uk_user_class_enrollments_user_class` (`user_id`, `class_group_id`),
  CONSTRAINT `fk_user_class_enrollments_user`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
      ON DELETE CASCADE,
  CONSTRAINT `fk_user_class_enrollments_class_group`
    FOREIGN KEY (`class_group_id`) REFERENCES `class_groups` (`class_group_id`)
      ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `user_track_enrollments` (
  `user_track_enrollment_id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `track_id` BIGINT NOT NULL,
  `cohort_id` BIGINT NOT NULL,
  `enrolled_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_track_enrollment_id`),
  UNIQUE KEY `uk_user_track_enrollments_user_track_cohort` (`user_id`, `track_id`, `cohort_id`),
  CONSTRAINT `fk_user_track_enrollments_user`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
      ON DELETE CASCADE,
  CONSTRAINT `fk_user_track_enrollments_track`
    FOREIGN KEY (`track_id`) REFERENCES `tracks` (`track_id`),
  CONSTRAINT `fk_user_track_enrollments_cohort`
    FOREIGN KEY (`cohort_id`) REFERENCES `cohorts` (`cohort_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `attachments` (
  `attachment_id` BIGINT NOT NULL AUTO_INCREMENT,
  `original_filename` VARCHAR(255) NOT NULL,
  `storage_key` VARCHAR(100) NULL,
  `stored_path` VARCHAR(500) NULL,
  `mime_type` VARCHAR(100) NULL,
  `file_size` BIGINT NULL,
  `checksum_sha256` CHAR(64) NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`attachment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `user_profile_attachments` (
  `user_id` BIGINT NOT NULL,
  `attachment_id` BIGINT NOT NULL,
  `attachment_role_code` VARCHAR(30) NOT NULL DEFAULT 'PROFILE_IMAGE',
  PRIMARY KEY (`user_id`, `attachment_id`),
  UNIQUE KEY `uk_user_profile_attachments_user_role` (`user_id`, `attachment_role_code`),
  UNIQUE KEY `uk_user_profile_attachments_attachment` (`attachment_id`),
  CONSTRAINT `fk_user_profile_attachments_user`
    FOREIGN KEY (`user_id`) REFERENCES `user_profiles` (`user_id`)
      ON DELETE CASCADE,
  CONSTRAINT `fk_user_profile_attachments_attachment`
    FOREIGN KEY (`attachment_id`) REFERENCES `attachments` (`attachment_id`)
      ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `user_level_statuses` (
  `user_id` BIGINT NOT NULL,
  `level_name` VARCHAR(50) NULL,
  `level_no` INT NULL,
  `exp` INT NOT NULL DEFAULT 0,
  `scholarship_point` INT NOT NULL DEFAULT 0,
  `rank_no` INT NULL,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  CONSTRAINT `fk_user_level_statuses_user`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
      ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `attendance_records` (
  `attendance_record_id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `attendance_date` DATE NOT NULL,
  `check_in_at` TIME NULL,
  `check_out_at` TIME NULL,
  `attendance_status_code` VARCHAR(30) NOT NULL,
  `approval_type_code` VARCHAR(30) NULL,
  PRIMARY KEY (`attendance_record_id`),
  UNIQUE KEY `uk_attendance_records_user_date` (`user_id`, `attendance_date`),
  CONSTRAINT `fk_attendance_records_user`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
      ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `attendance_appeals` (
  `attendance_appeal_id` BIGINT NOT NULL AUTO_INCREMENT,
  `attendance_record_id` BIGINT NOT NULL,
  `appeal_type_code` VARCHAR(30) NOT NULL,
  `reason` TEXT NULL,
  `approval_status_code` VARCHAR(30) NOT NULL DEFAULT 'REQUESTED',
  `requested_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `resolved_at` DATETIME NULL,
  PRIMARY KEY (`attendance_appeal_id`),
  CONSTRAINT `fk_attendance_appeals_record`
    FOREIGN KEY (`attendance_record_id`) REFERENCES `attendance_records` (`attendance_record_id`)
      ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `notifications` (
  `notification_id` BIGINT NOT NULL AUTO_INCREMENT,
  `sender_user_id` BIGINT NULL,
  `title` VARCHAR(255) NOT NULL,
  `body` TEXT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`notification_id`),
  CONSTRAINT `fk_notifications_sender`
    FOREIGN KEY (`sender_user_id`) REFERENCES `users` (`user_id`)
      ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `notification_recipients` (
  `notification_recipient_id` BIGINT NOT NULL AUTO_INCREMENT,
  `notification_id` BIGINT NOT NULL,
  `recipient_user_id` BIGINT NOT NULL,
  `read_at` DATETIME NULL,
  `deleted_at` DATETIME NULL,
  PRIMARY KEY (`notification_recipient_id`),
  UNIQUE KEY `uk_notification_recipients_notification_user` (`notification_id`, `recipient_user_id`),
  CONSTRAINT `fk_notification_recipients_notification`
    FOREIGN KEY (`notification_id`) REFERENCES `notifications` (`notification_id`)
      ON DELETE CASCADE,
  CONSTRAINT `fk_notification_recipients_user`
    FOREIGN KEY (`recipient_user_id`) REFERENCES `users` (`user_id`)
      ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `terms` (
  `term_id` BIGINT NOT NULL AUTO_INCREMENT,
  `term_name` VARCHAR(100) NOT NULL,
  `progress_status_code` VARCHAR(30) NOT NULL,
  `start_date` DATE NULL,
  `end_date` DATE NULL,
  PRIMARY KEY (`term_id`),
  UNIQUE KEY `uk_terms_name` (`term_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `curriculum_schedules` (
  `curriculum_schedule_id` BIGINT NOT NULL AUTO_INCREMENT,
  `term_id` BIGINT NOT NULL,
  `track_id` BIGINT NULL,
  `week_no` INT NULL,
  `class_date` DATE NULL,
  `start_time` TIME NULL,
  `end_time` TIME NULL,
  `curriculum_type_code` VARCHAR(50) NULL,
  `topic` VARCHAR(255) NOT NULL,
  `instructor_name` VARCHAR(100) NULL,
  `classroom` VARCHAR(100) NULL,
  PRIMARY KEY (`curriculum_schedule_id`),
  CONSTRAINT `fk_curriculum_schedules_term`
    FOREIGN KEY (`term_id`) REFERENCES `terms` (`term_id`),
  CONSTRAINT `fk_curriculum_schedules_track`
    FOREIGN KEY (`track_id`) REFERENCES `tracks` (`track_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `lecture_replays` (
  `lecture_replay_id` BIGINT NOT NULL AUTO_INCREMENT,
  `curriculum_schedule_id` BIGINT NULL,
  `content_external_id` VARCHAR(100) NULL,
  `group_name` VARCHAR(255) NULL,
  `title` VARCHAR(255) NOT NULL,
  `is_latest` BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (`lecture_replay_id`),
  UNIQUE KEY `uk_lecture_replays_external_id` (`content_external_id`),
  CONSTRAINT `fk_lecture_replays_schedule`
    FOREIGN KEY (`curriculum_schedule_id`) REFERENCES `curriculum_schedules` (`curriculum_schedule_id`)
      ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `learning_materials` (
  `learning_material_id` BIGINT NOT NULL AUTO_INCREMENT,
  `curriculum_schedule_id` BIGINT NULL,
  `track_id` BIGINT NULL,
  `content_external_id` VARCHAR(100) NULL,
  `category_parent` VARCHAR(100) NULL,
  `category_child` VARCHAR(100) NULL,
  `material_type_code` VARCHAR(50) NOT NULL,
  `title` VARCHAR(255) NOT NULL,
  `summary` TEXT NULL,
  `detail_url` VARCHAR(500) NULL,
  `view_count` INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`learning_material_id`),
  UNIQUE KEY `uk_learning_materials_external_id` (`content_external_id`),
  CONSTRAINT `fk_learning_materials_schedule`
    FOREIGN KEY (`curriculum_schedule_id`) REFERENCES `curriculum_schedules` (`curriculum_schedule_id`)
      ON DELETE SET NULL,
  CONSTRAINT `fk_learning_materials_track`
    FOREIGN KEY (`track_id`) REFERENCES `tracks` (`track_id`)
      ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `learning_material_resources` (
  `learning_material_resource_id` BIGINT NOT NULL AUTO_INCREMENT,
  `learning_material_id` BIGINT NOT NULL,
  `resource_type_code` VARCHAR(50) NOT NULL,
  `resource_title` VARCHAR(255) NOT NULL,
  `launch_mode_code` VARCHAR(30) NOT NULL,
  `target_url` VARCHAR(500) NULL,
  `external_resource_id` VARCHAR(100) NULL,
  `display_order` INT NOT NULL DEFAULT 1,
  PRIMARY KEY (`learning_material_resource_id`),
  UNIQUE KEY `uk_learning_material_resources_material_order` (`learning_material_id`, `display_order`),
  CONSTRAINT `fk_learning_material_resources_material`
    FOREIGN KEY (`learning_material_id`) REFERENCES `learning_materials` (`learning_material_id`)
      ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `learning_material_resource_attachments` (
  `learning_material_resource_id` BIGINT NOT NULL,
  `attachment_id` BIGINT NOT NULL,
  PRIMARY KEY (`learning_material_resource_id`, `attachment_id`),
  CONSTRAINT `fk_learning_material_resource_attachments_resource`
    FOREIGN KEY (`learning_material_resource_id`) REFERENCES `learning_material_resources` (`learning_material_resource_id`)
      ON DELETE CASCADE,
  CONSTRAINT `fk_learning_material_resource_attachments_attachment`
    FOREIGN KEY (`attachment_id`) REFERENCES `attachments` (`attachment_id`)
      ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `learning_material_reactions` (
  `learning_material_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `reaction_type_code` VARCHAR(30) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`learning_material_id`, `user_id`, `reaction_type_code`),
  CONSTRAINT `fk_learning_material_reactions_material`
    FOREIGN KEY (`learning_material_id`) REFERENCES `learning_materials` (`learning_material_id`)
      ON DELETE CASCADE,
  CONSTRAINT `fk_learning_material_reactions_user`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
      ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `quest_evaluations` (
  `quest_evaluation_id` BIGINT NOT NULL AUTO_INCREMENT,
  `track_id` BIGINT NULL,
  `task_seq` VARCHAR(100) NOT NULL,
  `quest_type_code` VARCHAR(30) NOT NULL,
  `tp_clfc_cd` VARCHAR(30) NULL,
  `title` VARCHAR(255) NOT NULL,
  `start_at` DATETIME NULL,
  `end_at` DATETIME NULL,
  `max_exp` INT NULL,
  `progress_status_code` VARCHAR(30) NOT NULL,
  PRIMARY KEY (`quest_evaluation_id`),
  UNIQUE KEY `uk_quest_evaluations_task_seq` (`task_seq`),
  CONSTRAINT `fk_quest_evaluations_track`
    FOREIGN KEY (`track_id`) REFERENCES `tracks` (`track_id`)
      ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `quest_submissions` (
  `quest_submission_id` BIGINT NOT NULL AUTO_INCREMENT,
  `quest_evaluation_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `result_status_code` VARCHAR(30) NULL,
  `score` DECIMAL(7,2) NULL,
  `submit_status_code` VARCHAR(30) NOT NULL,
  `submitted_at` DATETIME NULL,
  `graded_at` DATETIME NULL,
  PRIMARY KEY (`quest_submission_id`),
  UNIQUE KEY `uk_quest_submissions_quest_user` (`quest_evaluation_id`, `user_id`),
  CONSTRAINT `fk_quest_submissions_quest`
    FOREIGN KEY (`quest_evaluation_id`) REFERENCES `quest_evaluations` (`quest_evaluation_id`)
      ON DELETE CASCADE,
  CONSTRAINT `fk_quest_submissions_user`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
      ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `surveys` (
  `survey_id` BIGINT NOT NULL AUTO_INCREMENT,
  `track_id` BIGINT NULL,
  `title` VARCHAR(255) NOT NULL,
  `survey_category_code` VARCHAR(50) NOT NULL,
  `required_yn` BOOLEAN NOT NULL DEFAULT FALSE,
  `progress_status_code` VARCHAR(30) NOT NULL,
  `start_at` DATETIME NULL,
  `end_at` DATETIME NULL,
  PRIMARY KEY (`survey_id`),
  CONSTRAINT `fk_surveys_track`
    FOREIGN KEY (`track_id`) REFERENCES `tracks` (`track_id`)
      ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `survey_questions` (
  `survey_question_id` BIGINT NOT NULL AUTO_INCREMENT,
  `survey_id` BIGINT NOT NULL,
  `question_type_code` VARCHAR(30) NOT NULL,
  `question_text` TEXT NOT NULL,
  `display_order` INT NOT NULL,
  PRIMARY KEY (`survey_question_id`),
  UNIQUE KEY `uk_survey_questions_survey_order` (`survey_id`, `display_order`),
  CONSTRAINT `fk_survey_questions_survey`
    FOREIGN KEY (`survey_id`) REFERENCES `surveys` (`survey_id`)
      ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `survey_options` (
  `survey_option_id` BIGINT NOT NULL AUTO_INCREMENT,
  `survey_question_id` BIGINT NOT NULL,
  `option_text` VARCHAR(255) NOT NULL,
  `display_order` INT NOT NULL,
  PRIMARY KEY (`survey_option_id`),
  UNIQUE KEY `uk_survey_options_question_order` (`survey_question_id`, `display_order`),
  UNIQUE KEY `uk_survey_options_option_question` (`survey_option_id`, `survey_question_id`),
  CONSTRAINT `fk_survey_options_question`
    FOREIGN KEY (`survey_question_id`) REFERENCES `survey_questions` (`survey_question_id`)
      ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `survey_responses` (
  `survey_response_id` BIGINT NOT NULL AUTO_INCREMENT,
  `survey_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `completed_yn` BOOLEAN NOT NULL DEFAULT FALSE,
  `responded_at` DATETIME NULL,
  PRIMARY KEY (`survey_response_id`),
  UNIQUE KEY `uk_survey_responses_survey_user` (`survey_id`, `user_id`),
  CONSTRAINT `fk_survey_responses_survey`
    FOREIGN KEY (`survey_id`) REFERENCES `surveys` (`survey_id`)
      ON DELETE CASCADE,
  CONSTRAINT `fk_survey_responses_user`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
      ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `survey_response_answers` (
  `survey_response_answer_id` BIGINT NOT NULL AUTO_INCREMENT,
  `survey_response_id` BIGINT NOT NULL,
  `survey_question_id` BIGINT NOT NULL,
  `survey_option_id` BIGINT NULL,
  `answer_text` TEXT NULL,
  PRIMARY KEY (`survey_response_answer_id`),
  UNIQUE KEY `uk_survey_response_answers_response_question` (`survey_response_id`, `survey_question_id`),
  CONSTRAINT `fk_survey_response_answers_response`
    FOREIGN KEY (`survey_response_id`) REFERENCES `survey_responses` (`survey_response_id`)
      ON DELETE CASCADE,
  CONSTRAINT `fk_survey_response_answers_question`
    FOREIGN KEY (`survey_question_id`) REFERENCES `survey_questions` (`survey_question_id`)
      ON DELETE CASCADE,
  CONSTRAINT `fk_survey_response_answers_option_question`
    FOREIGN KEY (`survey_option_id`, `survey_question_id`) REFERENCES `survey_options` (`survey_option_id`, `survey_question_id`)
      ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `boards` (
  `board_id` BIGINT NOT NULL AUTO_INCREMENT,
  `board_code` VARCHAR(50) NOT NULL,
  `board_name` VARCHAR(100) NOT NULL,
  `board_group_code` VARCHAR(50) NOT NULL,
  `access_scope_code` VARCHAR(30) NOT NULL DEFAULT 'AUTHENTICATED',
  PRIMARY KEY (`board_id`),
  UNIQUE KEY `uk_boards_code` (`board_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `board_categories` (
  `board_category_id` BIGINT NOT NULL AUTO_INCREMENT,
  `board_id` BIGINT NOT NULL,
  `category_name` VARCHAR(100) NOT NULL,
  `sort_order` INT NOT NULL DEFAULT 1,
  PRIMARY KEY (`board_category_id`),
  UNIQUE KEY `uk_board_categories_board_name` (`board_id`, `category_name`),
  UNIQUE KEY `uk_board_categories_id_board` (`board_category_id`, `board_id`),
  CONSTRAINT `fk_board_categories_board`
    FOREIGN KEY (`board_id`) REFERENCES `boards` (`board_id`)
      ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `board_posts` (
  `board_post_id` BIGINT NOT NULL AUTO_INCREMENT,
  `board_id` BIGINT NOT NULL,
  `board_category_id` BIGINT NULL,
  `author_user_id` BIGINT NULL,
  `title` VARCHAR(255) NOT NULL,
  `content` LONGTEXT NULL,
  `notice_yn` BOOLEAN NOT NULL DEFAULT FALSE,
  `view_count` INT NOT NULL DEFAULT 0,
  `has_prev_next_nav` BOOLEAN NOT NULL DEFAULT FALSE,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`board_post_id`),
  CONSTRAINT `fk_board_posts_board`
    FOREIGN KEY (`board_id`) REFERENCES `boards` (`board_id`)
      ON DELETE CASCADE,
  CONSTRAINT `fk_board_posts_category_in_board`
    FOREIGN KEY (`board_category_id`, `board_id`) REFERENCES `board_categories` (`board_category_id`, `board_id`)
      ON DELETE RESTRICT,
  CONSTRAINT `fk_board_posts_author`
    FOREIGN KEY (`author_user_id`) REFERENCES `users` (`user_id`)
      ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `board_post_attachments` (
  `board_post_id` BIGINT NOT NULL,
  `attachment_id` BIGINT NOT NULL,
  PRIMARY KEY (`board_post_id`, `attachment_id`),
  CONSTRAINT `fk_board_post_attachments_post`
    FOREIGN KEY (`board_post_id`) REFERENCES `board_posts` (`board_post_id`)
      ON DELETE CASCADE,
  CONSTRAINT `fk_board_post_attachments_attachment`
    FOREIGN KEY (`attachment_id`) REFERENCES `attachments` (`attachment_id`)
      ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `board_comments` (
  `board_comment_id` BIGINT NOT NULL AUTO_INCREMENT,
  `board_post_id` BIGINT NOT NULL,
  `author_user_id` BIGINT NULL,
  `parent_comment_id` BIGINT NULL,
  `content` TEXT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`board_comment_id`),
  CONSTRAINT `fk_board_comments_post`
    FOREIGN KEY (`board_post_id`) REFERENCES `board_posts` (`board_post_id`)
      ON DELETE CASCADE,
  CONSTRAINT `fk_board_comments_author`
    FOREIGN KEY (`author_user_id`) REFERENCES `users` (`user_id`)
      ON DELETE SET NULL,
  CONSTRAINT `fk_board_comments_parent`
    FOREIGN KEY (`parent_comment_id`) REFERENCES `board_comments` (`board_comment_id`)
      ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `board_post_reactions` (
  `board_post_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `reaction_type_code` VARCHAR(30) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`board_post_id`, `user_id`, `reaction_type_code`),
  CONSTRAINT `fk_board_post_reactions_post`
    FOREIGN KEY (`board_post_id`) REFERENCES `board_posts` (`board_post_id`)
      ON DELETE CASCADE,
  CONSTRAINT `fk_board_post_reactions_user`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
      ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `support_tickets` (
  `support_ticket_id` BIGINT NOT NULL AUTO_INCREMENT,
  `requester_user_id` BIGINT NOT NULL,
  `title` VARCHAR(255) NOT NULL,
  `content` LONGTEXT NULL,
  `status_code` VARCHAR(30) NOT NULL DEFAULT 'OPEN',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`support_ticket_id`),
  CONSTRAINT `fk_support_tickets_requester`
    FOREIGN KEY (`requester_user_id`) REFERENCES `users` (`user_id`)
      ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `support_ticket_answers` (
  `support_ticket_answer_id` BIGINT NOT NULL AUTO_INCREMENT,
  `support_ticket_id` BIGINT NOT NULL,
  `responder_user_id` BIGINT NULL,
  `content` LONGTEXT NOT NULL,
  `answered_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`support_ticket_answer_id`),
  UNIQUE KEY `uk_support_ticket_answers_ticket` (`support_ticket_id`),
  CONSTRAINT `fk_support_ticket_answers_ticket`
    FOREIGN KEY (`support_ticket_id`) REFERENCES `support_tickets` (`support_ticket_id`)
      ON DELETE CASCADE,
  CONSTRAINT `fk_support_ticket_answers_responder`
    FOREIGN KEY (`responder_user_id`) REFERENCES `users` (`user_id`)
      ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `support_ticket_attachments` (
  `support_ticket_id` BIGINT NOT NULL,
  `attachment_id` BIGINT NOT NULL,
  PRIMARY KEY (`support_ticket_id`, `attachment_id`),
  CONSTRAINT `fk_support_ticket_attachments_ticket`
    FOREIGN KEY (`support_ticket_id`) REFERENCES `support_tickets` (`support_ticket_id`)
      ON DELETE CASCADE,
  CONSTRAINT `fk_support_ticket_attachments_attachment`
    FOREIGN KEY (`attachment_id`) REFERENCES `attachments` (`attachment_id`)
      ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
