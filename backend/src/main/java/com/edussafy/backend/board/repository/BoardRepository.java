package com.edussafy.backend.board.repository;

import com.edussafy.backend.board.dto.BoardPostDetailResponse.BoardCommentItem;
import com.edussafy.backend.board.dto.BoardPostDetailResponse.BoardPostDetail;
import com.edussafy.backend.board.dto.BoardPostDetailResponse.EngagementSummary;
import com.edussafy.backend.board.dto.BoardPostListItem;
import com.edussafy.backend.board.dto.CategoryItem;
import com.edussafy.backend.board.dto.CategorySummary;
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
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
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

    public Optional<Long> findDefaultAuthorUserId() {
        return jdbcClient.sql("""
                SELECT user_id
                FROM users
                WHERE deleted_at IS NULL
                ORDER BY CASE WHEN email = 'student@ssafy.com' THEN 0 ELSE 1 END, user_id ASC
                LIMIT 1
                """)
                .query(Long.class)
                .optional();
    }

    public boolean existsCategory(long boardId, long categoryId) {
        return jdbcClient.sql("""
                SELECT COUNT(*)
                FROM board_categories
                WHERE board_id = :boardId AND board_category_id = :categoryId
                """)
                .param("boardId", boardId)
                .param("categoryId", categoryId)
                .query(Long.class)
                .single() > 0;
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

    public long createPost(long boardId, Long categoryId, Long authorUserId, String title, String content) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql("""
                INSERT INTO board_posts (board_id, board_category_id, author_user_id, title, content)
                VALUES (:boardId, :categoryId, :authorUserId, :title, :content)
                """)
                .param("boardId", boardId)
                .param("categoryId", categoryId)
                .param("authorUserId", authorUserId)
                .param("title", title)
                .param("content", content)
                .update(keyHolder, "board_post_id");

        Number key = keyHolder.getKey();
        return key == null ? 0L : key.longValue();
    }

    public int updatePost(long boardId, long postId, Long categoryId, String title, String content) {
        return jdbcClient.sql("""
                UPDATE board_posts
                SET board_category_id = :categoryId,
                    title = :title,
                    content = :content
                WHERE board_id = :boardId
                  AND board_post_id = :postId
                """)
                .param("boardId", boardId)
                .param("postId", postId)
                .param("categoryId", categoryId)
                .param("title", title)
                .param("content", content)
                .update();
    }

    public int deletePost(long boardId, long postId) {
        return jdbcClient.sql("""
                DELETE FROM board_posts
                WHERE board_id = :boardId
                  AND board_post_id = :postId
                """)
                .param("boardId", boardId)
                .param("postId", postId)
                .update();
    }

    public long createComment(long postId, Long authorUserId, Long parentCommentId, String content) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql("""
                INSERT INTO board_comments (board_post_id, author_user_id, parent_comment_id, content)
                VALUES (:postId, :authorUserId, :parentCommentId, :content)
                """)
                .param("postId", postId)
                .param("authorUserId", authorUserId)
                .param("parentCommentId", parentCommentId)
                .param("content", content)
                .update(keyHolder, "board_comment_id");

        Number key = keyHolder.getKey();
        return key == null ? 0L : key.longValue();
    }

    public Optional<BoardCommentItem> findComment(long commentId) {
        return jdbcClient.sql("""
                SELECT bc.board_comment_id, bc.board_post_id, bc.parent_comment_id, bc.content,
                       COALESCE(u.name, 'Unknown') AS author_name, bc.created_at
                FROM board_comments bc
                LEFT JOIN users u ON u.user_id = bc.author_user_id
                WHERE bc.board_comment_id = :commentId
                """)
                .param("commentId", commentId)
                .query(this::mapComment)
                .optional();
    }

    public List<BoardCommentItem> findComments(long postId) {
        return jdbcClient.sql("""
                SELECT bc.board_comment_id, bc.board_post_id, bc.parent_comment_id, bc.content,
                       COALESCE(u.name, 'Unknown') AS author_name, bc.created_at
                FROM board_comments bc
                LEFT JOIN users u ON u.user_id = bc.author_user_id
                WHERE bc.board_post_id = :postId
                ORDER BY COALESCE(bc.parent_comment_id, bc.board_comment_id) ASC,
                         CASE WHEN bc.parent_comment_id IS NULL THEN 0 ELSE 1 END ASC,
                         bc.created_at ASC,
                         bc.board_comment_id ASC
                """)
                .param("postId", postId)
                .query(this::mapComment)
                .list();
    }

    public void createReaction(long postId, long userId, String reactionType) {
        jdbcClient.sql("""
                INSERT IGNORE INTO board_post_reactions (board_post_id, user_id, reaction_type_code)
                VALUES (:postId, :userId, :reactionType)
                """)
                .param("postId", postId)
                .param("userId", userId)
                .param("reactionType", reactionType)
                .update();
    }

    public void deleteReaction(long postId, long userId, String reactionType) {
        jdbcClient.sql("""
                DELETE FROM board_post_reactions
                WHERE board_post_id = :postId
                  AND user_id = :userId
                  AND reaction_type_code = :reactionType
                """)
                .param("postId", postId)
                .param("userId", userId)
                .param("reactionType", reactionType)
                .update();
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
                List.of(),
                rs.getLong("attachment_count") > 0,
                rs.getBoolean("notice_yn")
        );
    }

    private BoardCommentItem mapComment(ResultSet rs, int rowNum) throws SQLException {
        return new BoardCommentItem(
                rs.getLong("board_comment_id"),
                rs.getLong("board_post_id"),
                nullableLong(rs, "parent_comment_id"),
                rs.getString("content"),
                rs.getString("author_name"),
                toOffsetDateTime(rs.getTimestamp("created_at")),
                List.of()
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
