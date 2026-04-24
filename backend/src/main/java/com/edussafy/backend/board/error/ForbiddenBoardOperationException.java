package com.edussafy.backend.board.error;

public class ForbiddenBoardOperationException extends RuntimeException {

    public ForbiddenBoardOperationException(String message) {
        super(message);
    }
}
