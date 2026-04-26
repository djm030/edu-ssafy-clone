package com.edussafy.backend.mentoring.dto;

import com.edussafy.backend.board.dto.PageMeta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;

public final class MentoringMeetingDtos {

    private MentoringMeetingDtos() {
    }

    public enum MeetingType {
        ONLINE,
        OFFLINE
    }

    public enum MeetingStatus {
        RECRUITING,
        CLOSED,
        DONE
    }

    public enum ApplicationStatus {
        APPLIED,
        CANCELLED,
        SELECTED,
        REJECTED
    }

    public record MentoringMeetingsResponse(List<MentoringMeetingItem> items, PageMeta page) {
    }

    public record MentoringMeetingResponse(MentoringMeetingDetail item) {
    }

    public record MentoringMeetingApplicationsResponse(List<MentoringMeetingApplicationItem> items) {
    }

    public record MentoringMeetingApplicationResponse(MentoringMeetingApplicationItem item) {
    }

    public record MentoringMeetingApplicationRequest(@NotBlank @Size(max = 1000) String motivation) {
    }

    public record MentoringMeetingItem(
            long id,
            String title,
            String description,
            MeetingType meetingType,
            String topic,
            int capacity,
            int appliedCount,
            OffsetDateTime startsAt,
            OffsetDateTime endsAt,
            OffsetDateTime applicationStartsAt,
            OffsetDateTime applicationEndsAt,
            MeetingStatus status,
            String location,
            String meetingUrl,
            ApplicationStatus myApplicationStatus
    ) {
    }

    public record MentoringMeetingDetail(
            long id,
            String title,
            String description,
            MeetingType meetingType,
            String topic,
            int capacity,
            int appliedCount,
            OffsetDateTime startsAt,
            OffsetDateTime endsAt,
            OffsetDateTime applicationStartsAt,
            OffsetDateTime applicationEndsAt,
            MeetingStatus status,
            String location,
            String meetingUrl,
            ApplicationStatus myApplicationStatus,
            String myMotivation
    ) {
    }

    public record MentoringMeetingApplicationItem(
            long meetingId,
            String meetingTitle,
            ApplicationStatus status,
            String motivation,
            OffsetDateTime appliedAt,
            OffsetDateTime cancelledAt
    ) {
    }
}
