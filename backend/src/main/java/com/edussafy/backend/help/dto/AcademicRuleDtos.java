package com.edussafy.backend.help.dto;

import java.time.OffsetDateTime;
import java.util.List;

public final class AcademicRuleDtos {

    private AcademicRuleDtos() {
    }

    public record AcademicRulesResponse(List<AcademicRuleCategoryItem> categories, String keyword, long totalRuleCount) {
        public AcademicRulesResponse(List<AcademicRuleCategoryItem> categories, String keyword) {
            this(
                    categories,
                    keyword,
                    categories.stream().mapToLong(AcademicRuleCategoryItem::ruleCount).sum()
            );
        }
    }

    public record AcademicRuleDetailResponse(AcademicRuleItem item) {
    }

    public record AcademicRuleCategoryItem(
            long id,
            String name,
            int displayOrder,
            long ruleCount,
            List<AcademicRuleItem> rules
    ) {
    }

    public record AcademicRuleItem(
            long id,
            long categoryId,
            String categoryName,
            String question,
            String answer,
            OffsetDateTime updatedAt,
            String anchorId,
            String detailPath,
            boolean searchMatched
    ) {
        public AcademicRuleItem(
                long id,
                long categoryId,
                String categoryName,
                String question,
                String answer,
                OffsetDateTime updatedAt
        ) {
            this(id, categoryId, categoryName, question, answer, updatedAt, "rule-" + id, "/help/academic-rules#" + "rule-" + id, false);
        }
    }
}
