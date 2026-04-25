package com.edussafy.backend.priority.security;

import jakarta.servlet.http.HttpSession;
import java.util.Optional;

public final class AuthSession {

    public static final String CURRENT_USER_ID = "edussafy.currentUserId";
    public static final int MAX_INACTIVE_SECONDS = 60 * 60 * 2;

    private AuthSession() {
    }

    public static Optional<Long> currentUserId(HttpSession session) {
        if (session == null) {
            return Optional.empty();
        }

        Object userId = session.getAttribute(CURRENT_USER_ID);
        if (userId instanceof Number number) {
            return Optional.of(number.longValue());
        }
        if (userId instanceof String text && !text.isBlank()) {
            try {
                return Optional.of(Long.parseLong(text));
            } catch (NumberFormatException exception) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}
