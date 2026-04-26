package com.edussafy.backend.help.api;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edussafy.backend.help.dto.AcademicRuleDtos.AcademicRuleCategoryItem;
import com.edussafy.backend.help.dto.AcademicRuleDtos.AcademicRuleItem;
import com.edussafy.backend.help.dto.AcademicRuleDtos.AcademicRulesResponse;
import com.edussafy.backend.help.service.AcademicRuleService;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = AcademicRuleController.class, properties = "edussafy.auth.interceptor.enabled=false")
class AcademicRuleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AcademicRuleService academicRuleService;

    @Test
    void rulesExposeAnchorAndSearchMetadata() throws Exception {
        AcademicRuleItem rule = new AcademicRuleItem(
                3101L,
                31L,
                "출결",
                "출결 소명은 언제까지 가능한가요?",
                "발생일 기준 3영업일 이내에 증빙 자료와 함께 신청합니다.",
                OffsetDateTime.parse("2026-04-26T00:00:00Z"),
                "rule-3101",
                "/help/academic-rules#rule-3101",
                true
        );
        given(academicRuleService.rules(31L, "출결")).willReturn(new AcademicRulesResponse(
                List.of(new AcademicRuleCategoryItem(31L, "출결", 1, 1, List.of(rule))),
                "출결",
                1
        ));

        mockMvc.perform(get("/api/help/academic-rules?categoryId=31&keyword=출결"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keyword").value("출결"))
                .andExpect(jsonPath("$.totalRuleCount").value(1))
                .andExpect(jsonPath("$.categories[0].rules[0].anchorId").value("rule-3101"))
                .andExpect(jsonPath("$.categories[0].rules[0].detailPath").value("/help/academic-rules#rule-3101"))
                .andExpect(jsonPath("$.categories[0].rules[0].searchMatched").value(true));
    }
}
