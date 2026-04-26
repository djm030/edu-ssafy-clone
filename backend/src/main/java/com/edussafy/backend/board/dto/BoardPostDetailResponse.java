package com.edussafy.backend.board.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;
import java.util.List;

public record BoardPostDetailResponse(BoardPostDetail post) {

    public record BoardPostDetail(
            long id,
            String boardCode,
            CategorySummary category,
            String title,
            String content,
            @JsonInclude(JsonInclude.Include.NON_NULL)
            Long authorUserId,
            String authorName,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt,
            int viewCount,
            EngagementSummary engagement,
            List<BoardCommentItem> comments,
            List<BoardAttachmentItem> attachments,
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
            @JsonInclude(JsonInclude.Include.NON_NULL)
            Long authorUserId,
            String authorName,
            OffsetDateTime createdAt,
            List<BoardCommentItem> replies
    ) {
    }

    public record BoardAttachmentItem(
            long id,
            String originalFilename,
            String storageKey,
            String storedPath,
            String mimeType,
            Long fileSize,
            OffsetDateTime createdAt
    ) {
    }
}
