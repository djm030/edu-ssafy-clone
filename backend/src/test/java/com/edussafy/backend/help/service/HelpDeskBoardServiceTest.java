package com.edussafy.backend.help.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.edussafy.backend.board.dto.BoardCategoryListResponse;
import com.edussafy.backend.board.dto.BoardPostDetailResponse;
import com.edussafy.backend.board.dto.BoardPostDetailResponse.BoardPostDetail;
import com.edussafy.backend.board.dto.BoardPostDetailResponse.EngagementSummary;
import com.edussafy.backend.board.dto.BoardPostListItem;
import com.edussafy.backend.board.dto.BoardPostListResponse;
import com.edussafy.backend.board.dto.CategoryItem;
import com.edussafy.backend.board.dto.CategorySummary;
import com.edussafy.backend.board.dto.PageMeta;
import com.edussafy.backend.board.service.BoardQuery;
import com.edussafy.backend.board.service.BoardService;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class HelpDeskBoardServiceTest {

    @Test
    void noticesDelegateToNoticeBoardWithPinnedListShape() {
        BoardService boardService = mock(BoardService.class);
        BoardQuery query = new BoardQuery(3L, " 일정 ", 1, 20, "createdAt,desc");
        given(boardService.getCategories("notice"))
                .willReturn(new BoardCategoryListResponse(List.of(new CategoryItem(3L, "학사", 1, 7))));
        given(boardService.getPosts("notice", query))
                .willReturn(new BoardPostListResponse(List.of(listItem(10L, "notice", true)), new PageMeta(1, 20, 1, 1)));
        given(boardService.getPost("notice", 10L))
                .willReturn(new BoardPostDetailResponse(detail(10L, "notice", true)));
        HelpDeskBoardService service = new HelpDeskBoardService(boardService);

        assertThat(service.noticeCategories().items()).extracting(CategoryItem::postCount).containsExactly(7L);
        assertThat(service.notices(query).items().getFirst().isPinned()).isTrue();
        assertThat(service.notice(10L).post().viewCount()).isEqualTo(12);
    }

    @Test
    void faqsDelegateToFaqBoardForAccordionContent() {
        BoardService boardService = mock(BoardService.class);
        BoardQuery query = new BoardQuery(null, "비밀번호", 1, 20, "id,asc");
        given(boardService.getCategories("faq"))
                .willReturn(new BoardCategoryListResponse(List.of(new CategoryItem(5L, "계정", 1, 2))));
        given(boardService.getPosts("faq", query))
                .willReturn(new BoardPostListResponse(List.of(listItem(20L, "faq", false)), new PageMeta(1, 20, 1, 1)));
        given(boardService.getPost("faq", 20L))
                .willReturn(new BoardPostDetailResponse(detail(20L, "faq", false)));
        HelpDeskBoardService service = new HelpDeskBoardService(boardService);

        assertThat(service.faqCategories().items()).extracting(CategoryItem::name).containsExactly("계정");
        assertThat(service.faqs(query).items()).extracting(BoardPostListItem::boardCode).containsExactly("faq");
        assertThat(service.faq(20L).post().content()).contains("상세 답변");
    }

    private BoardPostListItem listItem(long id, String boardCode, boolean pinned) {
        return new BoardPostListItem(
                id,
                boardCode,
                new CategorySummary(3L, boardCode.equals("faq") ? "계정" : "학사"),
                boardCode.equals("faq") ? "비밀번호를 잊었어요" : "교육 일정 공지",
                "운영자",
                OffsetDateTime.parse("2026-04-26T00:00:00Z"),
                12,
                0,
                0,
                0,
                false,
                pinned
        );
    }

    private BoardPostDetail detail(long id, String boardCode, boolean pinned) {
        return new BoardPostDetail(
                id,
                boardCode,
                new CategorySummary(3L, boardCode.equals("faq") ? "계정" : "학사"),
                boardCode.equals("faq") ? "비밀번호를 잊었어요" : "교육 일정 공지",
                boardCode.equals("faq") ? "상세 답변: 비밀번호 재설정을 진행하세요." : "상세 공지 본문입니다.",
                1L,
                "운영자",
                OffsetDateTime.parse("2026-04-26T00:00:00Z"),
                OffsetDateTime.parse("2026-04-26T00:00:00Z"),
                12,
                new EngagementSummary(0, 0, 0),
                List.of(),
                List.of(),
                false,
                pinned
        );
    }
}
