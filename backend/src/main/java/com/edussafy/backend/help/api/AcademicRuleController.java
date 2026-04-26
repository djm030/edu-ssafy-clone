package com.edussafy.backend.help.api;

import com.edussafy.backend.help.dto.AcademicRuleDtos.AcademicRuleDetailResponse;
import com.edussafy.backend.help.dto.AcademicRuleDtos.AcademicRulesResponse;
import com.edussafy.backend.help.service.AcademicRuleService;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/help/academic-rules")
public class AcademicRuleController {

    private final AcademicRuleService academicRuleService;

    public AcademicRuleController(AcademicRuleService academicRuleService) {
        this.academicRuleService = academicRuleService;
    }

    @GetMapping
    public AcademicRulesResponse rules(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword
    ) {
        return academicRuleService.rules(categoryId, keyword);
    }

    @GetMapping("/{ruleId}")
    public AcademicRuleDetailResponse rule(@PathVariable @Min(1) long ruleId) {
        return academicRuleService.rule(ruleId);
    }
}
