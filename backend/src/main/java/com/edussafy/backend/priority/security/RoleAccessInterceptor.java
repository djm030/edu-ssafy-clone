package com.edussafy.backend.priority.security;

import com.edussafy.backend.board.dto.ErrorResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.UserProfile;
import com.edussafy.backend.priority.repository.PriorityApiRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@ConditionalOnProperty(prefix = "edussafy.auth.interceptor", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RoleAccessInterceptor implements HandlerInterceptor {

    public static final String AUTH_HEADER = "X-Demo-Auth";
    public static final String ROLE_HEADER = "X-User-Role";

    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/health",
            "/api/readiness",
            "/api/auth/login"
    );
    private static final List<AccessRule> ACCESS_RULES = List.of(
            new AccessRule("*", "/api/admin/", null, Set.of("admin")),
            new AccessRule("GET", "/api/attendance/appeals/pending", null, Set.of("coach", "admin")),
            new AccessRule("PATCH", "/api/attendance/appeals/", "/resolve", Set.of("coach", "admin")),
            new AccessRule("POST", "/api/community/classmates/", "/notifications", Set.of("coach", "admin")),
            new AccessRule("POST", "/api/learning/materials/", "/attachments", Set.of("coach", "admin")),
            new AccessRule("POST", "/api/support/tickets/", "/answers", Set.of("coach", "admin"))
    );

    private final ObjectMapper objectMapper;
    private final PriorityApiRepository repository;

    public RoleAccessInterceptor(ObjectMapper objectMapper, PriorityApiRepository repository) {
        this.objectMapper = objectMapper;
        this.repository = repository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        if (!path.startsWith("/api") || PUBLIC_PATHS.contains(path) || "OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        if ("false".equalsIgnoreCase(request.getHeader(AUTH_HEADER))) {
            writeError(response, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "로그인이 필요한 화면입니다.");
            return false;
        }

        Optional<Long> sessionUserId = AuthSession.currentUserId(request.getSession(false));
        if (sessionUserId.isEmpty()) {
            writeError(response, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "로그인이 필요한 화면입니다.");
            return false;
        }

        Optional<UserProfile> user = findSessionUser(sessionUserId.get());
        if (user.isEmpty()) {
            invalidateSession(request.getSession(false));
            writeError(response, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "세션 사용자 정보를 찾을 수 없습니다.");
            return false;
        }

        String role = normalizeRole(user.get().role());
        boolean denied = ACCESS_RULES.stream()
                .anyMatch(rule -> rule.matches(request.getMethod(), path) && !rule.allowedRoles().contains(role));
        if (!denied && isSurveyManagementDenied(request.getMethod(), path, role)) {
            denied = true;
        }
        if (denied) {
            writeError(response, HttpStatus.FORBIDDEN, "FORBIDDEN", "접근 권한이 없습니다.");
            return false;
        }

        request.setAttribute("currentUserId", user.get().id());
        request.setAttribute("currentRole", role);
        return true;
    }

    private Optional<UserProfile> findSessionUser(long userId) {
        try {
            return repository.findUserById(userId);
        } catch (DataAccessException exception) {
            return Optional.empty();
        }
    }

    private void invalidateSession(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
    }

    private String normalizeRole(String value) {
        if (value == null || value.isBlank()) {
            return "learner";
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if ("student".equals(normalized)) {
            return "learner";
        }
        if ("manager".equals(normalized) || "instructor".equals(normalized)) {
            return "coach";
        }
        if (Set.of("learner", "coach", "admin").contains(normalized)) {
            return normalized;
        }
        return "learner";
    }

    private boolean isSurveyManagementDenied(String method, String path, String role) {
        boolean managesSurvey = ("POST".equalsIgnoreCase(method) && "/api/surveys".equals(path))
                || (("PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)) && path.matches("^/api/surveys/\\d+$"));
        return managesSurvey && !Set.of("coach", "admin").contains(role);
    }

    private void writeError(HttpServletResponse response, HttpStatus status, String code, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), ErrorResponse.of(code, message));
    }

    private record AccessRule(String method, String pathPrefix, String pathSuffix, Set<String> allowedRoles) {
        boolean matches(String requestMethod, String path) {
            boolean methodMatches = "*".equals(method) || method.equalsIgnoreCase(requestMethod);
            boolean pathMatches = path.startsWith(pathPrefix) && (pathSuffix == null || path.endsWith(pathSuffix));
            return methodMatches && pathMatches;
        }
    }
}
