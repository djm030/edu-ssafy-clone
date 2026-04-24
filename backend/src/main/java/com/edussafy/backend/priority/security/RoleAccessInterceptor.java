package com.edussafy.backend.priority.security;

import com.edussafy.backend.board.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RoleAccessInterceptor implements HandlerInterceptor {

    public static final String AUTH_HEADER = "X-Demo-Auth";
    public static final String ROLE_HEADER = "X-User-Role";

    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/health",
            "/api/auth/login"
    );
    private static final List<AccessRule> PATH_RULES = List.of(
            new AccessRule("/api/admin/", Set.of("admin"))
    );
    private static final Map<String, List<AccessRule>> METHOD_RULES = Map.of(
            "POST", List.of(
                    new AccessRule("/api/community/classmates/", Set.of("coach", "admin"))
            )
    );

    private final ObjectMapper objectMapper;

    public RoleAccessInterceptor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        if (!path.startsWith("/api") || PUBLIC_PATHS.contains(path)) {
            return true;
        }

        if ("false".equalsIgnoreCase(request.getHeader(AUTH_HEADER))) {
            writeError(response, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "로그인이 필요한 화면입니다.");
            return false;
        }

        String role = normalizeRole(request.getHeader(ROLE_HEADER));
        boolean denied = PATH_RULES.stream()
                .anyMatch(rule -> rule.matches(path) && !rule.allowedRoles().contains(role))
                || METHOD_RULES.getOrDefault(request.getMethod(), List.of()).stream()
                .anyMatch(rule -> rule.matches(path) && !rule.allowedRoles().contains(role));
        if (denied) {
            writeError(response, HttpStatus.FORBIDDEN, "FORBIDDEN", "접근 권한이 없습니다.");
            return false;
        }

        request.setAttribute("currentRole", role);
        return true;
    }

    private String normalizeRole(String value) {
        if (value == null || value.isBlank()) {
            return "learner";
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if ("student".equals(normalized)) {
            return "learner";
        }
        if (Set.of("learner", "coach", "admin").contains(normalized)) {
            return normalized;
        }
        return "learner";
    }

    private void writeError(HttpServletResponse response, HttpStatus status, String code, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), ErrorResponse.of(code, message));
    }

    private record AccessRule(String pathPrefix, Set<String> allowedRoles) {
        boolean matches(String path) {
            if (pathPrefix.equals("/api/community/classmates/")) {
                return path.startsWith(pathPrefix) && path.endsWith("/notifications");
            }
            return path.startsWith(pathPrefix);
        }
    }
}
