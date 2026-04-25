package com.edussafy.backend.priority.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceAppealItem;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceAppealRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceAppealResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceAppealsResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceRecordItem;
import com.edussafy.backend.priority.dto.PriorityDtos.AuthSessionResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ClassmateNotificationRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.ClassmateNotificationResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ClassmateItem;
import com.edussafy.backend.priority.dto.PriorityDtos.LoginRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialItem;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialResourceItem;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialViewResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.NotificationDeleteResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.NotificationItem;
import com.edussafy.backend.priority.dto.PriorityDtos.NotificationReadResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.NotificationsReadAllResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.PasswordCheckRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.PasswordCheckResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ProfileDetails;
import com.edussafy.backend.priority.dto.PriorityDtos.ProfileResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ProfileUpdateRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.RoleAccessResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyAnswerRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyDetail;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyOptionItem;
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
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
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
                List.of()
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
        given(p3Repository.findMaterial(15L)).willReturn(Optional.of(updated));
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
    void checksProfilePasswordAgainstStoredNoopHash() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(repository.findPasswordHash(1L)).willReturn(Optional.of("{noop}password"));
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
        given(repository.findPasswordHash(2L)).willReturn(Optional.of("{noop}password"));
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
        given(repository.findPasswordHash(1L)).willReturn(Optional.of("{noop}password"));
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
        given(repository.findPasswordHash(1L)).willReturn(Optional.of("{noop}password"));
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

}
