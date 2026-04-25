package com.edussafy.backend.board.error;

import com.edussafy.backend.board.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
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
    public ResponseEntity<ErrorResponse> handleBoardNotFound(BoardNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of("BOARD_NOT_FOUND", "Board not found."));
    }

    @ExceptionHandler(BoardPostNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBoardPostNotFound(BoardPostNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of("BOARD_POST_NOT_FOUND", "Board post not found."));
    }

    @ExceptionHandler(InvalidBoardQueryException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBoardQuery(InvalidBoardQueryException exception) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(exception.code(), exception.getMessage()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException exception) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        return ResponseEntity.status(status)
                .body(ErrorResponse.of(status.name(), exception.getReason()));
    }

    @ExceptionHandler({
            ConstraintViolationException.class,
            MethodArgumentNotValidException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception exception) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of("INVALID_REQUEST", "Invalid request parameters."));
    }
}
