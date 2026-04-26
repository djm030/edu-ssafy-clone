package com.edussafy.backend.board.api;

import com.edussafy.backend.board.dto.BoardCategoryListResponse;
import com.edussafy.backend.board.dto.BoardPostDetailResponse;
import com.edussafy.backend.board.dto.BoardPostListResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardAttachmentCreateRequest;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardAttachmentCreateResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardAttachmentDeleteResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardAttachmentDownload;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardCommentCreateRequest;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardCommentCreateResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardCommentDeleteResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardCommentUpdateResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardPostCreateRequest;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardPostCreateResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardPostDeleteResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardPostUpdateResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardReactionCreateRequest;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardReactionCreateResponse;
import com.edussafy.backend.board.service.BoardQuery;
import com.edussafy.backend.board.service.BoardService;
import com.edussafy.backend.health.dto.HealthResponse;
import com.edussafy.backend.health.service.HealthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.nio.charset.StandardCharsets;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api")
public class BoardController {

    private final BoardService boardService;
    private final HealthService healthService;

    public BoardController(BoardService boardService, HealthService healthService) {
        this.boardService = boardService;
        this.healthService = healthService;
    }

    @GetMapping("/health")
    public HealthResponse health() {
        return healthService.getHealth();
    }

    @GetMapping("/boards/{boardCode}/categories")
    public BoardCategoryListResponse categories(@PathVariable String boardCode) {
        return boardService.getCategories(boardCode);
    }

    @GetMapping("/boards/{boardCode}/posts")
    public BoardPostListResponse posts(
            @PathVariable String boardCode,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        return boardService.getPosts(boardCode, new BoardQuery(categoryId, keyword, page, size, sort));
    }

    @GetMapping("/boards/{boardCode}/posts/{postId}")
    public BoardPostDetailResponse post(@PathVariable String boardCode, @PathVariable Long postId) {
        return boardService.getPost(boardCode, postId);
    }

    @PostMapping("/boards/{boardCode}/posts")
    @ResponseStatus(HttpStatus.CREATED)
    public BoardPostCreateResponse createPost(
            @PathVariable String boardCode,
            @Valid @RequestBody BoardPostCreateRequest request
    ) {
        return boardService.createPost(boardCode, request);
    }

    @PutMapping("/boards/{boardCode}/posts/{postId}")
    public BoardPostUpdateResponse updatePost(
            @PathVariable String boardCode,
            @PathVariable Long postId,
            @Valid @RequestBody BoardPostCreateRequest request
    ) {
        return boardService.updatePost(boardCode, postId, request);
    }

    @DeleteMapping("/boards/{boardCode}/posts/{postId}")
    public BoardPostDeleteResponse deletePost(
            @PathVariable String boardCode,
            @PathVariable Long postId
    ) {
        return boardService.deletePost(boardCode, postId);
    }

    @PostMapping("/boards/{boardCode}/posts/{postId}/attachments")
    @ResponseStatus(HttpStatus.CREATED)
    public BoardAttachmentCreateResponse createAttachment(
            @PathVariable String boardCode,
            @PathVariable Long postId,
            @Valid @RequestBody BoardAttachmentCreateRequest request
    ) {
        return boardService.createAttachment(boardCode, postId, request);
    }

    @GetMapping("/boards/{boardCode}/posts/{postId}/attachments/{attachmentId}")
    public ResponseEntity<byte[]> downloadAttachment(
            @PathVariable String boardCode,
            @PathVariable Long postId,
            @PathVariable Long attachmentId
    ) {
        BoardAttachmentDownload download = boardService.downloadAttachment(boardCode, postId, attachmentId);
        String mimeType = download.item().mimeType() == null || download.item().mimeType().isBlank()
                ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                : download.item().mimeType();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(download.item().originalFilename(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(download.content());
    }

    @DeleteMapping("/boards/{boardCode}/posts/{postId}/attachments/{attachmentId}")
    public BoardAttachmentDeleteResponse deleteAttachment(
            @PathVariable String boardCode,
            @PathVariable Long postId,
            @PathVariable Long attachmentId
    ) {
        return boardService.deleteAttachment(boardCode, postId, attachmentId);
    }

    @PostMapping("/boards/{boardCode}/posts/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public BoardCommentCreateResponse createComment(
            @PathVariable String boardCode,
            @PathVariable Long postId,
            @Valid @RequestBody BoardCommentCreateRequest request
    ) {
        return boardService.createComment(boardCode, postId, request);
    }

    @PutMapping("/boards/{boardCode}/posts/{postId}/comments/{commentId}")
    public BoardCommentUpdateResponse updateComment(
            @PathVariable String boardCode,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody BoardCommentCreateRequest request
    ) {
        return boardService.updateComment(boardCode, postId, commentId, request);
    }

    @DeleteMapping("/boards/{boardCode}/posts/{postId}/comments/{commentId}")
    public BoardCommentDeleteResponse deleteComment(
            @PathVariable String boardCode,
            @PathVariable Long postId,
            @PathVariable Long commentId
    ) {
        return boardService.deleteComment(boardCode, postId, commentId);
    }

    @PostMapping("/boards/{boardCode}/posts/{postId}/reactions")
    @ResponseStatus(HttpStatus.CREATED)
    public BoardReactionCreateResponse createReaction(
            @PathVariable String boardCode,
            @PathVariable Long postId,
            @Valid @RequestBody BoardReactionCreateRequest request
    ) {
        return boardService.createReaction(boardCode, postId, request);
    }

    @DeleteMapping("/boards/{boardCode}/posts/{postId}/reactions/{type}")
    public BoardReactionCreateResponse deleteReaction(
            @PathVariable String boardCode,
            @PathVariable Long postId,
            @PathVariable String type
    ) {
        return boardService.deleteReaction(boardCode, postId, type);
    }
}
