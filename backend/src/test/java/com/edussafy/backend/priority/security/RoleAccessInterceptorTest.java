package com.edussafy.backend.priority.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.edussafy.backend.priority.dto.PriorityDtos.UserProfile;
import com.edussafy.backend.priority.repository.PriorityApiRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
class RoleAccessInterceptorTest {

    @Mock
    private PriorityApiRepository repository;

    private RoleAccessInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new RoleAccessInterceptor(new ObjectMapper(), repository);
    }

    @Test
    void publicHealthEndpointSkipsSessionLookup() throws Exception {
        MockHttpServletRequest request = request("GET", "/api/health", null);
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertThat(allowed).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
        verify(repository, never()).findUserById(1L);
    }

    @Test
    void missingSessionReturnsUnauthorizedJson() throws Exception {
        MockHttpServletRequest request = request("GET", "/api/surveys", null);
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertThat(allowed).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("UNAUTHORIZED");
    }

    @ParameterizedTest
    @MethodSource("protectedRoleMatrix")
    void protectedMutationMatrixRequiresExpectedRole(
            String method,
            String path,
            String role,
            boolean expectedAllowed
    ) throws Exception {
        given(repository.findUserById(1L)).willReturn(Optional.of(profile(role)));
        MockHttpServletRequest request = request(method, path, 1L);
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertThat(allowed).isEqualTo(expectedAllowed);
        if (expectedAllowed) {
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(request.getAttribute("currentUserId")).isEqualTo(1L);
            assertThat(request.getAttribute("currentRole")).isEqualTo(normalized(role));
        } else {
            assertThat(response.getStatus()).isEqualTo(403);
            assertThat(response.getContentAsString()).contains("FORBIDDEN");
        }
    }

    @Test
    void learnerReadEndpointAllowsAuthenticatedSession() throws Exception {
        given(repository.findUserById(1L)).willReturn(Optional.of(profile("learner")));
        MockHttpServletRequest request = request("GET", "/api/surveys", 1L);
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertThat(allowed).isTrue();
        assertThat(request.getAttribute("currentRole")).isEqualTo("learner");
    }

    private static Stream<Arguments> protectedRoleMatrix() {
        return Stream.of(
                Arguments.of("POST", "/api/surveys", "learner", false),
                Arguments.of("POST", "/api/surveys", "coach", true),
                Arguments.of("POST", "/api/surveys", "admin", true),
                Arguments.of("PUT", "/api/surveys/9", "learner", false),
                Arguments.of("PUT", "/api/surveys/9", "coach", true),
                Arguments.of("DELETE", "/api/surveys/9", "learner", false),
                Arguments.of("DELETE", "/api/surveys/9", "admin", true),
                Arguments.of("PATCH", "/api/attendance/appeals/7/resolve", "learner", false),
                Arguments.of("PATCH", "/api/attendance/appeals/7/resolve", "coach", true),
                Arguments.of("POST", "/api/community/classmates/42/notifications", "learner", false),
                Arguments.of("POST", "/api/community/classmates/42/notifications", "coach", true),
                Arguments.of("POST", "/api/support/tickets/55/answers", "learner", false),
                Arguments.of("POST", "/api/support/tickets/55/answers", "coach", true),
                Arguments.of("GET", "/api/admin/campus-structure", "coach", false),
                Arguments.of("GET", "/api/admin/campus-structure", "admin", true),
                Arguments.of("GET", "/api/admin/campus-structure", "manager", false),
                Arguments.of("POST", "/api/surveys", "manager", true),
                Arguments.of("POST", "/api/surveys", "student", false)
        );
    }

    private static MockHttpServletRequest request(String method, String path, Long userId) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        if (userId != null) {
            request.getSession(true).setAttribute(AuthSession.CURRENT_USER_ID, userId);
        }
        return request;
    }

    private static UserProfile profile(String role) {
        return new UserProfile(1L, "Demo User", "user@ssafy.com", role, "Seoul", "12", "Java");
    }

    private static String normalized(String role) {
        if ("student".equals(role)) {
            return "learner";
        }
        if ("manager".equals(role) || "instructor".equals(role)) {
            return "coach";
        }
        return role;
    }
}
