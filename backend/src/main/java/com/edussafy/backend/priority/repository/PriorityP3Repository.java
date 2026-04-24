package com.edussafy.backend.priority.repository;

import com.edussafy.backend.priority.dto.PriorityDtos.MaterialItem;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialReactionSummary;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialResourceItem;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestItem;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyDetail;
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
public class PriorityP3Repository {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    private final JdbcClient jdbcClient;

    public PriorityP3Repository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<MaterialItem> findMaterial(long materialId) {
        return jdbcClient.sql("""
                SELECT learning_material_id, title, material_type_code, summary, detail_url, view_count, created_at
                FROM learning_materials
                WHERE learning_material_id = :materialId
                """)
                .param("materialId", materialId)
                .query((rs, rowNum) -> new MaterialItem(
                        rs.getLong("learning_material_id"),
                        rs.getString("title"),
                        rs.getString("material_type_code"),
                        rs.getString("summary"),
                        rs.getString("detail_url"),
                        rs.getInt("view_count"),
                        toOffset(rs.getTimestamp("created_at")),
                        0L,
                        0L,
                        0L,
                        false,
                        false,
                        false,
                        List.of()
                ))
                .optional();
    }

    public Optional<MaterialReactionSummary> findMaterialReactionSummary(long materialId, long userId) {
        return jdbcClient.sql("""
                SELECT lmr.learning_material_id,
                       SUM(CASE WHEN lmr.reaction_type_code = 'like' THEN 1 ELSE 0 END) AS like_count,
                       SUM(CASE WHEN lmr.reaction_type_code = 'bookmark' THEN 1 ELSE 0 END) AS bookmark_count,
                       SUM(CASE WHEN lmr.reaction_type_code = 'helpful' THEN 1 ELSE 0 END) AS favorite_count,
                       MAX(CASE WHEN lmr.reaction_type_code = 'like' AND lmr.user_id = :userId THEN 1 ELSE 0 END) AS liked,
                       MAX(CASE WHEN lmr.reaction_type_code = 'bookmark' AND lmr.user_id = :userId THEN 1 ELSE 0 END) AS bookmarked,
                       MAX(CASE WHEN lmr.reaction_type_code = 'helpful' AND lmr.user_id = :userId THEN 1 ELSE 0 END) AS favorited
                FROM learning_material_reactions lmr
                WHERE lmr.learning_material_id = :materialId
                GROUP BY lmr.learning_material_id
                """)
                .param("materialId", materialId)
                .param("userId", userId)
                .query((rs, rowNum) -> new MaterialReactionSummary(
                        rs.getLong("learning_material_id"),
                        rs.getLong("like_count"),
                        rs.getLong("bookmark_count"),
                        rs.getLong("favorite_count"),
                        rs.getInt("liked") == 1,
                        rs.getInt("bookmarked") == 1,
                        rs.getInt("favorited") == 1
                ))
                .optional();
    }

    public boolean toggleMaterialReaction(long materialId, long userId, String reactionTypeCode) {
        int deleted = jdbcClient.sql("""
                DELETE FROM learning_material_reactions
                WHERE learning_material_id = :materialId
                  AND user_id = :userId
                  AND reaction_type_code = :reactionTypeCode
                """)
                .param("materialId", materialId)
                .param("userId", userId)
                .param("reactionTypeCode", reactionTypeCode)
                .update();
        if (deleted > 0) {
            return false;
        }
        jdbcClient.sql("""
                INSERT INTO learning_material_reactions
                    (learning_material_id, user_id, reaction_type_code_group, reaction_type_code)
                VALUES (:materialId, :userId, 'REACTION_TYPE', :reactionTypeCode)
                """)
                .param("materialId", materialId)
                .param("userId", userId)
                .param("reactionTypeCode", reactionTypeCode)
                .update();
        return true;
    }

    public List<MaterialResourceItem> findMaterialResources(long materialId) {
        return jdbcClient.sql("""
                SELECT learning_material_resource_id, learning_material_id, resource_type_code,
                       resource_title, launch_mode_code, target_url, display_order
                FROM learning_material_resources
                WHERE learning_material_id = :materialId
                ORDER BY display_order ASC, learning_material_resource_id ASC
                """)
                .param("materialId", materialId)
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

    public Optional<QuestItem> findQuest(long userId, long questId) {
        return jdbcClient.sql("""
                SELECT qe.quest_evaluation_id, qe.title, qe.quest_type_code, qe.task_classification_code,
                       qe.start_at, qe.end_at, qe.max_exp, qe.progress_status_code,
                       qs.submit_status_code, qs.result_status_code
                FROM quest_evaluations qe
                LEFT JOIN quest_submissions qs ON qs.quest_evaluation_id = qe.quest_evaluation_id
                    AND qs.user_id = :userId
                WHERE qe.quest_evaluation_id = :questId
                """)
                .param("userId", userId)
                .param("questId", questId)
                .query(this::mapQuest)
                .optional();
    }

    public Optional<SurveyDetail> findSurvey(long userId, long surveyId) {
        return jdbcClient.sql("""
                SELECT s.survey_id, s.title, s.survey_category_code, s.required_yn,
                       s.start_at, s.end_at, s.progress_status_code,
                       COALESCE(sr.completed_yn, FALSE) AS completed_yn,
                       COALESCE(questions.question_count, 0) AS question_count
                FROM surveys s
                LEFT JOIN survey_responses sr ON sr.survey_id = s.survey_id AND sr.user_id = :userId
                LEFT JOIN (
                    SELECT survey_id, COUNT(*) AS question_count
                    FROM survey_questions
                    GROUP BY survey_id
                ) questions ON questions.survey_id = s.survey_id
                WHERE s.survey_id = :surveyId
                """)
                .param("userId", userId)
                .param("surveyId", surveyId)
                .query(this::mapSurvey)
                .optional();
    }

    private QuestItem mapQuest(ResultSet rs, int rowNum) throws SQLException {
        return new QuestItem(
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
        );
    }

    private SurveyDetail mapSurvey(ResultSet rs, int rowNum) throws SQLException {
        return new SurveyDetail(
                rs.getLong("survey_id"),
                rs.getString("title"),
                rs.getString("survey_category_code"),
                rs.getBoolean("required_yn"),
                toOffset(rs.getTimestamp("start_at")),
                toOffset(rs.getTimestamp("end_at")),
                rs.getString("progress_status_code"),
                rs.getBoolean("completed_yn"),
                rs.getLong("question_count")
        );
    }

    private Integer nullableInt(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }

    private OffsetDateTime toOffset(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime().atZone(SEOUL_ZONE).toOffsetDateTime();
    }
}
