package com.edussafy.backend.priority.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceAppealItem;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceAppealRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceAppealResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceRecordItem;
import com.edussafy.backend.priority.dto.PriorityDtos.ClassmateNotificationRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.ClassmateNotificationResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.LoginRequest;
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
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyResponseSubmitRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyResponseSubmitResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.UserProfile;
import com.edussafy.backend.priority.repository.PriorityApiRepository;
import com.edussafy.backend.priority.repository.PriorityP2Repository;
import com.edussafy.backend.priority.repository.PriorityP3Repository;
import com.edussafy.backend.priority.repository.PriorityP3Repository.SurveyResponsePersistence;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

class PriorityApiServiceTest {

    private static final UserProfile USER = new UserProfile(
            1L, "Demo Student", "student@ssafy.com", "learner", "Seoul", "12th", "Java"
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
        PriorityApiService service = new PriorityApiService(
                mock(PriorityApiRepository.class),
                mock(PriorityP2Repository.class),
                mock(PriorityP3Repository.class)
        );

        ClassmateNotificationResponse response = service.createClassmateNotification(
                7L,
                new ClassmateNotificationRequest(null, null)
        );

        assertThat(response.item().id()).isEqualTo(900_007L);
        assertThat(response.item().recipientUserId()).isEqualTo(7L);
        assertThat(response.item().type()).isEqualTo("contact_request");
        assertThat(response.item().message()).isEqualTo("Let's study together!");
        assertThat(response.item().status()).isEqualTo("sent");
        assertThat(response.item().notification()).isNotNull();
        assertThat(response.item().notification().id()).isEqualTo(900_007L);
        assertThat(response.item().notification().body()).isEqualTo("Let's study together!");
        assertThat(response.item().demo()).isTrue();
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

            assertThat(request.getSession(false).getAttribute("edussafy.currentUserId")).isEqualTo(2L);
            assertThat(service.me().user().email()).isEqualTo("manager@ssafy.com");
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
    void persistsProfileUpdateAndReturnsStoredProfile() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        PriorityP2Repository p2Repository = mock(PriorityP2Repository.class);
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
        given(repository.findDefaultUser()).willReturn(Optional.of(USER));
        given(p2Repository.findProfile(1L)).willReturn(Optional.of(current));
        given(p2Repository.updateProfile(1L, profileRequest, true)).willReturn(Optional.of(updated));
        PriorityApiService service = new PriorityApiService(
                repository,
                p2Repository,
                mock(PriorityP3Repository.class)
        );

        ProfileResponse response = service.updateProfile(profileRequest);

        verify(p2Repository).updateProfile(1L, profileRequest, true);
        assertThat(response.profile().name()).isEqualTo("Updated Student");
        assertThat(response.profile().zipCode()).isEqualTo("06234");
        assertThat(response.profile().marketingOptIn()).isTrue();
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
