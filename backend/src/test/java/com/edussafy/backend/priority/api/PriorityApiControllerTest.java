package com.edussafy.backend.priority.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceRecordsResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceAppealItem;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceAppealResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceAppealsResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceSummary;
import com.edussafy.backend.priority.dto.PriorityDtos.AuthActionResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ClassmateNotificationItem;
import com.edussafy.backend.priority.dto.PriorityDtos.ClassmateNotificationResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ClassmatesResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.CurriculumResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.DashboardSummary;
import com.edussafy.backend.priority.dto.PriorityDtos.LevelSummary;
import com.edussafy.backend.priority.dto.PriorityDtos.NotificationItem;
import com.edussafy.backend.priority.dto.PriorityDtos.NotificationReadResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.NotificationsReadAllResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialItem;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialResourcesResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialViewResponse;
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
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketAttachmentCreateResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketAttachmentDownload;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketAttachmentItem;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketCreateResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketDetail;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketItem;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketMessageCreateResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketMessageItem;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketsResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyDetail;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveysResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.TodaySummary;
import com.edussafy.backend.priority.dto.PriorityDtos.UserProfile;
import com.edussafy.backend.priority.dto.PriorityDtos.UserResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.UserSummary;
import com.edussafy.backend.priority.repository.PriorityApiRepository;
import com.edussafy.backend.priority.security.AuthSession;
import com.edussafy.backend.priority.security.RoleAccessInterceptor;
import com.edussafy.backend.priority.security.RoleAccessWebConfig;
import com.edussafy.backend.priority.service.PriorityApiService;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest({
        AuthController.class,
        DashboardController.class,
        AttendanceController.class,
        NotificationController.class,
        LearningController.class,
        QuestSurveyController.class,
        SupportController.class,
        CommunityController.class,
        ProfileController.class,
        AdminCampusController.class
})
@Import({RoleAccessInterceptor.class, RoleAccessWebConfig.class})
class PriorityApiControllerTest {

    private static final UserProfile USER = new UserProfile(
            1L, "Demo Learner", "student@ssafy.com", "learner", "Seoul", "12", "Java"
    );
    private static final UserProfile COACH_USER = new UserProfile(
            2L, "Demo Manager", "manager@ssafy.com", "manager", "Seoul", "12", "Java"
    );
    private static final UserProfile ADMIN_USER = new UserProfile(
            3L, "Demo Admin", "admin@ssafy.com", "admin", "Seoul", "12", "Java"
    );

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockBean
    private PriorityApiService priorityApiService;

    @MockBean
    private PriorityApiRepository priorityApiRepository;

    @BeforeEach
    void setUpAuthenticatedMvc() {
        given(priorityApiRepository.findUserById(1L)).willReturn(Optional.of(USER));
        given(priorityApiRepository.findUserById(2L)).willReturn(Optional.of(COACH_USER));
        given(priorityApiRepository.findUserById(3L)).willReturn(Optional.of(ADMIN_USER));
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .defaultRequest(get("/").session(sessionFor(1L)))
                .build();
    }

    private static MockHttpSession sessionFor(long userId) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthSession.CURRENT_USER_ID, userId);
        return session;
    }

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
    void logoutReturnsActionResponse() throws Exception {
        given(priorityApiService.logout()).willReturn(new AuthActionResponse(true, "Logged out."));

        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logged out."));
    }

    @Test
    void protectedApiReturnsUnauthorizedWhenDemoAuthDisabled() throws Exception {
        mockMvc.perform(get("/api/me")
                        .header(RoleAccessInterceptor.AUTH_HEADER, "false"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));

        verifyNoInteractions(priorityApiService);
    }

    @Test
    void protectedApiReturnsUnauthorizedWithoutSession() throws Exception {
        MockMvc unauthenticatedMvc = MockMvcBuilders.webAppContextSetup(context).build();

        unauthenticatedMvc.perform(get("/api/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));

        verifyNoInteractions(priorityApiService);
    }

    @Test
    void learnerCannotSendClassmateNotification() throws Exception {
        mockMvc.perform(post("/api/community/classmates/7/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"contact_request\",\"message\":\"Let's study together?\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));

        verifyNoInteractions(priorityApiService);
    }

    @Test
    void adminCampusStructureRequiresAdminRole() throws Exception {
        mockMvc.perform(get("/api/admin/campus-structure"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));

        mockMvc.perform(get("/api/admin/campus-structure")
                        .session(sessionFor(3L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.campuses[0].name").value("서울"));
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
    void attendanceAppealHistoryAndCancelReturnPersistedShape() throws Exception {
        AttendanceAppealItem requested = new AttendanceAppealItem(
                101L,
                7L,
                "status_change",
                "QR failed",
                "present",
                "requested",
                null,
                false
        );
        AttendanceAppealItem canceled = new AttendanceAppealItem(
                101L,
                7L,
                "status_change",
                "QR failed",
                "present",
                "canceled",
                null,
                false
        );
        given(priorityApiService.attendanceAppeals()).willReturn(new AttendanceAppealsResponse(List.of(requested)));
        given(priorityApiService.cancelAttendanceAppeal(101L)).willReturn(new AttendanceAppealResponse(canceled));

        mockMvc.perform(get("/api/attendance/appeals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(101))
                .andExpect(jsonPath("$.items[0].status").value("requested"))
                .andExpect(jsonPath("$.items[0].demo").value(false));
        mockMvc.perform(patch("/api/attendance/appeals/101/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.id").value(101))
                .andExpect(jsonPath("$.item.status").value("canceled"))
                .andExpect(jsonPath("$.item.demo").value(false));
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
        given(priorityApiService.recordMaterialView(5L)).willReturn(new MaterialViewResponse(new MaterialItem(
                5L, "Material", "document", null, null, 1, null, List.of()
        )));
        given(priorityApiService.materialResources(5L)).willReturn(new MaterialResourcesResponse(List.of()));

        mockMvc.perform(get("/api/learning/materials/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.id").value(5))
                .andExpect(jsonPath("$.item.resources").isArray());
        mockMvc.perform(post("/api/learning/materials/5/views"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.id").value(5))
                .andExpect(jsonPath("$.item.viewCount").value(1));
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
                8L, "Survey", "etc", false, null, null, "scheduled", false, 0, List.of()
        )));

        mockMvc.perform(get("/api/quests/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.id").value(7))
                .andExpect(jsonPath("$.item.status").value("scheduled"));
        mockMvc.perform(get("/api/surveys/8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.id").value(8))
                .andExpect(jsonPath("$.item.questionCount").value(0))
                .andExpect(jsonPath("$.item.questions").isArray());
    }

    @Test
    void supportTicketCreateReturnsPersistedRegistration() throws Exception {
        given(priorityApiService.createSupportTicket(any())).willReturn(new SupportTicketCreateResponse(
                new SupportTicketItem(55L, "Need help", "open", null, null, null, 1L, null)
        ));

        mockMvc.perform(post("/api/support/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Need help\",\"content\":\"Please check this.\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.item.id").value(55))
                .andExpect(jsonPath("$.item.title").value("Need help"))
                .andExpect(jsonPath("$.item.status").value("open"))
                .andExpect(jsonPath("$.item.messageCount").value(1));
    }

    @Test
    void supportTicketDetailReturnsMessageThread() throws Exception {
        SupportTicketMessageItem message = new SupportTicketMessageItem(
                66L,
                55L,
                1L,
                "Demo Learner",
                "user_message",
                "Please check this.",
                null,
                List.of()
        );
        given(priorityApiService.supportTicket(55L)).willReturn(new SupportTicketDetailResponse(
                new SupportTicketDetail(55L, "Need help", "open", null, null, null, 1L, null, List.of(message))
        ));

        mockMvc.perform(get("/api/support/tickets/55"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.id").value(55))
                .andExpect(jsonPath("$.item.messages[0].id").value(66))
                .andExpect(jsonPath("$.item.messages[0].senderName").value("Demo Learner"))
                .andExpect(jsonPath("$.item.messages[0].content").value("Please check this."));
    }

    @Test
    void supportTicketMessageCreateReturnsPersistedMessage() throws Exception {
        SupportTicketItem updated = new SupportTicketItem(55L, "Need help", "open", null, null, null, 2L, null);
        SupportTicketMessageItem message = new SupportTicketMessageItem(
                67L,
                55L,
                1L,
                "Demo Learner",
                "user_message",
                "More context.",
                null,
                List.of()
        );
        given(priorityApiService.createSupportTicketMessage(eq(55L), any()))
                .willReturn(new SupportTicketMessageCreateResponse(message, updated));

        mockMvc.perform(post("/api/support/tickets/55/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"More context.\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.item.id").value(67))
                .andExpect(jsonPath("$.item.content").value("More context."))
                .andExpect(jsonPath("$.ticket.messageCount").value(2));
    }

    @Test
    void supportTicketAnswerCreateReturnsPersistedAdminReply() throws Exception {
        SupportTicketItem updated = new SupportTicketItem(55L, "Need help", "answered", null, null, null, 2L, null);
        SupportTicketMessageItem message = new SupportTicketMessageItem(
                68L,
                55L,
                2L,
                "Demo Manager",
                "admin_reply",
                "We checked it.",
                null,
                List.of()
        );
        given(priorityApiService.createSupportTicketAnswer(eq(55L), any()))
                .willReturn(new SupportTicketMessageCreateResponse(message, updated));

        mockMvc.perform(post("/api/support/tickets/55/answers")
                        .session(sessionFor(2L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"We checked it.\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.item.type").value("admin_reply"))
                .andExpect(jsonPath("$.item.senderName").value("Demo Manager"))
                .andExpect(jsonPath("$.ticket.status").value("answered"));
    }

    @Test
    void supportTicketAttachmentCreateReturnsPersistedMetadata() throws Exception {
        SupportTicketAttachmentItem attachment = new SupportTicketAttachmentItem(
                77L,
                67L,
                "error.png",
                "support/tickets/55/messages/67/abc-error.png",
                "/support/tickets/55/messages/67/attachments/abc",
                "image/png",
                11L,
                "abc",
                null
        );
        SupportTicketMessageItem message = new SupportTicketMessageItem(
                67L,
                55L,
                1L,
                "Demo Learner",
                "user_message",
                "More context.",
                null,
                List.of(attachment)
        );
        given(priorityApiService.createSupportTicketMessageAttachment(eq(55L), eq(67L), any()))
                .willReturn(new SupportTicketAttachmentCreateResponse(attachment, message));

        mockMvc.perform(post("/api/support/tickets/55/messages/67/attachments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"filename\":\"error.png\",\"mimeType\":\"image/png\",\"contentBase64\":\"aGVsbG8=\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.item.id").value(77))
                .andExpect(jsonPath("$.item.filename").value("error.png"))
                .andExpect(jsonPath("$.message.attachments[0].id").value(77));
    }

    @Test
    void supportTicketAttachmentDownloadReturnsStoredBytes() throws Exception {
        SupportTicketAttachmentItem attachment = new SupportTicketAttachmentItem(
                77L,
                67L,
                "error.png",
                "support/tickets/55/messages/67/abc-error.png",
                "/support/tickets/55/messages/67/attachments/abc",
                "image/png",
                5L,
                "abc",
                null
        );
        given(priorityApiService.downloadSupportTicketMessageAttachment(55L, 67L, 77L))
                .willReturn(new SupportTicketAttachmentDownload(attachment, "hello".getBytes(StandardCharsets.UTF_8)));

        mockMvc.perform(get("/api/support/tickets/55/messages/67/attachments/77"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/png"))
                .andExpect(header().string(
                        "Content-Disposition",
                        "attachment; filename=\"=?UTF-8?Q?error.png?=\"; filename*=UTF-8''error.png"
                ))
                .andExpect(content().bytes("hello".getBytes(StandardCharsets.UTF_8)));
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
                        .session(sessionFor(2L))
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
    void notificationReadReturnsUpdatedItem() throws Exception {
        given(priorityApiService.markNotificationRead(9L))
                .willReturn(new NotificationReadResponse(
                        new NotificationItem(9L, "공지 확인", "알림 본문", null, true),
                        2L
                ));

        mockMvc.perform(patch("/api/notifications/9/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.id").value(9))
                .andExpect(jsonPath("$.item.read").value(true))
                .andExpect(jsonPath("$.unreadCount").value(2));
    }

    @Test
    void notificationReadAllReturnsUpdatedItems() throws Exception {
        given(priorityApiService.markAllNotificationsRead())
                .willReturn(new NotificationsReadAllResponse(
                        List.of(new NotificationItem(3L, "전체 확인", "알림 본문", null, true)),
                        0L
                ));

        mockMvc.perform(patch("/api/notifications/read-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(3))
                .andExpect(jsonPath("$.items[0].read").value(true))
                .andExpect(jsonPath("$.unreadCount").value(0));
    }

    @Test
    void profileUpdateReturnsPersistedProfileShape() throws Exception {
        given(priorityApiService.updateProfile(any())).willReturn(new ProfileResponse(new ProfileDetails(
                1L, "Updated Student", "student@ssafy.com", "learner", "SSAFY-12-0001",
                "Seoul", "12th", "Java", "Seoul Java 1", "06234", "서울시 강남구", "101호",
                "010-1111-2222", "010-3333-4444", true
        )));

        mockMvc.perform(put("/api/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Updated Student",
                                  "zipCode": "06234",
                                  "addressLine1": "서울시 강남구",
                                  "addressLine2": "101호",
                                  "mobilePhone": "010-1111-2222",
                                  "emergencyPhone": "010-3333-4444",
                                  "marketingOptIn": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile.name").value("Updated Student"))
                .andExpect(jsonPath("$.profile.learnerNo").value("SSAFY-12-0001"))
                .andExpect(jsonPath("$.profile.mobilePhone").value("010-1111-2222"))
                .andExpect(jsonPath("$.profile.marketingOptIn").value(true));
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
