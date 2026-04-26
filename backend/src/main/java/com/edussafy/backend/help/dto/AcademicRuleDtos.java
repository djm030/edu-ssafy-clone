package com.edussafy.backend.help.dto;

import java.time.OffsetDateTime;
import java.util.List;

public final class AcademicRuleDtos {

    private AcademicRuleDtos() {
    }

    public record AcademicRulesResponse(List<AcademicRuleCategoryItem> categories, String keyword) {
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
            OffsetDateTime updatedAt
    ) {
    }
}
