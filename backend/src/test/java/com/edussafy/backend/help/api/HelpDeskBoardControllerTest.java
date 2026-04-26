package com.edussafy.backend.help.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edussafy.backend.board.dto.BoardCategoryListResponse;
import com.edussafy.backend.board.dto.BoardPostDetailResponse;
import com.edussafy.backend.board.dto.BoardPostDetailResponse.BoardPostDetail;
import com.edussafy.backend.board.dto.BoardPostDetailResponse.EngagementSummary;
import com.edussafy.backend.board.dto.BoardPostListItem;
import com.edussafy.backend.board.dto.BoardPostListResponse;
import com.edussafy.backend.board.dto.CategoryItem;
import com.edussafy.backend.board.dto.CategorySummary;
import com.edussafy.backend.board.dto.PageMeta;
import com.edussafy.backend.help.service.HelpDeskBoardService;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = HelpDeskBoardController.class, properties = "edussafy.auth.interceptor.enabled=false")
class HelpDeskBoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HelpDeskBoardService helpDeskBoardService;

    @Test
    void noticesExposeHelpDeskSpecificRoutes() throws Exception {
        given(helpDeskBoardService.noticeCategories())
                .willReturn(new BoardCategoryListResponse(List.of(new CategoryItem(1L, "공지", 1, 3))));
        given(helpDeskBoardService.notices(any()))
                .willReturn(new BoardPostListResponse(List.of(listItem(10L, "notice", true)), new PageMeta(1, 20, 1, 1)));
        given(helpDeskBoardService.notice(10L))
                .willReturn(new BoardPostDetailResponse(detail(10L, "notice", true)));

        mockMvc.perform(get("/api/help/notices/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].postCount").value(3));
        mockMvc.perform(get("/api/help/notices?keyword=일정"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].isPinned").value(true));
        mockMvc.perform(get("/api/help/notices/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.post.viewCount").value(12));
    }

    @Test
    void faqsExposeAccordionContentRoutes() throws Exception {
        given(helpDeskBoardService.faqCategories())
                .willReturn(new BoardCategoryListResponse(List.of(new CategoryItem(2L, "계정", 1, 1))));
        given(helpDeskBoardService.faqs(any()))
                .willReturn(new BoardPostListResponse(List.of(listItem(20L, "faq", false)), new PageMeta(1, 20, 1, 1)));
        given(helpDeskBoardService.faq(20L))
                .willReturn(new BoardPostDetailResponse(detail(20L, "faq", false)));

        mockMvc.perform(get("/api/help/faqs/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].name").value("계정"));
        mockMvc.perform(get("/api/help/faqs?keyword=비밀번호"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].boardCode").value("faq"));
        mockMvc.perform(get("/api/help/faqs/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.post.content").value("상세 답변: 비밀번호 재설정을 진행하세요."));
    }

    private BoardPostListItem listItem(long id, String boardCode, boolean pinned) {
        return new BoardPostListItem(
                id,
                boardCode,
                new CategorySummary(1L, boardCode.equals("faq") ? "계정" : "공지"),
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
                new CategorySummary(1L, boardCode.equals("faq") ? "계정" : "공지"),
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
