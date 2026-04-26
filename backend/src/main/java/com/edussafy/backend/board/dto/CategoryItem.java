package com.edussafy.backend.board.dto;

public record CategoryItem(long id, String name, int sortOrder, long postCount) {
    public CategoryItem(long id, String name, int sortOrder) {
        this(id, name, sortOrder, 0);
    }
}
