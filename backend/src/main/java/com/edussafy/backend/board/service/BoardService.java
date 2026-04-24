package com.edussafy.backend.board.service;

import com.edussafy.backend.board.dto.BoardCategoryListResponse;
import com.edussafy.backend.board.dto.BoardPostDetailResponse;
import com.edussafy.backend.board.dto.BoardPostListItem;
import com.edussafy.backend.board.dto.BoardPostListResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardCommentCreateRequest;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardCommentCreateResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardCommentCreatedItem;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardPostCreateRequest;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardPostCreateResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardPostCreatedItem;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardReactionCreateRequest;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardReactionCreateResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardReactionCreatedItem;
import com.edussafy.backend.board.dto.PageMeta;
import com.edussafy.backend.board.error.BoardNotFoundException;
import com.edussafy.backend.board.error.BoardPostNotFoundException;
import com.edussafy.backend.board.error.InvalidBoardQueryException;
import com.edussafy.backend.board.repository.BoardRepository;
import java.util.List;
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
        requireBoardId(boardCode);
        return new BoardPostCreateResponse(new BoardPostCreatedItem(
                0L,
                boardCode,
                request.categoryId(),
                request.title().trim(),
                request.content().trim(),
                "Demo Learner",
                null,
                true
        ));
    }

    public BoardCommentCreateResponse createComment(
            String boardCode,
            long postId,
            BoardCommentCreateRequest request
    ) {
        requireBoardId(boardCode);
        return new BoardCommentCreateResponse(new BoardCommentCreatedItem(
                0L,
                postId,
                request.content().trim(),
                "Demo Learner",
                null,
                true
        ));
    }

    public BoardReactionCreateResponse createReaction(
            String boardCode,
            long postId,
            BoardReactionCreateRequest request
    ) {
        requireBoardId(boardCode);
        return new BoardReactionCreateResponse(new BoardReactionCreatedItem(
                postId,
                request.type().trim().toLowerCase(),
                true,
                true
        ));
    }

    private long requireBoardId(String boardCode) {
        if (!StringUtils.hasText(boardCode)) {
            throw new BoardNotFoundException(boardCode);
        }
        return boardRepository.findBoardId(boardCode)
                .orElseThrow(() -> new BoardNotFoundException(boardCode));
    }

    private void validatePagination(BoardQuery query) {
        if (query.page() < 1) {
            throw new InvalidBoardQueryException("INVALID_PAGE", "page must be greater than or equal to 1.");
        }
        if (query.size() < 1 || query.size() > 100) {
            throw new InvalidBoardQueryException("INVALID_SIZE", "size must be between 1 and 100.");
        }
    }
}
