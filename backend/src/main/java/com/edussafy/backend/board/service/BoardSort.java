package com.edussafy.backend.board.service;

import com.edussafy.backend.board.error.InvalidBoardQueryException;
import java.util.Locale;
import java.util.Map;

public record BoardSort(String orderBySql) {

    private static final Map<String, String> SORT_COLUMNS = Map.of(
            "createdat", "p.created_at",
            "id", "p.board_post_id",
            "title", "p.title",
            "viewcount", "p.view_count"
    );

    public static BoardSort parse(String value) {
        String raw = value == null || value.isBlank() ? "createdAt,desc" : value.trim();
        String[] parts = raw.split(",", -1);
        if (parts.length != 2) {
            throw invalidSort();
        }

        String column = SORT_COLUMNS.get(parts[0].trim().toLowerCase(Locale.ROOT));
        String direction = parts[1].trim().toLowerCase(Locale.ROOT);
        if (column == null || (!direction.equals("asc") && !direction.equals("desc"))) {
            throw invalidSort();
        }

        return new BoardSort(column + " " + direction.toUpperCase(Locale.ROOT));
    }

    private static InvalidBoardQueryException invalidSort() {
        return new InvalidBoardQueryException("INVALID_SORT", "sort must be one of createdAt,id,title,viewCount with asc or desc.");
    }
}
