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

class MentorStoryServiceTest {

    @Test
    void listReturnsPagedMentorStoriesWithParsedCompanyAndRole() {
        BoardRepository repository = mock(BoardRepository.class);
        BoardQuery query = new BoardQuery(null, null, 1, 20, "createdAt,desc");
        given(repository.findBoardId("mentor_story")).willReturn(Optional.of(12L));
        given(repository.countPosts(12L, query)).willReturn(1L);
        given(repository.findPosts(12L, query, BoardSort.parse("createdAt,desc")))
                .willReturn(List.of(listItem()));
        MentorStoryService service = new MentorStoryService(repository);

        var response = service.stories(1, 20, null);

        assertThat(response.page().totalItems()).isEqualTo(1L);
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().getFirst().mentorCompany()).isEqualTo("네이버");
        assertThat(response.items().getFirst().mentorRole()).isEqualTo("백엔드");
    }

    @Test
    void detailIncrementsViewCountAndReturnsContent() {
        BoardRepository repository = mock(BoardRepository.class);
        given(repository.findBoardId("mentor_story")).willReturn(Optional.of(12L));
        given(repository.findPostDetail(12L, 90L)).willReturn(Optional.of(detail()));
        MentorStoryService service = new MentorStoryService(repository);

        var response = service.story(90L);

        assertThat(response.item().content()).contains("성장");
        assertThat(response.item().mentorName()).isEqualTo("김멘토");
        verify(repository).incrementViewCount(12L, 90L);
    }

    @Test
    void keywordSearchCanReturnEmptyPage() {
        BoardRepository repository = mock(BoardRepository.class);
        BoardQuery query = new BoardQuery(null, "없는멘토", 1, 20, "createdAt,desc");
        given(repository.findBoardId("mentor_story")).willReturn(Optional.of(12L));
        given(repository.countPosts(12L, query)).willReturn(0L);
        MentorStoryService service = new MentorStoryService(repository);

        var response = service.stories(1, 20, " 없는멘토 ");

        assertThat(response.items()).isEmpty();
        assertThat(response.page().totalPages()).isZero();
    }

    private BoardPostListItem listItem() {
        return new BoardPostListItem(
                90L,
                "mentor_story",
                new CategorySummary(40L, "네이버 · 백엔드"),
                "비전공자에서 백엔드 개발자로",
                "김멘토",
                OffsetDateTime.now(),
                15,
                0,
                0,
                0,
                false,
                false
        );
    }

    private BoardPostDetail detail() {
        return new BoardPostDetail(
                90L,
                "mentor_story",
                new CategorySummary(40L, "네이버 · 백엔드"),
                "비전공자에서 백엔드 개발자로",
                "꾸준한 학습 기록과 코드 리뷰가 성장의 기준이 되었습니다.",
                7L,
                "김멘토",
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                16,
                new EngagementSummary(0, 0, 0),
                List.of(),
                List.of(),
                false,
                false
        );
    }
}
