package com.edussafy.backend.board.api;

import com.edussafy.backend.board.dto.BoardCategoryListResponse;
import com.edussafy.backend.board.dto.BoardPostDetailResponse;
import com.edussafy.backend.board.dto.BoardPostListResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardAttachmentCreateRequest;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardAttachmentCreateResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardCommentCreateRequest;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardCommentCreateResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardPostCreateRequest;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardPostCreateResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardPostUpdateRequest;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardPostUpdateResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardReactionCreateRequest;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardReactionCreateResponse;
import com.edussafy.backend.board.dto.HealthResponse;
import com.edussafy.backend.board.service.BoardQuery;
import com.edussafy.backend.board.service.BoardService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api")
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping("/health")
    public HealthResponse health() {
        return new HealthResponse("UP");
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
            @Valid @RequestBody BoardPostUpdateRequest request,
            @RequestHeader(value = "X-User-Role", defaultValue = "learner") String role
    ) {
        return boardService.updatePost(boardCode, postId, request, role);
    }

    @DeleteMapping("/boards/{boardCode}/posts/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(
            @PathVariable String boardCode,
            @PathVariable Long postId,
            @RequestHeader(value = "X-User-Role", defaultValue = "learner") String role
    ) {
        boardService.deletePost(boardCode, postId, role);
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

    @PostMapping("/boards/{boardCode}/posts/{postId}/reactions")
    @ResponseStatus(HttpStatus.CREATED)
    public BoardReactionCreateResponse createReaction(
            @PathVariable String boardCode,
            @PathVariable Long postId,
            @Valid @RequestBody BoardReactionCreateRequest request
    ) {
        return boardService.createReaction(boardCode, postId, request);
    }
    @PostMapping("/boards/{boardCode}/posts/{postId}/attachments")
    @ResponseStatus(HttpStatus.CREATED)
    public BoardAttachmentCreateResponse attachFile(
            @PathVariable String boardCode,
            @PathVariable Long postId,
            @Valid @RequestBody BoardAttachmentCreateRequest request,
            @RequestHeader(value = "X-User-Role", defaultValue = "learner") String role
    ) {
        return boardService.attachFile(boardCode, postId, request, role);
    }

}
