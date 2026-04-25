package com.edussafy.backend.docs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edussafy.backend.priority.api.QuestSurveyController;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyCreateRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyDetail;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyOptionItem;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyQuestionItem;
import com.edussafy.backend.priority.service.PriorityApiService;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = QuestSurveyController.class, properties = "edussafy.auth.interceptor.enabled=false")
@AutoConfigureRestDocs(outputDir = "target/generated-snippets")
class SurveyRestDocsTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PriorityApiService priorityApiService;

    @Test
    void documentsSurveyCreateEndpoint() throws Exception {
        OffsetDateTime startAt = OffsetDateTime.parse("2026-04-26T09:00:00+09:00");
        OffsetDateTime endAt = OffsetDateTime.parse("2026-04-30T18:00:00+09:00");
        given(priorityApiService.createSurvey(any(SurveyCreateRequest.class))).willReturn(new SurveyDetailResponse(new SurveyDetail(
                9L,
                "Weekly pulse",
                "satisfaction",
                true,
                startAt,
                endAt,
                "in_progress",
                false,
                1,
                List.of(new SurveyQuestionItem(
                        91L,
                        "single_choice",
                        "이번 주 과정은 어땠나요?",
                        1,
                        List.of(
                                new SurveyOptionItem(911L, "좋음", 1),
                                new SurveyOptionItem(912L, "보통", 2)
                        )
                ))
        )));

        mockMvc.perform(post("/api/surveys")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Weekly pulse",
                                  "category": "satisfaction",
                                  "required": true,
                                  "status": "in_progress",
                                  "startAt": "2026-04-26T09:00:00+09:00",
                                  "endAt": "2026-04-30T18:00:00+09:00",
                                  "questions": [
                                    {
                                      "type": "single_choice",
                                      "text": "이번 주 과정은 어땠나요?",
                                      "options": [{"text": "좋음"}, {"text": "보통"}]
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.item.id").value(9))
                .andExpect(jsonPath("$.item.questions[0].options[0].text").value("좋음"))
                .andDo(document(
                        "survey-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("title").description("Survey title."),
                                fieldWithPath("category").description("Survey category code, for example satisfaction/course/lecture/etc."),
                                fieldWithPath("required").description("Whether learners must answer this survey."),
                                fieldWithPath("status").description("Progress status code, for example draft/scheduled/in_progress."),
                                fieldWithPath("startAt").description("Survey start timestamp."),
                                fieldWithPath("endAt").description("Survey end timestamp."),
                                fieldWithPath("questions").description("Question definitions to persist with the survey."),
                                fieldWithPath("questions[].type").description("Question type code."),
                                fieldWithPath("questions[].text").description("Question prompt."),
                                fieldWithPath("questions[].options").description("Choice options; required for choice questions."),
                                fieldWithPath("questions[].options[].text").description("Choice option label.")
                        ),
                        responseFields(
                                fieldWithPath("item.id").description("Created survey id."),
                                fieldWithPath("item.title").description("Created survey title."),
                                fieldWithPath("item.category").description("Created survey category code."),
                                fieldWithPath("item.required").description("Whether the survey is required."),
                                fieldWithPath("item.startAt").description("Survey start timestamp."),
                                fieldWithPath("item.endAt").description("Survey end timestamp."),
                                fieldWithPath("item.status").description("Survey progress status."),
                                fieldWithPath("item.completed").description("Whether the current learner has completed it."),
                                fieldWithPath("item.questionCount").description("Number of questions."),
                                fieldWithPath("item.questions").description("Persisted questions."),
                                fieldWithPath("item.questions[].id").description("Question id."),
                                fieldWithPath("item.questions[].type").description("Question type code."),
                                fieldWithPath("item.questions[].text").description("Question prompt."),
                                fieldWithPath("item.questions[].displayOrder").description("Question display order."),
                                fieldWithPath("item.questions[].options").description("Persisted choice options."),
                                fieldWithPath("item.questions[].options[].id").description("Option id."),
                                fieldWithPath("item.questions[].options[].text").description("Option label."),
                                fieldWithPath("item.questions[].options[].displayOrder").description("Option display order.")
                        )
                ));
    }
}
