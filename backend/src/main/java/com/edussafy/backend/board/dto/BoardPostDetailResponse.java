package com.edussafy.backend.board.dto;

import java.time.OffsetDateTime;
import java.util.List;

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
            List<BoardCommentItem> comments,
            boolean hasAttachment,
            boolean isPinned
    ) {
    }

    public record EngagementSummary(long commentCount, long reactionCount, long bookmarkCount) {
    }

    public record BoardCommentItem(
            long id,
            long postId,
            Long parentCommentId,
            String content,
            String authorName,
            OffsetDateTime createdAt,
            List<BoardCommentItem> replies
    ) {
    }
}
