package com.edussafy.backend.board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;
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

    public record BoardCommentUpdateResponse(BoardCommentCreatedItem item) {
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

    public record BoardCommentDeleteResponse(BoardCommentDeletedItem item) {
    }

    public record BoardCommentDeletedItem(long id, long postId, boolean deleted, boolean demo) {
    }

    public record BoardReactionCreateRequest(
            @NotBlank
            @Size(max = 50)
            @Pattern(regexp = "(?i)bookmark|like|report", message = "type must be bookmark, like, or report")
            String type
    ) {
    }

    public record BoardReactionCreateResponse(BoardReactionCreatedItem item) {
    }

    public record BoardReactionCreatedItem(long postId, String type, boolean active, boolean demo) {
    }

    public record BoardPostDeleteResponse(BoardPostDeletedItem item) {
    }

    public record BoardPostDeletedItem(long id, String boardCode, boolean deleted, boolean demo) {
    }

    public record BoardAttachmentCreateRequest(
            @NotBlank @Size(max = 255) String originalFilename,
            @Size(max = 100) String storageKey,
            @Size(max = 500) String storedPath,
            @Size(max = 100) String mimeType,
            @PositiveOrZero Long fileSize,
            @Pattern(regexp = "^[a-fA-F0-9]{64}$", message = "checksumSha256 must be a 64 character hex string")
            String checksumSha256,
            @Size(max = 3_000_000) String contentBase64
    ) {
    }

    public record BoardAttachmentDownload(BoardPostDetailResponse.BoardAttachmentItem item, byte[] content) {
    }

    public record BoardAttachmentCreateResponse(BoardAttachmentCreatedItem item) {
    }

    public record BoardAttachmentDeleteResponse(BoardAttachmentDeletedItem item) {
    }

    public record BoardAttachmentCreatedItem(
            long id,
            long postId,
            String originalFilename,
            String storageKey,
            String storedPath,
            String mimeType,
            Long fileSize,
            OffsetDateTime createdAt,
            boolean demo
    ) {
    }

    public record BoardAttachmentDeletedItem(long id, long postId, boolean deleted, boolean demo) {
    }
}
