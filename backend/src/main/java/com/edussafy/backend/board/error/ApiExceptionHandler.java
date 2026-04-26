package com.edussafy.backend.board.error;

import com.edussafy.backend.board.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(BoardNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBoardNotFound(BoardNotFoundException exception, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "BOARD_NOT_FOUND", "Board not found.", request);
    }

    @ExceptionHandler(BoardPostNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBoardPostNotFound(BoardPostNotFoundException exception, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "BOARD_POST_NOT_FOUND", "Board post not found.", request);
    }

    @ExceptionHandler(InvalidBoardQueryException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBoardQuery(InvalidBoardQueryException exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, exception.code(), exception.getMessage(), request);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        return error(status, status.name(), exception.getReason(), request);
    }

    @ExceptionHandler({
            ConstraintViolationException.class,
            MethodArgumentNotValidException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Invalid request parameters.", request);
    }

    private ResponseEntity<ErrorResponse> error(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request
    ) {
        String requestId = requestId(request);
        return ResponseEntity.status(status)
                .header("X-Request-Id", requestId)
                .body(ErrorResponse.of(code, message, status.value(), request.getRequestURI(), requestId));
    }

    private String requestId(HttpServletRequest request) {
        String existing = request.getHeader("X-Request-Id");
        if (existing != null && !existing.isBlank()) {
            return existing.trim();
        }
        return UUID.randomUUID().toString();
    }
}
