package com.edussafy.backend.priority.repository;

import com.edussafy.backend.priority.dto.PriorityDtos.ClassmateItem;
import com.edussafy.backend.priority.dto.PriorityDtos.ProfileDetails;
import com.edussafy.backend.priority.dto.PriorityDtos.ProfileUpdateRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketAttachmentItem;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketItem;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketMessageItem;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

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

    public long countAllSupportTickets() {
        return jdbcClient.sql("""
                SELECT COUNT(*)
                FROM support_tickets
                """)
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
                .query(this::mapSupportTicket)
                .list();
    }

    public List<SupportTicketItem> findAllSupportTickets(int limit, int offset) {
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
                ORDER BY st.updated_at DESC, st.support_ticket_id DESC
                LIMIT :limit OFFSET :offset
                """)
                .param("limit", limit)
                .param("offset", offset)
                .query(this::mapSupportTicket)
                .list();
    }

    public Optional<SupportTicketItem> findSupportTicket(long userId, long ticketId) {
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
                WHERE st.requester_user_id = :userId AND st.support_ticket_id = :ticketId
                """)
                .param("userId", userId)
                .param("ticketId", ticketId)
                .query(this::mapSupportTicket)
                .optional();
    }

    public Optional<SupportTicketItem> findSupportTicketForStaff(long ticketId) {
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
                WHERE st.support_ticket_id = :ticketId
                """)
                .param("ticketId", ticketId)
                .query(this::mapSupportTicket)
                .optional();
    }

    public List<SupportTicketMessageItem> findSupportTicketMessages(long userId, long ticketId) {
        return jdbcClient.sql("""
                SELECT stm.support_ticket_message_id, stm.support_ticket_id, stm.sender_user_id,
                       COALESCE(u.name, 'Unknown') AS sender_name,
                       stm.message_type_code, stm.content, stm.created_at
                FROM support_ticket_messages stm
                JOIN support_tickets st ON st.support_ticket_id = stm.support_ticket_id
                LEFT JOIN users u ON u.user_id = stm.sender_user_id
                WHERE st.requester_user_id = :userId AND st.support_ticket_id = :ticketId
                ORDER BY stm.created_at ASC, stm.support_ticket_message_id ASC
                """)
                .param("userId", userId)
                .param("ticketId", ticketId)
                .query(this::mapSupportTicketMessage)
                .list();
    }

    public List<SupportTicketMessageItem> findSupportTicketMessagesForStaff(long ticketId) {
        return jdbcClient.sql("""
                SELECT stm.support_ticket_message_id, stm.support_ticket_id, stm.sender_user_id,
                       COALESCE(u.name, 'Unknown') AS sender_name,
                       stm.message_type_code, stm.content, stm.created_at
                FROM support_ticket_messages stm
                LEFT JOIN users u ON u.user_id = stm.sender_user_id
                WHERE stm.support_ticket_id = :ticketId
                ORDER BY stm.created_at ASC, stm.support_ticket_message_id ASC
                """)
                .param("ticketId", ticketId)
                .query(this::mapSupportTicketMessage)
                .list();
    }

    public Optional<SupportTicketMessageItem> findSupportTicketMessage(long userId, long ticketId, long messageId) {
        return jdbcClient.sql("""
                SELECT stm.support_ticket_message_id, stm.support_ticket_id, stm.sender_user_id,
                       COALESCE(u.name, 'Unknown') AS sender_name,
                       stm.message_type_code, stm.content, stm.created_at
                FROM support_ticket_messages stm
                JOIN support_tickets st ON st.support_ticket_id = stm.support_ticket_id
                LEFT JOIN users u ON u.user_id = stm.sender_user_id
                WHERE st.requester_user_id = :userId
                  AND st.support_ticket_id = :ticketId
                  AND stm.support_ticket_message_id = :messageId
                """)
                .param("userId", userId)
                .param("ticketId", ticketId)
                .param("messageId", messageId)
                .query(this::mapSupportTicketMessage)
                .optional();
    }

    public Optional<SupportTicketMessageItem> findSupportTicketMessageForStaff(long ticketId, long messageId) {
        return jdbcClient.sql("""
                SELECT stm.support_ticket_message_id, stm.support_ticket_id, stm.sender_user_id,
                       COALESCE(u.name, 'Unknown') AS sender_name,
                       stm.message_type_code, stm.content, stm.created_at
                FROM support_ticket_messages stm
                LEFT JOIN users u ON u.user_id = stm.sender_user_id
                WHERE stm.support_ticket_id = :ticketId
                  AND stm.support_ticket_message_id = :messageId
                """)
                .param("ticketId", ticketId)
                .param("messageId", messageId)
                .query(this::mapSupportTicketMessage)
                .optional();
    }

    public List<SupportTicketAttachmentItem> findSupportTicketMessageAttachments(List<Long> messageIds) {
        if (messageIds.isEmpty()) {
            return List.of();
        }
        return jdbcClient.sql("""
                SELECT stma.support_ticket_message_id,
                       a.attachment_id,
                       a.original_filename,
                       a.storage_key,
                       a.stored_path,
                       a.mime_type,
                       a.file_size,
                       a.checksum_sha256,
                       a.created_at
                FROM support_ticket_message_attachments stma
                JOIN attachments a ON a.attachment_id = stma.attachment_id
                WHERE stma.support_ticket_message_id IN (:messageIds)
                ORDER BY a.created_at ASC, a.attachment_id ASC
                """)
                .param("messageIds", messageIds)
                .query(this::mapSupportTicketAttachment)
                .list();
    }

    public Optional<SupportTicketAttachmentItem> findSupportTicketMessageAttachment(long messageId, long attachmentId) {
        return jdbcClient.sql("""
                SELECT stma.support_ticket_message_id,
                       a.attachment_id,
                       a.original_filename,
                       a.storage_key,
                       a.stored_path,
                       a.mime_type,
                       a.file_size,
                       a.checksum_sha256,
                       a.created_at
                FROM support_ticket_message_attachments stma
                JOIN attachments a ON a.attachment_id = stma.attachment_id
                WHERE stma.support_ticket_message_id = :messageId
                  AND a.attachment_id = :attachmentId
                """)
                .param("messageId", messageId)
                .param("attachmentId", attachmentId)
                .query(this::mapSupportTicketAttachment)
                .optional();
    }

    public long createSupportTicket(long userId, String title) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql("""
                INSERT INTO support_tickets (requester_user_id, title, status_code)
                VALUES (:userId, :title, 'open')
                """)
                .param("userId", userId)
                .param("title", title)
                .update(keyHolder, "support_ticket_id");

        Number key = keyHolder.getKey();
        return key == null ? 0L : key.longValue();
    }

    public long createSupportTicketMessage(long ticketId, long senderUserId, String content) {
        return createSupportTicketMessage(ticketId, senderUserId, "user_message", content);
    }

    public long createSupportTicketMessage(long ticketId, long senderUserId, String messageType, String content) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql("""
                INSERT INTO support_ticket_messages (
                    support_ticket_id,
                    sender_user_id,
                    message_type_code,
                    content
                )
                VALUES (:ticketId, :senderUserId, :messageType, :content)
                """)
                .param("ticketId", ticketId)
                .param("senderUserId", senderUserId)
                .param("messageType", messageType)
                .param("content", content)
                .update(keyHolder, "support_ticket_message_id");

        Number key = keyHolder.getKey();
        return key == null ? 0L : key.longValue();
    }

    public long createOrFindAttachment(
            String filename,
            String storageKey,
            String storedPath,
            String mimeType,
            long fileSize,
            String checksumSha256
    ) {
        Optional<Long> existing = findAttachmentIdByChecksum(checksumSha256);
        if (existing.isPresent()) {
            return existing.get();
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbcClient.sql("""
                    INSERT INTO attachments (
                        original_filename,
                        storage_key,
                        stored_path,
                        mime_type,
                        file_size,
                        checksum_sha256
                    )
                    VALUES (:filename, :storageKey, :storedPath, :mimeType, :fileSize, :checksumSha256)
                    """)
                    .param("filename", filename)
                    .param("storageKey", storageKey)
                    .param("storedPath", storedPath)
                    .param("mimeType", mimeType)
                    .param("fileSize", fileSize)
                    .param("checksumSha256", checksumSha256)
                    .update(keyHolder, "attachment_id");
        } catch (DuplicateKeyException exception) {
            return findAttachmentIdByChecksum(checksumSha256).orElseThrow(() -> exception);
        }

        Number key = keyHolder.getKey();
        return key == null ? findAttachmentIdByChecksum(checksumSha256).orElse(0L) : key.longValue();
    }

    public void linkSupportTicketMessageAttachment(long messageId, long attachmentId) {
        jdbcClient.sql("""
                INSERT IGNORE INTO support_ticket_message_attachments (support_ticket_message_id, attachment_id)
                VALUES (:messageId, :attachmentId)
                """)
                .param("messageId", messageId)
                .param("attachmentId", attachmentId)
                .update();
    }

    private Optional<Long> findAttachmentIdByChecksum(String checksumSha256) {
        return jdbcClient.sql("""
                SELECT attachment_id
                FROM attachments
                WHERE checksum_sha256 = :checksumSha256
                """)
                .param("checksumSha256", checksumSha256)
                .query(Long.class)
                .optional();
    }

    public void markSupportTicketOpen(long ticketId) {
        jdbcClient.sql("""
                UPDATE support_tickets
                SET status_code = 'open',
                    closed_at = NULL,
                    updated_at = CURRENT_TIMESTAMP
                WHERE support_ticket_id = :ticketId
                """)
                .param("ticketId", ticketId)
                .update();
    }

    public void markSupportTicketAnswered(long ticketId) {
        jdbcClient.sql("""
                UPDATE support_tickets
                SET status_code = 'answered',
                    closed_at = NULL,
                    updated_at = CURRENT_TIMESTAMP
                WHERE support_ticket_id = :ticketId
                """)
                .param("ticketId", ticketId)
                .update();
    }

    public List<ClassmateItem> findClassmates(long userId) {
        return findClassmates(userId, null, null);
    }

    public List<ClassmateItem> findClassmates(long userId, String keyword, String memberRole) {
        StringBuilder where = new StringBuilder();
        Map<String, Object> params = new java.util.HashMap<>();
        params.put("userId", userId);
        if (StringUtils.hasText(keyword)) {
            where.append("""
                 AND (LOWER(u.name) LIKE :keyword
                   OR LOWER(u.email) LIKE :keyword
                   OR LOWER(c.campus_name) LIKE :keyword
                   OR LOWER(t.track_name) LIKE :keyword
                   OR LOWER(cg.class_name) LIKE :keyword)
                """);
            params.put("keyword", "%" + keyword.trim().toLowerCase() + "%");
        }
        if (StringUtils.hasText(memberRole)) {
            where.append(" AND (LOWER(uce.member_role_code) = :memberRole OR LOWER(u.role_code) = :memberRole)");
            params.put("memberRole", memberRole.trim().toLowerCase());
        }
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
                """ + where + """
                ORDER BY u.name ASC, u.user_id ASC
                """)
                .params(params)
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

    private SupportTicketItem mapSupportTicket(ResultSet rs, int rowNum) throws SQLException {
        return new SupportTicketItem(
                rs.getLong("support_ticket_id"),
                rs.getString("title"),
                rs.getString("status_code"),
                toOffset(rs.getTimestamp("created_at")),
                toOffset(rs.getTimestamp("updated_at")),
                toOffset(rs.getTimestamp("closed_at")),
                rs.getLong("message_count"),
                toOffset(rs.getTimestamp("latest_message_at"))
        );
    }

    private SupportTicketMessageItem mapSupportTicketMessage(ResultSet rs, int rowNum) throws SQLException {
        long senderUserId = rs.getLong("sender_user_id");
        Long nullableSenderUserId = rs.wasNull() ? null : senderUserId;
        return new SupportTicketMessageItem(
                rs.getLong("support_ticket_message_id"),
                rs.getLong("support_ticket_id"),
                nullableSenderUserId,
                rs.getString("sender_name"),
                rs.getString("message_type_code"),
                rs.getString("content"),
                toOffset(rs.getTimestamp("created_at")),
                List.of()
        );
    }

    private SupportTicketAttachmentItem mapSupportTicketAttachment(ResultSet rs, int rowNum) throws SQLException {
        return new SupportTicketAttachmentItem(
                rs.getLong("attachment_id"),
                rs.getLong("support_ticket_message_id"),
                rs.getString("original_filename"),
                rs.getString("storage_key"),
                rs.getString("stored_path"),
                rs.getString("mime_type"),
                rs.getLong("file_size"),
                rs.getString("checksum_sha256"),
                toOffset(rs.getTimestamp("created_at"))
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
