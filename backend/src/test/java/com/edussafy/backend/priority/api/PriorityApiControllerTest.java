package com.edussafy.backend.priority.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceRecordsResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceRange;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceDaySummary;
import com.edussafy.backend.priority.dto.PriorityDtos.AccessPolicyItem;
import com.edussafy.backend.priority.dto.PriorityDtos.AccessPolicyResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceAppealItem;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceAppealResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceAppealsResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceAppealResolveRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceSummary;
import com.edussafy.backend.priority.dto.PriorityDtos.AuthActionResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.AuthSessionResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.BookmarkDeleteResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.BookmarkItem;
import com.edussafy.backend.priority.dto.PriorityDtos.BookmarkResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.BookmarksResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ClassmateNotificationItem;
import com.edussafy.backend.priority.dto.PriorityDtos.ClassmateNotificationResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ClassmateFilters;
import com.edussafy.backend.priority.dto.PriorityDtos.ClassmateSummary;
import com.edussafy.backend.priority.dto.PriorityDtos.ClassmatesResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.CurriculumResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.CurriculumSessionItem;
import com.edussafy.backend.priority.dto.PriorityDtos.CurriculumWeekDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.CurriculumWeekItem;
import com.edussafy.backend.priority.dto.PriorityDtos.CurriculumWeeksResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.DashboardAttendanceCheck;
import com.edussafy.backend.priority.dto.PriorityDtos.DashboardBoardPost;
import com.edussafy.backend.priority.dto.PriorityDtos.DashboardCurriculumSession;
import com.edussafy.backend.priority.dto.PriorityDtos.DashboardEbookCard;
import com.edussafy.backend.priority.dto.PriorityDtos.DashboardHomeWidgets;
import com.edussafy.backend.priority.dto.PriorityDtos.DashboardLearningCard;
import com.edussafy.backend.priority.dto.PriorityDtos.DashboardQuestCard;
import com.edussafy.backend.priority.dto.PriorityDtos.DashboardSummary;
import com.edussafy.backend.priority.dto.PriorityDtos.EducationAttendanceSummary;
import com.edussafy.backend.priority.dto.PriorityDtos.EducationLearningSummary;
import com.edussafy.backend.priority.dto.PriorityDtos.EducationPointSummary;
import com.edussafy.backend.priority.dto.PriorityDtos.EducationQuestSummary;
import com.edussafy.backend.priority.dto.PriorityDtos.EducationStatusResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.DocumentAttachmentDownload;
import com.edussafy.backend.priority.dto.PriorityDtos.DocumentAttachmentItem;
import com.edussafy.backend.priority.dto.PriorityDtos.DocumentRequestDetail;
import com.edussafy.backend.priority.dto.PriorityDtos.DocumentRequestDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.DocumentRequestItem;
import com.edussafy.backend.priority.dto.PriorityDtos.DocumentRequestsResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.DocumentSubmissionDeleteResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.DocumentSubmissionItem;
import com.edussafy.backend.priority.dto.PriorityDtos.DocumentSubmissionResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ElearningLessonItem;
import com.edussafy.backend.priority.dto.PriorityDtos.ElearningProgressDetail;
import com.edussafy.backend.priority.dto.PriorityDtos.ElearningProgressDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ElearningProgressItem;
import com.edussafy.backend.priority.dto.PriorityDtos.ElearningProgressResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ElearningResumeItem;
import com.edussafy.backend.priority.dto.PriorityDtos.ElearningResumeResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.EbookAccessLogItem;
import com.edussafy.backend.priority.dto.PriorityDtos.EbookAccessLogResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.EbookDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.EbookItem;
import com.edussafy.backend.priority.dto.PriorityDtos.EbooksResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.LevelSummary;
import com.edussafy.backend.priority.dto.PriorityDtos.LevelDetail;
import com.edussafy.backend.priority.dto.PriorityDtos.LevelDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.LevelHistoryItem;
import com.edussafy.backend.priority.dto.PriorityDtos.ScholarshipPointItem;
import com.edussafy.backend.priority.dto.PriorityDtos.CurrentLiveSessionResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.LiveSessionItem;
import com.edussafy.backend.priority.dto.PriorityDtos.LiveSessionJoinLogItem;
import com.edussafy.backend.priority.dto.PriorityDtos.LiveSessionJoinResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.LiveSessionsResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.NotificationDeleteResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.NotificationItem;
import com.edussafy.backend.priority.dto.PriorityDtos.NotificationReadResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.NotificationsReadAllResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialItem;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialReactionResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialResourceAttachmentCreateResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialResourceAttachmentDownload;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialResourceAttachmentItem;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialResourcesResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialResourceItem;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialViewResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialsResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.NotificationsResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.NotificationsSummary;
import com.edussafy.backend.priority.dto.PriorityDtos.PageMeta;
import com.edussafy.backend.priority.dto.PriorityDtos.PasswordCheckResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.PledgeAgreementItem;
import com.edussafy.backend.priority.dto.PriorityDtos.PledgeAgreementResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.PledgeDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.PledgeItem;
import com.edussafy.backend.priority.dto.PriorityDtos.PledgesResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ProfileDetails;
import com.edussafy.backend.priority.dto.PriorityDtos.ProfileEditAuthorizationResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ProfilePasswordChangeRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.ProfileResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestItem;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestListFilters;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestListSummary;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestSubmissionAttachmentCreateResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestSubmissionAttachmentDownload;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestSubmissionAttachmentItem;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestSubmissionItem;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestsResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ReplayDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ReplayItem;
import com.edussafy.backend.priority.dto.PriorityDtos.ReplayResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ReplayWatchLogItem;
import com.edussafy.backend.priority.dto.PriorityDtos.ReplayWatchLogResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.RequiredStudiesResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.RequiredStudyCompleteResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.RequiredStudyDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.RequiredStudyItem;
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
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyDeleteItem;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyDeleteResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyDetail;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyOptionItem;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyQuestionItem;
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
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest({
        AuthController.class,
        DashboardController.class,
        EducationStatusController.class,
        EbookController.class,
        RequiredStudyController.class,
        LiveSessionController.class,
        ReplayController.class,
        CurriculumController.class,
        AttendanceController.class,
        NotificationController.class,
        LearningController.class,
        ElearningController.class,
        BookmarkController.class,
        DocumentController.class,
        PledgeController.class,
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
                        .header("X-Request-Id", "req-login-invalid")
                        .content("{\"email\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("X-Request-Id", "req-login-invalid"))
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.error.status").value(400))
                .andExpect(jsonPath("$.error.path").value("/api/auth/login"))
                .andExpect(jsonPath("$.error.requestId").value("req-login-invalid"))
                .andExpect(jsonPath("$.error.timestamp").exists());

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
    void accessPolicyReturnsStaffOnlyApiMatrix() throws Exception {
        given(priorityApiService.accessPolicy()).willReturn(new AccessPolicyResponse(List.of(
                new AccessPolicyItem(
                        "support-answer",
                        "POST",
                        "/api/support/tickets/{ticketId}/answers",
                        List.of("coach", "admin"),
                        "1:1 문의",
                        "문의 답변 등록은 지원 담당 staff 역할 이상으로 제한한다."
                )
        )));

        mockMvc.perform(get("/api/auth/access-policy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value("support-answer"))
                .andExpect(jsonPath("$.items[0].allowedRoles[0]").value("coach"))
                .andExpect(jsonPath("$.items[0].allowedRoles[1]").value("admin"));
    }

    @Test
    void authSessionReturnsExpiryMetadata() throws Exception {
        given(priorityApiService.authSession()).willReturn(new AuthSessionResponse(
                true,
                OffsetDateTime.parse("2026-04-25T15:00:00Z"),
                7200,
                3600
        ));

        mockMvc.perform(get("/api/auth/session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.expiresAt").value("2026-04-25T15:00:00Z"))
                .andExpect(jsonPath("$.maxInactiveSeconds").value(7200))
                .andExpect(jsonPath("$.secondsRemaining").value(3600));
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
    void profileEditAuthorizationReturnsVerificationWindow() throws Exception {
        given(priorityApiService.profileEditAuthorization()).willReturn(new ProfileEditAuthorizationResponse(
                true,
                OffsetDateTime.parse("2026-04-25T16:00:00Z"),
                600
        ));

        mockMvc.perform(get("/api/profile/edit-authorization"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(true))
                .andExpect(jsonPath("$.verifiedUntil").value("2026-04-25T16:00:00Z"))
                .andExpect(jsonPath("$.ttlSeconds").value(600));
    }

    @Test
    void dashboardSummaryReturnsPriorityShape() throws Exception {
        given(priorityApiService.dashboardSummary()).willReturn(new DashboardSummary(
                new UserSummary("Demo Learner", "Seoul", "12", "Java"),
                new LevelSummary(1, 0, 1000, 0, null),
                new AttendanceSummary(0, 0, 0, true),
                new NotificationsSummary(0, List.of()),
                new TodaySummary(null, null, null),
                new DashboardHomeWidgets(
                        new DashboardAttendanceCheck("2026-04-26", true, true, "입·퇴실 가능", "오늘 출석을 확인하세요.", "/mycampus/attendance"),
                        List.of(new DashboardCurriculumSession(11, 3, LocalDate.parse("2026-04-26"), "09:00 ~ 18:00", "React API 연동", "Coach", "Seoul", "current", "/learning/curriculum/11")),
                        List.of(new DashboardQuestCard(7, "Dashboard Quest", "assignment", "progress", null, null, "/quest/7")),
                        List.of(new DashboardLearningCard(5, "REST API", "Backend", "API 자료", 0, 10, 2, 1, "/learning/materials/5")),
                        List.of(new DashboardLearningCard(6, "Java e-learning", "Java", "객체지향", 40, 0, 0, 0, "/mycampus/elearning/6")),
                        List.of(new DashboardBoardPost(9, "free", "자유게시판 글", "Demo", null, false, "/community/free/9")),
                        List.of(new DashboardBoardPost(10, "notice", "공지사항", "운영자", null, true, "/help/notice/10")),
                        List.of(new DashboardEbookCard(3, "Java e-book", "Java", "전자책", "/mycampus/ebooks/3"))
                )
        ));

        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.trackName").value("Java"))
                .andExpect(jsonPath("$.level.nextLevelExp").value(1000))
                .andExpect(jsonPath("$.attendance.appealAvailable").value(true))
                .andExpect(jsonPath("$.notifications.latest").isArray())
                .andExpect(jsonPath("$.home.attendanceCheck.detailPath").value("/mycampus/attendance"))
                .andExpect(jsonPath("$.home.curriculumSessions[0].title").value("React API 연동"))
                .andExpect(jsonPath("$.home.quests[0].detailPath").value("/quest/7"))
                .andExpect(jsonPath("$.home.notices[0].pinned").value(true));
    }

    @Test
    void levelDetailReturnsMyCampusLevelShape() throws Exception {
        given(priorityApiService.levelDetail()).willReturn(new LevelDetailResponse(new LevelDetail(
                new LevelSummary(5, 4200, 5000, 85, 12),
                "Silver Lv.5",
                84,
                800,
                List.of(new LevelHistoryItem(LocalDate.parse("2026-04-24"), 12, 4200, 85)),
                List.of(new ScholarshipPointItem("누적 장학 포인트", 85, "현재 사용자 기준 누적 장학 포인트입니다."))
        )));

        mockMvc.perform(get("/api/mycampus/level"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.detail.levelName").value("Silver Lv.5"))
                .andExpect(jsonPath("$.detail.current.level").value(5))
                .andExpect(jsonPath("$.detail.expPercent").value(84))
                .andExpect(jsonPath("$.detail.expRemaining").value(800))
                .andExpect(jsonPath("$.detail.history[0].rankNo").value(12))
                .andExpect(jsonPath("$.detail.pointBreakdown[0].category").value("누적 장학 포인트"));
    }

    @Test
    void ebookEndpointsReturnListDetailAndAccessLog() throws Exception {
        EbookItem ebook = new EbookItem(
                5L,
                "SSAFY Java e-book",
                "Java 트랙 학습서",
                null,
                "Java",
                "https://edu.ssafy.local/ebooks/java",
                OffsetDateTime.parse("2026-04-01T09:00:00+09:00"),
                null,
                0
        );
        EbookItem accessed = new EbookItem(
                5L,
                "SSAFY Java e-book",
                "Java 트랙 학습서",
                null,
                "Java",
                "https://edu.ssafy.local/ebooks/java",
                OffsetDateTime.parse("2026-04-01T09:00:00+09:00"),
                OffsetDateTime.parse("2026-04-26T10:00:00+09:00"),
                1
        );
        given(priorityApiService.ebooks(1, 20)).willReturn(new EbooksResponse(List.of(ebook), new PageMeta(1, 20, 1, 1)));
        given(priorityApiService.ebook(5L)).willReturn(new EbookDetailResponse(ebook));
        given(priorityApiService.logEbookAccess(5L)).willReturn(new EbookAccessLogResponse(
                accessed,
                new EbookAccessLogItem(91L, 5L, OffsetDateTime.parse("2026-04-26T10:00:00+09:00"))
        ));

        mockMvc.perform(get("/api/ebooks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].title").value("SSAFY Java e-book"))
                .andExpect(jsonPath("$.page.totalItems").value(1));
        mockMvc.perform(get("/api/ebooks/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.category").value("Java"));
        mockMvc.perform(post("/api/ebooks/5/access-log"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.item.accessCount").value(1))
                .andExpect(jsonPath("$.accessLog.ebookId").value(5));
    }

    @Test
    void requiredStudyEndpointsReturnListDetailAndComplete() throws Exception {
        RequiredStudyItem study = new RequiredStudyItem(
                7L,
                "Java 보안 필수학습",
                "보안 체크리스트",
                "Security",
                "Java",
                OffsetDateTime.parse("2026-05-01T18:00:00+09:00"),
                "url",
                "https://edu.ssafy.local/required-studies/java-security",
                "in_progress",
                40,
                null
        );
        RequiredStudyItem completed = new RequiredStudyItem(
                7L,
                "Java 보안 필수학습",
                "보안 체크리스트",
                "Security",
                "Java",
                OffsetDateTime.parse("2026-05-01T18:00:00+09:00"),
                "url",
                "https://edu.ssafy.local/required-studies/java-security",
                "completed",
                100,
                OffsetDateTime.parse("2026-04-26T10:00:00+09:00")
        );
        given(priorityApiService.requiredStudies(1, 20)).willReturn(new RequiredStudiesResponse(
                List.of(study),
                new PageMeta(1, 20, 1, 1)
        ));
        given(priorityApiService.requiredStudy(7L)).willReturn(new RequiredStudyDetailResponse(study));
        given(priorityApiService.completeRequiredStudy(7L)).willReturn(new RequiredStudyCompleteResponse(completed));

        mockMvc.perform(get("/api/required-studies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].status").value("in_progress"))
                .andExpect(jsonPath("$.page.totalItems").value(1));
        mockMvc.perform(get("/api/required-studies/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.requiredForTrack").value("Java"));
        mockMvc.perform(post("/api/required-studies/7/complete"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.item.status").value("completed"))
                .andExpect(jsonPath("$.item.progressPercent").value(100));
    }

    @Test
    void liveSessionEndpointsReturnTodayCurrentAndJoinLog() throws Exception {
        LiveSessionItem live = new LiveSessionItem(
                11L,
                "Java 라이브 알고리즘 코칭",
                "Java",
                "12th",
                "Seoul Java 1",
                OffsetDateTime.parse("2026-04-26T09:00:00+09:00"),
                OffsetDateTime.parse("2026-04-26T11:00:00+09:00"),
                "https://edu.ssafy.local/live/java-algorithm",
                "live",
                OffsetDateTime.parse("2026-04-25T09:00:00+09:00"),
                null,
                0
        );
        LiveSessionItem joined = new LiveSessionItem(
                11L,
                "Java 라이브 알고리즘 코칭",
                "Java",
                "12th",
                "Seoul Java 1",
                OffsetDateTime.parse("2026-04-26T09:00:00+09:00"),
                OffsetDateTime.parse("2026-04-26T11:00:00+09:00"),
                "https://edu.ssafy.local/live/java-algorithm",
                "live",
                OffsetDateTime.parse("2026-04-25T09:00:00+09:00"),
                OffsetDateTime.parse("2026-04-26T10:00:00+09:00"),
                1
        );
        given(priorityApiService.todayLiveSessions()).willReturn(new LiveSessionsResponse(List.of(live)));
        given(priorityApiService.currentLiveSession()).willReturn(new CurrentLiveSessionResponse(live));
        given(priorityApiService.joinLiveSession(11L)).willReturn(new LiveSessionJoinResponse(
                joined,
                new LiveSessionJoinLogItem(88L, 11L, OffsetDateTime.parse("2026-04-26T10:00:00+09:00"))
        ));

        mockMvc.perform(get("/api/live-sessions/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].status").value("live"));
        mockMvc.perform(get("/api/live-sessions/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.id").value(11));
        mockMvc.perform(post("/api/live-sessions/11/join"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.item.joinCount").value(1))
                .andExpect(jsonPath("$.joinLog.sessionId").value(11));
    }

    @Test
    void replaySplitEndpointsReturnMyAllDetailAndWatchLog() throws Exception {
        ReplayItem replay = new ReplayItem(
                21L,
                3L,
                "Spring Boot REST API Replay",
                1,
                OffsetDateTime.parse("2026-04-24T18:30:00+09:00"),
                "lecture",
                "Demo Instructor",
                "Seoul Java 1",
                LocalDate.parse("2026-04-24"),
                "class_group",
                null,
                0
        );
        ReplayItem watched = new ReplayItem(
                21L,
                3L,
                "Spring Boot REST API Replay",
                1,
                OffsetDateTime.parse("2026-04-24T18:30:00+09:00"),
                "lecture",
                "Demo Instructor",
                "Seoul Java 1",
                LocalDate.parse("2026-04-24"),
                "class_group",
                OffsetDateTime.parse("2026-04-26T10:00:00+09:00"),
                1
        );
        given(priorityApiService.myReplays("spring")).willReturn(new ReplayResponse(List.of(replay)));
        given(priorityApiService.allReplays(null)).willReturn(new ReplayResponse(List.of(replay)));
        given(priorityApiService.replay(21L)).willReturn(new ReplayDetailResponse(replay));
        given(priorityApiService.watchReplay(21L)).willReturn(new ReplayWatchLogResponse(
                watched,
                new ReplayWatchLogItem(55L, 21L, OffsetDateTime.parse("2026-04-26T10:00:00+09:00"))
        ));

        mockMvc.perform(get("/api/replays/my?keyword=spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].scope").value("class_group"));
        mockMvc.perform(get("/api/replays/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].title").value("Spring Boot REST API Replay"));
        mockMvc.perform(get("/api/replays/21"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.instructor").value("Demo Instructor"));
        mockMvc.perform(post("/api/replays/21/watch-log"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.item.watchCount").value(1))
                .andExpect(jsonPath("$.watchLog.replayId").value(21));
    }

    @Test
    void curriculumWeekEndpointsReturnFilteredWeeksAndDetail() throws Exception {
        CurriculumWeekItem week = new CurriculumWeekItem(
                31L,
                "2026 Priority 1 Term",
                4,
                "Java",
                LocalDate.parse("2026-04-20"),
                LocalDate.parse("2026-04-24"),
                "done",
                2,
                List.of(
                        new CurriculumSessionItem(
                                31L,
                                LocalDate.parse("2026-04-20"),
                                "09:00 ~ 12:00",
                                "Java Collections Review",
                                "Demo Instructor",
                                "Seoul 1",
                                "lecture"
                        ),
                        new CurriculumSessionItem(
                                32L,
                                LocalDate.parse("2026-04-24"),
                                "09:00 ~ 18:00",
                                "Spring Boot REST API",
                                "Demo Instructor",
                                "Seoul 1",
                                "lecture"
                        )
                )
        );
        given(priorityApiService.curriculumWeeks("2026 Priority 1 Term", "Java", "done"))
                .willReturn(new CurriculumWeeksResponse(List.of(week)));
        given(priorityApiService.curriculumWeek(31L)).willReturn(new CurriculumWeekDetailResponse(week));

        mockMvc.perform(get("/api/curriculum/weeks")
                        .param("semester", "2026 Priority 1 Term")
                        .param("track", "Java")
                        .param("status", "done"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].semester").value("2026 Priority 1 Term"))
                .andExpect(jsonPath("$.items[0].track").value("Java"))
                .andExpect(jsonPath("$.items[0].sessions[1].title").value("Spring Boot REST API"));

        mockMvc.perform(get("/api/curriculum/weeks/31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.weekNumber").value(4))
                .andExpect(jsonPath("$.item.sessionCount").value(2));
    }

    @Test
    void educationStatusReturnsAggregatedMyCampusShape() throws Exception {
        given(priorityApiService.educationStatus()).willReturn(new EducationStatusResponse(
                new EducationAttendanceSummary("2026-04", 18, 1, 0, 1),
                new EducationLearningSummary(3, 5, 8, 320),
                new EducationQuestSummary(2, 5, 0),
                new EducationPointSummary(0, 1153, "Bronze Lv.3")
        ));

        mockMvc.perform(get("/api/mycampus/education-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attendance.month").value("2026-04"))
                .andExpect(jsonPath("$.attendance.appealPendingCount").value(1))
                .andExpect(jsonPath("$.learning.inProgressElearningCount").value(3))
                .andExpect(jsonPath("$.quests.submittedCount").value(5))
                .andExpect(jsonPath("$.points.levelName").value("Bronze Lv.3"));
    }

    @Test
    void priorityListsReturnEmptyPageShapes() throws Exception {
        PageMeta page = new PageMeta(1, 20, 0, 0);
        given(priorityApiService.attendanceRecords(null, null, null))
                .willReturn(new AttendanceRecordsResponse(new AttendanceSummary(0, 0, 0, true), new AttendanceRange(null, null, null), List.of(), List.of()));
        given(priorityApiService.notifications(1, 20)).willReturn(new NotificationsResponse(List.of(), page));
        given(priorityApiService.materials(eq("spring"), eq("file"), eq(1), eq(20)))
                .willReturn(new MaterialsResponse(List.of(), page));
        given(priorityApiService.quests(eq(1), eq(20), isNull(), isNull()))
                .willReturn(new QuestsResponse(List.of(), page, new QuestListSummary(0, 0, 0, 0, 0), new QuestListFilters(null, null)));
        given(priorityApiService.surveys(1, 20)).willReturn(new SurveysResponse(List.of(), page));
        given(priorityApiService.supportTickets(1, 20)).willReturn(new SupportTicketsResponse(List.of(), page));

        mockMvc.perform(get("/api/attendance/records"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.days").isArray())
                .andExpect(jsonPath("$.summary.present").value(0));
        given(priorityApiService.attendanceRecords(LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30), "late"))
                .willReturn(new AttendanceRecordsResponse(new AttendanceSummary(0, 1, 0, true), new AttendanceRange(LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30), "late"), List.of(new AttendanceDaySummary(LocalDate.of(2026, 4, 23), "late", null, null, true, null)), List.of()));
        mockMvc.perform(get("/api/attendance/records?dateFrom=2026-04-01&dateTo=2026-04-30&status=late"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.late").value(1))
                .andExpect(jsonPath("$.range.status").value("late"))
                .andExpect(jsonPath("$.days[0].date").value("2026-04-23"));
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalItems").value(0));
        mockMvc.perform(get("/api/learning/materials?keyword=spring&type=file"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
        mockMvc.perform(get("/api/quests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalPages").value(0))
                .andExpect(jsonPath("$.summary.totalCount").value(0));
        given(priorityApiService.quests(eq(1), eq(20), eq("graded"), eq("algo")))
                .willReturn(new QuestsResponse(
                        List.of(),
                        page,
                        new QuestListSummary(3, 1, 1, 1, 0),
                        new QuestListFilters("graded", "algo")
                ));
        mockMvc.perform(get("/api/quests?status=graded&keyword=algo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.gradedCount").value(1))
                .andExpect(jsonPath("$.filters.status").value("graded"))
                .andExpect(jsonPath("$.filters.keyword").value("algo"));
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
    void attendanceAppealResolveRequiresStaffRole() throws Exception {
        mockMvc.perform(patch("/api/attendance/appeals/101/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"approved\",\"comment\":\"OK\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));

        verifyNoInteractions(priorityApiService);
    }

    @Test
    void attendanceAppealPendingAndResolveExposeStaffWorkflow() throws Exception {
        AttendanceAppealItem pending = new AttendanceAppealItem(
                101L,
                7L,
                "status_change",
                "QR failed",
                "present",
                "requested",
                OffsetDateTime.parse("2026-04-25T09:00:00+09:00"),
                LocalDate.of(2026, 4, 23),
                null,
                null,
                null,
                null,
                false
        );
        AttendanceAppealItem approved = new AttendanceAppealItem(
                101L,
                7L,
                "status_change",
                "QR failed",
                "present",
                "approved",
                OffsetDateTime.parse("2026-04-25T09:00:00+09:00"),
                LocalDate.of(2026, 4, 23),
                "present",
                OffsetDateTime.parse("2026-04-25T10:00:00+09:00"),
                "출석으로 정정했습니다.",
                "Demo Manager",
                false
        );
        given(priorityApiService.pendingAttendanceAppeals()).willReturn(new AttendanceAppealsResponse(List.of(pending)));
        given(priorityApiService.resolveAttendanceAppeal(eq(101L), any(AttendanceAppealResolveRequest.class)))
                .willReturn(new AttendanceAppealResponse(approved));

        mockMvc.perform(get("/api/attendance/appeals/pending")
                        .session(sessionFor(2L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(101))
                .andExpect(jsonPath("$.items[0].recordDate").value("2026-04-23"))
                .andExpect(jsonPath("$.items[0].status").value("requested"));
        mockMvc.perform(patch("/api/attendance/appeals/101/resolve")
                        .session(sessionFor(2L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"approved\",\"comment\":\"출석으로 정정했습니다.\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.status").value("approved"))
                .andExpect(jsonPath("$.item.resolvedStatus").value("present"))
                .andExpect(jsonPath("$.item.resolutionComment").value("출석으로 정정했습니다."))
                .andExpect(jsonPath("$.item.resolvedByName").value("Demo Manager"));
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
    void bookmarkEndpointsReturnAndMutateUserScopedBookmarks() throws Exception {
        PageMeta page = new PageMeta(1, 20, 1, 1);
        BookmarkItem item = new BookmarkItem(
                90L,
                "material",
                5L,
                "REST API Workbook",
                "학습자료",
                null,
                "/learning/materials/5",
                OffsetDateTime.parse("2026-04-25T10:00:00+09:00")
        );
        given(priorityApiService.bookmarks(eq("material"), eq(1), eq(20))).willReturn(new BookmarksResponse(List.of(item), page));
        given(priorityApiService.createBookmark(any())).willReturn(new BookmarkResponse(item));
        given(priorityApiService.deleteBookmark(90L)).willReturn(new BookmarkDeleteResponse(90L, true));

        mockMvc.perform(get("/api/me/bookmarks?targetType=material"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].title").value("REST API Workbook"))
                .andExpect(jsonPath("$.page.totalItems").value(1));
        mockMvc.perform(post("/api/me/bookmarks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetType\":\"material\",\"targetId\":5}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.item.id").value(90));
        mockMvc.perform(delete("/api/me/bookmarks/90"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deleted").value(true));
    }

    @Test
    void documentEndpointsReturnSubmitCancelAndDownloadFlow() throws Exception {
        PageMeta page = new PageMeta(1, 20, 1, 1);
        DocumentAttachmentItem attachment = new DocumentAttachmentItem(
                77L,
                88L,
                9L,
                "identity.pdf",
                "documents/9/submissions/88/identity.pdf",
                "application/pdf",
                5L,
                OffsetDateTime.parse("2026-04-25T14:30:00+09:00")
        );
        DocumentRequestItem listItem = new DocumentRequestItem(
                9L,
                "신분증 사본 제출",
                "본인 확인 서류",
                "identity",
                true,
                ".pdf,.jpg,.png",
                2_097_152L,
                OffsetDateTime.parse("2026-04-01T09:00:00+09:00"),
                OffsetDateTime.parse("2026-05-10T18:00:00+09:00"),
                "submitted",
                OffsetDateTime.parse("2026-04-25T14:30:00+09:00"),
                null,
                List.of(attachment)
        );
        DocumentRequestDetail detail = new DocumentRequestDetail(
                9L,
                "신분증 사본 제출",
                "본인 확인 서류",
                "identity",
                true,
                ".pdf,.jpg,.png",
                2_097_152L,
                OffsetDateTime.parse("2026-04-01T09:00:00+09:00"),
                OffsetDateTime.parse("2026-05-10T18:00:00+09:00"),
                "submitted",
                OffsetDateTime.parse("2026-04-25T14:30:00+09:00"),
                null,
                null,
                List.of(attachment)
        );
        given(priorityApiService.documentRequests(eq(1), eq(20))).willReturn(new DocumentRequestsResponse(List.of(listItem), page));
        given(priorityApiService.documentRequest(9L)).willReturn(new DocumentRequestDetailResponse(detail));
        given(priorityApiService.submitDocument(eq(9L), any())).willReturn(new DocumentSubmissionResponse(
                detail,
                new DocumentSubmissionItem(88L, 9L, "submitted", OffsetDateTime.parse("2026-04-25T14:30:00+09:00"), List.of(attachment))
        ));
        given(priorityApiService.cancelDocumentSubmission(9L, 88L)).willReturn(new DocumentSubmissionDeleteResponse(9L, 88L, true));
        given(priorityApiService.downloadDocumentAttachment(88L, 77L)).willReturn(new DocumentAttachmentDownload(attachment, "hello".getBytes(StandardCharsets.UTF_8)));

        mockMvc.perform(get("/api/documents/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].title").value("신분증 사본 제출"))
                .andExpect(jsonPath("$.items[0].attachments[0].filename").value("identity.pdf"));
        mockMvc.perform(get("/api/documents/requests/9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.status").value("submitted"));
        mockMvc.perform(post("/api/documents/requests/9/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"filename\":\"identity.pdf\",\"mimeType\":\"application/pdf\",\"contentBase64\":\"aGVsbG8=\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.submission.id").value(88));
        mockMvc.perform(delete("/api/documents/requests/9/submissions/88"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canceled").value(true));
        mockMvc.perform(get("/api/documents/submissions/88/attachments/77"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, org.hamcrest.Matchers.containsString("identity.pdf")))
                .andExpect(content().bytes("hello".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void pledgeEndpointsReturnAndPersistAgreementState() throws Exception {
        PageMeta page = new PageMeta(1, 20, 1, 1);
        PledgeItem item = new PledgeItem(
                3L,
                "교육생 기본 서약서",
                "학습 규칙을 준수합니다.",
                "2026.1",
                true,
                OffsetDateTime.parse("2026-04-01T09:00:00+09:00"),
                OffsetDateTime.parse("2026-05-10T18:00:00+09:00"),
                false,
                null,
                null
        );
        PledgeItem agreed = new PledgeItem(
                3L,
                "교육생 기본 서약서",
                "학습 규칙을 준수합니다.",
                "2026.1",
                true,
                OffsetDateTime.parse("2026-04-01T09:00:00+09:00"),
                OffsetDateTime.parse("2026-05-10T18:00:00+09:00"),
                true,
                OffsetDateTime.parse("2026-04-25T15:00:00+09:00"),
                "2026.1"
        );
        PledgeAgreementItem agreement = new PledgeAgreementItem(44L, 3L, true, OffsetDateTime.parse("2026-04-25T15:00:00+09:00"), "2026.1");
        given(priorityApiService.pledges(eq(1), eq(20))).willReturn(new PledgesResponse(List.of(item), page));
        given(priorityApiService.pledge(3L)).willReturn(new PledgeDetailResponse(item));
        given(priorityApiService.agreePledge(eq(3L), any())).willReturn(new PledgeAgreementResponse(agreed, agreement));

        mockMvc.perform(get("/api/pledges"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].title").value("교육생 기본 서약서"));
        mockMvc.perform(get("/api/pledges/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.version").value("2026.1"));
        mockMvc.perform(post("/api/pledges/3/agreements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"agreed\":true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.item.agreed").value(true))
                .andExpect(jsonPath("$.agreement.id").value(44));
    }

    @Test
    void elearningProgressEndpointsReturnUserScopedShapes() throws Exception {
        PageMeta page = new PageMeta(1, 10, 1, 1);
        ElearningProgressItem item = new ElearningProgressItem(
                10L,
                "Java 객체지향 이러닝",
                "Java",
                null,
                "SSAFY e-Learning",
                "객체지향 복습",
                50,
                3,
                6,
                14400L,
                "인터페이스 설계",
                OffsetDateTime.parse("2026-04-25T10:15:00+09:00"),
                "in_progress",
                "/mycampus/elearning/10"
        );
        ElearningProgressDetail detail = new ElearningProgressDetail(
                10L,
                "Java 객체지향 이러닝",
                "Java",
                null,
                "SSAFY e-Learning",
                "객체지향 복습",
                50,
                3,
                6,
                14400L,
                "인터페이스 설계",
                OffsetDateTime.parse("2026-04-25T10:15:00+09:00"),
                "in_progress",
                "/mycampus/elearning/10",
                List.of(new ElearningLessonItem(100L, 1, "클래스와 객체", 2400L, true, OffsetDateTime.parse("2026-04-25T10:00:00+09:00")))
        );
        given(priorityApiService.elearningInProgress(eq("in_progress"), eq("java"), eq(1), eq(10)))
                .willReturn(new ElearningProgressResponse(List.of(item), page));
        given(priorityApiService.elearningProgressDetail(10L))
                .willReturn(new ElearningProgressDetailResponse(detail));
        given(priorityApiService.resumeElearning(10L))
                .willReturn(new ElearningResumeResponse(new ElearningResumeItem(10L, "/mycampus/elearning/10", OffsetDateTime.parse("2026-04-25T10:20:00+09:00"), "in_progress")));

        mockMvc.perform(get("/api/elearning/in-progress?status=in_progress&keyword=java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].courseId").value(10))
                .andExpect(jsonPath("$.items[0].progressPercent").value(50))
                .andExpect(jsonPath("$.page.totalItems").value(1));
        mockMvc.perform(get("/api/elearning/in-progress/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.lessons[0].completed").value(true));
        mockMvc.perform(post("/api/elearning/in-progress/10/resume"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.resumeUrl").value("/mycampus/elearning/10"));
    }

    @Test
    void p3LearningDetailsReturnShapes() throws Exception {
        given(priorityApiService.material(5L)).willReturn(new MaterialDetailResponse(new MaterialItem(
                5L, "Material", "document", null, null, 0, null, List.of(), 0, 0, false, false
        )));
        given(priorityApiService.recordMaterialView(5L)).willReturn(new MaterialViewResponse(new MaterialItem(
                5L, "Material", "document", null, null, 1, null, List.of(), 1, 0, true, false
        )));
        given(priorityApiService.createMaterialReaction(5L, "like")).willReturn(new MaterialReactionResponse(new MaterialItem(
                5L, "Material", "document", null, null, 1, null, List.of(), 1, 0, true, false
        )));
        given(priorityApiService.deleteMaterialReaction(5L, "like")).willReturn(new MaterialReactionResponse(new MaterialItem(
                5L, "Material", "document", null, null, 1, null, List.of(), 0, 0, false, false
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
        mockMvc.perform(post("/api/learning/materials/5/reactions/like"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.liked").value(true))
                .andExpect(jsonPath("$.item.likeCount").value(1));
        mockMvc.perform(delete("/api/learning/materials/5/reactions/like"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.liked").value(false));
        mockMvc.perform(get("/api/learning/materials/5/resources"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void learningMaterialResourceAttachmentCreateRequiresStaffRole() throws Exception {
        mockMvc.perform(post("/api/learning/materials/5/resources/30/attachments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"filename\":\"rest-docs.pdf\",\"mimeType\":\"application/pdf\",\"contentBase64\":\"aGVsbG8=\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));

        verifyNoInteractions(priorityApiService);
    }

    @Test
    void learningMaterialResourceAttachmentCreateReturnsPersistedMetadata() throws Exception {
        MaterialResourceItem resource = new MaterialResourceItem(
                30L,
                5L,
                "file",
                "rest-docs.pdf",
                "download",
                "/materials/rest-docs.pdf",
                1
        );
        MaterialResourceAttachmentItem attachment = new MaterialResourceAttachmentItem(
                77L,
                30L,
                5L,
                "rest-docs.pdf",
                "learning/materials/5/resources/30/abc-rest-docs.pdf",
                "/learning/materials/5/resources/30/attachments/abc",
                "application/pdf",
                5L,
                "abc",
                null
        );
        given(priorityApiService.createMaterialResourceAttachment(eq(5L), eq(30L), any()))
                .willReturn(new MaterialResourceAttachmentCreateResponse(attachment, resource));

        mockMvc.perform(post("/api/learning/materials/5/resources/30/attachments")
                        .session(sessionFor(2L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"filename\":\"rest-docs.pdf\",\"mimeType\":\"application/pdf\",\"contentBase64\":\"aGVsbG8=\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.item.id").value(77))
                .andExpect(jsonPath("$.item.filename").value("rest-docs.pdf"))
                .andExpect(jsonPath("$.resource.id").value(30));
    }

    @Test
    void learningMaterialResourceAttachmentDownloadReturnsStoredBytes() throws Exception {
        MaterialResourceAttachmentItem attachment = new MaterialResourceAttachmentItem(
                77L,
                30L,
                5L,
                "rest-docs.pdf",
                "learning/materials/5/resources/30/abc-rest-docs.pdf",
                "/learning/materials/5/resources/30/attachments/abc",
                "application/pdf",
                5L,
                "abc",
                null
        );
        given(priorityApiService.downloadMaterialResourceAttachment(5L, 30L, 77L))
                .willReturn(new MaterialResourceAttachmentDownload(attachment, "hello".getBytes(StandardCharsets.UTF_8)));

        mockMvc.perform(get("/api/learning/materials/5/resources/30/attachments/77"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string(
                        "Content-Disposition",
                        "attachment; filename=\"=?UTF-8?Q?rest-docs.pdf?=\"; filename*=UTF-8''rest-docs.pdf"
                ))
                .andExpect(content().bytes("hello".getBytes(StandardCharsets.UTF_8)));
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
    void questSubmissionAttachmentCreateReturnsPersistedMetadata() throws Exception {
        QuestSubmissionItem submission = new QuestSubmissionItem(77L, 7L, "submitted", null, "pending", null, null, false);
        QuestSubmissionAttachmentItem attachment = new QuestSubmissionAttachmentItem(
                88L,
                7L,
                77L,
                "solution.zip",
                "quests/7/submissions/77/abc-solution.zip",
                "/quests/7/submissions/77/attachments/abc",
                "application/zip",
                5L,
                "abc",
                null
        );
        given(priorityApiService.createQuestSubmissionAttachment(eq(7L), eq(77L), any()))
                .willReturn(new QuestSubmissionAttachmentCreateResponse(attachment, submission));

        mockMvc.perform(post("/api/quests/7/submissions/77/attachments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"filename\":\"solution.zip\",\"mimeType\":\"application/zip\",\"contentBase64\":\"aGVsbG8=\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.item.id").value(88))
                .andExpect(jsonPath("$.item.filename").value("solution.zip"))
                .andExpect(jsonPath("$.submission.id").value(77));
    }

    @Test
    void questSubmissionAttachmentDownloadReturnsStoredBytes() throws Exception {
        QuestSubmissionAttachmentItem attachment = new QuestSubmissionAttachmentItem(
                88L,
                7L,
                77L,
                "solution.zip",
                "quests/7/submissions/77/abc-solution.zip",
                "/quests/7/submissions/77/attachments/abc",
                "application/zip",
                5L,
                "abc",
                null
        );
        given(priorityApiService.downloadQuestSubmissionAttachment(7L, 77L, 88L))
                .willReturn(new QuestSubmissionAttachmentDownload(attachment, "hello".getBytes(StandardCharsets.UTF_8)));

        mockMvc.perform(get("/api/quests/7/submissions/77/attachments/88"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/zip"))
                .andExpect(header().string(
                        "Content-Disposition",
                        "attachment; filename=\"=?UTF-8?Q?solution.zip?=\"; filename*=UTF-8''solution.zip"
                ))
                .andExpect(content().bytes("hello".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void surveyCreateReturnsPersistedDraftForStaff() throws Exception {
        given(priorityApiService.createSurvey(any())).willReturn(new SurveyDetailResponse(new SurveyDetail(
                9L,
                "Weekly pulse",
                "satisfaction",
                true,
                null,
                null,
                "in_progress",
                false,
                1,
                List.of(new SurveyQuestionItem(
                        91L,
                        "single_choice",
                        "이번 주 과정은 어땠나요?",
                        1,
                        List.of(new SurveyOptionItem(911L, "좋음", 1), new SurveyOptionItem(912L, "보통", 2))
                ))
        )));

        mockMvc.perform(post("/api/surveys")
                        .session(sessionFor(2L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Weekly pulse",
                                  "category": "satisfaction",
                                  "required": true,
                                  "status": "in_progress",
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
                .andExpect(jsonPath("$.item.questions[0].options[0].text").value("좋음"));
    }


    @Test
    void surveyUpdateAndDeleteReturnPersistedShapesForStaff() throws Exception {
        given(priorityApiService.updateSurvey(eq(9L), any())).willReturn(new SurveyDetailResponse(new SurveyDetail(
                9L,
                "Updated pulse",
                "course",
                false,
                null,
                null,
                "scheduled",
                false,
                1,
                List.of(new SurveyQuestionItem(92L, "long_text", "개선 의견을 적어 주세요.", 1, List.of()))
        )));
        given(priorityApiService.deleteSurvey(9L)).willReturn(new SurveyDeleteResponse(new SurveyDeleteItem(9L, true, false)));

        mockMvc.perform(put("/api/surveys/9")
                        .session(sessionFor(2L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Updated pulse",
                                  "category": "course",
                                  "required": false,
                                  "status": "scheduled",
                                  "questions": [{"type": "long_text", "text": "개선 의견을 적어 주세요."}]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.id").value(9))
                .andExpect(jsonPath("$.item.title").value("Updated pulse"))
                .andExpect(jsonPath("$.item.questions[0].text").value("개선 의견을 적어 주세요."));
        mockMvc.perform(delete("/api/surveys/9").session(sessionFor(2L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.id").value(9))
                .andExpect(jsonPath("$.item.deleted").value(true))
                .andExpect(jsonPath("$.item.demo").value(false));
    }

    @Test
    void surveyCreateRequiresStaffRole() throws Exception {
        mockMvc.perform(post("/api/surveys")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Weekly pulse",
                                  "category": "satisfaction",
                                  "required": true,
                                  "status": "in_progress",
                                  "questions": [{"type": "long_text", "text": "의견을 적어 주세요."}]
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
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
        given(priorityApiService.classmates(isNull(), isNull()))
                .willReturn(new ClassmatesResponse(List.of(), new ClassmateSummary(0, 0, 0, 0), new ClassmateFilters(null, null)));
        given(priorityApiService.classmates(eq("kim"), eq("coach")))
                .willReturn(new ClassmatesResponse(List.of(), new ClassmateSummary(1, 0, 1, 0), new ClassmateFilters("kim", "coach")));
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
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.summary.totalCount").value(0));
        mockMvc.perform(get("/api/community/classmates?keyword=kim&memberRole=coach"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filters.keyword").value("kim"))
                .andExpect(jsonPath("$.filters.memberRole").value("coach"))
                .andExpect(jsonPath("$.summary.coachCount").value(1));
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
    void notificationDeleteReturnsUnreadCount() throws Exception {
        given(priorityApiService.deleteNotification(9L))
                .willReturn(new NotificationDeleteResponse(9L, true, 1L));

        mockMvc.perform(delete("/api/notifications/9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.deleted").value(true))
                .andExpect(jsonPath("$.unreadCount").value(1));
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
    void profilePasswordChangeReturnsActionResult() throws Exception {
        given(priorityApiService.changeProfilePassword(any(ProfilePasswordChangeRequest.class)))
                .willReturn(new AuthActionResponse(true, "비밀번호가 변경되었습니다."));

        mockMvc.perform(patch("/api/profile/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentPassword": "password",
                                  "newPassword": "new-password-1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("비밀번호가 변경되었습니다."));
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
