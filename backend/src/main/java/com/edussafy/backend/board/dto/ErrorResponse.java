package com.edussafy.backend.board.dto;

import java.time.OffsetDateTime;

public record ErrorResponse(ErrorBody error) {

    public static ErrorResponse of(String code, String message) {
        return of(code, message, null, null, null);
    }

    public static ErrorResponse of(String code, String message, Integer status, String path, String requestId) {
        return new ErrorResponse(new ErrorBody(code, message, status, path, requestId, OffsetDateTime.now().toString()));
    }

    public record ErrorBody(
            String code,
            String message,
            Integer status,
            String path,
            String requestId,
            String timestamp
    ) {
    }
}
