package com.edussafy.backend.priority.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceRecordsResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceSummary;
import com.edussafy.backend.priority.dto.PriorityDtos.ClassmateNotificationItem;
import com.edussafy.backend.priority.dto.PriorityDtos.ClassmateNotificationResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ClassmatesResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.CurriculumResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.DashboardSummary;
import com.edussafy.backend.priority.dto.PriorityDtos.LevelSummary;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialItem;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialResourcesResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialsResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.NotificationsResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.NotificationsSummary;
import com.edussafy.backend.priority.dto.PriorityDtos.PageMeta;
import com.edussafy.backend.priority.dto.PriorityDtos.PasswordCheckResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ProfileDetails;
import com.edussafy.backend.priority.dto.PriorityDtos.ProfileResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestItem;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestsResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ReplayResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.RoleAccessResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketCreateResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketItem;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketsResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyDetail;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveysResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.TodaySummary;
import com.edussafy.backend.priority.dto.PriorityDtos.UserProfile;
import com.edussafy.backend.priority.dto.PriorityDtos.UserResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.UserSummary;
import com.edussafy.backend.priority.service.PriorityApiService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({
        AuthController.class,
        DashboardController.class,
        AttendanceController.class,
        NotificationController.class,
        LearningController.class,
        QuestSurveyController.class,
        SupportController.class,
        CommunityController.class,
        ProfileController.class
})
class PriorityApiControllerTest {

    private static final UserProfile USER = new UserProfile(
            1L, "Demo Learner", "student@ssafy.com", "learner", "Seoul", "12", "Java"
    );

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PriorityApiService priorityApiService;

    @Test
    void loginReturnsDemoUser() throws Exception {
        given(priorityApiService.login(any())).willReturn(new UserResponse(USER));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"student@ssafy.com\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id").value(1))
                .andExpect(jsonPath("$.user.email").value("student@ssafy.com"))
                .andExpect(jsonPath("$.user.role").value("learner"));
    }

    @Test
    void loginValidatesRequestBody() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));

        verifyNoInteractions(priorityApiService);
    }

    @Test
    void meReturnsCurrentUser() throws Exception {
        given(priorityApiService.me()).willReturn(new UserResponse(USER));

        mockMvc.perform(get("/api/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.name").value("Demo Learner"));
    }

    @Test
    void currentRoleAccessReturnsPermissions() throws Exception {
        given(priorityApiService.currentRoleAccess()).willReturn(new RoleAccessResponse(
                "learner",
                List.of("dashboard:read", "profile:update"),
                List.of("/admin")
        ));

        mockMvc.perform(get("/api/auth/roles/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("learner"))
                .andExpect(jsonPath("$.permissions[0]").value("dashboard:read"))
                .andExpect(jsonPath("$.deniedRoutes[0]").value("/admin"));
    }

    @Test
    void passwordCheckReturnsValidity() throws Exception {
        given(priorityApiService.passwordCheck(any())).willReturn(new PasswordCheckResponse(true));

        mockMvc.perform(post("/api/profile/password-check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    void dashboardSummaryReturnsPriorityShape() throws Exception {
        given(priorityApiService.dashboardSummary()).willReturn(new DashboardSummary(
                new UserSummary("Demo Learner", "Seoul", "12", "Java"),
                new LevelSummary(1, 0, 1000, 0, null),
                new AttendanceSummary(0, 0, 0, true),
                new NotificationsSummary(0, List.of()),
                new TodaySummary(null, null, null)
        ));

        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.trackName").value("Java"))
                .andExpect(jsonPath("$.level.nextLevelExp").value(1000))
                .andExpect(jsonPath("$.attendance.appealAvailable").value(true))
                .andExpect(jsonPath("$.notifications.latest").isArray());
    }

    @Test
    void priorityListsReturnEmptyPageShapes() throws Exception {
        PageMeta page = new PageMeta(1, 20, 0, 0);
        given(priorityApiService.attendanceRecords())
                .willReturn(new AttendanceRecordsResponse(new AttendanceSummary(0, 0, 0, true), List.of()));
        given(priorityApiService.notifications(1, 20)).willReturn(new NotificationsResponse(List.of(), page));
        given(priorityApiService.materials(eq("spring"), eq("file"), eq(1), eq(20)))
                .willReturn(new MaterialsResponse(List.of(), page));
        given(priorityApiService.quests(1, 20)).willReturn(new QuestsResponse(List.of(), page));
        given(priorityApiService.surveys(1, 20)).willReturn(new SurveysResponse(List.of(), page));
        given(priorityApiService.supportTickets(1, 20)).willReturn(new SupportTicketsResponse(List.of(), page));

        mockMvc.perform(get("/api/attendance/records"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.summary.present").value(0));
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalItems").value(0));
        mockMvc.perform(get("/api/learning/materials?keyword=spring&type=file"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
        mockMvc.perform(get("/api/quests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalPages").value(0));
        mockMvc.perform(get("/api/surveys"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
        mockMvc.perform(get("/api/support/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalItems").value(0));
    }

    @Test
    void learningListsReturnItems() throws Exception {
        given(priorityApiService.curriculum()).willReturn(new CurriculumResponse(List.of()));
        given(priorityApiService.replays()).willReturn(new ReplayResponse(List.of()));

        mockMvc.perform(get("/api/learning/curriculum"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
        mockMvc.perform(get("/api/learning/replays"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void p3LearningDetailsReturnShapes() throws Exception {
        given(priorityApiService.material(5L)).willReturn(new MaterialDetailResponse(new MaterialItem(
                5L, "Material", "document", null, null, 0, null, List.of()
        )));
        given(priorityApiService.materialResources(5L)).willReturn(new MaterialResourcesResponse(List.of()));

        mockMvc.perform(get("/api/learning/materials/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.id").value(5))
                .andExpect(jsonPath("$.item.resources").isArray());
        mockMvc.perform(get("/api/learning/materials/5/resources"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void p3QuestAndSurveyDetailsReturnShapes() throws Exception {
        given(priorityApiService.quest(7L)).willReturn(new QuestDetailResponse(new QuestItem(
                7L, "Quest", "assignment", null, null, null, null, "scheduled", null, null
        )));
        given(priorityApiService.survey(8L)).willReturn(new SurveyDetailResponse(new SurveyDetail(
                8L, "Survey", "etc", false, null, null, "scheduled", false, 0
        )));

        mockMvc.perform(get("/api/quests/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.id").value(7))
                .andExpect(jsonPath("$.item.status").value("scheduled"));
        mockMvc.perform(get("/api/surveys/8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.id").value(8))
                .andExpect(jsonPath("$.item.questionCount").value(0));
    }

    @Test
    void supportTicketCreateReturnsDemoRegistration() throws Exception {
        given(priorityApiService.createSupportTicket(any())).willReturn(new SupportTicketCreateResponse(
                new SupportTicketItem(0L, "Need help", "open", null, null, null, 1L, null)
        ));

        mockMvc.perform(post("/api/support/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Need help\",\"content\":\"Please check this.\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.item.title").value("Need help"))
                .andExpect(jsonPath("$.item.status").value("open"));
    }

    @Test
    void supportTicketCreateValidatesBody() throws Exception {
        mockMvc.perform(post("/api/support/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"content\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));

        verifyNoInteractions(priorityApiService);
    }

    @Test
    void communityAndProfileReturnP2Shapes() throws Exception {
        given(priorityApiService.classmates()).willReturn(new ClassmatesResponse(List.of()));
        given(priorityApiService.createClassmateNotification(eq(7L), any()))
                .willReturn(new ClassmateNotificationResponse(new ClassmateNotificationItem(
                        900_007L,
                        7L,
                        "contact_request",
                        "Let's study together?",
                        "sent",
                        null,
                        null,
                        true
                )));
        given(priorityApiService.profile()).willReturn(new ProfileResponse(new ProfileDetails(
                1L, "Demo Learner", "student@ssafy.com", "learner", null,
                "Seoul", "12", "Java", null, null, null, null, null, null, false
        )));

        mockMvc.perform(get("/api/community/classmates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
        mockMvc.perform(post("/api/community/classmates/7/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"contact_request\",\"message\":\"Let's study together?\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.item.id").value(900007))
                .andExpect(jsonPath("$.item.recipientUserId").value(7))
                .andExpect(jsonPath("$.item.status").value("sent"));
        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile.email").value("student@ssafy.com"))
                .andExpect(jsonPath("$.profile.trackName").value("Java"));
    }

    @Test
    void invalidPriorityPaginationReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/learning/materials?page=0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));

        verifyNoInteractions(priorityApiService);
    }

    @Test
    void invalidSupportPaginationReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/support/tickets?size=0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));

        verifyNoInteractions(priorityApiService);
    }
}
