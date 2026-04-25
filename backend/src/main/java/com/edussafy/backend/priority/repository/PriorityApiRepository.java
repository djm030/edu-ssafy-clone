package com.edussafy.backend.priority.repository;

import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceAppealItem;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceRecordItem;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceSummary;
import com.edussafy.backend.priority.dto.PriorityDtos.CurriculumItem;
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
        return jdbcClient.sql("""
                SELECT
                    SUM(CASE WHEN attendance_status_code = 'present' THEN 1 ELSE 0 END) AS present_count,
                    SUM(CASE WHEN attendance_status_code = 'late' THEN 1 ELSE 0 END) AS late_count,
                    SUM(CASE WHEN attendance_status_code = 'absent' THEN 1 ELSE 0 END) AS absent_count
                FROM attendance_records
                WHERE user_id = :userId
                """)
                .param("userId", userId)
                .query((rs, rowNum) -> new AttendanceSummary(
                        rs.getInt("present_count"),
                        rs.getInt("late_count"),
                        rs.getInt("absent_count"),
                        true
                ))
                .single();
    }

    public List<AttendanceRecordItem> findAttendanceRecords(long userId) {
        return jdbcClient.sql(attendanceRecordSelect("WHERE ar.user_id = :userId ORDER BY ar.attendance_date DESC LIMIT 60"))
                .param("userId", userId)
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
        return jdbcClient.sql("""
                SELECT attendance_appeal_id, attendance_record_id, appeal_type_code, reason,
                       requested_status_code, approval_status_code, requested_at
                FROM attendance_appeals
                WHERE attendance_appeal_id = :attendanceAppealId
                """)
                .param("attendanceAppealId", attendanceAppealId)
                .query((rs, rowNum) -> new AttendanceAppealItem(
                        rs.getLong("attendance_appeal_id"),
                        rs.getLong("attendance_record_id"),
                        rs.getString("appeal_type_code"),
                        rs.getString("reason"),
                        rs.getString("requested_status_code"),
                        rs.getString("approval_status_code"),
                        toOffset(rs.getTimestamp("requested_at")),
                        false
                ))
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

    public int cancelAttendanceAppeal(long userId, long attendanceAppealId) {
        return jdbcClient.sql("""
                UPDATE attendance_appeals aa
                JOIN attendance_records ar ON ar.attendance_record_id = aa.attendance_record_id
                SET aa.approval_status_code = 'canceled',
                    aa.resolved_at = CURRENT_TIMESTAMP
                WHERE ar.user_id = :userId
                  AND aa.attendance_appeal_id = :attendanceAppealId
                  AND aa.approval_status_code = 'requested'
                """)
                .param("userId", userId)
                .param("attendanceAppealId", attendanceAppealId)
                .update();
    }

    private String attendanceAppealSelect(String suffix) {
        return """
                SELECT aa.attendance_appeal_id, aa.attendance_record_id, aa.appeal_type_code, aa.reason,
                       aa.requested_status_code, aa.approval_status_code, aa.requested_at
                FROM attendance_appeals aa
                JOIN attendance_records ar ON ar.attendance_record_id = aa.attendance_record_id
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
                SELECT learning_material_id, title, material_type_code, summary, detail_url, view_count, created_at
                FROM learning_materials lm
                """ + parts.whereClause() + """
                ORDER BY created_at DESC, learning_material_id DESC
                LIMIT :limit OFFSET :offset
                """)
                .params(parts.params())
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
                        List.of()
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

    private OffsetDateTime toOffset(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime().atZone(SEOUL_ZONE).toOffsetDateTime();
    }

    private record SqlParts(String whereClause, Map<String, Object> params) {
    }
}
