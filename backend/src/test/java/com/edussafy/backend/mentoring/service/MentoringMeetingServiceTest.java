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
import com.edussafy.backend.board.dto.CategorySummary;
import com.edussafy.backend.board.repository.BoardRepository;
import com.edussafy.backend.board.service.BoardQuery;
import com.edussafy.backend.board.service.BoardSort;
import com.edussafy.backend.mentoring.dto.MentoringMeetingDtos.ApplicationStatus;
import com.edussafy.backend.mentoring.dto.MentoringMeetingDtos.MeetingStatus;
import com.edussafy.backend.mentoring.dto.MentoringMeetingDtos.MentoringMeetingApplicationRequest;
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

class MentoringMeetingServiceTest {

    private static final Clock NOW = Clock.fixed(Instant.parse("2026-04-26T00:00:00Z"), ZoneId.of("Asia/Seoul"));

    @AfterEach
    void resetRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void listShowsRecruitingMeetingAndMyApplicationStatus() {
        BoardRepository repository = mock(BoardRepository.class);
        BoardQuery query = new BoardQuery(null, null, 1, 20, "createdAt,desc");
        given(repository.findBoardId("mentoring_meeting")).willReturn(Optional.of(61L));
        given(repository.countPosts(61L, query)).willReturn(1L);
        given(repository.findPosts(61L, query, BoardSort.parse("createdAt,desc"))).willReturn(List.of(listItem()));
        given(repository.findPostDetail(61L, 100L)).willReturn(Optional.of(detail("2099-12-31T23:59:59+09:00", 2)));
        given(repository.findComments(100L)).willReturn(List.of(application(21L, 3L)));
        given(repository.findDefaultAuthorUserId()).willReturn(Optional.of(3L));
        MentoringMeetingService service = new MentoringMeetingService(repository, NOW);

        var response = service.meetings(1, 20, null);

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().getFirst().status()).isEqualTo(MeetingStatus.RECRUITING);
        assertThat(response.items().getFirst().myApplicationStatus()).isEqualTo(ApplicationStatus.APPLIED);
    }

    @Test
    void applyWithinRecruitingPeriodPersistsApplication() {
        BoardRepository repository = mock(BoardRepository.class);
        given(repository.findBoardId("mentoring_meeting")).willReturn(Optional.of(61L));
        given(repository.findPostDetail(61L, 100L)).willReturn(Optional.of(detail("2099-12-31T23:59:59+09:00", 2)));
        given(repository.findComments(100L)).willReturn(List.of());
        given(repository.createComment(100L, 3L, null, "<!--MENTORING_MEETING_APPLICATION-->\n참여하고 싶습니다.")).willReturn(44L);
        given(repository.findComment(44L)).willReturn(Optional.of(application(44L, 3L)));
        withUser(3L);
        MentoringMeetingService service = new MentoringMeetingService(repository, NOW);

        var response = service.apply(100L, new MentoringMeetingApplicationRequest(" 참여하고 싶습니다. "));

        assertThat(response.item().status()).isEqualTo(ApplicationStatus.APPLIED);
        assertThat(response.item().motivation()).isEqualTo("참여하고 싶습니다.");
    }

    @Test
    void applyOutsideRecruitingPeriodFails() {
        BoardRepository repository = mock(BoardRepository.class);
        given(repository.findBoardId("mentoring_meeting")).willReturn(Optional.of(61L));
        given(repository.findPostDetail(61L, 100L)).willReturn(Optional.of(detail("2026-04-01T00:00:00+09:00", 2)));
        withUser(3L);
        MentoringMeetingService service = new MentoringMeetingService(repository, NOW);

        assertThatThrownBy(() -> service.apply(100L, new MentoringMeetingApplicationRequest("참여")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400 BAD_REQUEST");
    }

    @Test
    void applyPreventsDuplicateAndCapacityExceeded() {
        BoardRepository repository = mock(BoardRepository.class);
        given(repository.findBoardId("mentoring_meeting")).willReturn(Optional.of(61L));
        given(repository.findPostDetail(61L, 100L)).willReturn(Optional.of(detail("2099-12-31T23:59:59+09:00", 1)));
        given(repository.findComments(100L)).willReturn(List.of(application(21L, 3L)));
        withUser(3L);
        MentoringMeetingService service = new MentoringMeetingService(repository, NOW);

        assertThatThrownBy(() -> service.apply(100L, new MentoringMeetingApplicationRequest("참여")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("409 CONFLICT");

        withUser(4L);
        assertThatThrownBy(() -> service.apply(100L, new MentoringMeetingApplicationRequest("참여")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("409 CONFLICT");
    }

    @Test
    void ownerCanCancelOwnApplication() {
        BoardRepository repository = mock(BoardRepository.class);
        given(repository.findBoardId("mentoring_meeting")).willReturn(Optional.of(61L));
        given(repository.findPostDetail(61L, 100L)).willReturn(Optional.of(detail("2099-12-31T23:59:59+09:00", 2)));
        given(repository.findComments(100L)).willReturn(List.of(application(21L, 3L)));
        given(repository.deleteComment(100L, 21L)).willReturn(1);
        withUser(3L);
        MentoringMeetingService service = new MentoringMeetingService(repository, NOW);

        var response = service.cancel(100L);

        assertThat(response.item().status()).isEqualTo(ApplicationStatus.CANCELLED);
        assertThat(response.item().cancelledAt()).isNotNull();
        verify(repository).deleteComment(100L, 21L);
    }

    private void withUser(long userId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("currentUserId", userId);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private BoardPostListItem listItem() {
        return new BoardPostListItem(
                100L,
                "mentoring_meeting",
                new CategorySummary(20L, "커리어"),
                "백엔드 커리어 간담회",
                "Demo Manager",
                OffsetDateTime.now(),
                3,
                1,
                0,
                0,
                false,
                false
        );
    }

    private BoardPostDetail detail(String applicationEndsAt, int capacity) {
        return new BoardPostDetail(
                100L,
                "mentoring_meeting",
                new CategorySummary(20L, "커리어"),
                "백엔드 커리어 간담회",
                """
                <!--MENTORING_MEETING
                type=ONLINE
                topic=커리어
                capacity=%d
                startsAt=2026-05-01T19:00:00+09:00
                endsAt=2026-05-01T20:30:00+09:00
                applicationStartsAt=2026-04-01T00:00:00+09:00
                applicationEndsAt=%s
                location=온라인
                meetingUrl=https://edu.ssafy.local/mentoring/backend-career
                -->
                현업 멘토와 백엔드 커리어 준비 방향을 이야기합니다.
                """.formatted(capacity, applicationEndsAt),
                2L,
                "Demo Manager",
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                4,
                new EngagementSummary(0, 0, 0),
                List.of(),
                List.of(),
                false,
                false
        );
    }

    private BoardCommentItem application(long commentId, long userId) {
        return new BoardCommentItem(
                commentId,
                100L,
                null,
                "<!--MENTORING_MEETING_APPLICATION-->\n참여하고 싶습니다.",
                userId,
                "Demo Student",
                OffsetDateTime.now(),
                List.of()
        );
    }
}
