package com.edussafy.backend.priority.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceAppealItem;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceAppealResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ProfileDetails;
import com.edussafy.backend.priority.dto.PriorityDtos.ProfileResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestSubmissionItem;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestSubmissionResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyResponseDetail;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyResponseDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveySavedAnswerItem;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyResponseSubmitItem;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyResponseSubmitResponse;
import com.edussafy.backend.priority.service.PriorityApiService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        value = {
                AttendanceController.class,
                ProfileController.class,
                QuestSurveyController.class
        },
        properties = "edussafy.auth.interceptor.enabled=false"
)
class PriorityP4ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PriorityApiService priorityApiService;

    @Test
    void attendanceAppealReturnsPersistedShape() throws Exception {
        given(priorityApiService.createAttendanceAppeal(any())).willReturn(new AttendanceAppealResponse(
                new AttendanceAppealItem(101L, 7L, "status_change", "Need correction", "present", "requested", null, false)
        ));

        mockMvc.perform(post("/api/attendance/appeals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"attendanceRecordId\":7,\"type\":\"status_change\",\"reason\":\"Need correction\",\"requestedStatus\":\"present\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.item.id").value(101))
                .andExpect(jsonPath("$.item.attendanceRecordId").value(7))
                .andExpect(jsonPath("$.item.status").value("requested"))
                .andExpect(jsonPath("$.item.demo").value(false));
    }

    @Test
    void profileUpdateReturnsUpdatedDemoProfile() throws Exception {
        given(priorityApiService.updateProfile(any())).willReturn(new ProfileResponse(new ProfileDetails(
                1L, "Updated Name", "student@ssafy.com", "learner", null,
                "Seoul", "12", "Java", null, "12345", null, null, null, null, true
        )));

        mockMvc.perform(put("/api/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Name\",\"zipCode\":\"12345\",\"marketingOptIn\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile.name").value("Updated Name"))
                .andExpect(jsonPath("$.profile.marketingOptIn").value(true));
    }

    @Test
    void questAndSurveySubmitReturnDemoShapes() throws Exception {
        given(priorityApiService.submitQuest(eq(5L), any())).willReturn(new QuestSubmissionResponse(
                new QuestSubmissionItem(0L, 5L, "submitted", null, true)
        ));
        given(priorityApiService.submitSurvey(eq(6L), any())).willReturn(new SurveyResponseSubmitResponse(
                new SurveyResponseSubmitItem(66L, 6L, true, 1, null, false)
        ));

        mockMvc.perform(post("/api/quests/5/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"submission text\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.item.questId").value(5))
                .andExpect(jsonPath("$.item.status").value("submitted"));
        mockMvc.perform(post("/api/surveys/6/responses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answers\":[{\"questionId\":1,\"answerText\":\"yes\"}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.item.id").value(66))
                .andExpect(jsonPath("$.item.surveyId").value(6))
                .andExpect(jsonPath("$.item.answerCount").value(1))
                .andExpect(jsonPath("$.item.demo").value(false));
    }

    @Test
    void surveyResponseReturnsPersistedAnswerShape() throws Exception {
        given(priorityApiService.surveyResponse(6L)).willReturn(new SurveyResponseDetailResponse(
                new SurveyResponseDetail(66L, 6L, true, null, List.of(
                        new SurveySavedAnswerItem(11L, null, List.of(101L)),
                        new SurveySavedAnswerItem(12L, "좋았습니다", List.of())
                ), false)
        ));

        mockMvc.perform(get("/api/surveys/6/responses/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.id").value(66))
                .andExpect(jsonPath("$.item.surveyId").value(6))
                .andExpect(jsonPath("$.item.completed").value(true))
                .andExpect(jsonPath("$.item.answers[0].questionId").value(11))
                .andExpect(jsonPath("$.item.answers[0].optionIds[0]").value(101))
                .andExpect(jsonPath("$.item.answers[1].answerText").value("좋았습니다"))
                .andExpect(jsonPath("$.item.demo").value(false));
    }

    @Test
    void priorityWritesValidateRequiredFields() throws Exception {
        mockMvc.perform(post("/api/attendance/appeals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"\",\"reason\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
        mockMvc.perform(put("/api/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
        mockMvc.perform(post("/api/quests/5/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
        mockMvc.perform(post("/api/surveys/6/responses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answers\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));

        verifyNoInteractions(priorityApiService);
    }
}
