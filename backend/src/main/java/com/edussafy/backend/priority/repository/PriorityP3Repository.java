package com.edussafy.backend.priority.repository;

import com.edussafy.backend.priority.dto.PriorityDtos.MaterialItem;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialResourceItem;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestItem;
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
                        List.of()
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

    public int incrementMaterialViewCount(long materialId) {
        return jdbcClient.sql("""
                UPDATE learning_materials
                SET view_count = COALESCE(view_count, 0) + 1
                WHERE learning_material_id = :materialId
                """)
                .param("materialId", materialId)
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
                SELECT quest_submission_id, quest_evaluation_id, submit_status_code, submitted_at
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
                        false
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

    private Integer nullableInt(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }

    private Long nullableLong(ResultSet rs, String columnName) throws SQLException {
        long value = rs.getLong(columnName);
        return rs.wasNull() ? null : value;
    }

    private OffsetDateTime toOffset(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime().atZone(SEOUL_ZONE).toOffsetDateTime();
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
