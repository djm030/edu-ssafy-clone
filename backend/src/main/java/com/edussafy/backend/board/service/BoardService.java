package com.edussafy.backend.board.service;

import com.edussafy.backend.board.dto.BoardCategoryListResponse;
import com.edussafy.backend.board.dto.BoardPostDetailResponse;
import com.edussafy.backend.board.dto.BoardPostListItem;
import com.edussafy.backend.board.dto.BoardPostListResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardAttachmentCreateRequest;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardAttachmentCreateResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardAttachmentItem;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardCommentCreateRequest;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardCommentCreateResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardCommentCreatedItem;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardPostCreateRequest;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardPostCreateResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardPostCreatedItem;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardPostUpdateRequest;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardPostUpdateResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardReactionCreateRequest;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardReactionCreateResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardReactionCreatedItem;
import com.edussafy.backend.board.dto.PageMeta;
import com.edussafy.backend.board.error.BoardNotFoundException;
import com.edussafy.backend.board.error.BoardPostNotFoundException;
import com.edussafy.backend.board.error.ForbiddenBoardOperationException;
import com.edussafy.backend.board.error.InvalidBoardQueryException;
import com.edussafy.backend.board.repository.BoardRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class BoardService {

    private final BoardRepository boardRepository;

    public BoardService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    public BoardCategoryListResponse getCategories(String boardCode) {
        long boardId = requireBoardId(boardCode);
        return new BoardCategoryListResponse(boardRepository.findCategories(boardId));
    }

    public BoardPostListResponse getPosts(String boardCode, BoardQuery query) {
        validatePagination(query);
        BoardSort sort = BoardSort.parse(query.sort());
        long boardId = requireBoardId(boardCode);

        long totalItems = boardRepository.countPosts(boardId, query);
        List<BoardPostListItem> items = totalItems == 0
                ? List.of()
                : boardRepository.findPosts(boardId, query, sort);
        int totalPages = totalItems == 0 ? 0 : (int) Math.ceil((double) totalItems / query.size());

        return new BoardPostListResponse(items, new PageMeta(query.page(), query.size(), totalItems, totalPages));
    }

    public BoardPostDetailResponse getPost(String boardCode, long postId) {
        long boardId = requireBoardId(boardCode);
        return new BoardPostDetailResponse(boardRepository.findPostDetail(boardId, postId)
                .orElseThrow(() -> new BoardPostNotFoundException(postId)));
    }

    public BoardPostCreateResponse createPost(String boardCode, BoardPostCreateRequest request) {
        long boardId = requireBoardId(boardCode);
        return new BoardPostCreateResponse(safe(
                () -> boardRepository.createPost(boardId, boardCode, request),
                new BoardPostCreatedItem(0L, boardCode, request.categoryId(), request.title().trim(), request.content().trim(), "Demo Learner", null, true)
        ));
    }

    public BoardPostUpdateResponse updatePost(String boardCode, long postId, BoardPostUpdateRequest request, String role) {
        requireAdmin(role);
        requireBoardId(boardCode);
        return new BoardPostUpdateResponse(safe(
                () -> boardRepository.updatePost(postId, boardCode, request),
                new BoardPostCreatedItem(postId, boardCode, request.categoryId(), request.title().trim(), request.content().trim(), "Demo Learner", OffsetDateTime.now(), true)
        ));
    }

    public void deletePost(String boardCode, long postId, String role) {
        requireAdmin(role);
        requireBoardId(boardCode);
        safe(() -> {
            boardRepository.deletePost(postId);
            return true;
        }, true);
    }

    public BoardCommentCreateResponse createComment(String boardCode, long postId, BoardCommentCreateRequest request) {
        requireBoardId(boardCode);
        return new BoardCommentCreateResponse(safe(
                () -> boardRepository.createComment(postId, request),
                new BoardCommentCreatedItem(0L, postId, request.content().trim(), "Demo Learner", null, true)
        ));
    }

    public BoardReactionCreateResponse createReaction(String boardCode, long postId, BoardReactionCreateRequest request) {
        requireBoardId(boardCode);
        String type = request.type().trim().toLowerCase(Locale.ROOT);
        return new BoardReactionCreateResponse(safe(
                () -> boardRepository.toggleReaction(postId, type),
                new BoardReactionCreatedItem(postId, type, true, true)
        ));
    }

    public BoardAttachmentCreateResponse attachFile(String boardCode, long postId, BoardAttachmentCreateRequest request, String role) {
        requireAdmin(role);
        requireBoardId(boardCode);
        return new BoardAttachmentCreateResponse(safe(
                () -> boardRepository.attachFile(postId, request),
                new BoardAttachmentItem(0L, postId, request.fileName().trim(), request.mimeType(), request.fileSize(), request.url(), true)
        ));
    }

    private long requireBoardId(String boardCode) {
        if (!StringUtils.hasText(boardCode)) {
            throw new BoardNotFoundException(boardCode);
        }
        return boardRepository.findBoardId(boardCode)
                .orElseThrow(() -> new BoardNotFoundException(boardCode));
    }

    private void requireAdmin(String role) {
        if (!"admin".equalsIgnoreCase(role == null ? "" : role.trim())) {
            throw new ForbiddenBoardOperationException("admin role is required");
        }
    }

    private void validatePagination(BoardQuery query) {
        if (query.page() < 1) {
            throw new InvalidBoardQueryException("INVALID_PAGE", "page must be greater than or equal to 1.");
        }
        if (query.size() < 1 || query.size() > 100) {
            throw new InvalidBoardQueryException("INVALID_SIZE", "size must be between 1 and 100.");
        }
    }

    private <T> T safe(ThrowingSupplier<T> supplier, T fallback) {
        try {
            return supplier.get();
        } catch (DataAccessException exception) {
            return fallback;
        }
    }

    @FunctionalInterface
    private interface ThrowingSupplier<T> {
        T get();
    }
}
