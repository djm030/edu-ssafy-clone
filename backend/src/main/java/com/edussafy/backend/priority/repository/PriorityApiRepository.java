package com.edussafy.backend.priority.repository;

import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceAppealItem;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceRecordItem;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceSummary;
import com.edussafy.backend.priority.dto.PriorityDtos.BookmarkItem;
import com.edussafy.backend.priority.dto.PriorityDtos.BookmarkSnapshot;
import com.edussafy.backend.priority.dto.PriorityDtos.CurriculumItem;
import com.edussafy.backend.priority.dto.PriorityDtos.DashboardBoardPost;
import com.edussafy.backend.priority.dto.PriorityDtos.CurriculumScheduleRow;
import com.edussafy.backend.priority.dto.PriorityDtos.DocumentAttachmentItem;
import com.edussafy.backend.priority.dto.PriorityDtos.DocumentRequestDetail;
import com.edussafy.backend.priority.dto.PriorityDtos.DocumentRequestItem;
import com.edussafy.backend.priority.dto.PriorityDtos.EducationAttendanceSummary;
import com.edussafy.backend.priority.dto.PriorityDtos.EducationLearningSummary;
import com.edussafy.backend.priority.dto.PriorityDtos.EducationPointSummary;
import com.edussafy.backend.priority.dto.PriorityDtos.EducationQuestSummary;
import com.edussafy.backend.priority.dto.PriorityDtos.EbookAccessLogItem;
import com.edussafy.backend.priority.dto.PriorityDtos.EbookItem;
import com.edussafy.backend.priority.dto.PriorityDtos.ElearningLessonItem;
import com.edussafy.backend.priority.dto.PriorityDtos.ElearningProgressDetail;
import com.edussafy.backend.priority.dto.PriorityDtos.ElearningProgressItem;
import com.edussafy.backend.priority.dto.PriorityDtos.LevelSummary;
import com.edussafy.backend.priority.dto.PriorityDtos.LevelHistoryItem;
import com.edussafy.backend.priority.dto.PriorityDtos.LiveSessionItem;
import com.edussafy.backend.priority.dto.PriorityDtos.LiveSessionJoinLogItem;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialItem;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialResourceItem;
import com.edussafy.backend.priority.dto.PriorityDtos.NotificationItem;
import com.edussafy.backend.priority.dto.PriorityDtos.PledgeAgreementItem;
import com.edussafy.backend.priority.dto.PriorityDtos.PledgeItem;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestItem;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestListSummary;
import com.edussafy.backend.priority.dto.PriorityDtos.ReplayItem;
import com.edussafy.backend.priority.dto.PriorityDtos.ReplayWatchLogItem;
import com.edussafy.backend.priority.dto.PriorityDtos.RequiredStudyItem;
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

    public Optional<String> findLevelName(long userId) {
        return jdbcClient.sql("""
                SELECT COALESCE(NULLIF(TRIM(level_name), ''), CONCAT('Lv.', COALESCE(level_no, 1))) AS level_name
                FROM user_level_statuses
                WHERE user_id = :userId
                """)
                .param("userId", userId)
                .query(String.class)
                .optional();
    }

    public List<LevelHistoryItem> findLevelHistory(long userId, int limit) {
        return jdbcClient.sql("""
                SELECT snapshot_date, rank_no, exp, scholarship_point
                FROM user_rank_snapshots
                WHERE user_id = :userId
                ORDER BY snapshot_date DESC
                LIMIT :limit
                """)
                .param("userId", userId)
                .param("limit", Math.max(1, limit))
                .query((rs, rowNum) -> new LevelHistoryItem(
                        rs.getObject("snapshot_date", LocalDate.class),
                        rs.getInt("rank_no"),
                        rs.getInt("exp"),
                        rs.getInt("scholarship_point")
                ))
                .list();
    }

    public EducationAttendanceSummary findEducationAttendanceSummary(long userId, LocalDate today) {
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate nextMonthStart = monthStart.plusMonths(1);
        return jdbcClient.sql("""
                SELECT
                    SUM(CASE WHEN ar.attendance_status_code = 'present' THEN 1 ELSE 0 END) AS present_days,
                    SUM(CASE WHEN ar.attendance_status_code = 'late' THEN 1 ELSE 0 END) AS late_days,
                    SUM(CASE WHEN ar.attendance_status_code = 'absent' THEN 1 ELSE 0 END) AS absent_days,
                    (
                        SELECT COUNT(*)
                        FROM attendance_appeals aa
                        JOIN attendance_records aar ON aar.attendance_record_id = aa.attendance_record_id
                        WHERE aar.user_id = :userId
                          AND aa.approval_status_code = 'requested'
                    ) AS appeal_pending_count
                FROM attendance_records ar
                WHERE ar.user_id = :userId
                  AND ar.attendance_date >= :monthStart
                  AND ar.attendance_date < :nextMonthStart
                """)
                .param("userId", userId)
                .param("monthStart", monthStart)
                .param("nextMonthStart", nextMonthStart)
                .query((rs, rowNum) -> new EducationAttendanceSummary(
                        monthStart.toString().substring(0, 7),
                        rs.getInt("present_days"),
                        rs.getInt("late_days"),
                        rs.getInt("absent_days"),
                        rs.getLong("appeal_pending_count")
                ))
                .single();
    }

    public EducationLearningSummary findEducationLearningSummary(long userId) {
        return jdbcClient.sql("""
                SELECT
                    SUM(CASE WHEN lep.status_code = 'in_progress' THEN 1 ELSE 0 END) AS in_progress_elearning_count,
                    SUM(CASE WHEN lep.status_code = 'completed' THEN 1 ELSE 0 END) AS completed_required_study_count,
                    COUNT(ec.elearning_course_id) AS total_required_study_count,
                    COALESCE(SUM(CASE WHEN lep.status_code IN ('in_progress', 'completed')
                        THEN COALESCE(ec.total_duration_seconds, 0) * COALESCE(lep.progress_percent, 0) / 100
                        ELSE 0 END), 0) DIV 60 AS replay_watch_minutes
                FROM learner_elearning_progress lep
                JOIN elearning_courses ec ON ec.elearning_course_id = lep.elearning_course_id
                WHERE lep.user_id = :userId
                  AND ec.active_yn = TRUE
                """)
                .param("userId", userId)
                .query((rs, rowNum) -> new EducationLearningSummary(
                        rs.getLong("in_progress_elearning_count"),
                        rs.getLong("completed_required_study_count"),
                        rs.getLong("total_required_study_count"),
                        rs.getLong("replay_watch_minutes")
                ))
                .single();
    }

    public EducationQuestSummary findEducationQuestSummary(long userId) {
        return jdbcClient.sql("""
                SELECT
                    SUM(CASE WHEN qe.progress_status_code IN ('scheduled', 'in_progress') THEN 1 ELSE 0 END) AS open_count,
                    SUM(CASE WHEN qs.submit_status_code IN ('submitted', 'done') THEN 1 ELSE 0 END) AS submitted_count,
                    SUM(CASE
                        WHEN COALESCE(qs.submit_status_code, 'not_submitted') NOT IN ('submitted', 'done')
                         AND qe.end_at IS NOT NULL
                         AND qe.end_at < CURRENT_TIMESTAMP THEN 1
                        ELSE 0
                    END) AS late_count
                FROM quest_evaluations qe
                LEFT JOIN quest_submissions qs ON qs.quest_evaluation_id = qe.quest_evaluation_id
                    AND qs.user_id = :userId
                """)
                .param("userId", userId)
                .query((rs, rowNum) -> new EducationQuestSummary(
                        rs.getLong("open_count"),
                        rs.getLong("submitted_count"),
                        rs.getLong("late_count")
                ))
                .single();
    }

    public Optional<EducationPointSummary> findEducationPointSummary(long userId) {
        return jdbcClient.sql("""
                SELECT
                    COALESCE(level_name, CONCAT('Lv.', COALESCE(level_no, 1))) AS level_name,
                    COALESCE(level_no, 1) AS level_no,
                    COALESCE(exp, 0) AS exp,
                    COALESCE(scholarship_point, 0) AS scholarship_point
                FROM user_level_statuses
                WHERE user_id = :userId
                LIMIT 1
                """)
                .param("userId", userId)
                .query((rs, rowNum) -> new EducationPointSummary(
                        rs.getInt("scholarship_point"),
                        rs.getInt("exp"),
                        rs.getString("level_name")
                ))
                .optional();
    }

    public long countEbooks(long userId) {
        return jdbcClient.sql("""
                SELECT COUNT(*)
                FROM ssafy_ebooks
                WHERE active_yn = TRUE
                """)
                .query(Long.class)
                .single();
    }

    public List<EbookItem> findEbooks(long userId, int limit, int offset) {
        return jdbcClient.sql(ebookSelect("""
                WHERE e.active_yn = TRUE
                ORDER BY e.created_at DESC, e.ebook_id DESC
                LIMIT :limit OFFSET :offset
                """))
                .param("userId", userId)
                .param("limit", limit)
                .param("offset", offset)
                .query(this::mapEbook)
                .list();
    }

    public Optional<EbookItem> findEbook(long userId, long ebookId) {
        return jdbcClient.sql(ebookSelect("""
                WHERE e.ebook_id = :ebookId
                  AND e.active_yn = TRUE
                LIMIT 1
                """))
                .param("userId", userId)
                .param("ebookId", ebookId)
                .query(this::mapEbook)
                .optional();
    }

    public long createEbookAccessLog(long userId, long ebookId) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql("""
                INSERT INTO ssafy_ebook_access_logs (ebook_id, user_id)
                VALUES (:ebookId, :userId)
                """)
                .param("ebookId", ebookId)
                .param("userId", userId)
                .update(keyHolder, "ebook_access_log_id");
        Number key = keyHolder.getKey();
        return key == null ? 0L : key.longValue();
    }

    public Optional<EbookAccessLogItem> findEbookAccessLog(long userId, long ebookId, long accessLogId) {
        return jdbcClient.sql("""
                SELECT ebook_access_log_id, ebook_id, accessed_at
                FROM ssafy_ebook_access_logs
                WHERE ebook_access_log_id = :accessLogId
                  AND ebook_id = :ebookId
                  AND user_id = :userId
                LIMIT 1
                """)
                .param("accessLogId", accessLogId)
                .param("ebookId", ebookId)
                .param("userId", userId)
                .query((rs, rowNum) -> new EbookAccessLogItem(
                        rs.getLong("ebook_access_log_id"),
                        rs.getLong("ebook_id"),
                        toOffset(rs.getTimestamp("accessed_at"))
                ))
                .optional();
    }

    public long countRequiredStudies(long userId) {
        return jdbcClient.sql("""
                SELECT COUNT(*)
                FROM required_studies rs
                """ + requiredStudyVisibilityWhere())
                .param("userId", userId)
                .query(Long.class)
                .single();
    }

    public List<RequiredStudyItem> findRequiredStudies(long userId, int limit, int offset) {
        return jdbcClient.sql(requiredStudySelect(requiredStudyVisibilityWhere() + """
                ORDER BY CASE WHEN rs.due_at IS NULL THEN 1 ELSE 0 END,
                         rs.due_at ASC,
                         rs.required_study_id DESC
                LIMIT :limit OFFSET :offset
                """))
                .param("userId", userId)
                .param("limit", limit)
                .param("offset", offset)
                .query(this::mapRequiredStudy)
                .list();
    }

    public Optional<RequiredStudyItem> findRequiredStudy(long userId, long studyId) {
        return jdbcClient.sql(requiredStudySelect(requiredStudyVisibilityWhere() + """
                AND rs.required_study_id = :studyId
                LIMIT 1
                """))
                .param("userId", userId)
                .param("studyId", studyId)
                .query(this::mapRequiredStudy)
                .optional();
    }

    public int completeRequiredStudy(long userId, long studyId) {
        return jdbcClient.sql("""
                INSERT INTO learner_required_study_progress (
                    user_id,
                    required_study_id,
                    status_code,
                    progress_percent,
                    completed_at
                )
                VALUES (
                    :userId,
                    :studyId,
                    'completed',
                    100,
                    CURRENT_TIMESTAMP
                )
                ON DUPLICATE KEY UPDATE
                    status_code = 'completed',
                    progress_percent = 100,
                    completed_at = COALESCE(completed_at, CURRENT_TIMESTAMP),
                    updated_at = CURRENT_TIMESTAMP
                """)
                .param("userId", userId)
                .param("studyId", studyId)
                .update();
    }

    public List<LiveSessionItem> findTodayLiveSessions(long userId) {
        return jdbcClient.sql(liveSessionSelect(liveSessionVisibilityWhere() + """
                AND DATE(ls.starts_at) = CURRENT_DATE
                ORDER BY ls.starts_at ASC, ls.live_session_id ASC
                """))
                .param("userId", userId)
                .query(this::mapLiveSession)
                .list();
    }

    public Optional<LiveSessionItem> findCurrentLiveSession(long userId) {
        return jdbcClient.sql(liveSessionSelect(liveSessionVisibilityWhere() + """
                AND ls.starts_at <= CURRENT_TIMESTAMP
                AND ls.ends_at > CURRENT_TIMESTAMP
                ORDER BY ls.starts_at DESC, ls.live_session_id DESC
                LIMIT 1
                """))
                .param("userId", userId)
                .query(this::mapLiveSession)
                .optional();
    }

    public Optional<LiveSessionItem> findLiveSession(long userId, long sessionId) {
        return jdbcClient.sql(liveSessionSelect(liveSessionVisibilityWhere() + """
                AND ls.live_session_id = :sessionId
                LIMIT 1
                """))
                .param("userId", userId)
                .param("sessionId", sessionId)
                .query(this::mapLiveSession)
                .optional();
    }

    public long createLiveSessionJoinLog(long userId, long sessionId) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql("""
                INSERT INTO live_session_join_logs (live_session_id, user_id)
                VALUES (:sessionId, :userId)
                """)
                .param("sessionId", sessionId)
                .param("userId", userId)
                .update(keyHolder, "live_session_join_log_id");
        Number key = keyHolder.getKey();
        return key == null ? 0L : key.longValue();
    }

    public Optional<LiveSessionJoinLogItem> findLiveSessionJoinLog(long userId, long sessionId, long joinLogId) {
        return jdbcClient.sql("""
                SELECT live_session_join_log_id, live_session_id, joined_at
                FROM live_session_join_logs
                WHERE live_session_join_log_id = :joinLogId
                  AND live_session_id = :sessionId
                  AND user_id = :userId
                LIMIT 1
                """)
                .param("joinLogId", joinLogId)
                .param("sessionId", sessionId)
                .param("userId", userId)
                .query((rs, rowNum) -> new LiveSessionJoinLogItem(
                        rs.getLong("live_session_join_log_id"),
                        rs.getLong("live_session_id"),
                        toOffset(rs.getTimestamp("joined_at"))
                ))
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

    private String curriculumWeekScheduleSelect(String suffix) {
        return """
                SELECT
                    cs.curriculum_schedule_id AS id,
                    cs.term_id,
                    t.term_name AS semester,
                    cs.content_scope_id,
                    cs.week_no,
                    COALESCE(scope_track.track_name, class_track.track_name, '공통') AS track,
                    cs.class_date,
                    cs.start_time,
                    cs.end_time,
                    COALESCE(cs.curriculum_type_code, 'lecture') AS session_type,
                    cs.topic AS title,
                    cs.instructor_name,
                    cs.classroom
                FROM curriculum_schedules cs
                JOIN terms t ON t.term_id = cs.term_id
                JOIN content_scopes scope ON scope.content_scope_id = cs.content_scope_id
                LEFT JOIN tracks scope_track ON scope_track.track_id = scope.track_id
                LEFT JOIN class_groups cg ON cg.class_group_id = scope.class_group_id
                LEFT JOIN tracks class_track ON class_track.track_id = cg.track_id
                WHERE (
                    scope.scope_type_code = 'all'
                    OR scope.user_id = :userId
                    OR scope.class_group_id IN (
                        SELECT class_group_id FROM user_class_enrollments WHERE user_id = :userId
                    )
                    OR EXISTS (
                        SELECT 1
                        FROM user_track_enrollments ute
                        WHERE ute.user_id = :userId
                          AND (scope.track_id IS NULL OR scope.track_id = ute.track_id)
                          AND (scope.cohort_id IS NULL OR scope.cohort_id = ute.cohort_id)
                          AND scope.scope_type_code IN ('track', 'cohort', 'track_cohort')
                    )
                )
                """ + suffix;
    }

    private CurriculumScheduleRow mapCurriculumScheduleRow(ResultSet rs, int rowNum) throws SQLException {
        return new CurriculumScheduleRow(
                rs.getLong("id"),
                rs.getLong("term_id"),
                rs.getString("semester"),
                rs.getLong("content_scope_id"),
                nullableInt(rs, "week_no"),
                rs.getString("track"),
                rs.getDate("class_date") == null ? null : rs.getDate("class_date").toLocalDate(),
                nullableTime(rs, "start_time"),
                nullableTime(rs, "end_time"),
                rs.getString("session_type"),
                rs.getString("title"),
                rs.getString("instructor_name"),
                rs.getString("classroom")
        );
    }

    private SqlParts replayWhere(long userId, String scope, String keyword) {
        Map<String, Object> params = new java.util.LinkedHashMap<>();
        params.put("userId", userId);
        StringBuilder where = new StringBuilder("""
                WHERE lr.published_at IS NOT NULL
                  AND (
                      cs_scope.scope_type_code = 'all'
                      OR cs_scope.user_id = :userId
                      OR cs_scope.class_group_id IN (
                          SELECT class_group_id FROM user_class_enrollments WHERE user_id = :userId
                      )
                      OR cs_scope.track_id IN (
                          SELECT track_id FROM user_track_enrollments WHERE user_id = :userId
                      )
                      OR cs_scope.cohort_id IN (
                          SELECT cohort_id FROM user_track_enrollments WHERE user_id = :userId
                      )
                      OR (cs_scope.track_id, cs_scope.cohort_id) IN (
                          SELECT track_id, cohort_id FROM user_track_enrollments WHERE user_id = :userId
                      )
                  )
                """);
        if ("my".equals(scope)) {
            where.append("""
                  AND (
                      cs_scope.user_id = :userId
                      OR cs_scope.class_group_id IN (
                          SELECT class_group_id FROM user_class_enrollments WHERE user_id = :userId
                      )
                      OR (cs_scope.track_id, cs_scope.cohort_id) IN (
                          SELECT track_id, cohort_id FROM user_track_enrollments WHERE user_id = :userId
                      )
                  )
                """);
        }
        if (StringUtils.hasText(keyword)) {
            where.append("""
                  AND (
                      LOWER(lr.title) LIKE :keyword
                      OR LOWER(COALESCE(cs.topic, '')) LIKE :keyword
                      OR LOWER(COALESCE(cs.instructor_name, '')) LIKE :keyword
                  )
                """);
            params.put("keyword", "%" + keyword.trim().toLowerCase() + "%");
        }
        return new SqlParts(" " + where, params);
    }

    private String replaySelect(String suffix) {
        return """
                SELECT
                    lr.lecture_replay_id,
                    lr.curriculum_schedule_id,
                    lr.title,
                    lr.version_no,
                    lr.published_at,
                    COALESCE(cs.curriculum_type_code, 'lecture') AS category,
                    cs.instructor_name,
                    cs.classroom,
                    cs.class_date,
                    cs_scope.scope_type_code,
                    latest_watch.last_watched_at,
                    COALESCE(watch_counts.watch_count, 0) AS watch_count
                FROM lecture_replays lr
                JOIN curriculum_schedules cs ON cs.curriculum_schedule_id = lr.curriculum_schedule_id
                JOIN content_scopes cs_scope ON cs_scope.content_scope_id = cs.content_scope_id
                LEFT JOIN (
                    SELECT lecture_replay_id, MAX(watched_at) AS last_watched_at
                    FROM lecture_replay_watch_logs
                    WHERE user_id = :userId
                    GROUP BY lecture_replay_id
                ) latest_watch ON latest_watch.lecture_replay_id = lr.lecture_replay_id
                LEFT JOIN (
                    SELECT lecture_replay_id, COUNT(*) AS watch_count
                    FROM lecture_replay_watch_logs
                    WHERE user_id = :userId
                    GROUP BY lecture_replay_id
                ) watch_counts ON watch_counts.lecture_replay_id = lr.lecture_replay_id
                """ + suffix;
    }

    private ReplayItem mapReplay(ResultSet rs, int rowNum) throws SQLException {
        return new ReplayItem(
                rs.getLong("lecture_replay_id"),
                rs.getLong("curriculum_schedule_id"),
                rs.getString("title"),
                rs.getInt("version_no"),
                toOffset(rs.getTimestamp("published_at")),
                rs.getString("category"),
                rs.getString("instructor_name"),
                rs.getString("classroom"),
                toLocalDate(rs, "class_date"),
                rs.getString("scope_type_code"),
                toOffset(rs.getTimestamp("last_watched_at")),
                rs.getLong("watch_count")
        );
    }

    private String ebookSelect(String suffix) {
        return """
                SELECT
                    e.ebook_id,
                    e.title,
                    e.description,
                    e.thumbnail_url,
                    e.category,
                    e.external_url,
                    e.created_at,
                    latest_access.last_accessed_at,
                    COALESCE(access_counts.access_count, 0) AS access_count
                FROM ssafy_ebooks e
                LEFT JOIN (
                    SELECT ebook_id, MAX(accessed_at) AS last_accessed_at
                    FROM ssafy_ebook_access_logs
                    WHERE user_id = :userId
                    GROUP BY ebook_id
                ) latest_access ON latest_access.ebook_id = e.ebook_id
                LEFT JOIN (
                    SELECT ebook_id, COUNT(*) AS access_count
                    FROM ssafy_ebook_access_logs
                    WHERE user_id = :userId
                    GROUP BY ebook_id
                ) access_counts ON access_counts.ebook_id = e.ebook_id
                """ + suffix;
    }

    private EbookItem mapEbook(ResultSet rs, int rowNum) throws SQLException {
        return new EbookItem(
                rs.getLong("ebook_id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("thumbnail_url"),
                rs.getString("category"),
                rs.getString("external_url"),
                toOffset(rs.getTimestamp("created_at")),
                toOffset(rs.getTimestamp("last_accessed_at")),
                rs.getLong("access_count")
        );
    }

    private String requiredStudyVisibilityWhere() {
        return """
                WHERE rs.active_yn = TRUE
                  AND (
                      rs.required_for_track IS NULL
                      OR EXISTS (
                          SELECT 1
                          FROM user_track_enrollments ute
                          JOIN tracks t ON t.track_id = ute.track_id
                          WHERE ute.user_id = :userId
                            AND t.track_name = rs.required_for_track
                      )
                  )
                """;
    }

    private String requiredStudySelect(String suffix) {
        return """
                SELECT
                    rs.required_study_id,
                    rs.title,
                    rs.description,
                    rs.category,
                    rs.required_for_track,
                    rs.due_at,
                    rs.content_type,
                    rs.content_url,
                    CASE
                        WHEN lrsp.status_code = 'completed' THEN 'completed'
                        WHEN rs.due_at IS NOT NULL AND rs.due_at < CURRENT_TIMESTAMP THEN 'overdue'
                        WHEN lrsp.status_code IS NOT NULL THEN lrsp.status_code
                        ELSE 'not_started'
                    END AS effective_status,
                    COALESCE(lrsp.progress_percent, 0) AS progress_percent,
                    lrsp.completed_at
                FROM required_studies rs
                LEFT JOIN learner_required_study_progress lrsp
                    ON lrsp.required_study_id = rs.required_study_id
                   AND lrsp.user_id = :userId
                """ + suffix;
    }

    private RequiredStudyItem mapRequiredStudy(ResultSet rs, int rowNum) throws SQLException {
        return new RequiredStudyItem(
                rs.getLong("required_study_id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("category"),
                rs.getString("required_for_track"),
                toOffset(rs.getTimestamp("due_at")),
                rs.getString("content_type"),
                rs.getString("content_url"),
                rs.getString("effective_status"),
                rs.getInt("progress_percent"),
                toOffset(rs.getTimestamp("completed_at"))
        );
    }

    private String liveSessionVisibilityWhere() {
        return """
                WHERE ls.active_yn = TRUE
                  AND (
                      ls.track IS NULL
                      OR EXISTS (
                          SELECT 1
                          FROM user_track_enrollments ute
                          JOIN tracks t ON t.track_id = ute.track_id
                          WHERE ute.user_id = :userId
                            AND t.track_name = ls.track
                      )
                  )
                  AND (
                      ls.cohort IS NULL
                      OR EXISTS (
                          SELECT 1
                          FROM user_track_enrollments ute
                          JOIN cohorts c ON c.cohort_id = ute.cohort_id
                          WHERE ute.user_id = :userId
                            AND c.cohort_name = ls.cohort
                      )
                  )
                  AND (
                      ls.class_room IS NULL
                      OR EXISTS (
                          SELECT 1
                          FROM user_class_enrollments uce
                          JOIN class_groups cg ON cg.class_group_id = uce.class_group_id
                          WHERE uce.user_id = :userId
                            AND cg.class_name = ls.class_room
                      )
                  )
                """;
    }

    private String liveSessionSelect(String suffix) {
        return """
                SELECT
                    ls.live_session_id,
                    ls.title,
                    ls.track,
                    ls.cohort,
                    ls.class_room,
                    ls.starts_at,
                    ls.ends_at,
                    ls.join_url,
                    CASE
                        WHEN CURRENT_TIMESTAMP < ls.starts_at THEN 'scheduled'
                        WHEN CURRENT_TIMESTAMP >= ls.starts_at AND CURRENT_TIMESTAMP < ls.ends_at THEN 'live'
                        ELSE 'ended'
                    END AS effective_status,
                    ls.created_at,
                    latest_join.last_joined_at,
                    COALESCE(join_counts.join_count, 0) AS join_count
                FROM live_sessions ls
                LEFT JOIN (
                    SELECT live_session_id, MAX(joined_at) AS last_joined_at
                    FROM live_session_join_logs
                    WHERE user_id = :userId
                    GROUP BY live_session_id
                ) latest_join ON latest_join.live_session_id = ls.live_session_id
                LEFT JOIN (
                    SELECT live_session_id, COUNT(*) AS join_count
                    FROM live_session_join_logs
                    WHERE user_id = :userId
                    GROUP BY live_session_id
                ) join_counts ON join_counts.live_session_id = ls.live_session_id
                """ + suffix;
    }

    private LiveSessionItem mapLiveSession(ResultSet rs, int rowNum) throws SQLException {
        return new LiveSessionItem(
                rs.getLong("live_session_id"),
                rs.getString("title"),
                rs.getString("track"),
                rs.getString("cohort"),
                rs.getString("class_room"),
                toOffset(rs.getTimestamp("starts_at")),
                toOffset(rs.getTimestamp("ends_at")),
                rs.getString("join_url"),
                rs.getString("effective_status"),
                toOffset(rs.getTimestamp("created_at")),
                toOffset(rs.getTimestamp("last_joined_at")),
                rs.getLong("join_count")
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

    public List<DashboardBoardPost> findDashboardPosts(String boardCode, int limit) {
        return jdbcClient.sql("""
                SELECT
                    p.board_post_id,
                    b.board_code,
                    p.title,
                    COALESCE(u.name, '운영자') AS author_label,
                    p.created_at,
                    p.notice_yn
                FROM board_posts p
                JOIN boards b ON b.board_id = p.board_id
                LEFT JOIN users u ON u.user_id = p.author_user_id
                WHERE b.board_code = :boardCode
                ORDER BY p.notice_yn DESC, p.created_at DESC, p.board_post_id DESC
                LIMIT :limit
                """)
                .param("boardCode", boardCode)
                .param("limit", limit)
                .query((rs, rowNum) -> new DashboardBoardPost(
                        rs.getLong("board_post_id"),
                        rs.getString("board_code"),
                        rs.getString("title"),
                        rs.getString("author_label"),
                        toOffset(rs.getTimestamp("created_at")),
                        rs.getBoolean("notice_yn"),
                        dashboardPostPath(rs.getString("board_code"), rs.getLong("board_post_id"))
                ))
                .list();
    }

    private String dashboardPostPath(String boardCode, long postId) {
        if ("notice".equalsIgnoreCase(boardCode)) {
            return "/help/notice/" + postId;
        }
        if ("faq".equalsIgnoreCase(boardCode)) {
            return "/help/faq/" + postId;
        }
        if ("anonymous".equalsIgnoreCase(boardCode)) {
            return "/community/anonymous/" + postId;
        }
        return "/community/free/" + postId;
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

    public List<CurriculumScheduleRow> findCurriculumWeekSchedules(
            long userId,
            String semester,
            String track
    ) {
        return jdbcClient.sql(curriculumWeekScheduleSelect("""
                AND (:semester IS NULL OR t.term_name = :semester OR CAST(t.term_id AS CHAR) = :semester)
                AND (:track IS NULL OR COALESCE(scope_track.track_name, class_track.track_name, '공통') = :track)
                ORDER BY COALESCE(t.start_date, DATE('1970-01-01')) DESC,
                         cs.week_no DESC,
                         cs.class_date,
                         cs.start_time,
                         cs.curriculum_schedule_id
                """))
                .param("userId", userId)
                .param("semester", normalizeBlank(semester))
                .param("track", normalizeBlank(track))
                .query(this::mapCurriculumScheduleRow)
                .list();
    }

    public List<CurriculumScheduleRow> findCurriculumWeekSchedules(long userId, long weekId) {
        return jdbcClient.sql("""
                SELECT
                    cs.curriculum_schedule_id AS id,
                    cs.term_id,
                    t.term_name AS semester,
                    cs.content_scope_id,
                    cs.week_no,
                    COALESCE(scope_track.track_name, class_track.track_name, '공통') AS track,
                    cs.class_date,
                    cs.start_time,
                    cs.end_time,
                    COALESCE(cs.curriculum_type_code, 'lecture') AS session_type,
                    cs.topic AS title,
                    cs.instructor_name,
                    cs.classroom
                FROM curriculum_schedules cs
                JOIN curriculum_schedules target
                  ON target.curriculum_schedule_id = :weekId
                 AND target.term_id = cs.term_id
                 AND target.content_scope_id = cs.content_scope_id
                 AND COALESCE(target.week_no, -1) = COALESCE(cs.week_no, -1)
                JOIN terms t ON t.term_id = cs.term_id
                JOIN content_scopes scope ON scope.content_scope_id = cs.content_scope_id
                LEFT JOIN tracks scope_track ON scope_track.track_id = scope.track_id
                LEFT JOIN class_groups cg ON cg.class_group_id = scope.class_group_id
                LEFT JOIN tracks class_track ON class_track.track_id = cg.track_id
                WHERE (
                    scope.scope_type_code = 'all'
                    OR scope.user_id = :userId
                    OR scope.class_group_id IN (
                        SELECT class_group_id FROM user_class_enrollments WHERE user_id = :userId
                    )
                    OR EXISTS (
                        SELECT 1
                        FROM user_track_enrollments ute
                        WHERE ute.user_id = :userId
                          AND (scope.track_id IS NULL OR scope.track_id = ute.track_id)
                          AND (scope.cohort_id IS NULL OR scope.cohort_id = ute.cohort_id)
                          AND scope.scope_type_code IN ('track', 'cohort', 'track_cohort')
                    )
                )
                ORDER BY cs.class_date, cs.start_time, cs.curriculum_schedule_id
                """)
                .param("userId", userId)
                .param("weekId", weekId)
                .query(this::mapCurriculumScheduleRow)
                .list();
    }

    public List<ReplayItem> findReplays(long userId) {
        return findReplays(userId, "my", null);
    }

    public List<ReplayItem> findReplays(long userId, String scope, String keyword) {
        SqlParts parts = replayWhere(userId, scope, keyword);
        return jdbcClient.sql(replaySelect(parts.whereClause() + """
                ORDER BY COALESCE(lr.published_at, lr.created_at) DESC, lr.lecture_replay_id DESC
                LIMIT 80
                """))
                .params(parts.params())
                .query(this::mapReplay)
                .list();
    }

    public Optional<ReplayItem> findReplay(long userId, long replayId) {
        SqlParts parts = replayWhere(userId, "all", null);
        return jdbcClient.sql(replaySelect(parts.whereClause() + """
                AND lr.lecture_replay_id = :replayId
                LIMIT 1
                """))
                .params(parts.params())
                .param("replayId", replayId)
                .query(this::mapReplay)
                .optional();
    }

    public long createReplayWatchLog(long userId, long replayId) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql("""
                INSERT INTO lecture_replay_watch_logs (lecture_replay_id, user_id)
                VALUES (:replayId, :userId)
                """)
                .param("replayId", replayId)
                .param("userId", userId)
                .update(keyHolder, "lecture_replay_watch_log_id");
        Number key = keyHolder.getKey();
        return key == null ? 0L : key.longValue();
    }

    public Optional<ReplayWatchLogItem> findReplayWatchLog(long userId, long replayId, long watchLogId) {
        return jdbcClient.sql("""
                SELECT lecture_replay_watch_log_id, lecture_replay_id, watched_at
                FROM lecture_replay_watch_logs
                WHERE lecture_replay_watch_log_id = :watchLogId
                  AND lecture_replay_id = :replayId
                  AND user_id = :userId
                LIMIT 1
                """)
                .param("watchLogId", watchLogId)
                .param("replayId", replayId)
                .param("userId", userId)
                .query((rs, rowNum) -> new ReplayWatchLogItem(
                        rs.getLong("lecture_replay_watch_log_id"),
                        rs.getLong("lecture_replay_id"),
                        toOffset(rs.getTimestamp("watched_at"))
                ))
                .optional();
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

    public long countDocumentRequests(long userId) {
        return jdbcClient.sql("""
                SELECT COUNT(*)
                FROM document_requests dr
                WHERE dr.active_yn = TRUE
                  AND (dr.starts_at IS NULL OR dr.starts_at <= CURRENT_TIMESTAMP)
                """)
                .query(Long.class)
                .single();
    }

    public List<DocumentRequestItem> findDocumentRequests(long userId, int limit, int offset) {
        return jdbcClient.sql("""
                SELECT
                    dr.document_request_id,
                    dr.title,
                    dr.description,
                    dr.category,
                    dr.required_yn,
                    dr.allowed_extensions,
                    dr.max_file_size_bytes,
                    dr.starts_at,
                    dr.due_at,
                    lds.document_submission_id,
                    COALESCE(lds.status_code, 'not_submitted') AS status_code,
                    lds.submitted_at,
                    lds.review_comment
                FROM document_requests dr
                LEFT JOIN learner_document_submissions lds
                    ON lds.document_request_id = dr.document_request_id
                   AND lds.user_id = :userId
                WHERE dr.active_yn = TRUE
                  AND (dr.starts_at IS NULL OR dr.starts_at <= CURRENT_TIMESTAMP)
                ORDER BY dr.required_yn DESC, dr.due_at ASC, dr.document_request_id ASC
                LIMIT :limit OFFSET :offset
                """)
                .param("userId", userId)
                .param("limit", limit)
                .param("offset", offset)
                .query(this::mapDocumentRequestItem)
                .list();
    }

    public Optional<DocumentRequestDetail> findDocumentRequestDetail(long userId, long requestId) {
        return jdbcClient.sql("""
                SELECT
                    dr.document_request_id,
                    dr.title,
                    dr.description,
                    dr.category,
                    dr.required_yn,
                    dr.allowed_extensions,
                    dr.max_file_size_bytes,
                    dr.starts_at,
                    dr.due_at,
                    lds.document_submission_id,
                    COALESCE(lds.status_code, 'not_submitted') AS status_code,
                    lds.submitted_at,
                    lds.reviewed_at,
                    lds.review_comment
                FROM document_requests dr
                LEFT JOIN learner_document_submissions lds
                    ON lds.document_request_id = dr.document_request_id
                   AND lds.user_id = :userId
                WHERE dr.document_request_id = :requestId
                  AND dr.active_yn = TRUE
                  AND (dr.starts_at IS NULL OR dr.starts_at <= CURRENT_TIMESTAMP)
                LIMIT 1
                """)
                .param("userId", userId)
                .param("requestId", requestId)
                .query(this::mapDocumentRequestDetail)
                .optional();
    }

    public List<DocumentAttachmentItem> findDocumentAttachmentsByRequestIds(long userId, List<Long> requestIds) {
        if (requestIds.isEmpty()) {
            return List.of();
        }
        return jdbcClient.sql("""
                SELECT
                    lda.learner_document_submission_id AS document_submission_id,
                    lds.document_request_id,
                    a.attachment_id,
                    a.original_filename,
                    a.storage_key,
                    a.mime_type,
                    a.file_size,
                    a.created_at
                FROM learner_document_submissions lds
                JOIN learner_document_attachments lda
                    ON lda.learner_document_submission_id = lds.document_submission_id
                JOIN attachments a ON a.attachment_id = lda.attachment_id
                WHERE lds.user_id = :userId
                  AND lds.document_request_id IN (:requestIds)
                ORDER BY a.created_at ASC, a.attachment_id ASC
                """)
                .param("userId", userId)
                .param("requestIds", requestIds)
                .query(this::mapDocumentAttachment)
                .list();
    }

    public long upsertDocumentSubmission(long userId, long requestId) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql("""
                INSERT INTO learner_document_submissions (
                    document_request_id,
                    user_id,
                    status_code,
                    submitted_at,
                    reviewed_at,
                    reviewed_by,
                    review_comment
                )
                VALUES (:requestId, :userId, 'submitted', CURRENT_TIMESTAMP, NULL, NULL, NULL)
                ON DUPLICATE KEY UPDATE
                    status_code = 'submitted',
                    submitted_at = CURRENT_TIMESTAMP,
                    reviewed_at = NULL,
                    reviewed_by = NULL,
                    review_comment = NULL,
                    updated_at = CURRENT_TIMESTAMP
                """)
                .param("requestId", requestId)
                .param("userId", userId)
                .update(keyHolder, "document_submission_id");
        Number key = keyHolder.getKey();
        if (key != null) {
            return key.longValue();
        }
        return jdbcClient.sql("""
                SELECT document_submission_id
                FROM learner_document_submissions
                WHERE document_request_id = :requestId AND user_id = :userId
                LIMIT 1
                """)
                .param("requestId", requestId)
                .param("userId", userId)
                .query(Long.class)
                .single();
    }

    public void linkDocumentSubmissionAttachment(long submissionId, long attachmentId) {
        jdbcClient.sql("""
                INSERT IGNORE INTO learner_document_attachments (learner_document_submission_id, attachment_id)
                VALUES (:submissionId, :attachmentId)
                """)
                .param("submissionId", submissionId)
                .param("attachmentId", attachmentId)
                .update();
    }

    public Optional<DocumentAttachmentItem> findDocumentAttachment(long userId, long submissionId, long attachmentId) {
        return jdbcClient.sql("""
                SELECT
                    lda.learner_document_submission_id AS document_submission_id,
                    lds.document_request_id,
                    a.attachment_id,
                    a.original_filename,
                    a.storage_key,
                    a.mime_type,
                    a.file_size,
                    a.created_at
                FROM learner_document_attachments lda
                JOIN learner_document_submissions lds
                    ON lds.document_submission_id = lda.learner_document_submission_id
                JOIN attachments a ON a.attachment_id = lda.attachment_id
                WHERE lds.user_id = :userId
                  AND lda.learner_document_submission_id = :submissionId
                  AND lda.attachment_id = :attachmentId
                LIMIT 1
                """)
                .param("userId", userId)
                .param("submissionId", submissionId)
                .param("attachmentId", attachmentId)
                .query(this::mapDocumentAttachment)
                .optional();
    }

    public int cancelDocumentSubmission(long userId, long requestId, long submissionId) {
        return jdbcClient.sql("""
                UPDATE learner_document_submissions
                SET status_code = 'canceled',
                    updated_at = CURRENT_TIMESTAMP
                WHERE user_id = :userId
                  AND document_request_id = :requestId
                  AND document_submission_id = :submissionId
                  AND reviewed_at IS NULL
                  AND status_code IN ('submitted', 'rejected')
                """)
                .param("userId", userId)
                .param("requestId", requestId)
                .param("submissionId", submissionId)
                .update();
    }

    public long countPledges(long userId) {
        return jdbcClient.sql("""
                SELECT COUNT(*)
                FROM pledge_documents pd
                WHERE pd.active_yn = TRUE
                  AND (pd.starts_at IS NULL OR pd.starts_at <= CURRENT_TIMESTAMP)
                """)
                .query(Long.class)
                .single();
    }

    public List<PledgeItem> findPledges(long userId, int limit, int offset) {
        return jdbcClient.sql("""
                SELECT
                    pd.pledge_document_id,
                    pd.title,
                    pd.content,
                    pd.version,
                    pd.required_yn,
                    pd.starts_at,
                    pd.due_at,
                    COALESCE(lpa.agreed_yn, FALSE) AS agreed_yn,
                    lpa.agreed_at,
                    lpa.version_snapshot
                FROM pledge_documents pd
                LEFT JOIN learner_pledge_agreements lpa
                    ON lpa.pledge_document_id = pd.pledge_document_id
                   AND lpa.user_id = :userId
                WHERE pd.active_yn = TRUE
                  AND (pd.starts_at IS NULL OR pd.starts_at <= CURRENT_TIMESTAMP)
                ORDER BY pd.required_yn DESC, pd.due_at ASC, pd.pledge_document_id ASC
                LIMIT :limit OFFSET :offset
                """)
                .param("userId", userId)
                .param("limit", limit)
                .param("offset", offset)
                .query(this::mapPledge)
                .list();
    }

    public Optional<PledgeItem> findPledge(long userId, long pledgeId) {
        return jdbcClient.sql("""
                SELECT
                    pd.pledge_document_id,
                    pd.title,
                    pd.content,
                    pd.version,
                    pd.required_yn,
                    pd.starts_at,
                    pd.due_at,
                    COALESCE(lpa.agreed_yn, FALSE) AS agreed_yn,
                    lpa.agreed_at,
                    lpa.version_snapshot
                FROM pledge_documents pd
                LEFT JOIN learner_pledge_agreements lpa
                    ON lpa.pledge_document_id = pd.pledge_document_id
                   AND lpa.user_id = :userId
                WHERE pd.pledge_document_id = :pledgeId
                  AND pd.active_yn = TRUE
                  AND (pd.starts_at IS NULL OR pd.starts_at <= CURRENT_TIMESTAMP)
                LIMIT 1
                """)
                .param("userId", userId)
                .param("pledgeId", pledgeId)
                .query(this::mapPledge)
                .optional();
    }

    public long upsertPledgeAgreement(long userId, PledgeItem pledge, boolean agreed, String ipHash, String userAgentHash) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql("""
                INSERT INTO learner_pledge_agreements (
                    pledge_document_id,
                    user_id,
                    agreed_yn,
                    agreed_at,
                    agreement_ip_hash,
                    user_agent_hash,
                    version_snapshot
                )
                VALUES (:pledgeId, :userId, :agreed, CURRENT_TIMESTAMP, :ipHash, :userAgentHash, :versionSnapshot)
                ON DUPLICATE KEY UPDATE
                    agreed_yn = VALUES(agreed_yn),
                    agreed_at = VALUES(agreed_at),
                    agreement_ip_hash = VALUES(agreement_ip_hash),
                    user_agent_hash = VALUES(user_agent_hash),
                    version_snapshot = VALUES(version_snapshot),
                    updated_at = CURRENT_TIMESTAMP
                """)
                .param("pledgeId", pledge.id())
                .param("userId", userId)
                .param("agreed", agreed)
                .param("ipHash", ipHash)
                .param("userAgentHash", userAgentHash)
                .param("versionSnapshot", pledge.version())
                .update(keyHolder, "pledge_agreement_id");
        Number key = keyHolder.getKey();
        if (key != null) {
            return key.longValue();
        }
        return jdbcClient.sql("""
                SELECT pledge_agreement_id
                FROM learner_pledge_agreements
                WHERE pledge_document_id = :pledgeId AND user_id = :userId
                LIMIT 1
                """)
                .param("pledgeId", pledge.id())
                .param("userId", userId)
                .query(Long.class)
                .single();
    }

    public Optional<PledgeAgreementItem> findPledgeAgreement(long userId, long pledgeId, long agreementId) {
        return jdbcClient.sql("""
                SELECT pledge_agreement_id, pledge_document_id, agreed_yn, agreed_at, version_snapshot
                FROM learner_pledge_agreements
                WHERE user_id = :userId
                  AND pledge_document_id = :pledgeId
                  AND pledge_agreement_id = :agreementId
                LIMIT 1
                """)
                .param("userId", userId)
                .param("pledgeId", pledgeId)
                .param("agreementId", agreementId)
                .query((rs, rowNum) -> new PledgeAgreementItem(
                        rs.getLong("pledge_agreement_id"),
                        rs.getLong("pledge_document_id"),
                        rs.getBoolean("agreed_yn"),
                        toOffset(rs.getTimestamp("agreed_at")),
                        rs.getString("version_snapshot")
                ))
                .optional();
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
        return countQuests(userId, null, null);
    }

    public long countQuests(long userId, String status, String keyword) {
        SqlParts parts = questWhere(userId, status, keyword);
        return jdbcClient.sql("""
                SELECT COUNT(*)
                FROM quest_evaluations qe
                LEFT JOIN quest_submissions qs ON qs.quest_evaluation_id = qe.quest_evaluation_id
                    AND qs.user_id = :userId
                """ + parts.whereClause())
                .params(parts.params())
                .query(Long.class)
                .single();
    }

    public QuestListSummary summarizeQuests(long userId, String keyword) {
        SqlParts parts = questWhere(userId, null, keyword);
        return jdbcClient.sql("""
                SELECT
                    COUNT(*) AS total_count,
                    COALESCE(SUM(CASE
                        WHEN (qs.quest_submission_id IS NULL
                              OR COALESCE(qs.submit_status_code, '') NOT IN ('submitted', 'done'))
                             AND COALESCE(qs.result_status_code, '') <> 'graded'
                             AND (qe.end_at IS NULL OR qe.end_at >= CURRENT_TIMESTAMP) THEN 1
                        ELSE 0
                    END), 0) AS progress_count,
                    COALESCE(SUM(CASE
                        WHEN COALESCE(qs.submit_status_code, '') IN ('submitted', 'done')
                             AND COALESCE(qs.result_status_code, '') <> 'graded' THEN 1
                        ELSE 0
                    END), 0) AS submitted_count,
                    COALESCE(SUM(CASE WHEN COALESCE(qs.result_status_code, '') = 'graded' THEN 1 ELSE 0 END), 0) AS graded_count,
                    COALESCE(SUM(CASE
                        WHEN (qs.quest_submission_id IS NULL
                              OR COALESCE(qs.submit_status_code, '') NOT IN ('submitted', 'done'))
                             AND COALESCE(qs.result_status_code, '') <> 'graded'
                             AND qe.end_at < CURRENT_TIMESTAMP THEN 1
                        ELSE 0
                    END), 0) AS overdue_count
                FROM quest_evaluations qe
                LEFT JOIN quest_submissions qs ON qs.quest_evaluation_id = qe.quest_evaluation_id
                    AND qs.user_id = :userId
                """ + parts.whereClause())
                .params(parts.params())
                .query((rs, rowNum) -> new QuestListSummary(
                        rs.getLong("total_count"),
                        rs.getLong("progress_count"),
                        rs.getLong("submitted_count"),
                        rs.getLong("graded_count"),
                        rs.getLong("overdue_count")
                ))
                .single();
    }

    public List<QuestItem> findQuests(long userId, int limit, int offset) {
        return findQuests(userId, null, null, limit, offset);
    }

    public List<QuestItem> findQuests(long userId, String status, String keyword, int limit, int offset) {
        SqlParts parts = questWhere(userId, status, keyword);
        Map<String, Object> params = new java.util.HashMap<>(parts.params());
        params.put("limit", limit);
        params.put("offset", offset);
        return jdbcClient.sql("""
                SELECT qe.quest_evaluation_id, qe.title, qe.quest_type_code, qe.task_classification_code,
                       qe.start_at, qe.end_at, qe.max_exp, qe.progress_status_code,
                       qs.submit_status_code, qs.result_status_code
                FROM quest_evaluations qe
                LEFT JOIN quest_submissions qs ON qs.quest_evaluation_id = qe.quest_evaluation_id
                    AND qs.user_id = :userId
                """ + parts.whereClause() + """
                ORDER BY COALESCE(qe.end_at, qe.start_at) DESC, qe.quest_evaluation_id DESC
                LIMIT :limit OFFSET :offset
                """)
                .params(params)
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

    private DocumentRequestItem mapDocumentRequestItem(ResultSet rs, int rowNum) throws SQLException {
        return new DocumentRequestItem(
                rs.getLong("document_request_id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("category"),
                rs.getBoolean("required_yn"),
                rs.getString("allowed_extensions"),
                rs.getLong("max_file_size_bytes"),
                toOffset(rs.getTimestamp("starts_at")),
                toOffset(rs.getTimestamp("due_at")),
                rs.getString("status_code"),
                toOffset(rs.getTimestamp("submitted_at")),
                rs.getString("review_comment"),
                List.of()
        );
    }

    private DocumentRequestDetail mapDocumentRequestDetail(ResultSet rs, int rowNum) throws SQLException {
        return new DocumentRequestDetail(
                rs.getLong("document_request_id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("category"),
                rs.getBoolean("required_yn"),
                rs.getString("allowed_extensions"),
                rs.getLong("max_file_size_bytes"),
                toOffset(rs.getTimestamp("starts_at")),
                toOffset(rs.getTimestamp("due_at")),
                rs.getString("status_code"),
                toOffset(rs.getTimestamp("submitted_at")),
                toOffset(rs.getTimestamp("reviewed_at")),
                rs.getString("review_comment"),
                List.of()
        );
    }

    private DocumentAttachmentItem mapDocumentAttachment(ResultSet rs, int rowNum) throws SQLException {
        return new DocumentAttachmentItem(
                rs.getLong("attachment_id"),
                rs.getLong("document_submission_id"),
                rs.getLong("document_request_id"),
                rs.getString("original_filename"),
                rs.getString("storage_key"),
                rs.getString("mime_type"),
                rs.getLong("file_size"),
                toOffset(rs.getTimestamp("created_at"))
        );
    }

    private PledgeItem mapPledge(ResultSet rs, int rowNum) throws SQLException {
        return new PledgeItem(
                rs.getLong("pledge_document_id"),
                rs.getString("title"),
                rs.getString("content"),
                rs.getString("version"),
                rs.getBoolean("required_yn"),
                toOffset(rs.getTimestamp("starts_at")),
                toOffset(rs.getTimestamp("due_at")),
                rs.getBoolean("agreed_yn"),
                toOffset(rs.getTimestamp("agreed_at")),
                rs.getString("version_snapshot")
        );
    }

    private SqlParts questWhere(long userId, String status, String keyword) {
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");
        Map<String, Object> params = new java.util.HashMap<>();
        params.put("userId", userId);
        if (StringUtils.hasText(keyword)) {
            where.append("""
                     AND (LOWER(qe.title) LIKE :keyword
                       OR LOWER(COALESCE(qe.quest_type_code, '')) LIKE :keyword
                       OR LOWER(COALESCE(qe.task_classification_code, '')) LIKE :keyword)
                    """);
            params.put("keyword", "%" + keyword.trim().toLowerCase() + "%");
        }
        if (StringUtils.hasText(status)) {
            switch (status.trim().toLowerCase()) {
                case "progress" -> where.append("""
                         AND (qs.quest_submission_id IS NULL
                              OR COALESCE(qs.submit_status_code, '') NOT IN ('submitted', 'done'))
                         AND COALESCE(qs.result_status_code, '') <> 'graded'
                         AND (qe.end_at IS NULL OR qe.end_at >= CURRENT_TIMESTAMP)
                        """);
                case "submitted" -> where.append("""
                         AND COALESCE(qs.submit_status_code, '') IN ('submitted', 'done')
                         AND COALESCE(qs.result_status_code, '') <> 'graded'
                        """);
                case "graded" -> where.append(" AND COALESCE(qs.result_status_code, '') = 'graded'");
                case "overdue" -> where.append("""
                         AND (qs.quest_submission_id IS NULL
                              OR COALESCE(qs.submit_status_code, '') NOT IN ('submitted', 'done'))
                         AND COALESCE(qs.result_status_code, '') <> 'graded'
                         AND qe.end_at < CURRENT_TIMESTAMP
                        """);
                default -> {
                }
            }
        }
        return new SqlParts(where.toString(), params);
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

    private String normalizeBlank(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
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
