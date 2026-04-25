package com.edussafy.backend.board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

public final class BoardWriteDtos {

    private BoardWriteDtos() {
    }

    public record BoardPostCreateRequest(
            Long categoryId,
            @NotBlank @Size(max = 255) String title,
            @NotBlank @Size(max = 4000) String content
    ) {
    }

    public record BoardPostCreateResponse(BoardPostCreatedItem item) {
    }

    public record BoardPostUpdateResponse(BoardPostCreatedItem item) {
    }

    public record BoardPostCreatedItem(
            long id,
            String boardCode,
            Long categoryId,
            String title,
            String content,
            String authorName,
            OffsetDateTime createdAt,
            boolean demo
    ) {
    }

    public record BoardCommentCreateRequest(@Positive Long parentCommentId, @NotBlank @Size(max = 4000) String content) {
    }

    public record BoardCommentCreateResponse(BoardCommentCreatedItem item) {
    }

    public record BoardCommentCreatedItem(
            long id,
            long postId,
            Long parentCommentId,
            String content,
            String authorName,
            OffsetDateTime createdAt,
            boolean demo
    ) {
    }

    public record BoardReactionCreateRequest(@NotBlank @Size(max = 50) String type) {
    }

    public record BoardReactionCreateResponse(BoardReactionCreatedItem item) {
    }

    public record BoardReactionCreatedItem(long postId, String type, boolean active, boolean demo) {
    }

    public record BoardPostDeleteResponse(BoardPostDeletedItem item) {
    }

    public record BoardPostDeletedItem(long id, String boardCode, boolean deleted, boolean demo) {
    }
}
