package com.edussafy.backend.board.dto;

import java.util.List;

public record BoardPostListResponse(List<BoardPostListItem> items, PageMeta page) {
}
