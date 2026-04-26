package com.edussafy.backend.priority.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceAppealItem;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceAppealRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceAppealResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceAppealResolveRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceAppealsResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceRecordItem;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceRecordsResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceSummary;
import com.edussafy.backend.priority.dto.PriorityDtos.AccessPolicyResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.AuthActionResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.AuthSessionResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.BookmarkDeleteResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.BookmarkItem;
import com.edussafy.backend.priority.dto.PriorityDtos.BookmarkRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.BookmarkResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.BookmarkSnapshot;
import com.edussafy.backend.priority.dto.PriorityDtos.BookmarksResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ClassmateNotificationRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.ClassmateNotificationResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ClassmateItem;
import com.edussafy.backend.priority.dto.PriorityDtos.ClassmatesResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.CurriculumScheduleRow;
import com.edussafy.backend.priority.dto.PriorityDtos.CurriculumWeekDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.CurriculumWeeksResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.DocumentAttachmentDownload;
import com.edussafy.backend.priority.dto.PriorityDtos.DocumentAttachmentItem;
import com.edussafy.backend.priority.dto.PriorityDtos.DocumentRequestDetail;
import com.edussafy.backend.priority.dto.PriorityDtos.DocumentRequestItem;
import com.edussafy.backend.priority.dto.PriorityDtos.DocumentRequestsResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.DocumentSubmissionDeleteResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.DocumentSubmissionRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.DocumentSubmissionResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.EducationAttendanceSummary;
import com.edussafy.backend.priority.dto.PriorityDtos.EducationLearningSummary;
import com.edussafy.backend.priority.dto.PriorityDtos.EducationPointSummary;
import com.edussafy.backend.priority.dto.PriorityDtos.EducationQuestSummary;
import com.edussafy.backend.priority.dto.PriorityDtos.EducationStatusResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.LevelSummary;
import com.edussafy.backend.priority.dto.PriorityDtos.LevelDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.LevelHistoryItem;
import com.edussafy.backend.priority.dto.PriorityDtos.ElearningLessonItem;
import com.edussafy.backend.priority.dto.PriorityDtos.ElearningProgressDetail;
import com.edussafy.backend.priority.dto.PriorityDtos.ElearningProgressItem;
import com.edussafy.backend.priority.dto.PriorityDtos.ElearningProgressResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.EbookAccessLogItem;
import com.edussafy.backend.priority.dto.PriorityDtos.EbookAccessLogResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.EbookItem;
import com.edussafy.backend.priority.dto.PriorityDtos.EbooksResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ElearningResumeResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.CurrentLiveSessionResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.LiveSessionItem;
import com.edussafy.backend.priority.dto.PriorityDtos.LiveSessionJoinLogItem;
import com.edussafy.backend.priority.dto.PriorityDtos.LiveSessionJoinResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.LiveSessionsResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.LoginRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialItem;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialReactionResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialResourceAttachmentCreateResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialResourceAttachmentDownload;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialResourceAttachmentItem;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialResourceAttachmentRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialResourceItem;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialViewResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.NotificationDeleteResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.NotificationItem;
import com.edussafy.backend.priority.dto.PriorityDtos.NotificationReadResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.NotificationsReadAllResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.PasswordCheckRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.PasswordCheckResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.PledgeAgreementItem;
import com.edussafy.backend.priority.dto.PriorityDtos.PledgeAgreementRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.PledgeAgreementResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.PledgeItem;
import com.edussafy.backend.priority.dto.PriorityDtos.PledgesResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ProfileDetails;
import com.edussafy.backend.priority.dto.PriorityDtos.ProfileEditAuthorizationResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ProfilePasswordChangeRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.ProfileResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ProfileUpdateRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestItem;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestListSummary;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestsResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestSubmissionAttachmentCreateResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestSubmissionAttachmentDownload;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestSubmissionAttachmentItem;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestSubmissionAttachmentRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestSubmissionDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestSubmissionItem;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestSubmissionRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestSubmissionResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ReplayDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ReplayItem;
import com.edussafy.backend.priority.dto.PriorityDtos.ReplayResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ReplayWatchLogItem;
import com.edussafy.backend.priority.dto.PriorityDtos.ReplayWatchLogResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.RequiredStudiesResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.RequiredStudyCompleteResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.RequiredStudyItem;
import com.edussafy.backend.priority.dto.PriorityDtos.RoleAccessResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyAnswerRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyCreateRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyDeleteResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyDetail;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyOptionCreateRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyOptionItem;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyQuestionCreateRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyQuestionItem;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyResponseDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyResponseSubmitRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyResponseSubmitResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveySavedAnswerItem;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketAttachmentCreateResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketAttachmentDownload;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketAttachmentItem;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketAttachmentRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketCreateRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketCreateResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketItem;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketMessageCreateResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketMessageItem;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketMessageRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.UserProfile;
import com.edussafy.backend.priority.repository.PriorityApiRepository;
import com.edussafy.backend.priority.repository.PriorityP2Repository;
import com.edussafy.backend.priority.repository.PriorityP3Repository;
import com.edussafy.backend.priority.repository.PriorityP3Repository.SurveyResponsePersistence;
import com.edussafy.backend.priority.security.AuthSession;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

class PriorityApiServiceTest {

    private static final UserProfile USER = new UserProfile(
            1L, "Demo Student", "student@ssafy.com", "learner", "Seoul", "12th", "Java"
    );
    private static final UserProfile STAFF_USER = new UserProfile(
            2L, "Demo Manager", "manager@ssafy.com", "manager", "Seoul", "12th", "Java"
    );
    private static final String PASSWORD_SHA256 = "{sha256}5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8";

    @Test
    void rejectsCurrentUserLookupWithoutWebSession() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        PriorityApiService service = new PriorityApiService(
                mock(PriorityApiRepository.class),
                mock(PriorityP2Repository.class),
                mock(PriorityP3Repository.class)
        );

        try {
            assertThatThrownBy(service::currentRoleAccess)
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("401")
                    .hasMessageContaining("로그인이 필요합니다.");
            assertThat(request.getSession(false)).isNull();
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    void invalidatesSessionWhenCurrentUserDisappears() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthSession.CURRENT_USER_ID, 99L);
        request.setSession(session);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        given(repository.findUserById(99L)).willReturn(Optional.empty());
        PriorityApiService service = new PriorityApiService(
                repository,
                mock(PriorityP2Repository.class),
                mock(PriorityP3Repository.class)
        );

        try {
            assertThatThrownBy(service::currentRoleAccess)
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("401")
                    .hasMessageContaining("세션 사용자 정보를 찾을 수 없습니다.");
            assertThat(session.isInvalid()).isTrue();
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    void exposesLearnerRolePermissionsByDefault() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        given(repository.findDefaultUser()).willReturn(Optional.empty());
        PriorityApiService service = new PriorityApiService(
                repository,
                mock(PriorityP2Repository.class),
                mock(PriorityP3Repository.class)
        );

        RoleAccessResponse response = service.currentRoleAccess();

        assertThat(response.role()).isEqualTo("learner");
        assertThat(response.permissions()).contains("dashboard:read", "profile:update", "quest:submit");
        assertThat(response.deniedRoutes()).contains("/admin");
    }

    @Test
    void exposesCoachRoleWithAdminRouteDenied() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        given(repository.findDefaultUser()).willReturn(Optional.of(STAFF_USER));
        PriorityApiService service = new PriorityApiService(
                repository,
                mock(PriorityP2Repository.class),
                mock(PriorityP3Repository.class)
        );

        RoleAccessResponse response = service.currentRoleAccess();

        assertThat(response.role()).isEqualTo("coach");
        assertThat(response.permissions()).contains("attendance:resolve", "support:answer", "board:moderate");
        assertThat(response.deniedRoutes()).containsExactly("/admin");
    }

    @Test
    void accessPolicyExposesProductionStaffApiMatrix() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        given(repository.findDefaultUser()).willReturn(Optional.of(STAFF_USER));
        PriorityApiService service = new PriorityApiService(
                repository,
                mock(PriorityP2Repository.class),
                mock(PriorityP3Repository.class)
        );

        AccessPolicyResponse response = service.accessPolicy();

        assertThat(response.items())
                .extracting("id")
                .contains(
                        "attendance-appeal-resolve",
                        "survey-manage",
                        "classmate-notification-send",
                        "learning-material-attachment",
                        "support-answer",
                        "admin-campus-structure"
                );
        assertThat(response.items())
                .filteredOn(item -> item.id().equals("support-answer"))
                .singleElement()
                .satisfies(item -> assertThat(item.allowedRoles()).containsExactly("coach", "admin"));
    }

    @Test
    void ebookListDetailAndAccessLogUseCurrentUser() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        EbookItem before = new EbookItem(
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
        EbookItem after = new EbookItem(
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
        EbookAccessLogItem accessLog = new EbookAccessLogItem(91L, 5L, OffsetDateTime.parse("2026-04-26T10:00:00+09:00"));
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(repository.countEbooks(USER.id())).willReturn(1L);
        given(repository.findEbooks(USER.id(), 20, 0)).willReturn(List.of(before));
        given(repository.findEbook(USER.id(), 5L)).willReturn(Optional.of(before), Optional.of(after));
        given(repository.createEbookAccessLog(USER.id(), 5L)).willReturn(91L);
        given(repository.findEbookAccessLog(USER.id(), 5L, 91L)).willReturn(Optional.of(accessLog));
        PriorityApiService service = new PriorityApiService(repository, mock(PriorityP2Repository.class), mock(PriorityP3Repository.class));

        EbooksResponse list = service.ebooks(1, 20);
        EbookAccessLogResponse response = service.logEbookAccess(5L);

        assertThat(list.items()).containsExactly(before);
        assertThat(response.item().accessCount()).isEqualTo(1);
        assertThat(response.accessLog()).isEqualTo(accessLog);
        verify(repository).createEbookAccessLog(USER.id(), 5L);
    }

    @Test
    void ebookDetailRejectsInactiveOrMissingEbook() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(repository.findEbook(USER.id(), 404L)).willReturn(Optional.empty());
        PriorityApiService service = new PriorityApiService(repository, mock(PriorityP2Repository.class), mock(PriorityP3Repository.class));

        assertThatThrownBy(() -> service.ebook(404L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
        verify(repository, never()).createEbookAccessLog(anyLong(), anyLong());
    }

    @Test
    void requiredStudiesListCompleteAndCurrentUserProgressOnly() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        RequiredStudyItem inProgress = new RequiredStudyItem(
                7L,
                "Java 보안 필수학습",
                "보안 체크리스트",
                "Security",
                "Java",
                OffsetDateTime.now().plusDays(7),
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
                inProgress.dueAt(),
                "url",
                "https://edu.ssafy.local/required-studies/java-security",
                "completed",
                100,
                OffsetDateTime.now()
        );
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(repository.countRequiredStudies(USER.id())).willReturn(1L);
        given(repository.findRequiredStudies(USER.id(), 20, 0)).willReturn(List.of(inProgress));
        given(repository.findRequiredStudy(USER.id(), 7L)).willReturn(Optional.of(inProgress), Optional.of(completed));
        PriorityApiService service = new PriorityApiService(repository, mock(PriorityP2Repository.class), mock(PriorityP3Repository.class));

        RequiredStudiesResponse list = service.requiredStudies(1, 20);
        RequiredStudyCompleteResponse result = service.completeRequiredStudy(7L);

        assertThat(list.items()).containsExactly(inProgress);
        assertThat(result.item().status()).isEqualTo("completed");
        assertThat(result.item().progressPercent()).isEqualTo(100);
        verify(repository).findRequiredStudies(USER.id(), 20, 0);
        verify(repository).completeRequiredStudy(USER.id(), 7L);
    }

    @Test
    void requiredStudyCompleteRejectsMissingOrInvisibleStudy() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(repository.findRequiredStudy(USER.id(), 99L)).willReturn(Optional.empty());
        PriorityApiService service = new PriorityApiService(repository, mock(PriorityP2Repository.class), mock(PriorityP3Repository.class));

        assertThatThrownBy(() -> service.completeRequiredStudy(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
        verify(repository, never()).completeRequiredStudy(anyLong(), anyLong());
    }

    @Test
    void liveSessionsListCurrentAndJoinUseCurrentUser() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        LiveSessionItem live = new LiveSessionItem(
                11L,
                "Java 라이브 알고리즘 코칭",
                "Java",
                "12th",
                "Seoul Java 1",
                OffsetDateTime.now().minusMinutes(30),
                OffsetDateTime.now().plusMinutes(90),
                "https://edu.ssafy.local/live/java-algorithm",
                "live",
                OffsetDateTime.now().minusDays(1),
                null,
                0
        );
        LiveSessionItem joined = new LiveSessionItem(
                11L,
                "Java 라이브 알고리즘 코칭",
                "Java",
                "12th",
                "Seoul Java 1",
                live.startsAt(),
                live.endsAt(),
                "https://edu.ssafy.local/live/java-algorithm",
                "live",
                live.createdAt(),
                OffsetDateTime.now(),
                1
        );
        LiveSessionJoinLogItem joinLog = new LiveSessionJoinLogItem(88L, 11L, OffsetDateTime.now());
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(repository.findTodayLiveSessions(USER.id())).willReturn(List.of(live));
        given(repository.findCurrentLiveSession(USER.id())).willReturn(Optional.of(live));
        given(repository.findLiveSession(USER.id(), 11L)).willReturn(Optional.of(live), Optional.of(joined));
        given(repository.createLiveSessionJoinLog(USER.id(), 11L)).willReturn(88L);
        given(repository.findLiveSessionJoinLog(USER.id(), 11L, 88L)).willReturn(Optional.of(joinLog));
        PriorityApiService service = new PriorityApiService(repository, mock(PriorityP2Repository.class), mock(PriorityP3Repository.class));

        LiveSessionsResponse today = service.todayLiveSessions();
        CurrentLiveSessionResponse current = service.currentLiveSession();
        LiveSessionJoinResponse joinedResponse = service.joinLiveSession(11L);

        assertThat(today.items()).containsExactly(live);
        assertThat(current.item()).isEqualTo(live);
        assertThat(joinedResponse.item().joinCount()).isEqualTo(1);
        assertThat(joinedResponse.joinLog()).isEqualTo(joinLog);
        verify(repository).createLiveSessionJoinLog(USER.id(), 11L);
    }

    @Test
    void liveSessionJoinRejectsEndedOrInvisibleSession() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        LiveSessionItem ended = new LiveSessionItem(
                12L,
                "종료된 라이브",
                "Java",
                "12th",
                "Seoul Java 1",
                OffsetDateTime.now().minusHours(3),
                OffsetDateTime.now().minusHours(1),
                "https://edu.ssafy.local/live/ended",
                "ended",
                OffsetDateTime.now().minusDays(1),
                null,
                0
        );
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(repository.findLiveSession(USER.id(), 12L)).willReturn(Optional.of(ended));
        given(repository.findLiveSession(USER.id(), 99L)).willReturn(Optional.empty());
        PriorityApiService service = new PriorityApiService(repository, mock(PriorityP2Repository.class), mock(PriorityP3Repository.class));

        assertThatThrownBy(() -> service.joinLiveSession(12L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");
        assertThatThrownBy(() -> service.joinLiveSession(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
        verify(repository, never()).createLiveSessionJoinLog(anyLong(), anyLong());
    }

    @Test
    void replaySplitListDetailAndWatchLogUseCurrentUser() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
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
                OffsetDateTime.now(),
                1
        );
        ReplayWatchLogItem watchLog = new ReplayWatchLogItem(55L, 21L, OffsetDateTime.now());
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(repository.findReplays(USER.id(), "my", "spring")).willReturn(List.of(replay));
        given(repository.findReplays(USER.id(), "all", null)).willReturn(List.of(replay));
        given(repository.findReplay(USER.id(), 21L)).willReturn(Optional.of(replay), Optional.of(watched));
        given(repository.createReplayWatchLog(USER.id(), 21L)).willReturn(55L);
        given(repository.findReplayWatchLog(USER.id(), 21L, 55L)).willReturn(Optional.of(watchLog));
        PriorityApiService service = new PriorityApiService(repository, mock(PriorityP2Repository.class), mock(PriorityP3Repository.class));

        ReplayResponse my = service.myReplays("spring");
        ReplayResponse all = service.allReplays(null);
        ReplayDetailResponse detail = service.replay(21L);
        ReplayWatchLogResponse watchedResponse = service.watchReplay(21L);

        assertThat(my.items()).containsExactly(replay);
        assertThat(all.items()).containsExactly(replay);
        assertThat(detail.item()).isEqualTo(replay);
        assertThat(watchedResponse.item().watchCount()).isEqualTo(1);
        assertThat(watchedResponse.watchLog()).isEqualTo(watchLog);
        verify(repository).findReplays(USER.id(), "my", "spring");
        verify(repository).findReplays(USER.id(), "all", null);
        verify(repository).createReplayWatchLog(USER.id(), 21L);
    }

    @Test
    void replayWatchRejectsInvisibleReplay() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(repository.findReplay(USER.id(), 404L)).willReturn(Optional.empty());
        PriorityApiService service = new PriorityApiService(repository, mock(PriorityP2Repository.class), mock(PriorityP3Repository.class));

        assertThatThrownBy(() -> service.watchReplay(404L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
        verify(repository, never()).createReplayWatchLog(anyLong(), anyLong());
    }

    @Test
    void curriculumWeeksGroupSessionsAndApplyFilters() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(repository.findCurriculumWeekSchedules(USER.id(), "2026 Priority 1 Term", "Java")).willReturn(List.of(
                curriculumRow(31L, "2026 Priority 1 Term", 4, "Java", "2026-04-20", "09:00", "12:00", "lecture", "Java Collections Review"),
                curriculumRow(32L, "2026 Priority 1 Term", 4, "Java", "2026-04-24", "13:00", "18:00", "practice", "Spring Boot REST API")
        ));
        PriorityApiService service = new PriorityApiService(repository, mock(PriorityP2Repository.class), mock(PriorityP3Repository.class));

        CurriculumWeeksResponse response = service.curriculumWeeks("2026 Priority 1 Term", "Java", "done");

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().getFirst().id()).isEqualTo(31L);
        assertThat(response.items().getFirst().sessionCount()).isEqualTo(2);
        assertThat(response.items().getFirst().sessions())
                .extracting("title")
                .containsExactly("Java Collections Review", "Spring Boot REST API");
        verify(repository).findCurriculumWeekSchedules(USER.id(), "2026 Priority 1 Term", "Java");
    }

    @Test
    void curriculumWeekDetailRejectsInvisibleWeek() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(repository.findCurriculumWeekSchedules(USER.id(), 404L)).willReturn(List.of());
        PriorityApiService service = new PriorityApiService(repository, mock(PriorityP2Repository.class), mock(PriorityP3Repository.class));

        assertThatThrownBy(() -> service.curriculumWeek(404L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    @Test
    void curriculumWeekDetailReturnsGroupedSessions() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(repository.findCurriculumWeekSchedules(USER.id(), 31L)).willReturn(List.of(
                curriculumRow(31L, "2026 Priority 1 Term", 4, "Java", "2026-04-20", "09:00", "12:00", "lecture", "Java Collections Review"),
                curriculumRow(32L, "2026 Priority 1 Term", 4, "Java", "2026-04-24", "13:00", "18:00", "practice", "Spring Boot REST API")
        ));
        PriorityApiService service = new PriorityApiService(repository, mock(PriorityP2Repository.class), mock(PriorityP3Repository.class));

        CurriculumWeekDetailResponse response = service.curriculumWeek(31L);

        assertThat(response.item().weekNumber()).isEqualTo(4);
        assertThat(response.item().track()).isEqualTo("Java");
        assertThat(response.item().sessions()).hasSize(2);
    }

    @Test
    void levelDetailAggregatesCurrentUserRankHistoryAndProgress() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        LevelSummary level = new LevelSummary(5, 4200, 5000, 85, 12);
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(repository.findLevel(USER.id())).willReturn(Optional.of(level));
        given(repository.findLevelName(USER.id())).willReturn(Optional.of("Silver Lv.5"));
        given(repository.findLevelHistory(USER.id(), 6)).willReturn(List.of(
                new LevelHistoryItem(LocalDate.parse("2026-04-24"), 12, 4200, 85),
                new LevelHistoryItem(LocalDate.parse("2026-04-17"), 18, 3800, 78)
        ));
        PriorityApiService service = new PriorityApiService(repository, mock(PriorityP2Repository.class), mock(PriorityP3Repository.class));

        LevelDetailResponse response = service.levelDetail();

        assertThat(response.detail().levelName()).isEqualTo("Silver Lv.5");
        assertThat(response.detail().expPercent()).isEqualTo(84);
        assertThat(response.detail().expRemaining()).isEqualTo(800);
        assertThat(response.detail().history()).hasSize(2);
        assertThat(response.detail().pointBreakdown())
                .extracting("category")
                .contains("누적 장학 포인트", "최근 반영 포인트", "경험치");
        assertThat(response.detail().pointBreakdown().get(1).points()).isEqualTo(7);
    }

    @Test
    void educationStatusAggregatesCurrentUserMetrics() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        EducationAttendanceSummary attendance = new EducationAttendanceSummary("2026-04", 18, 1, 0, 1);
        EducationLearningSummary learning = new EducationLearningSummary(3, 5, 8, 320);
        EducationQuestSummary quests = new EducationQuestSummary(2, 5, 0);
        EducationPointSummary points = new EducationPointSummary(0, 1153, "Bronze Lv.3");
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(repository.findEducationAttendanceSummary(eq(USER.id()), any())).willReturn(attendance);
        given(repository.findEducationLearningSummary(USER.id())).willReturn(learning);
        given(repository.findEducationQuestSummary(USER.id())).willReturn(quests);
        given(repository.findEducationPointSummary(USER.id())).willReturn(Optional.of(points));
        PriorityApiService service = new PriorityApiService(repository, mock(PriorityP2Repository.class), mock(PriorityP3Repository.class));

        EducationStatusResponse response = service.educationStatus();

        assertThat(response.attendance()).isEqualTo(attendance);
        assertThat(response.learning().replayWatchMinutes()).isEqualTo(320);
        assertThat(response.quests().openCount()).isEqualTo(2);
        assertThat(response.points().levelName()).isEqualTo("Bronze Lv.3");
        verify(repository).findEducationAttendanceSummary(eq(USER.id()), any());
    }

    @Test
    void educationStatusUsesZeroDefaultsWhenMetricQueriesFail() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(repository.findEducationAttendanceSummary(eq(USER.id()), any())).willThrow(new org.springframework.dao.TransientDataAccessResourceException("db offline"));
        given(repository.findEducationLearningSummary(USER.id())).willThrow(new org.springframework.dao.TransientDataAccessResourceException("db offline"));
        given(repository.findEducationQuestSummary(USER.id())).willThrow(new org.springframework.dao.TransientDataAccessResourceException("db offline"));
        given(repository.findEducationPointSummary(USER.id())).willThrow(new org.springframework.dao.TransientDataAccessResourceException("db offline"));
        PriorityApiService service = new PriorityApiService(repository, mock(PriorityP2Repository.class), mock(PriorityP3Repository.class));

        EducationStatusResponse response = service.educationStatus();

        assertThat(response.attendance().presentDays()).isZero();
        assertThat(response.learning().totalRequiredStudyCount()).isZero();
        assertThat(response.quests().lateCount()).isZero();
        assertThat(response.points().levelName()).isEqualTo("Lv.1");
    }

    @Test
    void classmatesReturnFilteredSummaryForCurrentClass() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        PriorityP2Repository p2Repository = mock(PriorityP2Repository.class);
        ClassmateItem coach = new ClassmateItem(
                7L,
                "Kim Coach",
                "coach@ssafy.com",
                "coach",
                "coach",
                "Seoul",
                "12th",
                "Java",
                "Seoul Java 1"
        );
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(p2Repository.findClassmates(USER.id(), "kim", "coach")).willReturn(List.of(coach));
        PriorityApiService service = new PriorityApiService(
                repository,
                p2Repository,
                mock(PriorityP3Repository.class)
        );

        ClassmatesResponse response = service.classmates(" kim ", "coach");

        assertThat(response.items()).containsExactly(coach);
        assertThat(response.summary().coachCount()).isEqualTo(1);
        assertThat(response.summary().learnerCount()).isZero();
        assertThat(response.filters().keyword()).isEqualTo("kim");
        assertThat(response.filters().memberRole()).isEqualTo("coach");
        verify(p2Repository).findClassmates(USER.id(), "kim", "coach");
    }

    @Test
    void createsClassmateNotificationWithDefaults() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        PriorityP2Repository p2Repository = mock(PriorityP2Repository.class);
        NotificationItem storedNotification = new NotificationItem(
                123L,
                "Classmate contact request",
                "Let's study together!",
                OffsetDateTime.parse("2026-04-25T09:00:00+09:00"),
                false
        );
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(p2Repository.findClassmates(1L)).willReturn(List.of(new ClassmateItem(
                7L,
                "Classmate",
                "classmate@ssafy.com",
                "learner",
                "member",
                "Seoul",
                "12th",
                "Java",
                "Seoul Java 1"
        )));
        given(repository.createNotification(1L, "Classmate contact request", "Let's study together!")).willReturn(123L);
        given(repository.findNotification(7L, 123L)).willReturn(Optional.of(storedNotification));
        PriorityApiService service = new PriorityApiService(
                repository,
                p2Repository,
                mock(PriorityP3Repository.class)
        );

        ClassmateNotificationResponse response = service.createClassmateNotification(
                7L,
                new ClassmateNotificationRequest(null, null)
        );

        assertThat(response.item().id()).isEqualTo(123L);
        assertThat(response.item().recipientUserId()).isEqualTo(7L);
        assertThat(response.item().type()).isEqualTo("contact_request");
        assertThat(response.item().message()).isEqualTo("Let's study together!");
        assertThat(response.item().status()).isEqualTo("sent");
        assertThat(response.item().notification()).isEqualTo(storedNotification);
        assertThat(response.item().notification().body()).isEqualTo("Let's study together!");
        assertThat(response.item().demo()).isFalse();
        verify(repository).createNotification(1L, "Classmate contact request", "Let's study together!");
        verify(repository).createNotificationRecipient(123L, 7L);
    }

    @Test
    void marksNotificationReadAndReturnsUnreadCount() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        NotificationItem unread = new NotificationItem(8L, "알림", "확인 필요", OffsetDateTime.parse("2026-04-25T09:00:00+09:00"), false);
        NotificationItem read = new NotificationItem(8L, "알림", "확인 필요", OffsetDateTime.parse("2026-04-25T09:00:00+09:00"), true);
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(repository.findNotification(1L, 8L)).willReturn(Optional.of(unread), Optional.of(read));
        given(repository.countUnreadNotifications(1L)).willReturn(2L);
        PriorityApiService service = new PriorityApiService(
                repository,
                mock(PriorityP2Repository.class),
                mock(PriorityP3Repository.class)
        );

        NotificationReadResponse response = service.markNotificationRead(8L);

        assertThat(response.item().read()).isTrue();
        assertThat(response.unreadCount()).isEqualTo(2L);
        verify(repository).markNotificationRead(1L, 8L);
    }

    @Test
    void marksAllNotificationsReadAndReturnsLatestItems() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        NotificationItem read = new NotificationItem(10L, "전체 알림", "모두 확인됨", OffsetDateTime.parse("2026-04-25T09:00:00+09:00"), true);
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(repository.findNotifications(1L, 20, 0)).willReturn(List.of(read));
        given(repository.countUnreadNotifications(1L)).willReturn(0L);
        PriorityApiService service = new PriorityApiService(
                repository,
                mock(PriorityP2Repository.class),
                mock(PriorityP3Repository.class)
        );

        NotificationsReadAllResponse response = service.markAllNotificationsRead();

        assertThat(response.items()).containsExactly(read);
        assertThat(response.unreadCount()).isZero();
        verify(repository).markAllNotificationsRead(1L);
    }

    @Test
    void deletesNotificationForCurrentUser() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        NotificationItem item = new NotificationItem(8L, "알림", "확인 필요", OffsetDateTime.parse("2026-04-25T09:00:00+09:00"), false);
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(repository.findNotification(1L, 8L)).willReturn(Optional.of(item));
        given(repository.countUnreadNotifications(1L)).willReturn(1L);
        PriorityApiService service = new PriorityApiService(
                repository,
                mock(PriorityP2Repository.class),
                mock(PriorityP3Repository.class)
        );

        NotificationDeleteResponse response = service.deleteNotification(8L);

        assertThat(response.id()).isEqualTo(8L);
        assertThat(response.deleted()).isTrue();
        assertThat(response.unreadCount()).isEqualTo(1L);
        verify(repository).deleteNotification(1L, 8L);
    }

    @Test
    void bookmarksListCreateAndDeleteUseCurrentUser() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
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
        BookmarkSnapshot snapshot = new BookmarkSnapshot("material", 5L, "REST API Workbook", "학습자료", null, "/learning/materials/5");
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(repository.countBookmarks(USER.id(), "material")).willReturn(1L);
        given(repository.findBookmarks(USER.id(), "material", 20, 0)).willReturn(List.of(item));
        given(repository.findBookmarkSnapshot("material", 5L)).willReturn(Optional.of(snapshot));
        given(repository.createOrUpdateBookmark(USER.id(), snapshot)).willReturn(90L);
        given(repository.findBookmark(USER.id(), 90L)).willReturn(Optional.of(item));
        given(repository.deleteBookmark(USER.id(), 90L)).willReturn(1);
        PriorityApiService service = new PriorityApiService(
                repository,
                mock(PriorityP2Repository.class),
                mock(PriorityP3Repository.class)
        );

        BookmarksResponse list = service.bookmarks("material", 1, 20);
        BookmarkResponse created = service.createBookmark(new BookmarkRequest("MATERIAL", 5L));
        BookmarkDeleteResponse deleted = service.deleteBookmark(90L);

        assertThat(list.items()).containsExactly(item);
        assertThat(created.item()).isEqualTo(item);
        assertThat(deleted.deleted()).isTrue();
        verify(repository).deleteBookmark(USER.id(), 90L);
    }

    @Test
    void bookmarkCreateRejectsUnknownTargetAndDeleteMissingBookmark() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(repository.findBookmarkSnapshot("material", 404L)).willReturn(Optional.empty());
        given(repository.deleteBookmark(USER.id(), 404L)).willReturn(0);
        PriorityApiService service = new PriorityApiService(
                repository,
                mock(PriorityP2Repository.class),
                mock(PriorityP3Repository.class)
        );

        assertThatThrownBy(() -> service.createBookmark(new BookmarkRequest("material", 404L)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
        assertThatThrownBy(() -> service.deleteBookmark(404L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    @Test
    void documentRequestsSubmitCancelAndDownloadUseCurrentUser() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        PriorityP2Repository p2Repository = mock(PriorityP2Repository.class);
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
                "not_submitted",
                null,
                null,
                List.of()
        );
        DocumentRequestDetail before = new DocumentRequestDetail(
                9L,
                "신분증 사본 제출",
                "본인 확인 서류",
                "identity",
                true,
                ".pdf,.jpg,.png",
                2_097_152L,
                OffsetDateTime.parse("2026-04-01T09:00:00+09:00"),
                OffsetDateTime.parse("2026-05-10T18:00:00+09:00"),
                "not_submitted",
                null,
                null,
                null,
                List.of()
        );
        DocumentAttachmentItem attachment = new DocumentAttachmentItem(
                77L,
                88L,
                9L,
                "identity.pdf",
                "documents/test/identity.pdf",
                "application/pdf",
                5L,
                OffsetDateTime.parse("2026-04-25T14:30:00+09:00")
        );
        DocumentRequestDetail after = new DocumentRequestDetail(
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
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(repository.countDocumentRequests(USER.id())).willReturn(1L);
        given(repository.findDocumentRequests(USER.id(), 20, 0)).willReturn(List.of(listItem));
        given(repository.findDocumentAttachmentsByRequestIds(USER.id(), List.of(9L)))
                .willReturn(List.of())
                .willReturn(List.of(attachment))
                .willReturn(List.of(attachment));
        given(repository.findDocumentRequestDetail(USER.id(), 9L))
                .willReturn(Optional.of(before))
                .willReturn(Optional.of(after))
                .willReturn(Optional.of(after));
        given(repository.upsertDocumentSubmission(USER.id(), 9L)).willReturn(88L);
        given(p2Repository.createOrFindAttachment(eq("identity.pdf"), anyString(), anyString(), eq("application/pdf"), eq(5L), anyString()))
                .willReturn(77L);
        given(repository.findDocumentAttachment(USER.id(), 88L, 77L)).willReturn(Optional.of(attachment));
        given(repository.cancelDocumentSubmission(USER.id(), 9L, 88L)).willReturn(1);
        PriorityApiService service = new PriorityApiService(repository, p2Repository, mock(PriorityP3Repository.class));

        DocumentRequestsResponse list = service.documentRequests(1, 20);
        DocumentSubmissionResponse submitted = service.submitDocument(
                9L,
                new DocumentSubmissionRequest("identity.pdf", "application/pdf", "aGVsbG8=")
        );
        DocumentAttachmentDownload download = service.downloadDocumentAttachment(88L, 77L);
        DocumentSubmissionDeleteResponse canceled = service.cancelDocumentSubmission(9L, 88L);

        assertThat(list.items()).hasSize(1);
        assertThat(submitted.submission().attachments()).containsExactly(attachment);
        assertThat(download.content()).isEqualTo("hello".getBytes(StandardCharsets.UTF_8));
        assertThat(canceled.canceled()).isTrue();
        verify(repository).linkDocumentSubmissionAttachment(88L, 77L);
    }

    @Test
    void documentSubmitRejectsDisallowedExtensionBeforeAttachmentInsert() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        PriorityP2Repository p2Repository = mock(PriorityP2Repository.class);
        DocumentRequestDetail target = new DocumentRequestDetail(
                9L,
                "신분증 사본 제출",
                "본인 확인 서류",
                "identity",
                true,
                ".pdf",
                2_097_152L,
                OffsetDateTime.parse("2026-04-01T09:00:00+09:00"),
                OffsetDateTime.parse("2026-05-10T18:00:00+09:00"),
                "not_submitted",
                null,
                null,
                null,
                List.of()
        );
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(repository.findDocumentRequestDetail(USER.id(), 9L)).willReturn(Optional.of(target));
        given(repository.findDocumentAttachmentsByRequestIds(USER.id(), List.of(9L))).willReturn(List.of());
        PriorityApiService service = new PriorityApiService(repository, p2Repository, mock(PriorityP3Repository.class));

        assertThatThrownBy(() -> service.submitDocument(
                9L,
                new DocumentSubmissionRequest("malware.exe", "application/octet-stream", "aGVsbG8=")
        ))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");
        verify(p2Repository, never()).createOrFindAttachment(anyString(), anyString(), anyString(), anyString(), anyLong(), anyString());
    }

    @Test
    void pledgeListAndAgreementUseCurrentUserAndPersistVersionSnapshot() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        PledgeItem before = new PledgeItem(
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
        PledgeItem after = new PledgeItem(
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
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(repository.countPledges(USER.id())).willReturn(1L);
        given(repository.findPledges(USER.id(), 20, 0)).willReturn(List.of(before));
        given(repository.findPledge(USER.id(), 3L)).willReturn(Optional.of(before), Optional.of(after));
        given(repository.upsertPledgeAgreement(eq(USER.id()), eq(before), eq(true), anyString(), anyString())).willReturn(44L);
        given(repository.findPledgeAgreement(USER.id(), 3L, 44L)).willReturn(Optional.of(agreement));
        PriorityApiService service = new PriorityApiService(repository, mock(PriorityP2Repository.class), mock(PriorityP3Repository.class));

        PledgesResponse list = service.pledges(1, 20);
        PledgeAgreementResponse response = service.agreePledge(3L, new PledgeAgreementRequest(true));

        assertThat(list.items()).containsExactly(before);
        assertThat(response.item().agreed()).isTrue();
        assertThat(response.agreement().versionSnapshot()).isEqualTo("2026.1");
        verify(repository).upsertPledgeAgreement(eq(USER.id()), eq(before), eq(true), anyString(), anyString());
    }

    @Test
    void pledgeAgreementRequiresPositiveAgreementAndActiveDueDate() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        PledgeItem target = new PledgeItem(
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
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(repository.findPledge(USER.id(), 3L)).willReturn(Optional.of(target));
        PriorityApiService service = new PriorityApiService(repository, mock(PriorityP2Repository.class), mock(PriorityP3Repository.class));

        assertThatThrownBy(() -> service.agreePledge(3L, new PledgeAgreementRequest(false)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");
        verify(repository, never()).upsertPledgeAgreement(anyLong(), eq(target), eq(true), anyString(), anyString());
    }

    @Test
    void elearningInProgressReturnsFilteredCurrentUserProgress() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
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
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(repository.countElearningProgress(USER.id(), "in_progress", "java")).willReturn(1L);
        given(repository.findElearningProgress(USER.id(), "in_progress", "java", 10, 0)).willReturn(List.of(item));
        PriorityApiService service = new PriorityApiService(
                repository,
                mock(PriorityP2Repository.class),
                mock(PriorityP3Repository.class)
        );

        ElearningProgressResponse response = service.elearningInProgress("IN_PROGRESS", "java", 1, 10);

        assertThat(response.items()).containsExactly(item);
        assertThat(response.page().totalItems()).isEqualTo(1);
        verify(repository).findElearningProgress(USER.id(), "in_progress", "java", 10, 0);
    }

    @Test
    void elearningDetailAndResumeUseOnlyCurrentUserProgress() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
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
                List.of()
        );
        ElearningLessonItem lesson = new ElearningLessonItem(100L, 1, "클래스와 객체", 2400L, true, OffsetDateTime.parse("2026-04-25T10:00:00+09:00"));
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(repository.findElearningProgressDetail(USER.id(), 10L)).willReturn(Optional.of(detail));
        given(repository.findElearningLessons(USER.id(), 10L)).willReturn(List.of(lesson));
        given(repository.touchElearningResume(USER.id(), 10L)).willReturn(1);
        PriorityApiService service = new PriorityApiService(
                repository,
                mock(PriorityP2Repository.class),
                mock(PriorityP3Repository.class)
        );

        assertThat(service.elearningProgressDetail(10L).item().lessons()).containsExactly(lesson);
        ElearningResumeResponse response = service.resumeElearning(10L);

        assertThat(response.item().courseId()).isEqualTo(10L);
        assertThat(response.item().resumeUrl()).isEqualTo("/mycampus/elearning/10");
        verify(repository).touchElearningResume(USER.id(), 10L);
    }

    @Test
    void elearningRejectsUnsupportedStatusAndMissingProgress() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(repository.touchElearningResume(USER.id(), 99L)).willReturn(0);
        PriorityApiService service = new PriorityApiService(
                repository,
                mock(PriorityP2Repository.class),
                mock(PriorityP3Repository.class)
        );

        assertThatThrownBy(() -> service.elearningInProgress("archived", null, 1, 10))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");
        assertThatThrownBy(() -> service.resumeElearning(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    @Test
    void recordsMaterialViewAndReturnsUpdatedMaterialWithResources() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        PriorityP3Repository p3Repository = mock(PriorityP3Repository.class);
        MaterialItem updated = new MaterialItem(
                15L,
                "Spring REST Docs",
                "file",
                "API 문서 실습",
                "/materials/rest-docs.pdf",
                6,
                OffsetDateTime.parse("2026-04-25T10:00:00+09:00"),
                List.of(),
                1,
                0,
                true,
                false
        );
        MaterialResourceItem resource = new MaterialResourceItem(
                30L,
                15L,
                "file",
                "rest-docs.pdf",
                "inline",
                "/materials/rest-docs.pdf",
                1
        );
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(p3Repository.incrementMaterialViewCount(15L)).willReturn(1);
        given(p3Repository.findMaterial(15L, USER.id())).willReturn(Optional.of(updated));
        given(p3Repository.findMaterialResources(15L)).willReturn(List.of(resource));
        PriorityApiService service = new PriorityApiService(
                repository,
                mock(PriorityP2Repository.class),
                p3Repository
        );

        MaterialViewResponse response = service.recordMaterialView(15L);

        assertThat(response.item().id()).isEqualTo(15L);
        assertThat(response.item().viewCount()).isEqualTo(6);
        assertThat(response.item().resources()).containsExactly(resource);
        verify(p3Repository).incrementMaterialViewCount(15L);
    }

    @Test
    void createsAndDeletesMaterialReactionForCurrentUser() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        PriorityP3Repository p3Repository = mock(PriorityP3Repository.class);
        MaterialItem before = new MaterialItem(
                15L,
                "Spring REST Docs",
                "file",
                "API 문서 실습",
                "/materials/rest-docs.pdf",
                6,
                OffsetDateTime.parse("2026-04-25T10:00:00+09:00"),
                List.of(),
                0,
                0,
                false,
                false
        );
        MaterialItem after = new MaterialItem(
                15L,
                "Spring REST Docs",
                "file",
                "API 문서 실습",
                "/materials/rest-docs.pdf",
                6,
                OffsetDateTime.parse("2026-04-25T10:00:00+09:00"),
                List.of(),
                1,
                0,
                true,
                false
        );
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(p3Repository.findMaterial(15L, USER.id()))
                .willReturn(Optional.of(before), Optional.of(after), Optional.of(after), Optional.of(before));
        PriorityApiService service = new PriorityApiService(
                repository,
                mock(PriorityP2Repository.class),
                p3Repository
        );

        MaterialReactionResponse created = service.createMaterialReaction(15L, " Like ");
        MaterialReactionResponse deleted = service.deleteMaterialReaction(15L, "like");

        assertThat(created.item().liked()).isTrue();
        assertThat(created.item().likeCount()).isEqualTo(1);
        assertThat(deleted.item().liked()).isFalse();
        verify(p3Repository).createMaterialReaction(15L, USER.id(), "like");
        verify(p3Repository).deleteMaterialReaction(15L, USER.id(), "like");
    }

    @Test
    void staffCreatesLearningMaterialResourceAttachment() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        PriorityP2Repository p2Repository = mock(PriorityP2Repository.class);
        PriorityP3Repository p3Repository = mock(PriorityP3Repository.class);
        MaterialResourceItem resource = new MaterialResourceItem(
                30L,
                15L,
                "file",
                "rest-docs.pdf",
                "download",
                "/materials/rest-docs.pdf",
                1
        );
        MaterialResourceAttachmentItem attachment = new MaterialResourceAttachmentItem(
                77L,
                30L,
                15L,
                "rest-docs.pdf",
                "learning/materials/15/resources/30/checksum-rest-docs.pdf",
                "/learning/materials/15/resources/30/attachments/checksum",
                "application/pdf",
                5L,
                "checksum",
                OffsetDateTime.parse("2026-04-25T10:00:00+09:00")
        );
        given(repository.findDefaultUser()).willReturn(Optional.of(STAFF_USER));
        given(p3Repository.findMaterialResource(15L, 30L)).willReturn(Optional.of(resource));
        given(p2Repository.createOrFindAttachment(
                eq("rest-docs.pdf"),
                anyString(),
                anyString(),
                eq("application/pdf"),
                eq(5L),
                anyString()
        )).willReturn(77L);
        given(p3Repository.findMaterialResourceAttachment(30L, 77L)).willReturn(Optional.of(attachment));
        PriorityApiService service = new PriorityApiService(repository, p2Repository, p3Repository);

        MaterialResourceAttachmentCreateResponse response = service.createMaterialResourceAttachment(
                15L,
                30L,
                new MaterialResourceAttachmentRequest("rest-docs.pdf", "application/pdf", "aGVsbG8=")
        );

        assertThat(response.item().id()).isEqualTo(77L);
        assertThat(response.item().filename()).isEqualTo("rest-docs.pdf");
        assertThat(response.resource()).isEqualTo(resource);
        verify(p3Repository).linkMaterialResourceAttachment(30L, 77L);
    }

    @Test
    void learnerCannotCreateLearningMaterialResourceAttachment() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        PriorityP2Repository p2Repository = mock(PriorityP2Repository.class);
        PriorityP3Repository p3Repository = mock(PriorityP3Repository.class);
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        PriorityApiService service = new PriorityApiService(repository, p2Repository, p3Repository);

        assertThatThrownBy(() -> service.createMaterialResourceAttachment(
                15L,
                30L,
                new MaterialResourceAttachmentRequest("rest-docs.pdf", "application/pdf", "aGVsbG8=")
        ))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403");

        verify(p2Repository, never()).createOrFindAttachment(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyLong(),
                anyString()
        );
    }

    @Test
    void downloadsLearningMaterialResourceAttachmentForAuthenticatedUser() throws Exception {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        PriorityP3Repository p3Repository = mock(PriorityP3Repository.class);
        MaterialResourceItem resource = new MaterialResourceItem(
                30L,
                15L,
                "file",
                "rest-docs.pdf",
                "download",
                "/materials/rest-docs.pdf",
                1
        );
        MaterialResourceAttachmentItem attachment = new MaterialResourceAttachmentItem(
                77L,
                30L,
                15L,
                "rest-docs.pdf",
                "learning/materials/15/resources/30/test-download-rest-docs.pdf",
                "/learning/materials/15/resources/30/attachments/test-download",
                "application/pdf",
                5L,
                "test-download",
                OffsetDateTime.parse("2026-04-25T10:00:00+09:00")
        );
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(p3Repository.findMaterialResource(15L, 30L)).willReturn(Optional.of(resource));
        given(p3Repository.findMaterialResourceAttachment(30L, 77L)).willReturn(Optional.of(attachment));
        PriorityApiService service = new PriorityApiService(repository, mock(PriorityP2Repository.class), p3Repository);
        Path storedFile = Path.of(System.getProperty("java.io.tmpdir"), "edussafy-attachments")
                .resolve(attachment.storageKey())
                .normalize();
        Files.createDirectories(storedFile.getParent());
        Files.writeString(storedFile, "hello", StandardCharsets.UTF_8);

        MaterialResourceAttachmentDownload response = service.downloadMaterialResourceAttachment(15L, 30L, 77L);

        assertThat(response.item()).isEqualTo(attachment);
        assertThat(response.content()).isEqualTo("hello".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void questsReturnFilteredStatusSummaryForCurrentUser() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        PriorityP3Repository p3Repository = mock(PriorityP3Repository.class);
        QuestItem quest = new QuestItem(
                5L,
                "Algorithm Quest",
                "assignment",
                "required",
                OffsetDateTime.parse("2026-04-25T09:00:00+09:00"),
                OffsetDateTime.parse("2026-04-25T18:00:00+09:00"),
                100,
                "completed",
                "submitted",
                "pending"
        );
        QuestListSummary summary = new QuestListSummary(3, 1, 1, 1, 0);
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(repository.countQuests(USER.id(), "submitted", "algo")).willReturn(1L);
        given(repository.summarizeQuests(USER.id(), "algo")).willReturn(summary);
        given(repository.findQuests(USER.id(), "submitted", "algo", 10, 0)).willReturn(List.of(quest));
        PriorityApiService service = new PriorityApiService(repository, mock(PriorityP2Repository.class), p3Repository);

        QuestsResponse response = service.quests(1, 10, "done", "algo");

        assertThat(response.items()).containsExactly(quest);
        assertThat(response.summary().gradedCount()).isEqualTo(1);
        assertThat(response.filters().status()).isEqualTo("submitted");
        assertThat(response.filters().keyword()).isEqualTo("algo");
        verify(repository).countQuests(USER.id(), "submitted", "algo");
        verify(repository).findQuests(USER.id(), "submitted", "algo", 10, 0);
    }

    @Test
    void persistsQuestSubmissionAndReturnsSavedStatus() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        PriorityP3Repository p3Repository = mock(PriorityP3Repository.class);
        QuestItem quest = new QuestItem(
                5L,
                "Algorithm Quest",
                "assignment",
                "required",
                OffsetDateTime.parse("2026-04-25T09:00:00+09:00"),
                OffsetDateTime.parse("2026-04-25T18:00:00+09:00"),
                100,
                "in_progress",
                null,
                null
        );
        QuestSubmissionItem saved = new QuestSubmissionItem(
                77L,
                5L,
                "submitted",
                OffsetDateTime.parse("2026-04-25T12:00:00+09:00"),
                "pending",
                null,
                null,
                false
        );
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(p3Repository.findQuest(1L, 5L)).willReturn(Optional.of(quest));
        given(p3Repository.upsertQuestSubmission(1L, 5L)).willReturn(Optional.of(saved));
        PriorityApiService service = new PriorityApiService(
                repository,
                mock(PriorityP2Repository.class),
                p3Repository
        );

        QuestSubmissionResponse response = service.submitQuest(
                5L,
                new QuestSubmissionRequest("Implemented dynamic programming solution.", "https://example.com/repo")
        );

        assertThat(response.item().id()).isEqualTo(77L);
        assertThat(response.item().questId()).isEqualTo(5L);
        assertThat(response.item().status()).isEqualTo("submitted");
        assertThat(response.item().demo()).isFalse();
        verify(p3Repository).findQuest(1L, 5L);
        verify(p3Repository).upsertQuestSubmission(1L, 5L);
    }

    @Test
    void readsQuestSubmissionResultDetail() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        PriorityP3Repository p3Repository = mock(PriorityP3Repository.class);
        QuestItem quest = new QuestItem(
                5L,
                "Algorithm Quest",
                "assignment",
                "required",
                OffsetDateTime.parse("2026-04-25T09:00:00+09:00"),
                OffsetDateTime.parse("2026-04-25T18:00:00+09:00"),
                100,
                "completed",
                "submitted",
                "graded"
        );
        QuestSubmissionItem saved = new QuestSubmissionItem(
                77L,
                5L,
                "submitted",
                OffsetDateTime.parse("2026-04-25T12:00:00+09:00"),
                "graded",
                95.0,
                OffsetDateTime.parse("2026-04-25T19:00:00+09:00"),
                false
        );
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(p3Repository.findQuest(1L, 5L)).willReturn(Optional.of(quest));
        given(p3Repository.findQuestSubmission(1L, 5L)).willReturn(Optional.of(saved));
        PriorityApiService service = new PriorityApiService(
                repository,
                mock(PriorityP2Repository.class),
                p3Repository
        );

        QuestSubmissionDetailResponse response = service.questSubmission(5L);

        assertThat(response.item().resultStatus()).isEqualTo("graded");
        assertThat(response.item().score()).isEqualTo(95.0);
        assertThat(response.item().gradedAt()).isNotNull();
        verify(p3Repository).findQuestSubmission(1L, 5L);
    }

    @Test
    void createsQuestSubmissionAttachmentForOwnerSubmission() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        PriorityP2Repository p2Repository = mock(PriorityP2Repository.class);
        PriorityP3Repository p3Repository = mock(PriorityP3Repository.class);
        QuestItem quest = new QuestItem(
                5L,
                "Algorithm Quest",
                "assignment",
                "required",
                OffsetDateTime.parse("2026-04-25T09:00:00+09:00"),
                OffsetDateTime.parse("2026-04-25T18:00:00+09:00"),
                100,
                "in_progress",
                "submitted",
                "pending"
        );
        QuestSubmissionItem submission = new QuestSubmissionItem(
                77L,
                5L,
                "submitted",
                OffsetDateTime.parse("2026-04-25T12:00:00+09:00"),
                "pending",
                null,
                null,
                false
        );
        QuestSubmissionAttachmentItem attachment = new QuestSubmissionAttachmentItem(
                88L,
                5L,
                77L,
                "solution.zip",
                "quests/5/submissions/77/checksum-solution.zip",
                "/quests/5/submissions/77/attachments/checksum",
                "application/zip",
                5L,
                "checksum",
                OffsetDateTime.parse("2026-04-25T12:01:00+09:00")
        );
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(p3Repository.findQuest(USER.id(), 5L)).willReturn(Optional.of(quest));
        given(p3Repository.findQuestSubmission(USER.id(), 5L)).willReturn(Optional.of(submission));
        given(p2Repository.createOrFindAttachment(
                eq("solution.zip"),
                anyString(),
                anyString(),
                eq("application/zip"),
                eq(5L),
                anyString()
        )).willReturn(88L);
        given(p3Repository.findQuestSubmissionAttachment(5L, 77L, 88L)).willReturn(Optional.of(attachment));
        PriorityApiService service = new PriorityApiService(repository, p2Repository, p3Repository);

        QuestSubmissionAttachmentCreateResponse response = service.createQuestSubmissionAttachment(
                5L,
                77L,
                new QuestSubmissionAttachmentRequest("solution.zip", "application/zip", "aGVsbG8=")
        );

        assertThat(response.item().id()).isEqualTo(88L);
        assertThat(response.submission()).isEqualTo(submission);
    }

    @Test
    void rejectsQuestSubmissionAttachmentForDifferentSubmission() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        PriorityP2Repository p2Repository = mock(PriorityP2Repository.class);
        PriorityP3Repository p3Repository = mock(PriorityP3Repository.class);
        QuestItem quest = new QuestItem(
                5L,
                "Algorithm Quest",
                "assignment",
                "required",
                null,
                null,
                null,
                "in_progress",
                "submitted",
                "pending"
        );
        QuestSubmissionItem submission = new QuestSubmissionItem(77L, 5L, "submitted", null, "pending", null, null, false);
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(p3Repository.findQuest(USER.id(), 5L)).willReturn(Optional.of(quest));
        given(p3Repository.findQuestSubmission(USER.id(), 5L)).willReturn(Optional.of(submission));
        PriorityApiService service = new PriorityApiService(repository, p2Repository, p3Repository);

        assertThatThrownBy(() -> service.createQuestSubmissionAttachment(
                5L,
                78L,
                new QuestSubmissionAttachmentRequest("solution.zip", "application/zip", "aGVsbG8=")
        ))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");

        verify(p2Repository, never()).createOrFindAttachment(anyString(), anyString(), anyString(), anyString(), anyLong(), anyString());
    }

    @Test
    void downloadsQuestSubmissionAttachmentForOwnerSubmission() throws Exception {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        PriorityP3Repository p3Repository = mock(PriorityP3Repository.class);
        QuestItem quest = new QuestItem(5L, "Algorithm Quest", "assignment", "required", null, null, null, "in_progress", "submitted", "pending");
        QuestSubmissionItem submission = new QuestSubmissionItem(77L, 5L, "submitted", null, "pending", null, null, false);
        QuestSubmissionAttachmentItem attachment = new QuestSubmissionAttachmentItem(
                88L,
                5L,
                77L,
                "solution.zip",
                "quests/5/submissions/77/test-download-solution.zip",
                "/quests/5/submissions/77/attachments/test-download",
                "application/zip",
                5L,
                "test-download",
                null
        );
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(p3Repository.findQuest(USER.id(), 5L)).willReturn(Optional.of(quest));
        given(p3Repository.findQuestSubmission(USER.id(), 5L)).willReturn(Optional.of(submission));
        given(p3Repository.findQuestSubmissionAttachment(5L, 77L, 88L)).willReturn(Optional.of(attachment));
        Path storedFile = Path.of(System.getProperty("java.io.tmpdir"), "edussafy-attachments")
                .resolve(attachment.storageKey())
                .normalize();
        Files.createDirectories(storedFile.getParent());
        Files.writeString(storedFile, "hello", StandardCharsets.UTF_8);
        PriorityApiService service = new PriorityApiService(repository, mock(PriorityP2Repository.class), p3Repository);

        QuestSubmissionAttachmentDownload response = service.downloadQuestSubmissionAttachment(5L, 77L, 88L);

        assertThat(response.item()).isEqualTo(attachment);
        assertThat(response.content()).isEqualTo("hello".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void checksProfilePasswordAgainstStoredSha256Hash() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(repository.findPasswordHash(1L)).willReturn(Optional.of(PASSWORD_SHA256));
        PriorityApiService service = new PriorityApiService(
                repository,
                mock(PriorityP2Repository.class),
                mock(PriorityP3Repository.class)
        );

        PasswordCheckResponse valid = service.passwordCheck(new PasswordCheckRequest("password"));
        PasswordCheckResponse invalid = service.passwordCheck(new PasswordCheckRequest("wrong"));

        assertThat(valid.valid()).isTrue();
        assertThat(invalid.valid()).isFalse();
    }


    @Test
    void rejectsLegacyNoopPasswordHashByDefault() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(repository.findPasswordHash(1L)).willReturn(Optional.of("{noop}password"));
        PriorityApiService service = new PriorityApiService(
                repository,
                mock(PriorityP2Repository.class),
                mock(PriorityP3Repository.class)
        );

        PasswordCheckResponse response = service.passwordCheck(new PasswordCheckRequest("password"));

        assertThat(response.valid()).isFalse();
    }

    @Test
    void loginStoresSessionAndMeUsesSessionUser() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        UserProfile manager = new UserProfile(
                2L, "Demo Manager", "manager@ssafy.com", "manager", "Seoul", "12th", "Java"
        );
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession anonymousSession = new MockHttpSession();
        anonymousSession.setAttribute(AuthSession.PROFILE_VERIFIED_UNTIL, Long.MAX_VALUE);
        request.setSession(anonymousSession);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        given(repository.findUserByEmail("manager@ssafy.com")).willReturn(Optional.of(manager));
        given(repository.findPasswordHash(2L)).willReturn(Optional.of(PASSWORD_SHA256));
        given(repository.findUserById(2L)).willReturn(Optional.of(manager));
        PriorityApiService service = new PriorityApiService(
                repository,
                mock(PriorityP2Repository.class),
                mock(PriorityP3Repository.class)
        );

        try {
            service.login(new LoginRequest("manager@ssafy.com", "password"));

            assertThat(anonymousSession.isInvalid()).isTrue();
            assertThat(request.getSession(false)).isNotSameAs(anonymousSession);
            assertThat(request.getSession(false).getAttribute(AuthSession.CURRENT_USER_ID)).isEqualTo(2L);
            assertThat(request.getSession(false).getAttribute(AuthSession.PROFILE_VERIFIED_UNTIL)).isNull();
            assertThat(request.getSession(false).getMaxInactiveInterval()).isEqualTo(AuthSession.MAX_INACTIVE_SECONDS);
            assertThat(service.me().user().email()).isEqualTo("manager@ssafy.com");
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    void authSessionReportsCurrentSessionExpiry() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute(AuthSession.CURRENT_USER_ID, 1L);
        request.getSession().setMaxInactiveInterval(900);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        PriorityApiService service = new PriorityApiService(
                mock(PriorityApiRepository.class),
                mock(PriorityP2Repository.class),
                mock(PriorityP3Repository.class)
        );

        try {
            AuthSessionResponse response = service.authSession();

            assertThat(response.authenticated()).isTrue();
            assertThat(response.maxInactiveSeconds()).isEqualTo(900);
            assertThat(response.secondsRemaining()).isBetween(1L, 900L);
            assertThat(response.expiresAt()).isNotNull();
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    void loginRejectsWrongPasswordWithoutCreatingSession() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        given(repository.findUserByEmail("student@ssafy.com")).willReturn(Optional.of(USER));
        given(repository.findPasswordHash(1L)).willReturn(Optional.of(PASSWORD_SHA256));
        PriorityApiService service = new PriorityApiService(
                repository,
                mock(PriorityP2Repository.class),
                mock(PriorityP3Repository.class)
        );

        try {
            assertThatThrownBy(() -> service.login(new LoginRequest("student@ssafy.com", "wrong")))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("401");
            assertThat(request.getSession(false)).isNull();
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    void loginRejectsUserWithoutStoredPasswordHash() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        given(repository.findUserByEmail("student@ssafy.com")).willReturn(Optional.of(USER));
        given(repository.findPasswordHash(1L)).willReturn(Optional.empty());
        PriorityApiService service = new PriorityApiService(
                repository,
                mock(PriorityP2Repository.class),
                mock(PriorityP3Repository.class)
        );

        try {
            assertThatThrownBy(() -> service.login(new LoginRequest("student@ssafy.com", "password")))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("401");
            assertThat(request.getSession(false)).isNull();
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    void loginRejectsUnknownEmailWithoutCreatingDemoSession() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        given(repository.findUserByEmail("intruder@ssafy.com")).willReturn(Optional.empty());
        PriorityApiService service = new PriorityApiService(
                repository,
                mock(PriorityP2Repository.class),
                mock(PriorityP3Repository.class)
        );

        try {
            assertThatThrownBy(() -> service.login(new LoginRequest("intruder@ssafy.com", "password")))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("401");
            assertThat(request.getSession(false)).isNull();
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    void profileEditAuthorizationReportsVerifiedWindow() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute(AuthSession.CURRENT_USER_ID, 1L);
        AuthSession.markProfileVerified(request.getSession(), java.time.Instant.now());
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        PriorityApiService service = new PriorityApiService(
                mock(PriorityApiRepository.class),
                mock(PriorityP2Repository.class),
                mock(PriorityP3Repository.class)
        );

        try {
            ProfileEditAuthorizationResponse response = service.profileEditAuthorization();

            assertThat(response.verified()).isTrue();
            assertThat(response.ttlSeconds()).isEqualTo(AuthSession.PROFILE_VERIFICATION_TTL_SECONDS);
            assertThat(response.verifiedUntil()).isNotNull();
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    void profileEditAuthorizationRequiresVerifiedSession() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute(AuthSession.CURRENT_USER_ID, 1L);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        PriorityApiService service = new PriorityApiService(
                mock(PriorityApiRepository.class),
                mock(PriorityP2Repository.class),
                mock(PriorityP3Repository.class)
        );

        try {
            ProfileEditAuthorizationResponse response = service.profileEditAuthorization();

            assertThat(response.verified()).isFalse();
            assertThat(response.verifiedUntil()).isNull();
            assertThat(response.ttlSeconds()).isEqualTo(AuthSession.PROFILE_VERIFICATION_TTL_SECONDS);
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    void persistsProfileUpdateAndReturnsStoredProfile() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        PriorityP2Repository p2Repository = mock(PriorityP2Repository.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute(AuthSession.CURRENT_USER_ID, 1L);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        ProfileDetails current = new ProfileDetails(
                1L,
                "Demo Student",
                "student@ssafy.com",
                "learner",
                "SSAFY-12-0001",
                "Seoul",
                "12th",
                "Java",
                "Seoul Java 1",
                null,
                null,
                null,
                "010-0000-0000",
                null,
                false
        );
        ProfileDetails updated = new ProfileDetails(
                1L,
                "Updated Student",
                "student@ssafy.com",
                "learner",
                "SSAFY-12-0001",
                "Seoul",
                "12th",
                "Java",
                "Seoul Java 1",
                "06234",
                "서울시 강남구",
                "101호",
                "010-1111-2222",
                "010-3333-4444",
                true
        );
        ProfileUpdateRequest profileRequest = new ProfileUpdateRequest(
                "Updated Student",
                "06234",
                "서울시 강남구",
                "101호",
                "010-1111-2222",
                "010-3333-4444",
                true
        );
        given(repository.findUserById(1L)).willReturn(Optional.of(USER));
        given(repository.findPasswordHash(1L)).willReturn(Optional.of(PASSWORD_SHA256));
        given(p2Repository.findProfile(1L)).willReturn(Optional.of(current));
        given(p2Repository.updateProfile(1L, profileRequest, true)).willReturn(Optional.of(updated));
        PriorityApiService service = new PriorityApiService(
                repository,
                p2Repository,
                mock(PriorityP3Repository.class)
        );

        try {
            PasswordCheckResponse passwordCheck = service.passwordCheck(new PasswordCheckRequest("password"));
            assertThat(passwordCheck.valid()).isTrue();
            assertThat(request.getSession().getAttribute(AuthSession.PROFILE_VERIFIED_UNTIL)).isNotNull();

            ProfileResponse response = service.updateProfile(profileRequest);

            assertThat(request.getSession().getAttribute(AuthSession.PROFILE_VERIFIED_UNTIL)).isNull();
            verify(p2Repository).updateProfile(1L, profileRequest, true);
            assertThat(response.profile().name()).isEqualTo("Updated Student");
            assertThat(response.profile().zipCode()).isEqualTo("06234");
            assertThat(response.profile().marketingOptIn()).isTrue();
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    void changesProfilePasswordAfterVerifyingCurrentPassword() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute(AuthSession.CURRENT_USER_ID, 1L);
        AuthSession.markProfileVerified(request.getSession(), java.time.Instant.now());
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        given(repository.findUserById(1L)).willReturn(Optional.of(USER));
        given(repository.findPasswordHash(1L)).willReturn(Optional.of(PASSWORD_SHA256));
        given(repository.updatePasswordHash(1L, "{sha256}05bef02ed98c2079162d4a8870bd88b66f587a90c57ae10a8502254c75cac717"))
                .willReturn(1);
        PriorityApiService service = new PriorityApiService(
                repository,
                mock(PriorityP2Repository.class),
                mock(PriorityP3Repository.class)
        );

        try {
            AuthActionResponse response = service.changeProfilePassword(
                    new ProfilePasswordChangeRequest("password", "new-password-1")
            );

            ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
            verify(repository).updatePasswordHash(eq(1L), hashCaptor.capture());
            assertThat(hashCaptor.getValue()).startsWith("{sha256}");
            assertThat(response.success()).isTrue();
            assertThat(response.message()).contains("비밀번호");
            assertThat(request.getSession().getAttribute(AuthSession.PROFILE_VERIFIED_UNTIL)).isNull();
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    void rejectsProfilePasswordChangeWhenCurrentPasswordDiffers() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute(AuthSession.CURRENT_USER_ID, 1L);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        given(repository.findUserById(1L)).willReturn(Optional.of(USER));
        given(repository.findPasswordHash(1L)).willReturn(Optional.of(PASSWORD_SHA256));
        PriorityApiService service = new PriorityApiService(
                repository,
                mock(PriorityP2Repository.class),
                mock(PriorityP3Repository.class)
        );

        try {
            assertThatThrownBy(() -> service.changeProfilePassword(
                    new ProfilePasswordChangeRequest("wrong-password", "new-password-1")
            ))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("401");
            verify(repository, never()).updatePasswordHash(eq(1L), anyString());
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    void rejectsProfileUpdateWithoutRecentPasswordVerification() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute(AuthSession.CURRENT_USER_ID, 1L);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        PriorityApiService service = new PriorityApiService(
                repository,
                mock(PriorityP2Repository.class),
                mock(PriorityP3Repository.class)
        );
        ProfileUpdateRequest profileRequest = new ProfileUpdateRequest(
                "Updated Student",
                "06234",
                "서울시 강남구",
                "101호",
                "010-1111-2222",
                "010-3333-4444",
                true
        );

        try {
            assertThatThrownBy(() -> service.updateProfile(profileRequest))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("403")
                    .hasMessageContaining("회원정보 수정 전 비밀번호 확인이 필요합니다.");
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    void filtersAttendanceRecordsByDateRangeAndStatus() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        LocalDate from = LocalDate.of(2026, 4, 1);
        LocalDate to = LocalDate.of(2026, 4, 30);
        AttendanceRecordItem lateRecord = new AttendanceRecordItem(
                7L,
                LocalDate.of(2026, 4, 23),
                null,
                null,
                "late",
                "auto",
                true,
                null,
                null,
                null
        );
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(repository.findAttendanceSummary(1L, from, to, "late"))
                .willReturn(new AttendanceSummary(0, 1, 0, true));
        given(repository.findAttendanceRecords(1L, from, to, "late")).willReturn(List.of(lateRecord));
        PriorityApiService service = new PriorityApiService(
                repository,
                mock(PriorityP2Repository.class),
                mock(PriorityP3Repository.class)
        );

        AttendanceRecordsResponse response = service.attendanceRecords(from, to, "late");

        assertThat(response.summary().late()).isEqualTo(1);
        assertThat(response.range().dateFrom()).isEqualTo(from);
        assertThat(response.range().dateTo()).isEqualTo(to);
        assertThat(response.range().status()).isEqualTo("late");
        assertThat(response.days()).hasSize(1);
        assertThat(response.days().get(0).status()).isEqualTo("late");
        assertThat(response.items()).containsExactly(lateRecord);
        verify(repository).findAttendanceSummary(1L, from, to, "late");
        verify(repository).findAttendanceRecords(1L, from, to, "late");
    }

    @Test
    void rejectsAttendanceRecordFilterWhenDateRangeIsReversed() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        PriorityApiService service = new PriorityApiService(
                repository,
                mock(PriorityP2Repository.class),
                mock(PriorityP3Repository.class)
        );

        assertThatThrownBy(() -> service.attendanceRecords(
                LocalDate.of(2026, 4, 30),
                LocalDate.of(2026, 4, 1),
                null
        ))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");
    }

    @Test
    void persistsAttendanceAppealForSelectedOwnedRecord() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        AttendanceRecordItem record = new AttendanceRecordItem(
                7L,
                LocalDate.of(2026, 4, 23),
                null,
                null,
                "late",
                "auto",
                true,
                null,
                null,
                null
        );
        AttendanceAppealItem stored = new AttendanceAppealItem(
                101L,
                7L,
                "status_change",
                "QR failed",
                "present",
                "requested",
                null,
                false
        );
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(repository.findAttendanceRecord(1L, 7L)).willReturn(Optional.of(record));
        given(repository.createAttendanceAppeal(7L, "status_change", "QR failed", "present")).willReturn(stored);
        PriorityApiService service = new PriorityApiService(
                repository,
                mock(PriorityP2Repository.class),
                mock(PriorityP3Repository.class)
        );

        AttendanceAppealResponse response = service.createAttendanceAppeal(
                new AttendanceAppealRequest(7L, "status_change", " QR failed ", "present")
        );

        verify(repository).createAttendanceAppeal(7L, "status_change", "QR failed", "present");
        assertThat(response.item().id()).isEqualTo(101L);
        assertThat(response.item().attendanceRecordId()).isEqualTo(7L);
        assertThat(response.item().demo()).isFalse();
    }

    @Test
    void listsOwnedAttendanceAppeals() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        AttendanceAppealItem requested = new AttendanceAppealItem(
                101L,
                7L,
                "status_change",
                "QR failed",
                "present",
                "requested",
                OffsetDateTime.parse("2026-04-25T09:00:00+09:00"),
                false
        );
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(repository.findAttendanceAppeals(1L)).willReturn(List.of(requested));
        PriorityApiService service = new PriorityApiService(
                repository,
                mock(PriorityP2Repository.class),
                mock(PriorityP3Repository.class)
        );

        AttendanceAppealsResponse response = service.attendanceAppeals();

        assertThat(response.items()).containsExactly(requested);
        verify(repository).findAttendanceAppeals(1L);
    }

    @Test
    void cancelsRequestedAttendanceAppeal() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        AttendanceAppealItem requested = new AttendanceAppealItem(
                101L,
                7L,
                "status_change",
                "QR failed",
                "present",
                "requested",
                OffsetDateTime.parse("2026-04-25T09:00:00+09:00"),
                false
        );
        AttendanceAppealItem canceled = new AttendanceAppealItem(
                101L,
                7L,
                "status_change",
                "QR failed",
                "present",
                "canceled",
                OffsetDateTime.parse("2026-04-25T09:00:00+09:00"),
                false
        );
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(repository.findAttendanceAppeal(1L, 101L)).willReturn(Optional.of(requested), Optional.of(canceled));
        given(repository.cancelAttendanceAppeal(1L, 101L)).willReturn(1);
        PriorityApiService service = new PriorityApiService(
                repository,
                mock(PriorityP2Repository.class),
                mock(PriorityP3Repository.class)
        );

        AttendanceAppealResponse response = service.cancelAttendanceAppeal(101L);

        assertThat(response.item()).isEqualTo(canceled);
        verify(repository).cancelAttendanceAppeal(1L, 101L);
    }

    @Test
    void staffApprovesAttendanceAppealAndUpdatesRecordStatus() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        AttendanceAppealItem requested = new AttendanceAppealItem(
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
        given(repository.findDefaultUser()).willReturn(Optional.of(STAFF_USER));
        given(repository.findAttendanceAppealForStaff(101L)).willReturn(Optional.of(requested), Optional.of(approved));
        given(repository.resolveAttendanceAppeal(2L, 101L, "approved", "present", "출석으로 정정했습니다.")).willReturn(1);
        PriorityApiService service = new PriorityApiService(
                repository,
                mock(PriorityP2Repository.class),
                mock(PriorityP3Repository.class)
        );

        AttendanceAppealResponse response = service.resolveAttendanceAppeal(
                101L,
                new AttendanceAppealResolveRequest("approved", null, "출석으로 정정했습니다.")
        );

        assertThat(response.item()).isEqualTo(approved);
        verify(repository).resolveAttendanceAppeal(2L, 101L, "approved", "present", "출석으로 정정했습니다.");
        verify(repository).updateAttendanceRecordStatusFromAppeal(101L, "present");
    }

    @Test
    void learnerCannotResolveAttendanceAppeal() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        PriorityApiService service = new PriorityApiService(
                repository,
                mock(PriorityP2Repository.class),
                mock(PriorityP3Repository.class)
        );

        assertThatThrownBy(() -> service.resolveAttendanceAppeal(
                101L,
                new AttendanceAppealResolveRequest("approved", "present", "OK")
        ))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403");
    }

    @Test
    void rejectsAttendanceAppealWhenRecordAlreadyHasPendingAppeal() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        AttendanceRecordItem record = new AttendanceRecordItem(
                7L,
                LocalDate.of(2026, 4, 23),
                null,
                null,
                "late",
                "auto",
                false,
                99L,
                "requested",
                null
        );
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(repository.findAttendanceRecord(1L, 7L)).willReturn(Optional.of(record));
        PriorityApiService service = new PriorityApiService(
                repository,
                mock(PriorityP2Repository.class),
                mock(PriorityP3Repository.class)
        );

        assertThatThrownBy(() -> service.createAttendanceAppeal(
                new AttendanceAppealRequest(7L, "status_change", "QR failed", "present")
        ))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("409");
    }

    @Test
    void persistsSurveyResponseWithChoiceAnswer() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        PriorityP3Repository p3Repository = mock(PriorityP3Repository.class);
        SurveyDetail survey = surveyDetail(6L);
        SurveyQuestionItem question = new SurveyQuestionItem(
                11L,
                "single_choice",
                "How was this week?",
                1,
                List.of(
                        new SurveyOptionItem(101L, "Good", 1),
                        new SurveyOptionItem(102L, "Needs support", 2)
                )
        );
        OffsetDateTime respondedAt = OffsetDateTime.now();
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(p3Repository.findSurvey(1L, 6L)).willReturn(Optional.of(survey));
        given(p3Repository.findSurveyQuestions(6L)).willReturn(List.of(question));
        given(p3Repository.saveSurveyResponse(6L, 1L))
                .willReturn(new SurveyResponsePersistence(77L, 6L, true, respondedAt));
        given(p3Repository.createSurveyAnswer(77L, 6L, 11L, null)).willReturn(88L);
        PriorityApiService service = new PriorityApiService(
                repository,
                mock(PriorityP2Repository.class),
                p3Repository
        );

        SurveyResponseSubmitResponse response = service.submitSurvey(
                6L,
                new SurveyResponseSubmitRequest(List.of(new SurveyAnswerRequest(11L, null, List.of(101L))))
        );

        assertThat(response.item().id()).isEqualTo(77L);
        assertThat(response.item().surveyId()).isEqualTo(6L);
        assertThat(response.item().answerCount()).isEqualTo(1);
        assertThat(response.item().respondedAt()).isEqualTo(respondedAt);
        assertThat(response.item().demo()).isFalse();
        verify(p3Repository).deleteSurveyAnswers(77L);
        verify(p3Repository).createSurveyAnswerOptions(88L, 11L, List.of(101L));
    }

    @Test
    void rejectsSurveyResponseWithInvalidOption() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        PriorityP3Repository p3Repository = mock(PriorityP3Repository.class);
        SurveyQuestionItem question = new SurveyQuestionItem(
                11L,
                "single_choice",
                "How was this week?",
                1,
                List.of(new SurveyOptionItem(101L, "Good", 1))
        );
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(p3Repository.findSurvey(1L, 6L)).willReturn(Optional.of(surveyDetail(6L)));
        given(p3Repository.findSurveyQuestions(6L)).willReturn(List.of(question));
        PriorityApiService service = new PriorityApiService(
                repository,
                mock(PriorityP2Repository.class),
                p3Repository
        );

        assertThatThrownBy(() -> service.submitSurvey(
                6L,
                new SurveyResponseSubmitRequest(List.of(new SurveyAnswerRequest(11L, null, List.of(999L))))
        ))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");
    }

    @Test
    void returnsPersistedSurveyResponseAnswers() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        PriorityP3Repository p3Repository = mock(PriorityP3Repository.class);
        OffsetDateTime respondedAt = OffsetDateTime.now();
        List<SurveySavedAnswerItem> answers = List.of(
                new SurveySavedAnswerItem(11L, null, List.of(101L)),
                new SurveySavedAnswerItem(12L, "좋았습니다", List.of())
        );
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(p3Repository.findSurvey(1L, 6L)).willReturn(Optional.of(surveyDetail(6L)));
        given(p3Repository.findSurveyResponse(1L, 6L))
                .willReturn(Optional.of(new SurveyResponsePersistence(77L, 6L, true, respondedAt)));
        given(p3Repository.findSurveyResponseAnswers(77L)).willReturn(answers);
        PriorityApiService service = new PriorityApiService(
                repository,
                mock(PriorityP2Repository.class),
                p3Repository
        );

        SurveyResponseDetailResponse response = service.surveyResponse(6L);

        assertThat(response.item().id()).isEqualTo(77L);
        assertThat(response.item().surveyId()).isEqualTo(6L);
        assertThat(response.item().completed()).isTrue();
        assertThat(response.item().respondedAt()).isEqualTo(respondedAt);
        assertThat(response.item().answers()).containsExactlyElementsOf(answers);
        assertThat(response.item().demo()).isFalse();
        verify(p3Repository).findSurveyResponseAnswers(77L);
    }

    @Test
    void staffCreatesSurveyWithQuestionOptions() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        PriorityP3Repository p3Repository = mock(PriorityP3Repository.class);
        SurveyQuestionItem savedQuestion = new SurveyQuestionItem(
                11L,
                "single_choice",
                "이번 주 과정은 어땠나요?",
                1,
                List.of(new SurveyOptionItem(101L, "좋음", 1), new SurveyOptionItem(102L, "보통", 2))
        );
        SurveyDetail savedSurvey = new SurveyDetail(
                6L,
                "Weekly pulse",
                "satisfaction",
                true,
                null,
                null,
                "in_progress",
                false,
                1,
                List.of()
        );
        given(repository.findDefaultUser()).willReturn(Optional.of(STAFF_USER));
        given(p3Repository.findDefaultContentScopeId()).willReturn(Optional.of(42L));
        given(p3Repository.createSurvey(42L, "Weekly pulse", "satisfaction", true, null, null, "in_progress"))
                .willReturn(6L);
        given(p3Repository.createSurveyQuestion(6L, "single_choice", "이번 주 과정은 어땠나요?", 1))
                .willReturn(11L);
        given(p3Repository.findSurvey(2L, 6L)).willReturn(Optional.of(savedSurvey));
        given(p3Repository.findSurveyQuestions(6L)).willReturn(List.of(savedQuestion));
        PriorityApiService service = new PriorityApiService(
                repository,
                mock(PriorityP2Repository.class),
                p3Repository
        );

        SurveyDetailResponse response = service.createSurvey(new SurveyCreateRequest(
                " Weekly pulse ",
                "satisfaction",
                true,
                null,
                null,
                "in_progress",
                List.of(new SurveyQuestionCreateRequest(
                        "single_choice",
                        " 이번 주 과정은 어땠나요? ",
                        List.of(new SurveyOptionCreateRequest("좋음"), new SurveyOptionCreateRequest("보통"))
                ))
        ));

        assertThat(response.item().id()).isEqualTo(6L);
        assertThat(response.item().questions()).containsExactly(savedQuestion);
        verify(p3Repository).createSurveyOption(11L, "좋음", 1);
        verify(p3Repository).createSurveyOption(11L, "보통", 2);
    }


    @Test
    void staffUpdatesAndDeletesSurvey() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        PriorityP3Repository p3Repository = mock(PriorityP3Repository.class);
        SurveyDetail existingSurvey = surveyDetail(6L);
        SurveyDetail savedSurvey = new SurveyDetail(
                6L,
                "Updated pulse",
                "course",
                false,
                null,
                null,
                "scheduled",
                false,
                1,
                List.of()
        );
        SurveyQuestionItem savedQuestion = new SurveyQuestionItem(
                21L,
                "long_text",
                "개선 의견을 적어 주세요.",
                1,
                List.of()
        );
        given(repository.findDefaultUser()).willReturn(Optional.of(STAFF_USER));
        given(p3Repository.findSurvey(2L, 6L)).willReturn(Optional.of(existingSurvey), Optional.of(savedSurvey), Optional.of(savedSurvey));
        given(p3Repository.updateSurvey(6L, "Updated pulse", "course", false, null, null, "scheduled")).willReturn(1);
        given(p3Repository.createSurveyQuestion(6L, "long_text", "개선 의견을 적어 주세요.", 1)).willReturn(21L);
        given(p3Repository.findSurveyQuestions(6L)).willReturn(List.of(savedQuestion));
        given(p3Repository.deleteSurvey(6L)).willReturn(1);
        PriorityApiService service = new PriorityApiService(
                repository,
                mock(PriorityP2Repository.class),
                p3Repository
        );

        SurveyDetailResponse updated = service.updateSurvey(6L, new SurveyCreateRequest(
                " Updated pulse ",
                "course",
                false,
                null,
                null,
                "scheduled",
                List.of(new SurveyQuestionCreateRequest("long_text", " 개선 의견을 적어 주세요. ", List.of()))
        ));
        SurveyDeleteResponse deleted = service.deleteSurvey(6L);

        assertThat(updated.item().title()).isEqualTo("Updated pulse");
        assertThat(updated.item().questions()).containsExactly(savedQuestion);
        assertThat(deleted.item().id()).isEqualTo(6L);
        assertThat(deleted.item().deleted()).isTrue();
        assertThat(deleted.item().demo()).isFalse();
        verify(p3Repository).deleteSurveyResponses(6L);
        verify(p3Repository).deleteSurveyQuestions(6L);
        verify(p3Repository).deleteSurvey(6L);
    }

    @Test
    void learnerCannotCreateSurvey() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        PriorityP3Repository p3Repository = mock(PriorityP3Repository.class);
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        PriorityApiService service = new PriorityApiService(
                repository,
                mock(PriorityP2Repository.class),
                p3Repository
        );

        assertThatThrownBy(() -> service.createSurvey(new SurveyCreateRequest(
                "Weekly pulse",
                "satisfaction",
                true,
                null,
                null,
                "in_progress",
                List.of(new SurveyQuestionCreateRequest("long_text", "의견을 적어 주세요.", List.of()))
        )))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403");
        verify(p3Repository, never()).findDefaultContentScopeId();
    }

    @Test
    void persistsSupportTicketWithInitialUserMessage() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        PriorityP2Repository p2Repository = mock(PriorityP2Repository.class);
        SupportTicketItem stored = new SupportTicketItem(
                55L,
                "Need help",
                "open",
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                null,
                1L,
                OffsetDateTime.now()
        );
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(p2Repository.createSupportTicket(1L, "Need help")).willReturn(55L);
        given(p2Repository.createSupportTicketMessage(55L, 1L, "Please check this.")).willReturn(66L);
        given(p2Repository.findSupportTicket(1L, 55L)).willReturn(Optional.of(stored));
        PriorityApiService service = new PriorityApiService(
                repository,
                p2Repository,
                mock(PriorityP3Repository.class)
        );

        SupportTicketCreateResponse response = service.createSupportTicket(
                new SupportTicketCreateRequest(" Need help ", " Please check this. ")
        );

        assertThat(response.item().id()).isEqualTo(55L);
        assertThat(response.item().status()).isEqualTo("open");
        assertThat(response.item().messageCount()).isEqualTo(1L);
        verify(p2Repository).createSupportTicket(1L, "Need help");
        verify(p2Repository).createSupportTicketMessage(55L, 1L, "Please check this.");
    }

    @Test
    void loadsSupportTicketDetailWithMessages() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        PriorityP2Repository p2Repository = mock(PriorityP2Repository.class);
        SupportTicketItem ticket = new SupportTicketItem(
                55L,
                "Need help",
                "open",
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                null,
                1L,
                OffsetDateTime.now()
        );
        SupportTicketMessageItem message = new SupportTicketMessageItem(
                66L,
                55L,
                1L,
                "Demo Student",
                "user_message",
                "Please check this.",
                OffsetDateTime.now(),
                List.of()
        );
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(p2Repository.findSupportTicket(1L, 55L)).willReturn(Optional.of(ticket));
        given(p2Repository.findSupportTicketMessages(1L, 55L)).willReturn(List.of(message));
        given(p2Repository.findSupportTicketMessageAttachments(List.of(66L))).willReturn(List.of());
        PriorityApiService service = new PriorityApiService(
                repository,
                p2Repository,
                mock(PriorityP3Repository.class)
        );

        SupportTicketDetailResponse response = service.supportTicket(55L);

        assertThat(response.item().id()).isEqualTo(55L);
        assertThat(response.item().messages()).containsExactly(message);
    }

    @Test
    void persistsSupportTicketFollowUpMessage() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        PriorityP2Repository p2Repository = mock(PriorityP2Repository.class);
        OffsetDateTime now = OffsetDateTime.now();
        SupportTicketItem existing = new SupportTicketItem(
                55L,
                "Need help",
                "answered",
                now,
                now,
                null,
                1L,
                now
        );
        SupportTicketItem updated = new SupportTicketItem(
                55L,
                "Need help",
                "open",
                now,
                now,
                null,
                2L,
                now
        );
        SupportTicketMessageItem storedMessage = new SupportTicketMessageItem(
                67L,
                55L,
                1L,
                "Demo Student",
                "user_message",
                "More context.",
                now,
                List.of()
        );
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(p2Repository.findSupportTicket(1L, 55L)).willReturn(Optional.of(existing), Optional.of(updated));
        given(p2Repository.createSupportTicketMessage(55L, 1L, "user_message", "More context.")).willReturn(67L);
        given(p2Repository.findSupportTicketMessage(1L, 55L, 67L)).willReturn(Optional.of(storedMessage));
        PriorityApiService service = new PriorityApiService(
                repository,
                p2Repository,
                mock(PriorityP3Repository.class)
        );

        SupportTicketMessageCreateResponse response = service.createSupportTicketMessage(
                55L,
                new SupportTicketMessageRequest(" More context. ")
        );

        assertThat(response.item()).isEqualTo(storedMessage);
        assertThat(response.ticket().messageCount()).isEqualTo(2L);
        assertThat(response.ticket().status()).isEqualTo("open");
        verify(p2Repository).markSupportTicketOpen(55L);
    }

    @Test
    void persistsSupportTicketMessageAttachmentMetadata() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        PriorityP2Repository p2Repository = mock(PriorityP2Repository.class);
        OffsetDateTime now = OffsetDateTime.now();
        SupportTicketMessageItem message = new SupportTicketMessageItem(
                67L,
                55L,
                1L,
                "Demo Student",
                "user_message",
                "More context.",
                now,
                List.of()
        );
        SupportTicketAttachmentItem attachment = new SupportTicketAttachmentItem(
                77L,
                67L,
                "error.png",
                "support/tickets/55/messages/67/2cf24dba5fb0-error.png",
                "/support/tickets/55/messages/67/attachments/2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824",
                "image/png",
                5L,
                "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824",
                now
        );
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(p2Repository.findSupportTicketMessage(1L, 55L, 67L)).willReturn(Optional.of(message));
        given(p2Repository.createOrFindAttachment(
                "error.png",
                "support/tickets/55/messages/67/2cf24dba5fb0-error.png",
                "/support/tickets/55/messages/67/attachments/2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824",
                "image/png",
                5L,
                "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824"
        )).willReturn(77L);
        given(p2Repository.findSupportTicketMessageAttachment(67L, 77L)).willReturn(Optional.of(attachment));
        given(p2Repository.findSupportTicketMessageAttachments(List.of(67L))).willReturn(List.of(attachment));
        PriorityApiService service = new PriorityApiService(
                repository,
                p2Repository,
                mock(PriorityP3Repository.class)
        );

        SupportTicketAttachmentCreateResponse response = service.createSupportTicketMessageAttachment(
                55L,
                67L,
                new SupportTicketAttachmentRequest(" error.png ", "image/png", "aGVsbG8=")
        );

        assertThat(response.item()).isEqualTo(attachment);
        assertThat(response.message().attachments()).containsExactly(attachment);
        SupportTicketAttachmentDownload download = service.downloadSupportTicketMessageAttachment(55L, 67L, 77L);
        assertThat(download.item()).isEqualTo(attachment);
        assertThat(download.content()).isEqualTo("hello".getBytes(StandardCharsets.UTF_8));
        verify(p2Repository).linkSupportTicketMessageAttachment(67L, 77L);
    }

    @Test
    void persistsSupportTicketAnswerAsAdminReply() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        PriorityP2Repository p2Repository = mock(PriorityP2Repository.class);
        OffsetDateTime now = OffsetDateTime.now();
        SupportTicketItem existing = new SupportTicketItem(
                55L,
                "Need help",
                "open",
                now,
                now,
                null,
                1L,
                now
        );
        SupportTicketItem answered = new SupportTicketItem(
                55L,
                "Need help",
                "answered",
                now,
                now,
                null,
                2L,
                now
        );
        SupportTicketMessageItem storedAnswer = new SupportTicketMessageItem(
                68L,
                55L,
                2L,
                "Demo Manager",
                "admin_reply",
                "We checked it.",
                now,
                List.of()
        );
        given(repository.findDefaultUser()).willReturn(Optional.of(STAFF_USER));
        given(p2Repository.findSupportTicketForStaff(55L)).willReturn(Optional.of(existing), Optional.of(answered));
        given(p2Repository.createSupportTicketMessage(55L, 2L, "admin_reply", "We checked it.")).willReturn(68L);
        given(p2Repository.findSupportTicketMessageForStaff(55L, 68L)).willReturn(Optional.of(storedAnswer));
        PriorityApiService service = new PriorityApiService(
                repository,
                p2Repository,
                mock(PriorityP3Repository.class)
        );

        SupportTicketMessageCreateResponse response = service.createSupportTicketAnswer(
                55L,
                new SupportTicketMessageRequest(" We checked it. ")
        );

        assertThat(response.item()).isEqualTo(storedAnswer);
        assertThat(response.ticket().status()).isEqualTo("answered");
        verify(p2Repository).markSupportTicketAnswered(55L);
    }

    @Test
    void rejectsSupportTicketAnswerForLearner() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        PriorityP2Repository p2Repository = mock(PriorityP2Repository.class);
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        PriorityApiService service = new PriorityApiService(
                repository,
                p2Repository,
                mock(PriorityP3Repository.class)
        );

        assertThatThrownBy(() -> service.createSupportTicketAnswer(
                55L,
                new SupportTicketMessageRequest("Learner cannot answer.")
        ))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403");
    }

    private SurveyDetail surveyDetail(long id) {
        return new SurveyDetail(
                id,
                "Weekly satisfaction survey",
                "satisfaction",
                true,
                null,
                null,
                "in_progress",
                false,
                1,
                List.of()
        );
    }

    private CurriculumScheduleRow curriculumRow(
            long id,
            String semester,
            int weekNumber,
            String track,
            String classDate,
            String startTime,
            String endTime,
            String sessionType,
            String title
    ) {
        return new CurriculumScheduleRow(
                id,
                1L,
                semester,
                10L,
                weekNumber,
                track,
                LocalDate.parse(classDate),
                LocalTime.parse(startTime),
                LocalTime.parse(endTime),
                sessionType,
                title,
                "Demo Instructor",
                "Seoul 1"
        );
    }

}
