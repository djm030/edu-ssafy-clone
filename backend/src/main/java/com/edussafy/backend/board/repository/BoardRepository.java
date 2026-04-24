package com.edussafy.backend.board.repository;

import com.edussafy.backend.board.dto.BoardPostDetailResponse.BoardPostDetail;
import com.edussafy.backend.board.dto.BoardPostDetailResponse.EngagementSummary;
import com.edussafy.backend.board.dto.BoardPostListItem;
import com.edussafy.backend.board.dto.CategoryItem;
import com.edussafy.backend.board.dto.CategorySummary;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardAttachmentCreateRequest;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardAttachmentItem;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardCommentCreateRequest;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardCommentCreatedItem;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardPostCreateRequest;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardPostCreatedItem;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardPostUpdateRequest;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardReactionCreatedItem;
import com.edussafy.backend.board.service.BoardQuery;
import com.edussafy.backend.board.service.BoardSort;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class BoardRepository {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    private final JdbcClient jdbcClient;

    public BoardRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<Long> findBoardId(String boardCode) {
        return jdbcClient.sql("SELECT board_id FROM boards WHERE board_code = :boardCode")
                .param("boardCode", boardCode)
                .query(Long.class)
                .optional();
    }

    public List<CategoryItem> findCategories(long boardId) {
        return jdbcClient.sql("""
                SELECT board_category_id, category_name, sort_order
                FROM board_categories
                WHERE board_id = :boardId
                ORDER BY sort_order ASC, board_category_id ASC
                """)
                .param("boardId", boardId)
                .query((rs, rowNum) -> new CategoryItem(
                        rs.getLong("board_category_id"),
                        rs.getString("category_name"),
                        rs.getInt("sort_order")
                ))
                .list();
    }

    public long countPosts(long boardId, BoardQuery query) {
        SqlParts parts = buildWhereClause(boardId, query);
        return jdbcClient.sql("SELECT COUNT(*) FROM board_posts p " + parts.whereClause())
                .params(parts.params())
                .query(Long.class)
                .single();
    }

    public List<BoardPostListItem> findPosts(long boardId, BoardQuery query, BoardSort sort) {
        SqlParts parts = buildWhereClause(boardId, query);
        Map<String, Object> params = new LinkedHashMap<>(parts.params());
        params.put("limit", query.size());
        params.put("offset", (query.page() - 1) * query.size());

        String sql = """
                SELECT
                    p.board_post_id,
                    b.board_code,
                    c.board_category_id,
                    c.category_name,
                    p.title,
                    COALESCE(u.name, 'Unknown') AS author_name,
                    p.created_at,
                    p.view_count,
                    p.notice_yn,
                    COALESCE(comments.comment_count, 0) AS comment_count,
                    COALESCE(reactions.reaction_count, 0) AS reaction_count,
                    COALESCE(reactions.bookmark_count, 0) AS bookmark_count,
                    COALESCE(attachments.attachment_count, 0) AS attachment_count
                FROM board_posts p
                JOIN boards b ON b.board_id = p.board_id
                LEFT JOIN board_categories c ON c.board_category_id = p.board_category_id
                LEFT JOIN users u ON u.user_id = p.author_user_id
                LEFT JOIN (
                    SELECT board_post_id, COUNT(*) AS comment_count
                    FROM board_comments
                    GROUP BY board_post_id
                ) comments ON comments.board_post_id = p.board_post_id
                LEFT JOIN (
                    SELECT
                        board_post_id,
                        COUNT(*) AS reaction_count,
                        SUM(CASE WHEN LOWER(reaction_type_code) = 'bookmark' THEN 1 ELSE 0 END) AS bookmark_count
                    FROM board_post_reactions
                    GROUP BY board_post_id
                ) reactions ON reactions.board_post_id = p.board_post_id
                LEFT JOIN (
                    SELECT board_post_id, COUNT(*) AS attachment_count
                    FROM board_post_attachments
                    GROUP BY board_post_id
                ) attachments ON attachments.board_post_id = p.board_post_id
                """ + parts.whereClause() + " " + """
                ORDER BY p.notice_yn DESC, """ + sort.orderBySql() + ", p.board_post_id DESC\n" +
                "LIMIT :limit OFFSET :offset";

        return jdbcClient.sql(sql)
                .params(params)
                .query(this::mapPost)
                .list();
    }

    public Optional<BoardPostDetail> findPostDetail(long boardId, long postId) {
        return jdbcClient.sql("""
                SELECT
                    p.board_post_id,
                    b.board_code,
                    c.board_category_id,
                    c.category_name,
                    p.title,
                    p.content,
                    COALESCE(u.name, 'Unknown') AS author_name,
                    p.created_at,
                    p.updated_at,
                    p.view_count,
                    p.notice_yn,
                    COALESCE(comments.comment_count, 0) AS comment_count,
                    COALESCE(reactions.reaction_count, 0) AS reaction_count,
                    COALESCE(reactions.bookmark_count, 0) AS bookmark_count,
                    COALESCE(attachments.attachment_count, 0) AS attachment_count
                FROM board_posts p
                JOIN boards b ON b.board_id = p.board_id
                LEFT JOIN board_categories c ON c.board_category_id = p.board_category_id
                LEFT JOIN users u ON u.user_id = p.author_user_id
                LEFT JOIN (
                    SELECT board_post_id, COUNT(*) AS comment_count
                    FROM board_comments
                    GROUP BY board_post_id
                ) comments ON comments.board_post_id = p.board_post_id
                LEFT JOIN (
                    SELECT
                        board_post_id,
                        COUNT(*) AS reaction_count,
                        SUM(CASE WHEN LOWER(reaction_type_code) = 'bookmark' THEN 1 ELSE 0 END) AS bookmark_count
                    FROM board_post_reactions
                    GROUP BY board_post_id
                ) reactions ON reactions.board_post_id = p.board_post_id
                LEFT JOIN (
                    SELECT board_post_id, COUNT(*) AS attachment_count
                    FROM board_post_attachments
                    GROUP BY board_post_id
                ) attachments ON attachments.board_post_id = p.board_post_id
                WHERE p.board_id = :boardId AND p.board_post_id = :postId
                """)
                .param("boardId", boardId)
                .param("postId", postId)
                .query(this::mapPostDetail)
                .optional();
    }

    public BoardPostCreatedItem createPost(long boardId, String boardCode, BoardPostCreateRequest request) {
        jdbcClient.sql("""
                INSERT INTO board_posts (board_id, board_category_id, author_user_id, title, content)
                VALUES (:boardId, :categoryId, NULL, :title, :content)
                """)
                .param("boardId", boardId)
                .param("categoryId", request.categoryId())
                .param("title", request.title().trim())
                .param("content", request.content().trim())
                .update();
        long id = lastInsertId();
        return new BoardPostCreatedItem(id, boardCode, request.categoryId(), request.title().trim(), request.content().trim(), "Demo Learner", OffsetDateTime.now(SEOUL_ZONE), false);
    }

    public BoardPostCreatedItem updatePost(long postId, String boardCode, BoardPostUpdateRequest request) {
        jdbcClient.sql("""
                UPDATE board_posts
                SET board_category_id = :categoryId, title = :title, content = :content
                WHERE board_post_id = :postId
                """)
                .param("categoryId", request.categoryId())
                .param("title", request.title().trim())
                .param("content", request.content().trim())
                .param("postId", postId)
                .update();
        return new BoardPostCreatedItem(postId, boardCode, request.categoryId(), request.title().trim(), request.content().trim(), "Demo Learner", OffsetDateTime.now(SEOUL_ZONE), false);
    }

    public void deletePost(long postId) {
        jdbcClient.sql("DELETE FROM board_posts WHERE board_post_id = :postId")
                .param("postId", postId)
                .update();
    }

    public BoardCommentCreatedItem createComment(long postId, BoardCommentCreateRequest request) {
        jdbcClient.sql("""
                INSERT INTO board_comments (board_post_id, author_user_id, content)
                VALUES (:postId, NULL, :content)
                """)
                .param("postId", postId)
                .param("content", request.content().trim())
                .update();
        long id = lastInsertId();
        return new BoardCommentCreatedItem(id, postId, request.content().trim(), "Demo Learner", OffsetDateTime.now(SEOUL_ZONE), false);
    }

    public BoardReactionCreatedItem toggleReaction(long postId, String type) {
        long demoUserId = 1L;
        int deleted = jdbcClient.sql("""
                DELETE FROM board_post_reactions
                WHERE board_post_id = :postId AND user_id = :userId AND reaction_type_code = :type
                """)
                .param("postId", postId)
                .param("userId", demoUserId)
                .param("type", type)
                .update();
        if (deleted > 0) {
            return new BoardReactionCreatedItem(postId, type, false, false);
        }
        jdbcClient.sql("""
                INSERT INTO board_post_reactions (board_post_id, user_id, reaction_type_code)
                VALUES (:postId, :userId, :type)
                """)
                .param("postId", postId)
                .param("userId", demoUserId)
                .param("type", type)
                .update();
        return new BoardReactionCreatedItem(postId, type, true, false);
    }

    public BoardAttachmentItem attachFile(long postId, BoardAttachmentCreateRequest request) {
        jdbcClient.sql("""
                INSERT INTO attachments (original_filename, storage_key, stored_path, mime_type, file_size)
                VALUES (:fileName, :storageKey, :storedPath, :mimeType, :fileSize)
                """)
                .param("fileName", request.fileName().trim())
                .param("storageKey", "board/" + postId + "/" + request.fileName().trim())
                .param("storedPath", request.url())
                .param("mimeType", request.mimeType())
                .param("fileSize", request.fileSize())
                .update();
        long attachmentId = lastInsertId();
        jdbcClient.sql("INSERT INTO board_post_attachments (board_post_id, attachment_id) VALUES (:postId, :attachmentId)")
                .param("postId", postId)
                .param("attachmentId", attachmentId)
                .update();
        return new BoardAttachmentItem(attachmentId, postId, request.fileName().trim(), request.mimeType(), request.fileSize(), request.url(), false);
    }

    private long lastInsertId() {
        return jdbcClient.sql("SELECT LAST_INSERT_ID()")
                .query(Long.class)
                .single();
    }

    private SqlParts buildWhereClause(long boardId, BoardQuery query) {
        StringBuilder where = new StringBuilder("WHERE p.board_id = :boardId");
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("boardId", boardId);

        if (query.categoryId() != null) {
            where.append(" AND p.board_category_id = :categoryId");
            params.put("categoryId", query.categoryId());
        }

        if (StringUtils.hasText(query.keyword())) {
            where.append(" AND (p.title LIKE :keyword OR p.content LIKE :keyword)");
            params.put("keyword", "%" + query.keyword().trim() + "%");
        }

        return new SqlParts(" " + where, params);
    }

    private BoardPostListItem mapPost(ResultSet rs, int rowNum) throws SQLException {
        Long categoryId = nullableLong(rs, "board_category_id");
        CategorySummary category = categoryId == null
                ? null
                : new CategorySummary(categoryId, rs.getString("category_name"));

        return new BoardPostListItem(
                rs.getLong("board_post_id"),
                rs.getString("board_code"),
                category,
                rs.getString("title"),
                rs.getString("author_name"),
                toOffsetDateTime(rs.getTimestamp("created_at")),
                rs.getInt("view_count"),
                rs.getLong("comment_count"),
                rs.getLong("reaction_count"),
                rs.getLong("bookmark_count"),
                rs.getLong("attachment_count") > 0,
                rs.getBoolean("notice_yn")
        );
    }

    private BoardPostDetail mapPostDetail(ResultSet rs, int rowNum) throws SQLException {
        Long categoryId = nullableLong(rs, "board_category_id");
        CategorySummary category = categoryId == null
                ? null
                : new CategorySummary(categoryId, rs.getString("category_name"));

        return new BoardPostDetail(
                rs.getLong("board_post_id"),
                rs.getString("board_code"),
                category,
                rs.getString("title"),
                rs.getString("content"),
                rs.getString("author_name"),
                toOffsetDateTime(rs.getTimestamp("created_at")),
                toOffsetDateTime(rs.getTimestamp("updated_at")),
                rs.getInt("view_count"),
                new EngagementSummary(
                        rs.getLong("comment_count"),
                        rs.getLong("reaction_count"),
                        rs.getLong("bookmark_count")
                ),
                rs.getLong("attachment_count") > 0,
                rs.getBoolean("notice_yn")
        );
    }

    private Long nullableLong(ResultSet rs, String columnName) throws SQLException {
        long value = rs.getLong(columnName);
        return rs.wasNull() ? null : value;
    }

    private OffsetDateTime toOffsetDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime().atZone(SEOUL_ZONE).toOffsetDateTime();
    }

    private record SqlParts(String whereClause, Map<String, Object> params) {
    }
}
