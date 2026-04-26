package com.edussafy.backend.mentoring.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.edussafy.backend.board.dto.BoardPostDetailResponse.BoardPostDetail;
import com.edussafy.backend.board.dto.BoardPostDetailResponse.EngagementSummary;
import com.edussafy.backend.board.dto.BoardPostListItem;
import com.edussafy.backend.board.dto.CategorySummary;
import com.edussafy.backend.board.repository.BoardRepository;
import com.edussafy.backend.board.service.BoardQuery;
import com.edussafy.backend.board.service.BoardSort;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class MentoringNoticeServiceTest {

    @Test
    void listReturnsPinnedMentoringNoticeWithSearchKeyword() {
        BoardRepository repository = mock(BoardRepository.class);
        BoardQuery query = new BoardQuery(11L, "특강", 1, 20, "createdAt,desc");
        given(repository.findBoardId("mentoring_notice")).willReturn(Optional.of(51L));
        given(repository.countPosts(51L, query)).willReturn(1L);
        given(repository.findPosts(51L, query, BoardSort.parse("createdAt,desc"))).willReturn(List.of(listItem(true)));
        MentoringNoticeService service = new MentoringNoticeService(repository);

        var response = service.notices(11L, " 특강 ", 1, 20);

        assertThat(response.keyword()).isEqualTo("특강");
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().getFirst().pinned()).isTrue();
        assertThat(response.items().getFirst().categoryName()).isEqualTo("특강");
    }

    @Test
    void detailIncrementsViewCount() {
        BoardRepository repository = mock(BoardRepository.class);
        given(repository.findBoardId("mentoring_notice")).willReturn(Optional.of(51L));
        given(repository.findPostDetail(51L, 77L)).willReturn(Optional.of(detail()));
        MentoringNoticeService service = new MentoringNoticeService(repository);

        var response = service.notice(77L);

        assertThat(response.item().title()).isEqualTo("멘토링 특강 안내");
        assertThat(response.item().content()).contains("멘토링 특강");
        verify(repository).incrementViewCount(51L, 77L);
    }

    @Test
    void emptySearchKeepsPageMetadata() {
        BoardRepository repository = mock(BoardRepository.class);
        BoardQuery query = new BoardQuery(null, "없음", 2, 10, "createdAt,desc");
        given(repository.findBoardId("mentoring_notice")).willReturn(Optional.of(51L));
        given(repository.countPosts(51L, query)).willReturn(0L);
        MentoringNoticeService service = new MentoringNoticeService(repository);

        var response = service.notices(null, "없음", 2, 10);

        assertThat(response.items()).isEmpty();
        assertThat(response.page().page()).isEqualTo(2);
        assertThat(response.page().totalPages()).isZero();
    }

    private BoardPostListItem listItem(boolean pinned) {
        return new BoardPostListItem(
                77L,
                "mentoring_notice",
                new CategorySummary(11L, "특강"),
                "멘토링 특강 안내",
                "Demo Manager",
                OffsetDateTime.now(),
                12,
                0,
                0,
                0,
                false,
                pinned
        );
    }

    private BoardPostDetail detail() {
        return new BoardPostDetail(
                77L,
                "mentoring_notice",
                new CategorySummary(11L, "특강"),
                "멘토링 특강 안내",
                "현업 멘토링 특강 일정과 신청 전 확인 사항을 안내합니다.",
                2L,
                "Demo Manager",
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                13,
                new EngagementSummary(0, 0, 0),
                List.of(),
                List.of(),
                false,
                true
        );
    }
}
