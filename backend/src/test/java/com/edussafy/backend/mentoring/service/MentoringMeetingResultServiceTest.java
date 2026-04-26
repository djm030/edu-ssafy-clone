package com.edussafy.backend.mentoring.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.edussafy.backend.board.dto.BoardPostDetailResponse.BoardCommentItem;
import com.edussafy.backend.board.dto.BoardPostDetailResponse.BoardPostDetail;
import com.edussafy.backend.board.dto.BoardPostDetailResponse.EngagementSummary;
import com.edussafy.backend.board.dto.BoardPostListItem;
import com.edussafy.backend.board.dto.CategoryItem;
import com.edussafy.backend.board.dto.CategorySummary;
import com.edussafy.backend.board.repository.BoardRepository;
import com.edussafy.backend.board.service.BoardQuery;
import com.edussafy.backend.board.service.BoardSort;
import com.edussafy.backend.mentoring.dto.MentoringMeetingResultDtos.MentoringMeetingReviewCreateRequest;
import com.edussafy.backend.mentoring.dto.MentoringMeetingResultDtos.MentoringMeetingReviewUpdateRequest;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

class MentoringMeetingResultServiceTest {

    private static final Clock NOW = Clock.fixed(Instant.parse("2026-04-26T00:00:00Z"), ZoneId.of("Asia/Seoul"));

    @AfterEach
    void resetRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void listAndDetailExposeResultWithReviewStats() {
        BoardRepository repository = baseRepository();
        BoardQuery query = new BoardQuery(null, null, 1, 20, "createdAt,desc");
        given(repository.countPosts(71L, query)).willReturn(1L);
        given(repository.findPosts(71L, query, BoardSort.parse("createdAt,desc"))).willReturn(List.of(listItem(900L, "간담회 결과")));
        given(repository.findPosts(71L, new BoardQuery(null, null, 1, 100, "createdAt,desc"), BoardSort.parse("createdAt,desc")))
                .willReturn(List.of(listItem(900L, "간담회 결과")));
        given(repository.findPostDetail(71L, 900L)).willReturn(Optional.of(resultDetail()));
        given(repository.findPosts(72L, new BoardQuery(null, null, 1, 100, "createdAt,desc"), BoardSort.parse("createdAt,desc")))
                .willReturn(List.of(listItem(910L, "간담회 후기")));
        given(repository.findPostDetail(72L, 910L)).willReturn(Optional.of(reviewDetail(910L, 3L, 5)));
        given(repository.findPostDetail(61L, 100L)).willReturn(Optional.of(meetingDetail("2026-04-01T20:30:00+09:00")));
        withUser(3L);
        MentoringMeetingResultService service = new MentoringMeetingResultService(repository, NOW);

        var list = service.results(1, 20);
        var detail = service.result(100L);

        assertThat(list.items()).hasSize(1);
        assertThat(list.items().getFirst().reviewCount()).isEqualTo(1);
        assertThat(list.items().getFirst().averageRating()).isEqualTo(5.0);
        assertThat(detail.item().reviews()).hasSize(1);
        assertThat(detail.item().meetingId()).isEqualTo(100L);
    }

    @Test
    void appliedUserCanWriteReviewAfterMeetingEnds() {
        BoardRepository repository = baseRepository();
        given(repository.findPostDetail(61L, 100L)).willReturn(Optional.of(meetingDetail("2026-04-01T20:30:00+09:00")));
        given(repository.findComments(100L)).willReturn(List.of(application(3L)));
        given(repository.findCategories(72L)).willReturn(List.of(new CategoryItem(501L, "후기", 1, 0)));
        given(repository.createPost(72L, 501L, 3L, "좋은 간담회", """
                <!--MENTORING_MEETING_REVIEW
                meetingId=100
                rating=5
                -->
                실무 이야기가 도움이 됐습니다.""")).willReturn(910L);
        given(repository.findPostDetail(72L, 910L)).willReturn(Optional.of(reviewDetail(910L, 3L, 5)));
        withUser(3L);
        MentoringMeetingResultService service = new MentoringMeetingResultService(repository, NOW);

        var response = service.createReview(new MentoringMeetingReviewCreateRequest(100L, " 좋은 간담회 ", " 실무 이야기가 도움이 됐습니다. ", 5));

        assertThat(response.item().id()).isEqualTo(910L);
        assertThat(response.item().editable()).isTrue();
    }

    @Test
    void notAppliedUserCannotWriteReview() {
        BoardRepository repository = baseRepository();
        given(repository.findPostDetail(61L, 100L)).willReturn(Optional.of(meetingDetail("2026-04-01T20:30:00+09:00")));
        given(repository.findComments(100L)).willReturn(List.of(application(3L)));
        withUser(4L);
        MentoringMeetingResultService service = new MentoringMeetingResultService(repository, NOW);

        assertThatThrownBy(() -> service.createReview(new MentoringMeetingReviewCreateRequest(100L, "후기", "내용", 5)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403 FORBIDDEN");
    }

    @Test
    void ownerCanUpdateAndDeleteReview() {
        BoardRepository repository = baseRepository();
        given(repository.findPostDetail(72L, 910L)).willReturn(Optional.of(reviewDetail(910L, 3L, 5)), Optional.of(reviewDetail(910L, 3L, 4)), Optional.of(reviewDetail(910L, 3L, 4)));
        given(repository.updatePost(72L, 910L, 502L, "수정 후기", """
                <!--MENTORING_MEETING_REVIEW
                meetingId=100
                rating=4
                -->
                업데이트된 후기""")).willReturn(1);
        given(repository.deletePost(72L, 910L)).willReturn(1);
        given(repository.findPostDetail(61L, 100L)).willReturn(Optional.of(meetingDetail("2026-04-01T20:30:00+09:00")));
        withUser(3L);
        MentoringMeetingResultService service = new MentoringMeetingResultService(repository, NOW);

        var updated = service.updateReview(910L, new MentoringMeetingReviewUpdateRequest("수정 후기", "업데이트된 후기", 4));
        var deleted = service.deleteReview(910L);

        assertThat(updated.item().rating()).isEqualTo(4);
        assertThat(deleted.item().id()).isEqualTo(910L);
        verify(repository).deletePost(72L, 910L);
    }

    private BoardRepository baseRepository() {
        BoardRepository repository = mock(BoardRepository.class);
        given(repository.findBoardId("mentoring_meeting")).willReturn(Optional.of(61L));
        given(repository.findBoardId("mentoring_meeting_result")).willReturn(Optional.of(71L));
        given(repository.findBoardId("mentoring_meeting_review")).willReturn(Optional.of(72L));
        given(repository.findDefaultAuthorUserId()).willReturn(Optional.of(3L));
        return repository;
    }

    private void withUser(long userId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("currentUserId", userId);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private BoardPostListItem listItem(long id, String title) {
        return new BoardPostListItem(id, "mentoring_meeting_result", new CategorySummary(501L, "결과"), title, "Demo Student", OffsetDateTime.now(), 0, 0, 0, 0, false, false);
    }

    private BoardPostDetail meetingDetail(String endsAt) {
        return new BoardPostDetail(100L, "mentoring_meeting", new CategorySummary(500L, "커리어"), "완료된 커리어 간담회", """
                <!--MENTORING_MEETING
                type=ONLINE
                topic=커리어
                capacity=20
                startsAt=2026-04-01T19:00:00+09:00
                endsAt=%s
                applicationStartsAt=2026-03-01T00:00:00+09:00
                applicationEndsAt=2026-03-31T23:59:59+09:00
                location=온라인
                -->
                완료된 간담회입니다.
                """.formatted(endsAt), 2L, "Demo Manager", OffsetDateTime.now(), OffsetDateTime.now(), 0, new EngagementSummary(0, 0, 0), List.of(), List.of(), false, false);
    }

    private BoardPostDetail resultDetail() {
        return new BoardPostDetail(900L, "mentoring_meeting_result", new CategorySummary(501L, "결과"), "완료된 커리어 간담회 결과", """
                <!--MENTORING_MEETING_RESULT
                meetingId=100
                startsAt=2026-04-01T19:00:00+09:00
                endedAt=2026-04-01T20:30:00+09:00
                participantCount=12
                -->
                백엔드 커리어 전환과 면접 준비를 다룬 간담회입니다.
                """, 2L, "Demo Manager", OffsetDateTime.now(), OffsetDateTime.now(), 0, new EngagementSummary(0, 0, 0), List.of(), List.of(), false, false);
    }

    private BoardPostDetail reviewDetail(long id, long authorId, int rating) {
        return new BoardPostDetail(id, "mentoring_meeting_review", new CategorySummary(502L, "후기"), "간담회 후기", """
                <!--MENTORING_MEETING_REVIEW
                meetingId=100
                rating=%d
                -->
                실무 이야기가 도움이 됐습니다.
                """.formatted(rating), authorId, "Demo Student", OffsetDateTime.now(), OffsetDateTime.now(), 0, new EngagementSummary(0, 0, 0), List.of(), List.of(), false, false);
    }

    private BoardCommentItem application(long userId) {
        return new BoardCommentItem(77L, 100L, null, "<!--MENTORING_MEETING_APPLICATION-->\n참여했습니다.", userId, "Demo Student", OffsetDateTime.now(), List.of());
    }
}
