package com.edussafy.backend.external.service;

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
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

class ExternalServiceServiceTest {

    private static final Clock NOW = Clock.fixed(Instant.parse("2026-04-26T00:00:00Z"), ZoneId.of("Asia/Seoul"));

    @AfterEach
    void resetRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void listReturnsEnabledDisabledServicesAndAccessStats() {
        BoardRepository repository = baseRepository();
        BoardQuery query = new BoardQuery(null, null, 1, 100, "createdAt,asc");
        given(repository.findPosts(81L, query, BoardSort.parse("createdAt,asc"))).willReturn(List.of(listItem(1L, "JOB SSAFY"), listItem(2L, "Meeting! SSAFY")));
        given(repository.findPostDetail(81L, 1L)).willReturn(Optional.of(detail(1L, "JOB SSAFY", "JOB_SSAFY", true)));
        given(repository.findPostDetail(81L, 2L)).willReturn(Optional.of(detail(2L, "Meeting! SSAFY", "MEETING_SSAFY", false)));
        given(repository.findComments(1L)).willReturn(List.of(accessLog(1L)));
        given(repository.findComments(2L)).willReturn(List.of());
        ExternalServiceService service = new ExternalServiceService(repository, NOW);

        var response = service.services();

        assertThat(response.items()).hasSize(2);
        assertThat(response.items().getFirst().code()).isEqualTo("JOB_SSAFY");
        assertThat(response.items().getFirst().launchType()).isEqualTo("SSO_FORM");
        assertThat(response.items().getFirst().launchable()).isTrue();
        assertThat(response.items().getFirst().accessCount()).isEqualTo(1);
        assertThat(response.items().get(1).enabled()).isFalse();
        assertThat(response.items().get(1).launchable()).isFalse();
        assertThat(response.items().get(1).disabledReason()).contains("운영 연결 전");
    }

    @Test
    void enabledServiceAccessIsLoggedForCurrentUser() {
        BoardRepository repository = baseRepository();
        BoardQuery query = new BoardQuery(null, null, 1, 100, "createdAt,asc");
        given(repository.findPosts(81L, query, BoardSort.parse("createdAt,asc"))).willReturn(List.of(listItem(1L, "JOB SSAFY")));
        given(repository.findPostDetail(81L, 1L)).willReturn(Optional.of(detail(1L, "JOB SSAFY", "JOB_SSAFY", true)));
        withUser(3L);
        ExternalServiceService service = new ExternalServiceService(repository, NOW);

        var response = service.logAccess("job-ssafy");

        assertThat(response.item().code()).isEqualTo("JOB_SSAFY");
        assertThat(response.item().launchType()).isEqualTo("SSO_FORM");
        assertThat(response.item().openInNewWindow()).isTrue();
        verify(repository).createComment(1L, 3L, null, "<!--EXTERNAL_SERVICE_ACCESS code=JOB_SSAFY launchType=SSO_FORM -->");
    }

    @Test
    void disabledServiceAccessFails() {
        BoardRepository repository = baseRepository();
        BoardQuery query = new BoardQuery(null, null, 1, 100, "createdAt,asc");
        given(repository.findPosts(81L, query, BoardSort.parse("createdAt,asc"))).willReturn(List.of(listItem(2L, "Meeting! SSAFY")));
        given(repository.findPostDetail(81L, 2L)).willReturn(Optional.of(detail(2L, "Meeting! SSAFY", "MEETING_SSAFY", false)));
        withUser(3L);
        ExternalServiceService service = new ExternalServiceService(repository, NOW);

        assertThatThrownBy(() -> service.logAccess("MEETING_SSAFY"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400 BAD_REQUEST");
    }

    @Test
    void placeholderUrlIsDisabledUntilConfigured() {
        BoardRepository repository = baseRepository();
        BoardQuery query = new BoardQuery(null, null, 1, 100, "createdAt,asc");
        given(repository.findPosts(81L, query, BoardSort.parse("createdAt,asc"))).willReturn(List.of(listItem(3L, "SSAFY GIT")));
        given(repository.findPostDetail(81L, 3L)).willReturn(Optional.of(detail(3L, "SSAFY GIT", "SSAFY_GIT", true, "#none;")));
        ExternalServiceService service = new ExternalServiceService(repository, NOW);

        var response = service.services();

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().getFirst().enabled()).isTrue();
        assertThat(response.items().getFirst().launchable()).isFalse();
        assertThat(response.items().getFirst().disabledReason()).contains("URL");
    }

    @Test
    void environmentOverrideCanEnableConfiguredLaunchUrl() {
        BoardRepository repository = baseRepository();
        BoardQuery query = new BoardQuery(null, null, 1, 100, "createdAt,asc");
        given(repository.findPosts(81L, query, BoardSort.parse("createdAt,asc"))).willReturn(List.of(listItem(3L, "SSAFY GIT")));
        given(repository.findPostDetail(81L, 3L)).willReturn(Optional.of(detail(3L, "SSAFY GIT", "SSAFY_GIT", true, "#none;")));
        MockEnvironment environment = new MockEnvironment()
                .withProperty("edussafy.external-services.ssafy-git.url", "https://project.example.test")
                .withProperty("edussafy.external-services.ssafy-git.enabled", "true");
        ExternalServiceService service = new ExternalServiceService(repository, NOW, environment);

        var response = service.services();

        assertThat(response.items().getFirst().url()).isEqualTo("https://project.example.test");
        assertThat(response.items().getFirst().launchable()).isTrue();
    }

    private BoardRepository baseRepository() {
        BoardRepository repository = mock(BoardRepository.class);
        given(repository.findBoardId("external_service")).willReturn(Optional.of(81L));
        given(repository.findDefaultAuthorUserId()).willReturn(Optional.of(3L));
        return repository;
    }

    private void withUser(long userId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("currentUserId", userId);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private BoardPostListItem listItem(long id, String title) {
        return new BoardPostListItem(id, "external_service", new CategorySummary(801L, "링크"), title, "Demo Manager", OffsetDateTime.now(), 0, 0, 0, 0, false, false);
    }

    private BoardPostDetail detail(long id, String title, String code, boolean enabled) {
        return detail(id, title, code, enabled, "https://%s.local".formatted(code.toLowerCase().replace('_', '-')));
    }

    private BoardPostDetail detail(long id, String title, String code, boolean enabled, String url) {
        return new BoardPostDetail(id, "external_service", new CategorySummary(801L, "링크"), title, """
                <!--EXTERNAL_SERVICE
                code=%s
                url=%s
                enabled=%s
                launchType=%s
                policyLabel=%s
                disabledReason=%s
                requiresAuth=true
                openInNewWindow=true
                -->
                외부 서비스 링크입니다.
                """.formatted(
                        code,
                        url,
                        enabled,
                        code.equals("JOB_SSAFY") ? "SSO_FORM" : "EXTERNAL_LINK",
                        code.equals("JOB_SSAFY") ? "SSO launch" : "새 창 외부 링크",
                        enabled ? "" : "운영 연결 전까지 비활성화합니다."
                ), 2L, "Demo Manager", OffsetDateTime.now(), OffsetDateTime.now(), 0, new EngagementSummary(0, 0, 0), List.of(), List.of(), false, false);
    }

    private BoardCommentItem accessLog(long postId) {
        return new BoardCommentItem(90L, postId, null, "<!--EXTERNAL_SERVICE_ACCESS code=JOB_SSAFY -->", 3L, "Demo Student", OffsetDateTime.now(), List.of());
    }
}
