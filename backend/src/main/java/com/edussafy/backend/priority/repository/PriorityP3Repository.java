package com.edussafy.backend.priority.repository;

import com.edussafy.backend.priority.dto.PriorityDtos.MaterialItem;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialResourceAttachmentItem;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialResourceItem;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestItem;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestSubmissionAttachmentItem;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestSubmissionItem;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyDetail;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyOptionItem;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyQuestionItem;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveySavedAnswerItem;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
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
        return findMaterial(materialId, null);
    }

    public Optional<MaterialItem> findMaterial(long materialId, Long userId) {
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
                WHERE lm.learning_material_id = :materialId
                """)
                .param("materialId", materialId)
                .param("userId", userId)
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
                .optional();
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

    public Optional<MaterialResourceItem> findMaterialResource(long materialId, long resourceId) {
        return jdbcClient.sql("""
                SELECT learning_material_resource_id, learning_material_id, resource_type_code,
                       resource_title, launch_mode_code, target_url, display_order
                FROM learning_material_resources
                WHERE learning_material_id = :materialId
                  AND learning_material_resource_id = :resourceId
                """)
                .param("materialId", materialId)
                .param("resourceId", resourceId)
                .query((rs, rowNum) -> mapMaterialResource(rs))
                .optional();
    }

    public void linkMaterialResourceAttachment(long resourceId, long attachmentId) {
        jdbcClient.sql("""
                INSERT IGNORE INTO learning_material_resource_attachments (
                    learning_material_resource_id,
                    attachment_id
                )
                VALUES (:resourceId, :attachmentId)
                """)
                .param("resourceId", resourceId)
                .param("attachmentId", attachmentId)
                .update();
    }

    public Optional<MaterialResourceAttachmentItem> findMaterialResourceAttachment(long resourceId, long attachmentId) {
        return jdbcClient.sql("""
                SELECT
                    a.attachment_id,
                    lmra.learning_material_resource_id,
                    lmr.learning_material_id,
                    a.original_filename,
                    a.storage_key,
                    a.stored_path,
                    a.mime_type,
                    a.file_size,
                    a.checksum_sha256,
                    a.created_at
                FROM learning_material_resource_attachments lmra
                JOIN learning_material_resources lmr
                    ON lmr.learning_material_resource_id = lmra.learning_material_resource_id
                JOIN attachments a ON a.attachment_id = lmra.attachment_id
                WHERE lmra.learning_material_resource_id = :resourceId
                  AND a.attachment_id = :attachmentId
                """)
                .param("resourceId", resourceId)
                .param("attachmentId", attachmentId)
                .query((rs, rowNum) -> mapMaterialResourceAttachment(rs))
                .optional();
    }

    public int incrementMaterialViewCount(long materialId) {
        return jdbcClient.sql("""
                UPDATE learning_materials
                SET view_count = COALESCE(view_count, 0) + 1
                WHERE learning_material_id = :materialId
                """)
                .param("materialId", materialId)
                .update();
    }

    public void createMaterialReaction(long materialId, long userId, String reactionType) {
        jdbcClient.sql("""
                INSERT IGNORE INTO learning_material_reactions (
                    learning_material_id,
                    user_id,
                    reaction_type_code_group,
                    reaction_type_code
                )
                VALUES (:materialId, :userId, 'REACTION_TYPE', :reactionType)
                """)
                .param("materialId", materialId)
                .param("userId", userId)
                .param("reactionType", reactionType)
                .update();
    }

    public void deleteMaterialReaction(long materialId, long userId, String reactionType) {
        jdbcClient.sql("""
                DELETE FROM learning_material_reactions
                WHERE learning_material_id = :materialId
                  AND user_id = :userId
                  AND reaction_type_code = :reactionType
                """)
                .param("materialId", materialId)
                .param("userId", userId)
                .param("reactionType", reactionType)
                .update();
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

    public Optional<QuestSubmissionItem> upsertQuestSubmission(long userId, long questId) {
        jdbcClient.sql("""
                INSERT INTO quest_submissions (
                    quest_evaluation_id,
                    user_id,
                    result_status_code,
                    submit_status_code,
                    submitted_at,
                    graded_at,
                    score
                )
                VALUES (:questId, :userId, 'pending', 'submitted', CURRENT_TIMESTAMP, NULL, NULL)
                ON DUPLICATE KEY UPDATE
                    result_status_code = 'pending',
                    submit_status_code = 'submitted',
                    submitted_at = VALUES(submitted_at),
                    graded_at = NULL,
                    score = NULL
                """)
                .param("questId", questId)
                .param("userId", userId)
                .update();
        return findQuestSubmission(userId, questId);
    }

    public Optional<QuestSubmissionItem> findQuestSubmission(long userId, long questId) {
        return jdbcClient.sql("""
                SELECT
                    quest_submission_id,
                    quest_evaluation_id,
                    submit_status_code,
                    submitted_at,
                    result_status_code,
                    score,
                    graded_at
                FROM quest_submissions
                WHERE user_id = :userId AND quest_evaluation_id = :questId
                """)
                .param("userId", userId)
                .param("questId", questId)
                .query((rs, rowNum) -> new QuestSubmissionItem(
                        rs.getLong("quest_submission_id"),
                        rs.getLong("quest_evaluation_id"),
                        rs.getString("submit_status_code"),
                        toOffset(rs.getTimestamp("submitted_at")),
                        rs.getString("result_status_code"),
                        nullableDouble(rs, "score"),
                        toOffset(rs.getTimestamp("graded_at")),
                        false
                ))
                .optional();
    }

    public Optional<QuestSubmissionAttachmentItem> findQuestSubmissionAttachment(
            long questId,
            long submissionId,
            long attachmentId
    ) {
        String storagePrefix = "quests/%d/submissions/%d/".formatted(questId, submissionId);
        return jdbcClient.sql("""
                SELECT attachment_id, original_filename, storage_key, stored_path, mime_type,
                       file_size, checksum_sha256, created_at
                FROM attachments
                WHERE attachment_id = :attachmentId
                  AND storage_key LIKE :storagePrefix
                """)
                .param("attachmentId", attachmentId)
                .param("storagePrefix", storagePrefix + "%")
                .query((rs, rowNum) -> new QuestSubmissionAttachmentItem(
                        rs.getLong("attachment_id"),
                        questId,
                        submissionId,
                        rs.getString("original_filename"),
                        rs.getString("storage_key"),
                        rs.getString("stored_path"),
                        rs.getString("mime_type"),
                        rs.getLong("file_size"),
                        rs.getString("checksum_sha256"),
                        toOffset(rs.getTimestamp("created_at"))
                ))
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

    public List<SurveyQuestionItem> findSurveyQuestions(long surveyId) {
        List<SurveyQuestionRow> questions = jdbcClient.sql("""
                SELECT survey_question_id, question_type_code, question_text, display_order
                FROM survey_questions
                WHERE survey_id = :surveyId
                ORDER BY display_order ASC, survey_question_id ASC
                """)
                .param("surveyId", surveyId)
                .query((rs, rowNum) -> new SurveyQuestionRow(
                        rs.getLong("survey_question_id"),
                        rs.getString("question_type_code"),
                        rs.getString("question_text"),
                        rs.getInt("display_order")
                ))
                .list();

        if (questions.isEmpty()) {
            return List.of();
        }

        Map<Long, List<SurveyOptionItem>> options = findSurveyOptions(
                questions.stream().map(SurveyQuestionRow::id).toList()
        );
        return questions.stream()
                .map(question -> new SurveyQuestionItem(
                        question.id(),
                        question.type(),
                        question.text(),
                        question.displayOrder(),
                        options.getOrDefault(question.id(), List.of())
                ))
                .toList();
    }

    public Optional<Long> findDefaultContentScopeId() {
        return jdbcClient.sql("""
                SELECT content_scope_id
                FROM content_scopes
                ORDER BY
                    CASE scope_type_code
                        WHEN 'class_group' THEN 1
                        WHEN 'track_cohort' THEN 2
                        WHEN 'cohort' THEN 3
                        ELSE 4
                    END,
                    content_scope_id ASC
                LIMIT 1
                """)
                .query(Long.class)
                .optional();
    }

    public long createSurvey(
            long contentScopeId,
            String title,
            String category,
            boolean required,
            OffsetDateTime startAt,
            OffsetDateTime endAt,
            String status
    ) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql("""
                INSERT INTO surveys (
                    content_scope_id,
                    title,
                    survey_category_code,
                    required_yn,
                    progress_status_code,
                    start_at,
                    end_at
                )
                VALUES (
                    :contentScopeId,
                    :title,
                    :category,
                    :required,
                    :status,
                    :startAt,
                    :endAt
                )
                """)
                .param("contentScopeId", contentScopeId)
                .param("title", title)
                .param("category", category)
                .param("required", required)
                .param("status", status)
                .param("startAt", toTimestamp(startAt))
                .param("endAt", toTimestamp(endAt))
                .update(keyHolder, "survey_id");

        Number key = keyHolder.getKey();
        return key == null ? 0L : key.longValue();
    }

    public long createSurveyQuestion(long surveyId, String type, String text, int displayOrder) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql("""
                INSERT INTO survey_questions (
                    survey_id,
                    question_type_code,
                    question_text,
                    display_order
                )
                VALUES (
                    :surveyId,
                    :type,
                    :text,
                    :displayOrder
                )
                """)
                .param("surveyId", surveyId)
                .param("type", type)
                .param("text", text)
                .param("displayOrder", displayOrder)
                .update(keyHolder, "survey_question_id");

        Number key = keyHolder.getKey();
        return key == null ? 0L : key.longValue();
    }

    public long createSurveyOption(long questionId, String text, int displayOrder) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql("""
                INSERT INTO survey_options (
                    survey_question_id,
                    option_text,
                    display_order
                )
                VALUES (
                    :questionId,
                    :text,
                    :displayOrder
                )
                """)
                .param("questionId", questionId)
                .param("text", text)
                .param("displayOrder", displayOrder)
                .update(keyHolder, "survey_option_id");

        Number key = keyHolder.getKey();
        return key == null ? 0L : key.longValue();
    }

    public SurveyResponsePersistence saveSurveyResponse(long surveyId, long userId) {
        jdbcClient.sql("""
                INSERT INTO survey_responses (survey_id, user_id, completed_yn, responded_at)
                VALUES (:surveyId, :userId, TRUE, CURRENT_TIMESTAMP)
                ON DUPLICATE KEY UPDATE
                    completed_yn = VALUES(completed_yn),
                    responded_at = VALUES(responded_at)
                """)
                .param("surveyId", surveyId)
                .param("userId", userId)
                .update();

        return jdbcClient.sql("""
                SELECT survey_response_id, survey_id, completed_yn, responded_at
                FROM survey_responses
                WHERE survey_id = :surveyId AND user_id = :userId
                """)
                .param("surveyId", surveyId)
                .param("userId", userId)
                .query((rs, rowNum) -> new SurveyResponsePersistence(
                        rs.getLong("survey_response_id"),
                        rs.getLong("survey_id"),
                        rs.getBoolean("completed_yn"),
                        toOffset(rs.getTimestamp("responded_at"))
                ))
                .single();
    }

    public Optional<SurveyResponsePersistence> findSurveyResponse(long userId, long surveyId) {
        return jdbcClient.sql("""
                SELECT survey_response_id, survey_id, completed_yn, responded_at
                FROM survey_responses
                WHERE survey_id = :surveyId AND user_id = :userId
                """)
                .param("surveyId", surveyId)
                .param("userId", userId)
                .query((rs, rowNum) -> new SurveyResponsePersistence(
                        rs.getLong("survey_response_id"),
                        rs.getLong("survey_id"),
                        rs.getBoolean("completed_yn"),
                        toOffset(rs.getTimestamp("responded_at"))
                ))
                .optional();
    }

    public List<SurveySavedAnswerItem> findSurveyResponseAnswers(long responseId) {
        List<SurveySavedAnswerRow> rows = jdbcClient.sql("""
                SELECT
                    a.survey_question_id,
                    a.answer_text,
                    o.survey_option_id
                FROM survey_response_answers a
                LEFT JOIN survey_response_answer_options o
                  ON o.survey_response_answer_id = a.survey_response_answer_id
                WHERE a.survey_response_id = :responseId
                ORDER BY a.survey_question_id ASC, o.survey_option_id ASC
                """)
                .param("responseId", responseId)
                .query((rs, rowNum) -> new SurveySavedAnswerRow(
                        rs.getLong("survey_question_id"),
                        rs.getString("answer_text"),
                        nullableLong(rs, "survey_option_id")
                ))
                .list();

        Map<Long, List<SurveySavedAnswerRow>> groupedRows = rows.stream()
                .collect(Collectors.groupingBy(
                        SurveySavedAnswerRow::questionId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return groupedRows.entrySet().stream()
                .map(entry -> new SurveySavedAnswerItem(
                        entry.getKey(),
                        entry.getValue().getFirst().answerText(),
                        entry.getValue().stream()
                                .map(SurveySavedAnswerRow::optionId)
                                .filter(Objects::nonNull)
                                .distinct()
                                .toList()
                ))
                .toList();
    }

    public void deleteSurveyAnswers(long responseId) {
        jdbcClient.sql("""
                DELETE FROM survey_response_answers
                WHERE survey_response_id = :responseId
                """)
                .param("responseId", responseId)
                .update();
    }

    public long createSurveyAnswer(long responseId, long surveyId, long questionId, String answerText) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql("""
                INSERT INTO survey_response_answers (survey_response_id, survey_id, survey_question_id, answer_text)
                VALUES (:responseId, :surveyId, :questionId, :answerText)
                """)
                .param("responseId", responseId)
                .param("surveyId", surveyId)
                .param("questionId", questionId)
                .param("answerText", normalizeNullable(answerText))
                .update(keyHolder, "survey_response_answer_id");

        Number key = keyHolder.getKey();
        return key == null ? 0L : key.longValue();
    }

    public void createSurveyAnswerOptions(long answerId, long questionId, List<Long> optionIds) {
        if (optionIds == null || optionIds.isEmpty()) {
            return;
        }

        for (Long optionId : optionIds) {
            jdbcClient.sql("""
                    INSERT INTO survey_response_answer_options (
                        survey_response_answer_id,
                        survey_question_id,
                        survey_option_id
                    )
                    VALUES (:answerId, :questionId, :optionId)
                    """)
                    .param("answerId", answerId)
                    .param("questionId", questionId)
                    .param("optionId", optionId)
                    .update();
        }
    }

    private Map<Long, List<SurveyOptionItem>> findSurveyOptions(List<Long> questionIds) {
        return jdbcClient.sql("""
                SELECT survey_question_id, survey_option_id, option_text, display_order
                FROM survey_options
                WHERE survey_question_id IN (:questionIds)
                ORDER BY survey_question_id ASC, display_order ASC, survey_option_id ASC
                """)
                .param("questionIds", questionIds)
                .query((rs, rowNum) -> new SurveyOptionRow(
                        rs.getLong("survey_question_id"),
                        new SurveyOptionItem(
                                rs.getLong("survey_option_id"),
                                rs.getString("option_text"),
                                rs.getInt("display_order")
                        )
                ))
                .list()
                .stream()
                .collect(Collectors.groupingBy(SurveyOptionRow::questionId, Collectors.mapping(
                        SurveyOptionRow::option,
                        Collectors.toList()
                )));
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
                rs.getLong("question_count"),
                List.of()
        );
    }

    private MaterialResourceItem mapMaterialResource(ResultSet rs) throws SQLException {
        return new MaterialResourceItem(
                rs.getLong("learning_material_resource_id"),
                rs.getLong("learning_material_id"),
                rs.getString("resource_type_code"),
                rs.getString("resource_title"),
                rs.getString("launch_mode_code"),
                rs.getString("target_url"),
                rs.getInt("display_order")
        );
    }

    private MaterialResourceAttachmentItem mapMaterialResourceAttachment(ResultSet rs) throws SQLException {
        return new MaterialResourceAttachmentItem(
                rs.getLong("attachment_id"),
                rs.getLong("learning_material_resource_id"),
                rs.getLong("learning_material_id"),
                rs.getString("original_filename"),
                rs.getString("storage_key"),
                rs.getString("stored_path"),
                rs.getString("mime_type"),
                rs.getLong("file_size"),
                rs.getString("checksum_sha256"),
                toOffset(rs.getTimestamp("created_at"))
        );
    }

    private Integer nullableInt(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }

    private Long nullableLong(ResultSet rs, String columnName) throws SQLException {
        long value = rs.getLong(columnName);
        return rs.wasNull() ? null : value;
    }

    private Double nullableDouble(ResultSet rs, String columnName) throws SQLException {
        double value = rs.getDouble(columnName);
        return rs.wasNull() ? null : value;
    }

    private OffsetDateTime toOffset(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime().atZone(SEOUL_ZONE).toOffsetDateTime();
    }

    private Timestamp toTimestamp(OffsetDateTime value) {
        return value == null ? null : Timestamp.from(value.toInstant());
    }

    private String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private record SurveyQuestionRow(long id, String type, String text, int displayOrder) {
    }

    private record SurveyOptionRow(long questionId, SurveyOptionItem option) {
    }

    private record SurveySavedAnswerRow(long questionId, String answerText, Long optionId) {
    }

    public record SurveyResponsePersistence(
            long id,
            long surveyId,
            boolean completed,
            OffsetDateTime respondedAt
    ) {
    }
}
