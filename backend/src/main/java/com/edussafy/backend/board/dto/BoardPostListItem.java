package com.edussafy.backend.board.dto;

import java.time.OffsetDateTime;

public record BoardPostListItem(
        long id,
        String boardCode,
        CategorySummary category,
        String title,
        String authorName,
        OffsetDateTime createdAt,
        int viewCount,
        long commentCount,
        long reactionCount,
        long bookmarkCount,
        boolean hasAttachment,
        boolean isPinned
) {
}
