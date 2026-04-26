package com.edussafy.backend.mentoring.dto;

import com.edussafy.backend.board.dto.PageMeta;
import java.time.OffsetDateTime;
import java.util.List;

public final class MentorStoryDtos {

    private MentorStoryDtos() {
    }

    public record MentorStoriesResponse(List<MentorStoryItem> items, PageMeta page) {
    }

    public record MentorStoryDetailResponse(MentorStoryDetail item) {
    }

    public record MentorStoryItem(
            long id,
            String title,
            String summary,
            String mentorName,
            String mentorCompany,
            String mentorRole,
            String thumbnailUrl,
            int viewCount,
            OffsetDateTime publishedAt
    ) {
    }

    public record MentorStoryDetail(
            long id,
            String title,
            String content,
            String mentorName,
            String mentorCompany,
            String mentorRole,
            String thumbnailUrl,
            int viewCount,
            OffsetDateTime publishedAt
    ) {
    }
}
