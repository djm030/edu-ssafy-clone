package com.edussafy.backend.board.service;

import com.edussafy.backend.board.dto.BoardCategoryListResponse;
import com.edussafy.backend.board.dto.BoardPostDetailResponse;
import com.edussafy.backend.board.dto.BoardPostDetailResponse.BoardCommentItem;
import com.edussafy.backend.board.dto.BoardPostDetailResponse.BoardPostDetail;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
        BoardPostDetail post = boardRepository.findPostDetail(boardId, postId)
                .orElseThrow(() -> new BoardPostNotFoundException(postId));
        return new BoardPostDetailResponse(withComments(post, nestComments(boardRepository.findComments(postId))));
    }

    @Transactional
    public BoardPostCreateResponse createPost(String boardCode, BoardPostCreateRequest request) {
        long boardId = requireBoardId(boardCode);
        validateCategory(boardId, request.categoryId());

        Long authorUserId = boardRepository.findDefaultAuthorUserId().orElse(null);
        long postId = boardRepository.createPost(
                boardId,
                request.categoryId(),
                authorUserId,
                request.title().trim(),
                request.content().trim()
        );
        BoardPostDetail created = boardRepository.findPostDetail(boardId, postId)
                .orElseThrow(() -> new BoardPostNotFoundException(postId));

        return new BoardPostCreateResponse(new BoardPostCreatedItem(
                created.id(),
                created.boardCode(),
                created.category() == null ? null : created.category().id(),
                created.title(),
                created.content(),
                created.authorName(),
                created.createdAt(),
                false
        ));
    }

    @Transactional
    public BoardCommentCreateResponse createComment(
            String boardCode,
            long postId,
            BoardCommentCreateRequest request
    ) {
        long boardId = requireBoardId(boardCode);
        boardRepository.findPostDetail(boardId, postId)
                .orElseThrow(() -> new BoardPostNotFoundException(postId));
        validateParentComment(postId, request.parentCommentId());

        Long authorUserId = boardRepository.findDefaultAuthorUserId().orElse(null);
        long commentId = boardRepository.createComment(postId, authorUserId, request.parentCommentId(), request.content().trim());
        BoardCommentItem created = boardRepository.findComment(commentId)
                .orElseThrow(() -> new BoardPostNotFoundException(postId));

        return new BoardCommentCreateResponse(new BoardCommentCreatedItem(
                created.id(),
                created.postId(),
                created.parentCommentId(),
                created.content(),
                created.authorName(),
                created.createdAt(),
                false
        ));
    }

    @Transactional
    public BoardReactionCreateResponse createReaction(
            String boardCode,
            long postId,
            BoardReactionCreateRequest request
    ) {
        long boardId = requireBoardId(boardCode);
        boardRepository.findPostDetail(boardId, postId)
                .orElseThrow(() -> new BoardPostNotFoundException(postId));
        Long userId = boardRepository.findDefaultAuthorUserId().orElse(null);
        String type = request.type().trim().toLowerCase();
        if (userId != null) {
            boardRepository.createReaction(postId, userId, type);
        }
        return new BoardReactionCreateResponse(new BoardReactionCreatedItem(
                postId,
                type,
                true,
                false
        ));
    }

    @Transactional
    public BoardReactionCreateResponse deleteReaction(
            String boardCode,
            long postId,
            String type
    ) {
        long boardId = requireBoardId(boardCode);
        boardRepository.findPostDetail(boardId, postId)
                .orElseThrow(() -> new BoardPostNotFoundException(postId));
        Long userId = boardRepository.findDefaultAuthorUserId().orElse(null);
        String normalizedType = StringUtils.hasText(type) ? type.trim().toLowerCase() : "like";
        if (userId != null) {
            boardRepository.deleteReaction(postId, userId, normalizedType);
        }
        return new BoardReactionCreateResponse(new BoardReactionCreatedItem(
                postId,
                normalizedType,
                false,
                false
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

    private void validateCategory(long boardId, Long categoryId) {
        if (categoryId != null && !boardRepository.existsCategory(boardId, categoryId)) {
            throw new InvalidBoardQueryException("INVALID_CATEGORY", "categoryId must belong to the selected board.");
        }
    }

    private void validateParentComment(long postId, Long parentCommentId) {
        if (parentCommentId == null) {
            return;
        }
        BoardCommentItem parent = boardRepository.findComment(parentCommentId)
                .orElseThrow(() -> new BoardPostNotFoundException(postId));
        if (parent.postId() != postId) {
            throw new BoardPostNotFoundException(postId);
        }
    }

    private List<BoardCommentItem> nestComments(List<BoardCommentItem> flatComments) {
        Map<Long, BoardCommentItem> byId = new LinkedHashMap<>();
        for (BoardCommentItem comment : flatComments) {
            byId.put(comment.id(), new BoardCommentItem(
                    comment.id(),
                    comment.postId(),
                    comment.parentCommentId(),
                    comment.content(),
                    comment.authorName(),
                    comment.createdAt(),
                    new ArrayList<>()
            ));
        }

        List<BoardCommentItem> roots = new ArrayList<>();
        for (BoardCommentItem comment : byId.values()) {
            if (comment.parentCommentId() != null && byId.containsKey(comment.parentCommentId())) {
                byId.get(comment.parentCommentId()).replies().add(comment);
            } else {
                roots.add(comment);
            }
        }
        return roots;
    }

    private BoardPostDetail withComments(BoardPostDetail post, List<BoardCommentItem> comments) {
        return new BoardPostDetail(
                post.id(),
                post.boardCode(),
                post.category(),
                post.title(),
                post.content(),
                post.authorName(),
                post.createdAt(),
                post.updatedAt(),
                post.viewCount(),
                post.engagement(),
                comments,
                post.hasAttachment(),
                post.isPinned()
        );
    }
}
