package com.edussafy.backend.priority.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
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
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyResponseSubmitItem;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyResponseSubmitResponse;
import com.edussafy.backend.priority.service.PriorityApiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({
        AttendanceController.class,
        ProfileController.class,
        QuestSurveyController.class
})
class PriorityP4ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PriorityApiService priorityApiService;

    @Test
    void attendanceAppealReturnsDemoShape() throws Exception {
        given(priorityApiService.createAttendanceAppeal(any())).willReturn(new AttendanceAppealResponse(
                new AttendanceAppealItem(0L, "status_change", "Need correction", "present", "requested", null, true)
        ));

        mockMvc.perform(post("/api/attendance/appeals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"status_change\",\"reason\":\"Need correction\",\"requestedStatus\":\"present\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.item.status").value("requested"))
                .andExpect(jsonPath("$.item.demo").value(true));
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
                new SurveyResponseSubmitItem(0L, 6L, true, 1, null, true)
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
                .andExpect(jsonPath("$.item.surveyId").value(6))
                .andExpect(jsonPath("$.item.answerCount").value(1));
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
