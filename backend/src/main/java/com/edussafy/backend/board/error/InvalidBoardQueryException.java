package com.edussafy.backend.board.error;

public class InvalidBoardQueryException extends RuntimeException {

    private final String code;

    public InvalidBoardQueryException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
