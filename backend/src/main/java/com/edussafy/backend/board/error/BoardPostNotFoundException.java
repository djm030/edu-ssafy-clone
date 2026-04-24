package com.edussafy.backend.board.error;

public class BoardPostNotFoundException extends RuntimeException {

    public BoardPostNotFoundException(long postId) {
        super("Board post not found: " + postId);
    }
}
