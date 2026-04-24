package com.edussafy.backend.board.service;

public record BoardQuery(Long categoryId, String keyword, int page, int size, String sort) {
}
