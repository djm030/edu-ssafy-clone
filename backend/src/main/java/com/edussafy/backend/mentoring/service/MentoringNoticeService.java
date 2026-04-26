package com.edussafy.backend.mentoring.service;

import com.edussafy.backend.board.dto.BoardPostDetailResponse.BoardPostDetail;
import com.edussafy.backend.board.dto.BoardPostListItem;
import com.edussafy.backend.board.dto.CategorySummary;
import com.edussafy.backend.board.dto.PageMeta;
import com.edussafy.backend.board.error.BoardNotFoundException;
import com.edussafy.backend.board.error.BoardPostNotFoundException;
import com.edussafy.backend.board.repository.BoardRepository;
import com.edussafy.backend.board.service.BoardQuery;
import com.edussafy.backend.board.service.BoardSort;
import com.edussafy.backend.mentoring.dto.MentoringNoticeDtos.MentoringNoticeDetail;
import com.edussafy.backend.mentoring.dto.MentoringNoticeDtos.MentoringNoticeItem;
import com.edussafy.backend.mentoring.dto.MentoringNoticeDtos.MentoringNoticeResponse;
import com.edussafy.backend.mentoring.dto.MentoringNoticeDtos.MentoringNoticesResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MentoringNoticeService {

    private static final String BOARD_CODE = "mentoring_notice";

    private final BoardRepository boardRepository;

    public MentoringNoticeService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    public MentoringNoticesResponse notices(Long categoryId, String keyword, int page, int size) {
        validatePagination(page, size);
        long boardId = requireBoardId();
        BoardQuery query = new BoardQuery(categoryId, normalizeKeyword(keyword), page, size, "createdAt,desc");
        long totalItems = boardRepository.countPosts(boardId, query);
        var items = totalItems == 0
                ? java.util.List.<MentoringNoticeItem>of()
                : boardRepository.findPosts(boardId, query, BoardSort.parse(query.sort())).stream()
                        .map(this::toItem)
                        .toList();
        int totalPages = totalItems == 0 ? 0 : (int) Math.ceil((double) totalItems / size);
        return new MentoringNoticesResponse(items, new PageMeta(page, size, totalItems, totalPages), normalizeKeyword(keyword));
    }

    public MentoringNoticeResponse notice(long noticeId) {
        long boardId = requireBoardId();
        boardRepository.incrementViewCount(boardId, noticeId);
        BoardPostDetail detail = boardRepository.findPostDetail(boardId, noticeId)
                .orElseThrow(() -> new BoardPostNotFoundException(noticeId));
        return new MentoringNoticeResponse(toDetail(detail));
    }

    private long requireBoardId() {
        return boardRepository.findBoardId(BOARD_CODE)
                .orElseThrow(() -> new BoardNotFoundException(BOARD_CODE));
    }

    private MentoringNoticeItem toItem(BoardPostListItem post) {
        return new MentoringNoticeItem(
                post.id(),
                post.title(),
                "멘토링 프로그램 운영 안내입니다.",
                categoryName(post.category()),
                post.isPinned(),
                post.viewCount(),
                post.createdAt()
        );
    }

    private MentoringNoticeDetail toDetail(BoardPostDetail detail) {
        return new MentoringNoticeDetail(
                detail.id(),
                detail.title(),
                detail.content(),
                categoryName(detail.category()),
                detail.isPinned(),
                detail.viewCount(),
                detail.createdAt()
        );
    }

    private String categoryName(CategorySummary category) {
        return category == null ? "일반" : category.name();
    }

    private String normalizeKeyword(String keyword) {
        return StringUtils.hasText(keyword) ? keyword.trim() : null;
    }

    private void validatePagination(int page, int size) {
        if (page < 1 || size < 1 || size > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page and size must be within the supported range.");
        }
    }
}
