package com.edussafy.backend.mentoring.dto;

import com.edussafy.backend.board.dto.PageMeta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;

public final class MentoringQuestionDtos {

    private MentoringQuestionDtos() {
    }

    public enum MentoringQuestionStatus {
        OPEN,
        ANSWERED,
        CLOSED
    }

    public record MentoringQuestionsResponse(List<MentoringQuestionItem> items, PageMeta page) {
    }

    public record MentoringQuestionResponse(MentoringQuestionDetail item) {
    }

    public record MentoringQuestionItem(
            long id,
            String title,
            String summary,
            String category,
            MentoringQuestionStatus status,
            boolean anonymous,
            String authorName,
            long answerCount,
            OffsetDateTime createdAt
    ) {
    }

    public record MentoringQuestionDetail(
            long id,
            String title,
            String content,
            String category,
            MentoringQuestionStatus status,
            boolean anonymous,
            String authorName,
            long answerCount,
            OffsetDateTime createdAt,
            List<MentoringAnswerItem> answers
    ) {
    }

    public record MentoringAnswerItem(
            long id,
            String content,
            String mentorName,
            OffsetDateTime createdAt
    ) {
    }

    public record MentoringQuestionCreateRequest(
            Long categoryId,
            @NotBlank @Size(max = 240) String title,
            @NotBlank @Size(max = 3800) String content,
            boolean anonymousAllowed
    ) {
    }

    public record MentoringAnswerCreateRequest(@NotBlank @Size(max = 4000) String content) {
    }
}
