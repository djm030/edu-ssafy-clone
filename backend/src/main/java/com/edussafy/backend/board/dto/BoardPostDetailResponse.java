package com.edussafy.backend.board.dto;

import java.time.OffsetDateTime;

public record BoardPostDetailResponse(BoardPostDetail post) {

    public record BoardPostDetail(
            long id,
            String boardCode,
            CategorySummary category,
            String title,
            String content,
            String authorName,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt,
            int viewCount,
            EngagementSummary engagement,
            boolean hasAttachment,
            boolean isPinned
    ) {
    }

    public record EngagementSummary(long commentCount, long reactionCount, long bookmarkCount) {
    }
}
