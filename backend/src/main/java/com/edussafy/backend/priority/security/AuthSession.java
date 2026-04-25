package com.edussafy.backend.priority.security;

import jakarta.servlet.http.HttpSession;
import java.time.Instant;
import java.util.Optional;

public final class AuthSession {

    public static final String CURRENT_USER_ID = "edussafy.currentUserId";
    public static final String PROFILE_VERIFIED_UNTIL = "edussafy.profileVerifiedUntil";
    public static final int MAX_INACTIVE_SECONDS = 60 * 60 * 2;
    public static final int PROFILE_VERIFICATION_TTL_SECONDS = 10 * 60;

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

    public static boolean profileVerified(HttpSession session, Instant now) {
        Optional<Instant> verifiedUntil = profileVerifiedUntil(session);
        return now != null && verifiedUntil.isPresent() && verifiedUntil.get().isAfter(now);
    }

    public static Optional<Instant> profileVerifiedUntil(HttpSession session) {
        if (session == null) {
            return Optional.empty();
        }

        Object expiresAt = session.getAttribute(PROFILE_VERIFIED_UNTIL);
        if (expiresAt instanceof Number number) {
            return Optional.of(Instant.ofEpochMilli(number.longValue()));
        }
        if (expiresAt instanceof String text && !text.isBlank()) {
            try {
                return Optional.of(Instant.ofEpochMilli(Long.parseLong(text)));
            } catch (NumberFormatException exception) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    public static void markProfileVerified(HttpSession session, Instant now) {
        if (session != null && now != null) {
            session.setAttribute(
                    PROFILE_VERIFIED_UNTIL,
                    now.plusSeconds(PROFILE_VERIFICATION_TTL_SECONDS).toEpochMilli()
            );
        }
    }

    public static void clearProfileVerification(HttpSession session) {
        if (session != null) {
            session.removeAttribute(PROFILE_VERIFIED_UNTIL);
        }
    }
}
