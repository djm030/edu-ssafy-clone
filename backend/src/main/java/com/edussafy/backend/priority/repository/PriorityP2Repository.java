package com.edussafy.backend.priority.repository;

import com.edussafy.backend.priority.dto.PriorityDtos.ClassmateItem;
import com.edussafy.backend.priority.dto.PriorityDtos.ProfileDetails;
import com.edussafy.backend.priority.dto.PriorityDtos.ProfileUpdateRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketItem;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class PriorityP2Repository {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    private final JdbcClient jdbcClient;

    public PriorityP2Repository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public long countSupportTickets(long userId) {
        return jdbcClient.sql("""
                SELECT COUNT(*)
                FROM support_tickets
                WHERE requester_user_id = :userId
                """)
                .param("userId", userId)
                .query(Long.class)
                .single();
    }

    public List<SupportTicketItem> findSupportTickets(long userId, int limit, int offset) {
        return jdbcClient.sql("""
                SELECT st.support_ticket_id, st.title, st.status_code, st.created_at, st.updated_at, st.closed_at,
                       COALESCE(messages.message_count, 0) AS message_count,
                       messages.latest_message_at
                FROM support_tickets st
                LEFT JOIN (
                    SELECT support_ticket_id, COUNT(*) AS message_count, MAX(created_at) AS latest_message_at
                    FROM support_ticket_messages
                    GROUP BY support_ticket_id
                ) messages ON messages.support_ticket_id = st.support_ticket_id
                WHERE st.requester_user_id = :userId
                ORDER BY st.updated_at DESC, st.support_ticket_id DESC
                LIMIT :limit OFFSET :offset
                """)
                .param("userId", userId)
                .param("limit", limit)
                .param("offset", offset)
                .query((rs, rowNum) -> new SupportTicketItem(
                        rs.getLong("support_ticket_id"),
                        rs.getString("title"),
                        rs.getString("status_code"),
                        toOffset(rs.getTimestamp("created_at")),
                        toOffset(rs.getTimestamp("updated_at")),
                        toOffset(rs.getTimestamp("closed_at")),
                        rs.getLong("message_count"),
                        toOffset(rs.getTimestamp("latest_message_at"))
                ))
                .list();
    }

    public List<ClassmateItem> findClassmates(long userId) {
        return jdbcClient.sql("""
                SELECT u.user_id, u.name, u.email, u.role_code, uce.member_role_code,
                       c.campus_name, co.cohort_name, t.track_name, cg.class_name
                FROM (
                    SELECT class_group_id
                    FROM user_class_enrollments
                    WHERE user_id = :userId
                    ORDER BY enrolled_at DESC
                    LIMIT 1
                ) current_class
                JOIN user_class_enrollments uce ON uce.class_group_id = current_class.class_group_id
                JOIN users u ON u.user_id = uce.user_id AND u.deleted_at IS NULL
                JOIN class_groups cg ON cg.class_group_id = uce.class_group_id
                JOIN campuses c ON c.campus_id = cg.campus_id
                JOIN cohorts co ON co.cohort_id = cg.cohort_id
                JOIN tracks t ON t.track_id = cg.track_id
                ORDER BY u.name ASC, u.user_id ASC
                """)
                .param("userId", userId)
                .query(this::mapClassmate)
                .list();
    }

    public Optional<ProfileDetails> findProfile(long userId) {
        return jdbcClient.sql("""
                SELECT u.user_id, u.name, u.email, u.role_code, u.learner_no,
                       c.campus_name, co.cohort_name, t.track_name, cg.class_name,
                       up.zip_code, up.address_line1, up.address_line2,
                       up.mobile_phone, up.emergency_phone, up.marketing_opt_in
                FROM users u
                LEFT JOIN user_profiles up ON up.user_id = u.user_id
                LEFT JOIN user_class_enrollments uce ON uce.user_id = u.user_id
                LEFT JOIN class_groups cg ON cg.class_group_id = uce.class_group_id
                LEFT JOIN campuses c ON c.campus_id = cg.campus_id
                LEFT JOIN cohorts co ON co.cohort_id = cg.cohort_id
                LEFT JOIN tracks t ON t.track_id = cg.track_id
                WHERE u.user_id = :userId
                ORDER BY uce.enrolled_at DESC
                LIMIT 1
                """)
                .param("userId", userId)
                .query(this::mapProfile)
                .optional();
    }

    public Optional<ProfileDetails> updateProfile(
            long userId,
            ProfileUpdateRequest request,
            boolean marketingOptIn
    ) {
        jdbcClient.sql("""
                UPDATE users
                SET name = :name
                WHERE user_id = :userId AND deleted_at IS NULL
                """)
                .param("userId", userId)
                .param("name", request.name().trim())
                .update();

        jdbcClient.sql("""
                INSERT INTO user_profiles (
                    user_id,
                    zip_code,
                    address_line1,
                    address_line2,
                    mobile_phone,
                    emergency_phone,
                    marketing_opt_in
                )
                VALUES (
                    :userId,
                    :zipCode,
                    :addressLine1,
                    :addressLine2,
                    :mobilePhone,
                    :emergencyPhone,
                    :marketingOptIn
                )
                ON DUPLICATE KEY UPDATE
                    zip_code = VALUES(zip_code),
                    address_line1 = VALUES(address_line1),
                    address_line2 = VALUES(address_line2),
                    mobile_phone = VALUES(mobile_phone),
                    emergency_phone = VALUES(emergency_phone),
                    marketing_opt_in = VALUES(marketing_opt_in)
                """)
                .param("userId", userId)
                .param("zipCode", normalizeNullable(request.zipCode()))
                .param("addressLine1", normalizeNullable(request.addressLine1()))
                .param("addressLine2", normalizeNullable(request.addressLine2()))
                .param("mobilePhone", normalizeNullable(request.mobilePhone()))
                .param("emergencyPhone", normalizeNullable(request.emergencyPhone()))
                .param("marketingOptIn", marketingOptIn)
                .update();

        return findProfile(userId);
    }

    private ClassmateItem mapClassmate(ResultSet rs, int rowNum) throws SQLException {
        String role = normalizeRole(rs.getString("role_code"));
        return new ClassmateItem(
                rs.getLong("user_id"),
                rs.getString("name"),
                rs.getString("email"),
                role,
                rs.getString("member_role_code"),
                rs.getString("campus_name"),
                rs.getString("cohort_name"),
                rs.getString("track_name"),
                rs.getString("class_name")
        );
    }

    private ProfileDetails mapProfile(ResultSet rs, int rowNum) throws SQLException {
        return new ProfileDetails(
                rs.getLong("user_id"),
                rs.getString("name"),
                rs.getString("email"),
                normalizeRole(rs.getString("role_code")),
                rs.getString("learner_no"),
                rs.getString("campus_name"),
                rs.getString("cohort_name"),
                rs.getString("track_name"),
                rs.getString("class_name"),
                rs.getString("zip_code"),
                rs.getString("address_line1"),
                rs.getString("address_line2"),
                rs.getString("mobile_phone"),
                rs.getString("emergency_phone"),
                rs.getBoolean("marketing_opt_in")
        );
    }

    private String normalizeRole(String roleCode) {
        return "student".equals(roleCode) ? "learner" : roleCode;
    }

    private String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private OffsetDateTime toOffset(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime().atZone(SEOUL_ZONE).toOffsetDateTime();
    }
}
