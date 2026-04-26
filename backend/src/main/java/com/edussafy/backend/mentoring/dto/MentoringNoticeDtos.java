package com.edussafy.backend.mentoring.dto;

import com.edussafy.backend.board.dto.PageMeta;
import java.time.OffsetDateTime;
import java.util.List;

public final class MentoringNoticeDtos {

    private MentoringNoticeDtos() {
    }

    public record MentoringNoticesResponse(List<MentoringNoticeItem> items, PageMeta page, String keyword) {
    }

    public record MentoringNoticeResponse(MentoringNoticeDetail item) {
    }

    public record MentoringNoticeItem(
            long id,
            String title,
            String summary,
            String categoryName,
            boolean pinned,
            int viewCount,
            OffsetDateTime publishedAt
    ) {
    }

    public record MentoringNoticeDetail(
            long id,
            String title,
            String content,
            String categoryName,
            boolean pinned,
            int viewCount,
            OffsetDateTime publishedAt
    ) {
    }
}
