package com.edussafy.backend.help.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.edussafy.backend.board.dto.BoardPostDetailResponse.BoardPostDetail;
import com.edussafy.backend.board.dto.BoardPostDetailResponse.EngagementSummary;
import com.edussafy.backend.board.dto.BoardPostListItem;
import com.edussafy.backend.board.dto.CategoryItem;
import com.edussafy.backend.board.dto.CategorySummary;
import com.edussafy.backend.board.repository.BoardRepository;
import com.edussafy.backend.board.service.BoardQuery;
import com.edussafy.backend.board.service.BoardSort;
import com.edussafy.backend.help.dto.AcademicRuleDtos.AcademicRulesResponse;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class AcademicRuleServiceTest {

    @Test
    void groupsAcademicRulesByCategory() {
        BoardRepository repository = mock(BoardRepository.class);
        given(repository.findBoardId("academic_rules")).willReturn(Optional.of(9L));
        given(repository.findCategories(9L)).willReturn(List.of(
                new CategoryItem(1L, "출결", 1),
                new CategoryItem(2L, "평가", 2)
        ));
        given(repository.findPosts(9L, new BoardQuery(null, null, 1, 100, "id,asc"), BoardSort.parse("id,asc")))
                .willReturn(List.of(listItem(10L, 1L, "출결", "지각 기준"), listItem(11L, 2L, "평가", "재평가 기준")));
        given(repository.findPostDetail(9L, 10L)).willReturn(Optional.of(detail(10L, 1L, "출결", "지각 기준", "오전 입실 시간을 기준으로 합니다.")));
        given(repository.findPostDetail(9L, 11L)).willReturn(Optional.of(detail(11L, 2L, "평가", "재평가 기준", "평가 정책에 따라 재평가를 신청할 수 있습니다.")));
        AcademicRuleService service = new AcademicRuleService(repository);

        AcademicRulesResponse response = service.rules(null, null);

        assertThat(response.categories()).hasSize(2);
        assertThat(response.categories().getFirst().name()).isEqualTo("출결");
        assertThat(response.categories().getFirst().rules()).extracting("question").containsExactly("지각 기준");
        assertThat(response.categories().get(1).rules()).extracting("categoryName").containsExactly("평가");
    }

    @Test
    void searchOnlyReturnsCategoriesWithMatchedRules() {
        BoardRepository repository = mock(BoardRepository.class);
        given(repository.findBoardId("academic_rules")).willReturn(Optional.of(9L));
        given(repository.findCategories(9L)).willReturn(List.of(
                new CategoryItem(1L, "출결", 1),
                new CategoryItem(2L, "평가", 2)
        ));
        given(repository.findPosts(9L, new BoardQuery(null, "재평가", 1, 100, "id,asc"), BoardSort.parse("id,asc")))
                .willReturn(List.of(listItem(11L, 2L, "평가", "재평가 기준")));
        given(repository.findPostDetail(9L, 11L)).willReturn(Optional.of(detail(11L, 2L, "평가", "재평가 기준", "평가 정책에 따라 재평가를 신청할 수 있습니다.")));
        AcademicRuleService service = new AcademicRuleService(repository);

        AcademicRulesResponse response = service.rules(null, " 재평가 ");

        assertThat(response.keyword()).isEqualTo("재평가");
        assertThat(response.categories()).hasSize(1);
        assertThat(response.categories().getFirst().name()).isEqualTo("평가");
        assertThat(response.categories().getFirst().rules()).hasSize(1);
    }

    @Test
    void emptySearchReturnsEmptyCategories() {
        BoardRepository repository = mock(BoardRepository.class);
        given(repository.findBoardId("academic_rules")).willReturn(Optional.of(9L));
        given(repository.findCategories(9L)).willReturn(List.of(new CategoryItem(1L, "출결", 1)));
        given(repository.findPosts(9L, new BoardQuery(null, "없는규정", 1, 100, "id,asc"), BoardSort.parse("id,asc")))
                .willReturn(List.of());
        AcademicRuleService service = new AcademicRuleService(repository);

        AcademicRulesResponse response = service.rules(null, "없는규정");

        assertThat(response.categories()).isEmpty();
    }

    private BoardPostListItem listItem(long id, long categoryId, String categoryName, String title) {
        return new BoardPostListItem(
                id,
                "academic_rules",
                new CategorySummary(categoryId, categoryName),
                title,
                "운영자",
                OffsetDateTime.now(),
                0,
                0,
                0,
                0,
                false,
                false
        );
    }

    private BoardPostDetail detail(long id, long categoryId, String categoryName, String title, String content) {
        return new BoardPostDetail(
                id,
                "academic_rules",
                new CategorySummary(categoryId, categoryName),
                title,
                content,
                1L,
                "운영자",
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                0,
                new EngagementSummary(0, 0, 0),
                List.of(),
                List.of(),
                false,
                false
        );
    }
}
