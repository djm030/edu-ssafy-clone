package com.edussafy.backend.priority.repository;

import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceAppealItem;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceRecordItem;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceSummary;
import com.edussafy.backend.priority.dto.PriorityDtos.BookmarkItem;
import com.edussafy.backend.priority.dto.PriorityDtos.BookmarkSnapshot;
import com.edussafy.backend.priority.dto.PriorityDtos.CurriculumItem;
import com.edussafy.backend.priority.dto.PriorityDtos.ElearningLessonItem;
import com.edussafy.backend.priority.dto.PriorityDtos.ElearningProgressDetail;
import com.edussafy.backend.priority.dto.PriorityDtos.ElearningProgressItem;
import com.edussafy.backend.priority.dto.PriorityDtos.LevelSummary;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialItem;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialResourceItem;
import com.edussafy.backend.priority.dto.PriorityDtos.NotificationItem;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestItem;
import com.edussafy.backend.priority.dto.PriorityDtos.ReplayItem;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyItem;
import com.edussafy.backend.priority.dto.PriorityDtos.TodaySummary;
import com.edussafy.backend.priority.dto.PriorityDtos.UserProfile;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class PriorityApiRepository {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    private final JdbcClient jdbcClient;

    public PriorityApiRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<UserProfile> findUserByEmail(String email) {
        return userSql("WHERE u.email = :email ORDER BY u.user_id ASC LIMIT 1")
                .param("email", email)
                .query(this::mapUser)
                .optional();
    }

    public Optional<UserProfile> findUserById(long userId) {
        return userSql("WHERE u.user_id = :userId AND u.deleted_at IS NULL ORDER BY u.user_id ASC LIMIT 1")
                .param("userId", userId)
                .query(this::mapUser)
                .optional();
    }

    public Optional<UserProfile> findDefaultUser() {
        return userSql("WHERE u.deleted_at IS NULL ORDER BY u.user_id ASC LIMIT 1")
                .query(this::mapUser)
                .optional();
    }

    public Optional<String> findPasswordHash(long userId) {
        return jdbcClient.sql("""
                SELECT password_hash
                FROM users
                WHERE user_id = :userId AND deleted_at IS NULL
                """)
                .param("userId", userId)
                .query(String.class)
                .optional();
    }

    public int updatePasswordHash(long userId, String passwordHash) {
        return jdbcClient.sql("""
                UPDATE users
                SET password_hash = :passwordHash,
                    updated_at = CURRENT_TIMESTAMP
                WHERE user_id = :userId AND deleted_at IS NULL
                """)
                .param("userId", userId)
                .param("passwordHash", passwordHash)
                .update();
    }

    public Optional<LevelSummary> findLevel(long userId) {
        return jdbcClient.sql("""
                SELECT
                    COALESCE(uls.level_no, 1) AS level_no,
                    COALESCE(uls.exp, 0) AS exp,
                    COALESCE(uls.scholarship_point, 0) AS scholarship_point,
                    latest_rank.rank_no
                FROM users u
                LEFT JOIN user_level_statuses uls ON uls.user_id = u.user_id
                LEFT JOIN (
                    SELECT user_id, rank_no
                    FROM user_rank_snapshots
                    WHERE snapshot_date = (SELECT MAX(snapshot_date) FROM user_rank_snapshots)
                ) latest_rank ON latest_rank.user_id = u.user_id
                WHERE u.user_id = :userId
                """)
                .param("userId", userId)
                .query((rs, rowNum) -> {
                    int level = rs.getInt("level_no");
                    return new LevelSummary(
                            level,
                            rs.getInt("exp"),
                            Math.max(level + 1, 1) * 1000,
                            rs.getInt("scholarship_point"),
                            nullableInt(rs, "rank_no")
                    );
                })
                .optional();
    }

    public AttendanceSummary findAttendanceSummary(long userId) {
        return findAttendanceSummary(userId, null, null, null);
    }

    public AttendanceSummary findAttendanceSummary(long userId, LocalDate dateFrom, LocalDate dateTo, String status) {
        SqlParts parts = attendanceWhere(userId, dateFrom, dateTo, status);
        return jdbcClient.sql("""
                SELECT
                    SUM(CASE WHEN attendance_status_code = 'present' THEN 1 ELSE 0 END) AS present_count,
                    SUM(CASE WHEN attendance_status_code = 'late' THEN 1 ELSE 0 END) AS late_count,
                    SUM(CASE WHEN attendance_status_code = 'absent' THEN 1 ELSE 0 END) AS absent_count
                FROM attendance_records
                """ + parts.whereClause())
                .params(parts.params())
                .query((rs, rowNum) -> new AttendanceSummary(
                        rs.getInt("present_count"),
                        rs.getInt("late_count"),
                        rs.getInt("absent_count"),
                        true
                ))
                .single();
    }

    public List<AttendanceRecordItem> findAttendanceRecords(long userId) {
        return findAttendanceRecords(userId, null, null, null);
    }

    public List<AttendanceRecordItem> findAttendanceRecords(long userId, LocalDate dateFrom, LocalDate dateTo, String status) {
        SqlParts parts = attendanceWhere("ar", userId, dateFrom, dateTo, status);
        return jdbcClient.sql(attendanceRecordSelect(parts.whereClause() + " ORDER BY ar.attendance_date DESC LIMIT 120"))
                .params(parts.params())
                .query(this::mapAttendanceRecord)
                .list();
    }

    public Optional<AttendanceRecordItem> findAttendanceRecord(long userId, long attendanceRecordId) {
        return jdbcClient.sql(attendanceRecordSelect("WHERE ar.user_id = :userId AND ar.attendance_record_id = :attendanceRecordId LIMIT 1"))
                .param("userId", userId)
                .param("attendanceRecordId", attendanceRecordId)
                .query(this::mapAttendanceRecord)
                .optional();
    }

    public AttendanceAppealItem createAttendanceAppeal(
            long attendanceRecordId,
            String type,
            String reason,
            String requestedStatus
    ) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql("""
                INSERT INTO attendance_appeals (
                    attendance_record_id,
                    appeal_type_code,
                    reason,
                    requested_status_code
                )
                VALUES (
                    :attendanceRecordId,
                    :appealType,
                    :reason,
                    :requestedStatus
                )
                """)
                .param("attendanceRecordId", attendanceRecordId)
                .param("appealType", type)
                .param("reason", reason)
                .param("requestedStatus", requestedStatus)
                .update(keyHolder, "attendance_appeal_id");

        Number key = keyHolder.getKey();
        long appealId = key == null ? 0L : key.longValue();
        return findAttendanceAppeal(appealId).orElseGet(() -> new AttendanceAppealItem(
                appealId,
                attendanceRecordId,
                type,
                reason,
                requestedStatus,
                "requested",
                OffsetDateTime.now(SEOUL_ZONE),
                false
        ));
    }

    public Optional<AttendanceAppealItem> findAttendanceAppeal(long attendanceAppealId) {
        return jdbcClient.sql(attendanceAppealSelect("""
                WHERE aa.attendance_appeal_id = :attendanceAppealId
                LIMIT 1
                """))
                .param("attendanceAppealId", attendanceAppealId)
                .query(this::mapAttendanceAppeal)
                .optional();
    }

    public List<AttendanceAppealItem> findAttendanceAppeals(long userId) {
        return jdbcClient.sql(attendanceAppealSelect("""
                WHERE ar.user_id = :userId
                ORDER BY aa.requested_at DESC, aa.attendance_appeal_id DESC
                """))
                .param("userId", userId)
                .query(this::mapAttendanceAppeal)
                .list();
    }

    public Optional<AttendanceAppealItem> findAttendanceAppeal(long userId, long attendanceAppealId) {
        return jdbcClient.sql(attendanceAppealSelect("""
                WHERE ar.user_id = :userId AND aa.attendance_appeal_id = :attendanceAppealId
                LIMIT 1
                """))
                .param("userId", userId)
                .param("attendanceAppealId", attendanceAppealId)
                .query(this::mapAttendanceAppeal)
                .optional();
    }

    public List<AttendanceAppealItem> findPendingAttendanceAppealsForStaff() {
        return jdbcClient.sql(attendanceAppealSelect("""
                WHERE aa.approval_status_code = 'requested'
                ORDER BY aa.requested_at ASC, aa.attendance_appeal_id ASC
                """))
                .query(this::mapAttendanceAppeal)
                .list();
    }

    public Optional<AttendanceAppealItem> findAttendanceAppealForStaff(long attendanceAppealId) {
        return jdbcClient.sql(attendanceAppealSelect("""
                WHERE aa.attendance_appeal_id = :attendanceAppealId
                LIMIT 1
                """))
                .param("attendanceAppealId", attendanceAppealId)
                .query(this::mapAttendanceAppeal)
                .optional();
    }

    public int cancelAttendanceAppeal(long userId, long attendanceAppealId) {
        return jdbcClient.sql("""
                UPDATE attendance_appeals aa
                JOIN attendance_records ar ON ar.attendance_record_id = aa.attendance_record_id
                SET aa.approval_status_code = 'canceled',
                    aa.resolved_at = CURRENT_TIMESTAMP,
                    aa.resolved_by_user_id = :userId,
                    aa.resolution_comment = 'Canceled by requester.'
                WHERE ar.user_id = :userId
                  AND aa.attendance_appeal_id = :attendanceAppealId
                  AND aa.approval_status_code = 'requested'
                """)
                .param("userId", userId)
                .param("attendanceAppealId", attendanceAppealId)
                .update();
    }

    public int resolveAttendanceAppeal(
            long resolverUserId,
            long attendanceAppealId,
            String approvalStatus,
            String resolvedStatus,
            String resolutionComment
    ) {
        return jdbcClient.sql("""
                UPDATE attendance_appeals
                SET approval_status_code = :approvalStatus,
                    resolved_status_code = :resolvedStatus,
                    resolved_at = CURRENT_TIMESTAMP,
                    resolved_by_user_id = :resolverUserId,
                    resolution_comment = :resolutionComment
                WHERE attendance_appeal_id = :attendanceAppealId
                  AND approval_status_code = 'requested'
                """)
                .param("approvalStatus", approvalStatus)
                .param("resolvedStatus", resolvedStatus)
                .param("resolverUserId", resolverUserId)
                .param("resolutionComment", resolutionComment)
                .param("attendanceAppealId", attendanceAppealId)
                .update();
    }

    public int updateAttendanceRecordStatusFromAppeal(long attendanceAppealId, String attendanceStatus) {
        return jdbcClient.sql("""
                UPDATE attendance_records ar
                JOIN attendance_appeals aa ON aa.attendance_record_id = ar.attendance_record_id
                SET ar.attendance_status_code = :attendanceStatus,
                    ar.approval_type_code = 'appeal'
                WHERE aa.attendance_appeal_id = :attendanceAppealId
                """)
                .param("attendanceStatus", attendanceStatus)
                .param("attendanceAppealId", attendanceAppealId)
                .update();
    }

    private String attendanceAppealSelect(String suffix) {
        return """
                SELECT aa.attendance_appeal_id, aa.attendance_record_id, aa.appeal_type_code, aa.reason,
                       aa.requested_status_code, aa.approval_status_code, aa.requested_at,
                       ar.attendance_date, aa.resolved_status_code, aa.resolved_at,
                       aa.resolution_comment, resolver.name AS resolved_by_name
                FROM attendance_appeals aa
                JOIN attendance_records ar ON ar.attendance_record_id = aa.attendance_record_id
                LEFT JOIN users resolver ON resolver.user_id = aa.resolved_by_user_id
                """ + suffix;
    }

    private AttendanceAppealItem mapAttendanceAppeal(ResultSet rs, int rowNum) throws SQLException {
        return new AttendanceAppealItem(
                rs.getLong("attendance_appeal_id"),
                rs.getLong("attendance_record_id"),
                rs.getString("appeal_type_code"),
                rs.getString("reason"),
                rs.getString("requested_status_code"),
                rs.getString("approval_status_code"),
                toOffset(rs.getTimestamp("requested_at")),
                toLocalDate(rs, "attendance_date"),
                rs.getString("resolved_status_code"),
                toOffset(rs.getTimestamp("resolved_at")),
                rs.getString("resolution_comment"),
                rs.getString("resolved_by_name"),
                false
        );
    }

    private String attendanceRecordSelect(String suffix) {
        return """
                SELECT ar.attendance_record_id, ar.attendance_date, ar.check_in_at, ar.check_out_at,
                       ar.attendance_status_code, ar.approval_type_code,
                       latest_appeal.attendance_appeal_id, latest_appeal.approval_status_code AS appeal_status,
                       latest_appeal.requested_at AS appeal_requested_at
                FROM attendance_records ar
                LEFT JOIN (
                    SELECT a.attendance_appeal_id, a.attendance_record_id, a.approval_status_code, a.requested_at
                    FROM attendance_appeals a
                    JOIN (
                        SELECT attendance_record_id, MAX(attendance_appeal_id) AS latest_id
                        FROM attendance_appeals
                        GROUP BY attendance_record_id
                    ) latest ON latest.latest_id = a.attendance_appeal_id
                ) latest_appeal ON latest_appeal.attendance_record_id = ar.attendance_record_id
                """ + suffix;
    }

    private AttendanceRecordItem mapAttendanceRecord(ResultSet rs, int rowNum) throws SQLException {
        String appealStatus = rs.getString("appeal_status");
        Long appealId = nullableLong(rs, "attendance_appeal_id");
        boolean appealAvailable = appealStatus == null || "rejected".equals(appealStatus) || "canceled".equals(appealStatus);
        return new AttendanceRecordItem(
                rs.getLong("attendance_record_id"),
                rs.getDate("attendance_date").toLocalDate(),
                nullableTime(rs, "check_in_at"),
                nullableTime(rs, "check_out_at"),
                rs.getString("attendance_status_code"),
                rs.getString("approval_type_code"),
                appealAvailable,
                appealId,
                appealStatus,
                toOffset(rs.getTimestamp("appeal_requested_at"))
        );
    }

    public long countNotifications(long userId) {
        return count("""
                SELECT COUNT(*)
                FROM notification_recipients nr
                JOIN notifications n ON n.notification_id = nr.notification_id
                WHERE nr.recipient_user_id = :userId AND nr.deleted_at IS NULL
                """, userId);
    }

    public long countUnreadNotifications(long userId) {
        return count("""
                SELECT COUNT(*)
                FROM notification_recipients nr
                WHERE nr.recipient_user_id = :userId AND nr.deleted_at IS NULL AND nr.read_at IS NULL
                """, userId);
    }

    public List<NotificationItem> findNotifications(long userId, int limit, int offset) {
        return notificationSql("""
                WHERE nr.recipient_user_id = :userId AND nr.deleted_at IS NULL
                ORDER BY n.created_at DESC, n.notification_id DESC
                LIMIT :limit OFFSET :offset
                """)
                .param("userId", userId)
                .param("limit", limit)
                .param("offset", offset)
                .query(this::mapNotification)
                .list();
    }

    public Optional<NotificationItem> findNotification(long userId, long notificationId) {
        return notificationSql("""
                WHERE nr.recipient_user_id = :userId
                  AND n.notification_id = :notificationId
                  AND nr.deleted_at IS NULL
                LIMIT 1
                """)
                .param("userId", userId)
                .param("notificationId", notificationId)
                .query(this::mapNotification)
                .optional();
    }

    public void markNotificationRead(long userId, long notificationId) {
        jdbcClient.sql("""
                UPDATE notification_recipients
                SET read_at = COALESCE(read_at, CURRENT_TIMESTAMP)
                WHERE recipient_user_id = :userId
                  AND notification_id = :notificationId
                  AND deleted_at IS NULL
                """)
                .param("userId", userId)
                .param("notificationId", notificationId)
                .update();
    }

    public void markAllNotificationsRead(long userId) {
        jdbcClient.sql("""
                UPDATE notification_recipients
                SET read_at = COALESCE(read_at, CURRENT_TIMESTAMP)
                WHERE recipient_user_id = :userId
                  AND deleted_at IS NULL
                  AND read_at IS NULL
                """)
                .param("userId", userId)
                .update();
    }

    public int deleteNotification(long userId, long notificationId) {
        return jdbcClient.sql("""
                UPDATE notification_recipients
                SET deleted_at = COALESCE(deleted_at, CURRENT_TIMESTAMP),
                    read_at = COALESCE(read_at, CURRENT_TIMESTAMP)
                WHERE recipient_user_id = :userId
                  AND notification_id = :notificationId
                  AND deleted_at IS NULL
                """)
                .param("userId", userId)
                .param("notificationId", notificationId)
                .update();
    }

    public long createNotification(long senderUserId, String title, String body) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql("""
                INSERT INTO notifications (sender_user_id, title, body)
                VALUES (:senderUserId, :title, :body)
                """)
                .param("senderUserId", senderUserId)
                .param("title", title)
                .param("body", body)
                .update(keyHolder, "notification_id");

        Number key = keyHolder.getKey();
        return key == null ? 0L : key.longValue();
    }

    public void createNotificationRecipient(long notificationId, long recipientUserId) {
        jdbcClient.sql("""
                INSERT INTO notification_recipients (notification_id, recipient_user_id)
                VALUES (:notificationId, :recipientUserId)
                ON DUPLICATE KEY UPDATE
                    deleted_at = NULL,
                    read_at = NULL
                """)
                .param("notificationId", notificationId)
                .param("recipientUserId", recipientUserId)
                .update();
    }

    public TodaySummary findTodaySummary(long userId) {
        return new TodaySummary(
                firstTitle("SELECT topic FROM curriculum_schedules WHERE class_date = CURRENT_DATE() ORDER BY start_time LIMIT 1"),
                firstTitle("SELECT title FROM quest_evaluations WHERE CURRENT_TIMESTAMP() BETWEEN COALESCE(start_at, CURRENT_TIMESTAMP()) AND COALESCE(end_at, CURRENT_TIMESTAMP()) ORDER BY end_at LIMIT 1"),
                firstTitle("SELECT title FROM surveys WHERE CURRENT_TIMESTAMP() BETWEEN COALESCE(start_at, CURRENT_TIMESTAMP()) AND COALESCE(end_at, CURRENT_TIMESTAMP()) ORDER BY end_at LIMIT 1")
        );
    }

    public List<CurriculumItem> findCurriculum(long userId) {
        return jdbcClient.sql("""
                SELECT curriculum_schedule_id, week_no, class_date, start_time, end_time,
                       curriculum_type_code, topic, instructor_name, classroom
                FROM curriculum_schedules
                ORDER BY class_date DESC, start_time DESC, curriculum_schedule_id DESC
                LIMIT 80
                """)
                .query((rs, rowNum) -> new CurriculumItem(
                        rs.getLong("curriculum_schedule_id"),
                        nullableInt(rs, "week_no"),
                        rs.getDate("class_date") == null ? null : rs.getDate("class_date").toLocalDate(),
                        nullableTime(rs, "start_time"),
                        nullableTime(rs, "end_time"),
                        rs.getString("curriculum_type_code"),
                        rs.getString("topic"),
                        rs.getString("instructor_name"),
                        rs.getString("classroom")
                ))
                .list();
    }

    public List<ReplayItem> findReplays(long userId) {
        return jdbcClient.sql("""
                SELECT lecture_replay_id, curriculum_schedule_id, title, version_no, published_at
                FROM lecture_replays
                ORDER BY COALESCE(published_at, created_at) DESC, lecture_replay_id DESC
                LIMIT 80
                """)
                .query((rs, rowNum) -> new ReplayItem(
                        rs.getLong("lecture_replay_id"),
                        rs.getLong("curriculum_schedule_id"),
                        rs.getString("title"),
                        rs.getInt("version_no"),
                        toOffset(rs.getTimestamp("published_at"))
                ))
                .list();
    }

    public long countBookmarks(long userId, String targetType) {
        SqlParts parts = bookmarksWhere(userId, targetType);
        return jdbcClient.sql("SELECT COUNT(*) FROM learner_bookmarks lb " + parts.whereClause())
                .params(parts.params())
                .query(Long.class)
                .single();
    }

    public List<BookmarkItem> findBookmarks(long userId, String targetType, int limit, int offset) {
        SqlParts parts = bookmarksWhere(userId, targetType);
        return jdbcClient.sql("""
                SELECT bookmark_id, target_type_code, target_id, title_snapshot, description_snapshot,
                       thumbnail_url, target_url, created_at
                FROM learner_bookmarks lb
                """ + parts.whereClause() + """
                ORDER BY lb.created_at DESC, lb.bookmark_id DESC
                LIMIT :limit OFFSET :offset
                """)
                .params(parts.params())
                .param("limit", limit)
                .param("offset", offset)
                .query(this::mapBookmark)
                .list();
    }

    public Optional<BookmarkItem> findBookmark(long userId, long bookmarkId) {
        return jdbcClient.sql("""
                SELECT bookmark_id, target_type_code, target_id, title_snapshot, description_snapshot,
                       thumbnail_url, target_url, created_at
                FROM learner_bookmarks
                WHERE user_id = :userId AND bookmark_id = :bookmarkId
                LIMIT 1
                """)
                .param("userId", userId)
                .param("bookmarkId", bookmarkId)
                .query(this::mapBookmark)
                .optional();
    }

    public Optional<BookmarkSnapshot> findBookmarkSnapshot(String targetType, long targetId) {
        return switch (targetType) {
            case "material" -> jdbcClient.sql("""
                    SELECT 'material' AS target_type, learning_material_id AS target_id, title, summary AS description,
                           NULL AS thumbnail_url, CONCAT('/learning/materials/', learning_material_id) AS target_url
                    FROM learning_materials
                    WHERE learning_material_id = :targetId
                    LIMIT 1
                    """)
                    .param("targetId", targetId)
                    .query(this::mapBookmarkSnapshot)
                    .optional();
            case "elearning" -> jdbcClient.sql("""
                    SELECT 'elearning' AS target_type, elearning_course_id AS target_id, title, description,
                           thumbnail_url, CONCAT('/mycampus/elearning/', elearning_course_id) AS target_url
                    FROM elearning_courses
                    WHERE elearning_course_id = :targetId AND active_yn = TRUE
                    LIMIT 1
                    """)
                    .param("targetId", targetId)
                    .query(this::mapBookmarkSnapshot)
                    .optional();
            case "replay" -> jdbcClient.sql("""
                    SELECT 'replay' AS target_type, lecture_replay_id AS target_id, title, replay_group_key AS description,
                           NULL AS thumbnail_url, '/learning/replays' AS target_url
                    FROM lecture_replays
                    WHERE lecture_replay_id = :targetId
                    LIMIT 1
                    """)
                    .param("targetId", targetId)
                    .query(this::mapBookmarkSnapshot)
                    .optional();
            default -> Optional.empty();
        };
    }

    public long createOrUpdateBookmark(long userId, BookmarkSnapshot snapshot) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql("""
                INSERT INTO learner_bookmarks (
                    user_id, target_type_code, target_id, title_snapshot, description_snapshot, thumbnail_url, target_url
                )
                VALUES (
                    :userId, :targetType, :targetId, :title, :description, :thumbnailUrl, :targetUrl
                )
                ON DUPLICATE KEY UPDATE
                    title_snapshot = VALUES(title_snapshot),
                    description_snapshot = VALUES(description_snapshot),
                    thumbnail_url = VALUES(thumbnail_url),
                    target_url = VALUES(target_url)
                """)
                .param("userId", userId)
                .param("targetType", snapshot.targetType())
                .param("targetId", snapshot.targetId())
                .param("title", snapshot.title())
                .param("description", snapshot.description())
                .param("thumbnailUrl", snapshot.thumbnailUrl())
                .param("targetUrl", snapshot.targetUrl())
                .update(keyHolder, "bookmark_id");
        Number key = keyHolder.getKey();
        if (key != null) {
            return key.longValue();
        }
        return jdbcClient.sql("""
                SELECT bookmark_id
                FROM learner_bookmarks
                WHERE user_id = :userId
                  AND target_type_code = :targetType
                  AND target_id = :targetId
                LIMIT 1
                """)
                .param("userId", userId)
                .param("targetType", snapshot.targetType())
                .param("targetId", snapshot.targetId())
                .query(Long.class)
                .single();
    }

    public int deleteBookmark(long userId, long bookmarkId) {
        return jdbcClient.sql("""
                DELETE FROM learner_bookmarks
                WHERE user_id = :userId AND bookmark_id = :bookmarkId
                """)
                .param("userId", userId)
                .param("bookmarkId", bookmarkId)
                .update();
    }

    public long countElearningProgress(long userId, String status, String keyword) {
        SqlParts parts = elearningProgressWhere(userId, status, keyword);
        return jdbcClient.sql("""
                SELECT COUNT(*)
                FROM learner_elearning_progress lep
                JOIN elearning_courses ec ON ec.elearning_course_id = lep.elearning_course_id
                """ + parts.whereClause())
                .params(parts.params())
                .query(Long.class)
                .single();
    }

    public List<ElearningProgressItem> findElearningProgress(
            long userId,
            String status,
            String keyword,
            int limit,
            int offset
    ) {
        SqlParts parts = elearningProgressWhere(userId, status, keyword);
        return jdbcClient.sql("""
                SELECT
                    ec.elearning_course_id,
                    ec.title,
                    ec.category,
                    ec.thumbnail_url,
                    ec.provider,
                    ec.description,
                    lep.progress_percent,
                    lep.completed_lessons,
                    ec.total_lessons,
                    ec.total_duration_seconds,
                    lep.last_lesson_title,
                    lep.last_learning_at,
                    lep.status_code,
                    lep.resume_url
                FROM learner_elearning_progress lep
                JOIN elearning_courses ec ON ec.elearning_course_id = lep.elearning_course_id
                """ + parts.whereClause() + """
                ORDER BY COALESCE(lep.last_learning_at, lep.updated_at, lep.created_at) DESC, ec.elearning_course_id DESC
                LIMIT :limit OFFSET :offset
                """)
                .params(parts.params())
                .param("limit", limit)
                .param("offset", offset)
                .query(this::mapElearningProgress)
                .list();
    }

    public Optional<ElearningProgressDetail> findElearningProgressDetail(long userId, long courseId) {
        return jdbcClient.sql("""
                SELECT
                    ec.elearning_course_id,
                    ec.title,
                    ec.category,
                    ec.thumbnail_url,
                    ec.provider,
                    ec.description,
                    lep.progress_percent,
                    lep.completed_lessons,
                    ec.total_lessons,
                    ec.total_duration_seconds,
                    lep.last_lesson_title,
                    lep.last_learning_at,
                    lep.status_code,
                    lep.resume_url
                FROM learner_elearning_progress lep
                JOIN elearning_courses ec ON ec.elearning_course_id = lep.elearning_course_id
                WHERE lep.user_id = :userId
                  AND lep.elearning_course_id = :courseId
                  AND ec.active_yn = TRUE
                LIMIT 1
                """)
                .param("userId", userId)
                .param("courseId", courseId)
                .query((rs, rowNum) -> new ElearningProgressDetail(
                        rs.getLong("elearning_course_id"),
                        rs.getString("title"),
                        rs.getString("category"),
                        rs.getString("thumbnail_url"),
                        rs.getString("provider"),
                        rs.getString("description"),
                        rs.getInt("progress_percent"),
                        rs.getInt("completed_lessons"),
                        rs.getInt("total_lessons"),
                        rs.getLong("total_duration_seconds"),
                        rs.getString("last_lesson_title"),
                        toOffset(rs.getTimestamp("last_learning_at")),
                        rs.getString("status_code"),
                        rs.getString("resume_url"),
                        List.of()
                ))
                .optional();
    }

    public List<ElearningLessonItem> findElearningLessons(long userId, long courseId) {
        return jdbcClient.sql("""
                SELECT
                    el.elearning_lesson_id,
                    el.lesson_no,
                    el.title,
                    el.duration_seconds,
                    CASE WHEN lelp.completed_at IS NULL THEN FALSE ELSE TRUE END AS completed,
                    lelp.completed_at
                FROM elearning_lessons el
                LEFT JOIN learner_elearning_lesson_progress lelp
                    ON lelp.elearning_lesson_id = el.elearning_lesson_id
                   AND lelp.user_id = :userId
                WHERE el.elearning_course_id = :courseId
                ORDER BY el.lesson_no ASC, el.elearning_lesson_id ASC
                """)
                .param("userId", userId)
                .param("courseId", courseId)
                .query((rs, rowNum) -> new ElearningLessonItem(
                        rs.getLong("elearning_lesson_id"),
                        rs.getInt("lesson_no"),
                        rs.getString("title"),
                        rs.getLong("duration_seconds"),
                        rs.getBoolean("completed"),
                        toOffset(rs.getTimestamp("completed_at"))
                ))
                .list();
    }

    public int touchElearningResume(long userId, long courseId) {
        return jdbcClient.sql("""
                UPDATE learner_elearning_progress
                SET last_learning_at = CURRENT_TIMESTAMP,
                    status_code = CASE WHEN status_code = 'not_started' THEN 'in_progress' ELSE status_code END,
                    updated_at = CURRENT_TIMESTAMP
                WHERE user_id = :userId
                  AND elearning_course_id = :courseId
                """)
                .param("userId", userId)
                .param("courseId", courseId)
                .update();
    }

    public long countMaterials(long userId, String keyword, String type) {
        SqlParts parts = materialWhere(keyword, type);
        return jdbcClient.sql("SELECT COUNT(*) FROM learning_materials lm " + parts.whereClause())
                .params(parts.params())
                .query(Long.class)
                .single();
    }

    public List<MaterialItem> findMaterials(long userId, String keyword, String type, int limit, int offset) {
        SqlParts parts = materialWhere(keyword, type);
        return jdbcClient.sql("""
                SELECT
                    lm.learning_material_id,
                    lm.title,
                    lm.material_type_code,
                    lm.summary,
                    lm.detail_url,
                    lm.view_count,
                    lm.created_at,
                    COALESCE(reactions.like_count, 0) AS like_count,
                    COALESCE(reactions.bookmark_count, 0) AS bookmark_count,
                    COALESCE(user_reactions.liked, 0) AS liked,
                    COALESCE(user_reactions.bookmarked, 0) AS bookmarked
                FROM learning_materials lm
                LEFT JOIN (
                    SELECT
                        learning_material_id,
                        SUM(CASE WHEN reaction_type_code = 'like' THEN 1 ELSE 0 END) AS like_count,
                        SUM(CASE WHEN reaction_type_code = 'bookmark' THEN 1 ELSE 0 END) AS bookmark_count
                    FROM learning_material_reactions
                    GROUP BY learning_material_id
                ) reactions ON reactions.learning_material_id = lm.learning_material_id
                LEFT JOIN (
                    SELECT
                        learning_material_id,
                        MAX(CASE WHEN reaction_type_code = 'like' THEN 1 ELSE 0 END) AS liked,
                        MAX(CASE WHEN reaction_type_code = 'bookmark' THEN 1 ELSE 0 END) AS bookmarked
                    FROM learning_material_reactions
                    WHERE user_id = :userId
                    GROUP BY learning_material_id
                ) user_reactions ON user_reactions.learning_material_id = lm.learning_material_id
                """ + parts.whereClause() + """
                ORDER BY lm.created_at DESC, lm.learning_material_id DESC
                LIMIT :limit OFFSET :offset
                """)
                .params(parts.params())
                .param("userId", userId)
                .param("limit", limit)
                .param("offset", offset)
                .query((rs, rowNum) -> new MaterialItem(
                        rs.getLong("learning_material_id"),
                        rs.getString("title"),
                        rs.getString("material_type_code"),
                        rs.getString("summary"),
                        rs.getString("detail_url"),
                        rs.getInt("view_count"),
                        toOffset(rs.getTimestamp("created_at")),
                        List.of(),
                        rs.getLong("like_count"),
                        rs.getLong("bookmark_count"),
                        rs.getBoolean("liked"),
                        rs.getBoolean("bookmarked")
                ))
                .list();
    }

    public List<MaterialResourceItem> findMaterialResources(List<Long> materialIds) {
        return jdbcClient.sql("""
                SELECT learning_material_resource_id, learning_material_id, resource_type_code,
                       resource_title, launch_mode_code, target_url, display_order
                FROM learning_material_resources
                WHERE learning_material_id IN (:materialIds)
                ORDER BY learning_material_id ASC, display_order ASC
                """)
                .param("materialIds", materialIds)
                .query((rs, rowNum) -> new MaterialResourceItem(
                        rs.getLong("learning_material_resource_id"),
                        rs.getLong("learning_material_id"),
                        rs.getString("resource_type_code"),
                        rs.getString("resource_title"),
                        rs.getString("launch_mode_code"),
                        rs.getString("target_url"),
                        rs.getInt("display_order")
                ))
                .list();
    }

    public long countQuests(long userId) {
        return jdbcClient.sql("SELECT COUNT(*) FROM quest_evaluations").query(Long.class).single();
    }

    public List<QuestItem> findQuests(long userId, int limit, int offset) {
        return jdbcClient.sql("""
                SELECT qe.quest_evaluation_id, qe.title, qe.quest_type_code, qe.task_classification_code,
                       qe.start_at, qe.end_at, qe.max_exp, qe.progress_status_code,
                       qs.submit_status_code, qs.result_status_code
                FROM quest_evaluations qe
                LEFT JOIN quest_submissions qs ON qs.quest_evaluation_id = qe.quest_evaluation_id
                    AND qs.user_id = :userId
                ORDER BY COALESCE(qe.end_at, qe.start_at) DESC, qe.quest_evaluation_id DESC
                LIMIT :limit OFFSET :offset
                """)
                .param("userId", userId)
                .param("limit", limit)
                .param("offset", offset)
                .query((rs, rowNum) -> new QuestItem(
                        rs.getLong("quest_evaluation_id"),
                        rs.getString("title"),
                        rs.getString("quest_type_code"),
                        rs.getString("task_classification_code"),
                        toOffset(rs.getTimestamp("start_at")),
                        toOffset(rs.getTimestamp("end_at")),
                        nullableInt(rs, "max_exp"),
                        rs.getString("progress_status_code"),
                        rs.getString("submit_status_code"),
                        rs.getString("result_status_code")
                ))
                .list();
    }

    public long countSurveys(long userId) {
        return jdbcClient.sql("SELECT COUNT(*) FROM surveys").query(Long.class).single();
    }

    public List<SurveyItem> findSurveys(long userId, int limit, int offset) {
        return jdbcClient.sql("""
                SELECT s.survey_id, s.title, s.survey_category_code, s.required_yn,
                       s.start_at, s.end_at, s.progress_status_code, sr.completed_yn
                FROM surveys s
                LEFT JOIN survey_responses sr ON sr.survey_id = s.survey_id AND sr.user_id = :userId
                ORDER BY COALESCE(s.end_at, s.start_at) DESC, s.survey_id DESC
                LIMIT :limit OFFSET :offset
                """)
                .param("userId", userId)
                .param("limit", limit)
                .param("offset", offset)
                .query((rs, rowNum) -> new SurveyItem(
                        rs.getLong("survey_id"),
                        rs.getString("title"),
                        rs.getString("survey_category_code"),
                        rs.getBoolean("required_yn"),
                        toOffset(rs.getTimestamp("start_at")),
                        toOffset(rs.getTimestamp("end_at")),
                        rs.getString("progress_status_code"),
                        rs.getBoolean("completed_yn")
                ))
                .list();
    }

    private JdbcClient.StatementSpec userSql(String suffix) {
        return jdbcClient.sql("""
                SELECT u.user_id, u.name, u.email, u.role_code,
                       COALESCE(cp.campus_name, c.campus_name) AS campus_name,
                       co.cohort_name, t.track_name
                FROM users u
                LEFT JOIN user_track_enrollments ute ON ute.user_id = u.user_id
                LEFT JOIN tracks t ON t.track_id = ute.track_id
                LEFT JOIN cohorts co ON co.cohort_id = ute.cohort_id
                LEFT JOIN user_class_enrollments uce ON uce.user_id = u.user_id
                LEFT JOIN class_groups cg ON cg.class_group_id = uce.class_group_id
                LEFT JOIN campuses cp ON cp.campus_id = cg.campus_id
                LEFT JOIN campuses c ON c.campus_id = cp.campus_id
                """ + suffix);
    }

    private UserProfile mapUser(ResultSet rs, int rowNum) throws SQLException {
        String role = "student".equals(rs.getString("role_code")) ? "learner" : rs.getString("role_code");
        return new UserProfile(
                rs.getLong("user_id"),
                rs.getString("name"),
                rs.getString("email"),
                role,
                rs.getString("campus_name"),
                rs.getString("cohort_name"),
                rs.getString("track_name")
        );
    }

    private NotificationItem mapNotification(ResultSet rs, int rowNum) throws SQLException {
        return new NotificationItem(
                rs.getLong("notification_id"),
                rs.getString("title"),
                rs.getString("body"),
                toOffset(rs.getTimestamp("created_at")),
                rs.getTimestamp("read_at") != null
        );
    }

    private JdbcClient.StatementSpec notificationSql(String suffix) {
        return jdbcClient.sql("""
                SELECT n.notification_id, n.title, n.body, n.created_at, nr.read_at
                FROM notification_recipients nr
                JOIN notifications n ON n.notification_id = nr.notification_id
                """ + suffix);
    }

    private SqlParts materialWhere(String keyword, String type) {
        StringBuilder where = new StringBuilder("WHERE 1 = 1");
        Map<String, Object> params = new java.util.LinkedHashMap<>();
        if (StringUtils.hasText(keyword)) {
            where.append(" AND (lm.title LIKE :keyword OR lm.summary LIKE :keyword)");
            params.put("keyword", "%" + keyword.trim() + "%");
        }
        if (StringUtils.hasText(type)) {
            where.append("""
                     AND (lm.material_type_code = :materialType OR EXISTS (
                         SELECT 1 FROM learning_material_resources lmr
                         WHERE lmr.learning_material_id = lm.learning_material_id
                           AND lmr.resource_type_code = :resourceType
                     ))
                    """);
            String normalizedType = type.trim().toLowerCase();
            params.put("materialType", normalizeMaterialType(normalizedType));
            params.put("resourceType", normalizedType);
        }
        return new SqlParts(" " + where, params);
    }

    private SqlParts attendanceWhere(long userId, LocalDate dateFrom, LocalDate dateTo, String status) {
        return attendanceWhere(null, userId, dateFrom, dateTo, status);
    }

    private SqlParts bookmarksWhere(long userId, String targetType) {
        StringBuilder where = new StringBuilder("WHERE lb.user_id = :userId");
        Map<String, Object> params = new java.util.HashMap<>();
        params.put("userId", userId);
        if (StringUtils.hasText(targetType)) {
            where.append(" AND LOWER(lb.target_type_code) = :targetType");
            params.put("targetType", targetType.trim().toLowerCase());
        }
        return new SqlParts(" " + where, params);
    }

    private BookmarkItem mapBookmark(ResultSet rs, int rowNum) throws SQLException {
        return new BookmarkItem(
                rs.getLong("bookmark_id"),
                rs.getString("target_type_code"),
                rs.getLong("target_id"),
                rs.getString("title_snapshot"),
                rs.getString("description_snapshot"),
                rs.getString("thumbnail_url"),
                rs.getString("target_url"),
                toOffset(rs.getTimestamp("created_at"))
        );
    }

    private BookmarkSnapshot mapBookmarkSnapshot(ResultSet rs, int rowNum) throws SQLException {
        return new BookmarkSnapshot(
                rs.getString("target_type"),
                rs.getLong("target_id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("thumbnail_url"),
                rs.getString("target_url")
        );
    }

    private SqlParts elearningProgressWhere(long userId, String status, String keyword) {
        StringBuilder where = new StringBuilder("WHERE lep.user_id = :userId AND ec.active_yn = TRUE");
        Map<String, Object> params = new java.util.HashMap<>();
        params.put("userId", userId);
        if (StringUtils.hasText(status)) {
            where.append(" AND LOWER(lep.status_code) = :status");
            params.put("status", status.trim().toLowerCase());
        }
        if (StringUtils.hasText(keyword)) {
            where.append("""
                     AND (LOWER(ec.title) LIKE :keyword
                       OR LOWER(ec.category) LIKE :keyword
                       OR LOWER(ec.provider) LIKE :keyword
                       OR LOWER(COALESCE(lep.last_lesson_title, '')) LIKE :keyword)
                    """);
            params.put("keyword", "%" + keyword.trim().toLowerCase() + "%");
        }
        return new SqlParts(" " + where, params);
    }

    private ElearningProgressItem mapElearningProgress(ResultSet rs, int rowNum) throws SQLException {
        return new ElearningProgressItem(
                rs.getLong("elearning_course_id"),
                rs.getString("title"),
                rs.getString("category"),
                rs.getString("thumbnail_url"),
                rs.getString("provider"),
                rs.getString("description"),
                rs.getInt("progress_percent"),
                rs.getInt("completed_lessons"),
                rs.getInt("total_lessons"),
                rs.getLong("total_duration_seconds"),
                rs.getString("last_lesson_title"),
                toOffset(rs.getTimestamp("last_learning_at")),
                rs.getString("status_code"),
                rs.getString("resume_url")
        );
    }

    private SqlParts attendanceWhere(String tableAlias, long userId, LocalDate dateFrom, LocalDate dateTo, String status) {
        String prefix = StringUtils.hasText(tableAlias) ? tableAlias + "." : "";
        StringBuilder where = new StringBuilder("WHERE ").append(prefix).append("user_id = :userId");
        Map<String, Object> params = new java.util.LinkedHashMap<>();
        params.put("userId", userId);
        if (dateFrom != null) {
            where.append(" AND ").append(prefix).append("attendance_date >= :dateFrom");
            params.put("dateFrom", dateFrom);
        }
        if (dateTo != null) {
            where.append(" AND ").append(prefix).append("attendance_date <= :dateTo");
            params.put("dateTo", dateTo);
        }
        if (StringUtils.hasText(status)) {
            where.append(" AND ").append(prefix).append("attendance_status_code = :status");
            params.put("status", status);
        }
        return new SqlParts(" " + where, params);
    }

    private String normalizeMaterialType(String type) {
        return switch (type) {
            case "ebook", "file" -> "document";
            default -> type;
        };
    }

    private long count(String sql, long userId) {
        return jdbcClient.sql(sql).param("userId", userId).query(Long.class).single();
    }

    private String firstTitle(String sql) {
        return jdbcClient.sql(sql).query(String.class).optional().orElse(null);
    }

    private Integer nullableInt(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }

    private Long nullableLong(ResultSet rs, String columnName) throws SQLException {
        long value = rs.getLong(columnName);
        return rs.wasNull() ? null : value;
    }

    private LocalTime nullableTime(ResultSet rs, String columnName) throws SQLException {
        Time value = rs.getTime(columnName);
        return value == null ? null : value.toLocalTime();
    }

    private LocalDate toLocalDate(ResultSet rs, String columnName) throws SQLException {
        return rs.getDate(columnName).toLocalDate();
    }

    private OffsetDateTime toOffset(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime().atZone(SEOUL_ZONE).toOffsetDateTime();
    }

    private record SqlParts(String whereClause, Map<String, Object> params) {
    }
}
