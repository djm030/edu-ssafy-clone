package com.edussafy.backend.mentoring.dto;

import com.edussafy.backend.board.dto.PageMeta;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;

public final class MentoringMeetingResultDtos {

    private MentoringMeetingResultDtos() {
    }

    public record MentoringMeetingResultsResponse(List<MentoringMeetingResultItem> items, PageMeta page) {
    }

    public record MentoringMeetingResultResponse(MentoringMeetingResultDetail item) {
    }

    public record MentoringMeetingReviewsResponse(List<MentoringMeetingReviewItem> items, PageMeta page) {
    }

    public record MentoringMeetingReviewResponse(MentoringMeetingReviewDetail item) {
    }

    public record MentoringMeetingReviewCreateRequest(
            @Min(1) long meetingId,
            @NotBlank @Size(max = 120) String title,
            @NotBlank @Size(max = 4000) String content,
            @Min(1) @Max(5) int rating
    ) {
    }

    public record MentoringMeetingReviewUpdateRequest(
            @NotBlank @Size(max = 120) String title,
            @NotBlank @Size(max = 4000) String content,
            @Min(1) @Max(5) int rating
    ) {
    }

    public record MentoringMeetingResultItem(
            long meetingId,
            long resultId,
            String title,
            String summary,
            OffsetDateTime startsAt,
            OffsetDateTime endedAt,
            int participantCount,
            int reviewCount,
            double averageRating
    ) {
    }

    public record MentoringMeetingResultDetail(
            long meetingId,
            long resultId,
            String title,
            String content,
            OffsetDateTime startsAt,
            OffsetDateTime endedAt,
            int participantCount,
            List<MentoringMeetingReviewItem> reviews,
            double averageRating
    ) {
    }

    public record MentoringMeetingReviewItem(
            long id,
            long meetingId,
            String meetingTitle,
            String title,
            String excerpt,
            int rating,
            String authorName,
            OffsetDateTime createdAt,
            boolean editable
    ) {
    }

    public record MentoringMeetingReviewDetail(
            long id,
            long meetingId,
            String meetingTitle,
            String title,
            String content,
            int rating,
            String authorName,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt,
            boolean editable
    ) {
    }
}
