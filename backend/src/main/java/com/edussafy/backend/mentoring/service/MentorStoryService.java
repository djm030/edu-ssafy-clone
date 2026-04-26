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
import com.edussafy.backend.mentoring.dto.MentorStoryDtos.MentorStoriesResponse;
import com.edussafy.backend.mentoring.dto.MentorStoryDtos.MentorStoryDetail;
import com.edussafy.backend.mentoring.dto.MentorStoryDtos.MentorStoryDetailResponse;
import com.edussafy.backend.mentoring.dto.MentorStoryDtos.MentorStoryItem;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MentorStoryService {

    private static final String BOARD_CODE = "mentor_story";

    private final BoardRepository boardRepository;

    public MentorStoryService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    public MentorStoriesResponse stories(int page, int size, String keyword) {
        long boardId = requireBoardId();
        BoardQuery query = new BoardQuery(null, normalizeKeyword(keyword), page, size, "createdAt,desc");
        long totalItems = boardRepository.countPosts(boardId, query);
        var sort = BoardSort.parse(query.sort());
        var items = totalItems == 0
                ? java.util.List.<MentorStoryItem>of()
                : boardRepository.findPosts(boardId, query, sort).stream().map(this::toItem).toList();
        int totalPages = totalItems == 0 ? 0 : (int) Math.ceil((double) totalItems / size);
        return new MentorStoriesResponse(items, new PageMeta(page, size, totalItems, totalPages));
    }

    public MentorStoryDetailResponse story(long storyId) {
        long boardId = requireBoardId();
        boardRepository.incrementViewCount(boardId, storyId);
        BoardPostDetail detail = boardRepository.findPostDetail(boardId, storyId)
                .orElseThrow(() -> new BoardPostNotFoundException(storyId));
        return new MentorStoryDetailResponse(toDetail(detail));
    }

    private long requireBoardId() {
        return boardRepository.findBoardId(BOARD_CODE)
                .orElseThrow(() -> new BoardNotFoundException(BOARD_CODE));
    }

    private MentorStoryItem toItem(BoardPostListItem post) {
        MentorMeta meta = mentorMeta(post.category());
        return new MentorStoryItem(
                post.id(),
                post.title(),
                "현업 멘토가 전하는 학습과 커리어 성장 이야기입니다.",
                post.authorName(),
                meta.company(),
                meta.role(),
                null,
                post.viewCount(),
                post.createdAt()
        );
    }

    private MentorStoryDetail toDetail(BoardPostDetail detail) {
        MentorMeta meta = mentorMeta(detail.category());
        return new MentorStoryDetail(
                detail.id(),
                detail.title(),
                detail.content(),
                detail.authorName(),
                meta.company(),
                meta.role(),
                null,
                detail.viewCount(),
                detail.createdAt()
        );
    }

    private MentorMeta mentorMeta(CategorySummary category) {
        if (category == null || !StringUtils.hasText(category.name())) {
            return new MentorMeta("SSAFY", "멘토");
        }
        String[] parts = category.name().split("·", 2);
        if (parts.length == 2) {
            return new MentorMeta(parts[0].trim(), parts[1].trim());
        }
        return new MentorMeta("SSAFY", category.name());
    }

    private String normalizeKeyword(String keyword) {
        return StringUtils.hasText(keyword) ? keyword.trim() : null;
    }

    private record MentorMeta(String company, String role) {
    }
}
