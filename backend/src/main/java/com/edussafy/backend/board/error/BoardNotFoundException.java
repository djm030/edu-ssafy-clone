package com.edussafy.backend.board.error;

public class BoardNotFoundException extends RuntimeException {

    public BoardNotFoundException(String boardCode) {
        super("Board not found: " + boardCode);
    }
}
