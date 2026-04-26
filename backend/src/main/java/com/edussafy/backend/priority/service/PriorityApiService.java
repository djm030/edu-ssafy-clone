package com.edussafy.backend.priority.service;

import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceAppealItem;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceAppealRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceAppealResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceAppealResolveRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceAppealsResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceRecordItem;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceRecordsResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceSummary;
import com.edussafy.backend.priority.dto.PriorityDtos.AccessPolicyItem;
import com.edussafy.backend.priority.dto.PriorityDtos.AccessPolicyResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.AuthActionResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.AuthSessionResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ClassmateNotificationItem;
import com.edussafy.backend.priority.dto.PriorityDtos.ClassmateNotificationRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.ClassmateNotificationResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ClassmateItem;
import com.edussafy.backend.priority.dto.PriorityDtos.ClassmatesResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.CurriculumResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.DashboardSummary;
import com.edussafy.backend.priority.dto.PriorityDtos.ElearningLessonItem;
import com.edussafy.backend.priority.dto.PriorityDtos.ElearningProgressDetail;
import com.edussafy.backend.priority.dto.PriorityDtos.ElearningProgressDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ElearningProgressResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ElearningResumeItem;
import com.edussafy.backend.priority.dto.PriorityDtos.ElearningResumeResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.LevelSummary;
import com.edussafy.backend.priority.dto.PriorityDtos.LoginRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialItem;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialResourceAttachmentCreateResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialResourceAttachmentDownload;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialResourceAttachmentItem;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialResourceAttachmentRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialResourceItem;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialResourcesResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialReactionResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialViewResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialsResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.NotificationDeleteResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.NotificationItem;
import com.edussafy.backend.priority.dto.PriorityDtos.NotificationReadResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.NotificationsReadAllResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.NotificationsResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.NotificationsSummary;
import com.edussafy.backend.priority.dto.PriorityDtos.PageMeta;
import com.edussafy.backend.priority.dto.PriorityDtos.PasswordCheckRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.PasswordCheckResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ProfileDetails;
import com.edussafy.backend.priority.dto.PriorityDtos.ProfileEditAuthorizationResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ProfilePasswordChangeRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.ProfileResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ProfileUpdateRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.RoleAccessResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestItem;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestSubmissionAttachmentCreateResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestSubmissionAttachmentDownload;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestSubmissionAttachmentItem;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestSubmissionAttachmentRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestSubmissionDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestSubmissionItem;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestSubmissionRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestSubmissionResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestsResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ReplayResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketCreateRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketCreateResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketAttachmentCreateResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketAttachmentDownload;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketAttachmentItem;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketAttachmentRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketDetail;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketItem;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketMessageCreateResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketMessageItem;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketMessageRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketsResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyAnswerRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyCreateRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyDeleteItem;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyDeleteResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyDetail;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyItem;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyOptionCreateRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyQuestionCreateRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyQuestionItem;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyResponseDetail;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyResponseDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyResponseSubmitItem;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyResponseSubmitRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyResponseSubmitResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveysResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.TodaySummary;
import com.edussafy.backend.priority.dto.PriorityDtos.UserProfile;
import com.edussafy.backend.priority.dto.PriorityDtos.UserResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.UserSummary;
import com.edussafy.backend.priority.repository.PriorityApiRepository;
import com.edussafy.backend.priority.repository.PriorityP2Repository;
import com.edussafy.backend.priority.repository.PriorityP3Repository;
import com.edussafy.backend.priority.repository.PriorityP3Repository.SurveyResponsePersistence;
import com.edussafy.backend.priority.security.AuthSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PriorityApiService {

    private static final UserProfile DEMO_USER = new UserProfile(
            1L, "Demo Learner", "student@ssafy.com", "learner", "Seoul", "12", "Java"
    );
    private static final LevelSummary DEMO_LEVEL = new LevelSummary(1, 0, 1000, 0, null);
    private static final AttendanceSummary EMPTY_ATTENDANCE = new AttendanceSummary(0, 0, 0, true);
    private static final TodaySummary EMPTY_TODAY = new TodaySummary(null, null, null);
    private static final String DEFAULT_CLASSMATE_NOTIFICATION_TYPE = "contact_request";
    private static final String DEFAULT_CLASSMATE_NOTIFICATION_MESSAGE = "Let's study together!";
    private static final Set<String> ATTENDANCE_APPEAL_TYPES = Set.of("check_in", "check_out", "status_change", "other");
    private static final Set<String> ATTENDANCE_STATUSES = Set.of("present", "late", "absent", "early_leave", "excused");
    private static final Set<String> ATTENDANCE_APPEAL_DECISIONS = Set.of("approved", "rejected");
    private static final Set<String> CLOSED_ATTENDANCE_APPEAL_STATUSES = Set.of("rejected", "canceled");
    private static final Set<String> SURVEY_CATEGORIES = Set.of("satisfaction", "course", "lecture", "etc");
    private static final Set<String> SURVEY_STATUSES = Set.of("draft", "scheduled", "in_progress", "completed", "closed", "archived");
    private static final Set<String> ELEARNING_PROGRESS_STATUSES = Set.of("not_started", "in_progress", "completed");
    private static final Set<String> SURVEY_QUESTION_TYPES = Set.of(
            "single_choice", "multiple_choice", "short_text", "long_text", "score"
    );
    private static final Set<String> SURVEY_CHOICE_TYPES = Set.of("single_choice", "multiple_choice");
    private static final int SUPPORT_ATTACHMENT_MAX_BYTES = 2 * 1024 * 1024;
    private static final Map<String, List<String>> ROLE_PERMISSIONS = Map.of(
            "learner", List.of(
                    "dashboard:read",
                    "attendance:read",
                    "attendance:appeal",
                    "notifications:read",
                    "learning:read",
                    "quest:submit",
                    "survey:respond",
                    "board:write",
                    "support:write",
                    "profile:update"
            ),
            "coach", List.of(
                    "dashboard:read",
                    "attendance:read",
                    "attendance:resolve",
                    "notifications:send",
                    "learning:manage",
                    "quest:review",
                    "survey:manage",
                    "board:moderate",
                    "support:answer",
                    "profile:update"
            ),
            "admin", List.of("*")
    );
    private static final Map<String, List<String>> ROLE_DENIED_ROUTES = Map.of(
            "learner", List.of("/admin", "/coach/reviews", "/management"),
            "coach", List.of("/admin"),
            "admin", List.of()
    );
    private static final List<AccessPolicyItem> ACCESS_POLICY = List.of(
            new AccessPolicyItem(
                    "attendance-appeal-resolve",
                    "PATCH",
                    "/api/attendance/appeals/{appealId}/resolve",
                    List.of("coach", "admin"),
                    "출석 이의신청",
                    "출석 이의신청 처리와 출석 상태 정정은 staff 역할 이상만 수행한다."
            ),
            new AccessPolicyItem(
                    "attendance-appeal-pending",
                    "GET",
                    "/api/attendance/appeals/pending",
                    List.of("coach", "admin"),
                    "출석 이의신청",
                    "처리 대기 이의신청 목록은 coach/admin 운영자에게만 노출한다."
            ),
            new AccessPolicyItem(
                    "survey-manage",
                    "POST|PUT|DELETE",
                    "/api/surveys",
                    List.of("coach", "admin"),
                    "설문",
                    "설문 생성, 수정, 삭제는 coach/admin만 가능하고 learner는 응답 제출만 가능하다."
            ),
            new AccessPolicyItem(
                    "classmate-notification-send",
                    "POST",
                    "/api/community/classmates/{userId}/notifications",
                    List.of("coach", "admin"),
                    "알림",
                    "동료 학습자에게 운영 알림을 발송하는 기능은 staff 역할 이상으로 제한한다."
            ),
            new AccessPolicyItem(
                    "learning-material-attachment",
                    "POST",
                    "/api/learning/materials/{id}/resources/{resourceId}/attachments",
                    List.of("coach", "admin"),
                    "학습자료",
                    "학습자료 첨부파일 업로드는 콘텐츠 운영 권한이 있는 coach/admin만 가능하다."
            ),
            new AccessPolicyItem(
                    "support-answer",
                    "POST",
                    "/api/support/tickets/{ticketId}/answers",
                    List.of("coach", "admin"),
                    "1:1 문의",
                    "문의 답변 등록은 지원 담당 staff 역할 이상으로 제한한다."
            ),
            new AccessPolicyItem(
                    "admin-campus-structure",
                    "GET|POST",
                    "/api/admin/campus-structure/**",
                    List.of("admin"),
                    "관리",
                    "캠퍼스, 기수, 트랙, 반 구조 조회/변경은 admin만 가능하다."
            )
    );

    private final PriorityApiRepository repository;
    private final PriorityP2Repository p2Repository;
    private final PriorityP3Repository p3Repository;

    @Value("${edussafy.auth.password.allow-noop:false}")
    private boolean allowNoopPasswordHash;

    public PriorityApiService(
            PriorityApiRepository repository,
            PriorityP2Repository p2Repository,
            PriorityP3Repository p3Repository
    ) {
        this.repository = repository;
        this.p2Repository = p2Repository;
        this.p3Repository = p3Repository;
    }

    public UserResponse login(LoginRequest request) {
        Optional<UserProfile> persistedUser = safe(() -> repository.findUserByEmail(request.email()), Optional.empty());
        if (persistedUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
        }

        UserProfile user = persistedUser.get();
        String storedPasswordHash = safe(() -> repository.findPasswordHash(user.id()).orElse(null), null);

        if (!passwordMatches(request.password(), storedPasswordHash)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
        }

        storeCurrentUserId(user.id());
        return new UserResponse(user);
    }

    public UserResponse me() {
        return new UserResponse(currentUser());
    }

    public PasswordCheckResponse passwordCheck(PasswordCheckRequest request) {
        UserProfile user = currentUser();
        String storedHash = safe(() -> repository.findPasswordHash(user.id()).orElse(null), null);
        boolean valid = passwordMatches(request.password(), storedHash);
        if (valid) {
            markProfileVerified();
        }
        return new PasswordCheckResponse(valid);
    }

    public ProfileEditAuthorizationResponse profileEditAuthorization() {
        HttpSession session = currentSession(false);
        if (session == null || AuthSession.currentUserId(session).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        Instant now = Instant.now();
        Optional<Instant> verifiedUntil = AuthSession.profileVerifiedUntil(session)
                .filter(expiresAt -> expiresAt.isAfter(now));
        return new ProfileEditAuthorizationResponse(
                verifiedUntil.isPresent(),
                verifiedUntil.map(expiresAt -> OffsetDateTime.ofInstant(expiresAt, ZoneOffset.UTC)).orElse(null),
                AuthSession.PROFILE_VERIFICATION_TTL_SECONDS
        );
    }

    public RoleAccessResponse currentRoleAccess() {
        UserProfile user = currentUser();
        String role = normalizeAccessRole(user.role());
        return new RoleAccessResponse(
                role,
                ROLE_PERMISSIONS.getOrDefault(role, ROLE_PERMISSIONS.get("learner")),
                ROLE_DENIED_ROUTES.getOrDefault(role, ROLE_DENIED_ROUTES.get("learner"))
        );
    }

    public AccessPolicyResponse accessPolicy() {
        currentUser();
        return new AccessPolicyResponse(ACCESS_POLICY);
    }

    public AuthSessionResponse authSession() {
        HttpSession session = currentSession(false);
        if (session == null || AuthSession.currentUserId(session).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        int maxInactiveSeconds = session.getMaxInactiveInterval();
        Instant expiresAt = Instant.ofEpochMilli(session.getLastAccessedTime()).plusSeconds(maxInactiveSeconds);
        long secondsRemaining = Math.max(0, (expiresAt.toEpochMilli() - System.currentTimeMillis()) / 1000);
        return new AuthSessionResponse(
                true,
                OffsetDateTime.ofInstant(expiresAt, ZoneOffset.UTC),
                maxInactiveSeconds,
                secondsRemaining
        );
    }

    public AuthActionResponse logout() {
        clearCurrentSession();
        return new AuthActionResponse(true, "Logged out.");
    }

    public DashboardSummary dashboardSummary() {
        UserProfile user = currentUser();
        LevelSummary level = safe(() -> repository.findLevel(user.id()).orElse(DEMO_LEVEL), DEMO_LEVEL);
        AttendanceSummary attendance = safe(() -> repository.findAttendanceSummary(user.id()), EMPTY_ATTENDANCE);
        List<NotificationItem> latest = safe(() -> repository.findNotifications(user.id(), 5, 0), List.of());
        long unreadCount = safe(() -> repository.countUnreadNotifications(user.id()), 0L);

        return new DashboardSummary(
                new UserSummary(user.name(), user.campusName(), user.cohortName(), user.trackName()),
                level,
                attendance,
                new NotificationsSummary(unreadCount, latest),
                safe(() -> repository.findTodaySummary(user.id()), EMPTY_TODAY)
        );
    }

    public AttendanceRecordsResponse attendanceRecords() {
        return attendanceRecords(null, null, null);
    }

    public AttendanceRecordsResponse attendanceRecords(LocalDate dateFrom, LocalDate dateTo, String status) {
        UserProfile user = currentUser();
        String normalizedStatus = normalizeNullable(status);
        if (normalizedStatus != null) {
            normalizedStatus = normalizeAttendanceStatus(normalizedStatus, normalizedStatus);
        }
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Attendance dateFrom must be before or equal to dateTo.");
        }

        final String filterStatus = normalizedStatus;
        AttendanceSummary summary = safe(
                () -> repository.findAttendanceSummary(user.id(), dateFrom, dateTo, filterStatus),
                EMPTY_ATTENDANCE
        );
        return new AttendanceRecordsResponse(
                summary,
                safe(() -> repository.findAttendanceRecords(user.id(), dateFrom, dateTo, filterStatus), List.of())
        );
    }

    public AttendanceAppealsResponse attendanceAppeals() {
        UserProfile user = currentUser();
        return new AttendanceAppealsResponse(safe(() -> repository.findAttendanceAppeals(user.id()), List.of()));
    }

    public AttendanceAppealsResponse pendingAttendanceAppeals() {
        UserProfile user = currentUser();
        if (!canResolveAttendanceAppeals(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Attendance appeal resolution permission is required.");
        }
        return new AttendanceAppealsResponse(safe(repository::findPendingAttendanceAppealsForStaff, List.of()));
    }

    @Transactional
    public AttendanceAppealResponse createAttendanceAppeal(AttendanceAppealRequest request) {
        UserProfile user = currentUser();
        AttendanceRecordItem record = repository.findAttendanceRecord(user.id(), request.attendanceRecordId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attendance record not found."));
        if (!isAttendanceAppealAvailable(record)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Attendance appeal is already pending or resolved.");
        }

        String type = normalizeAttendanceAppealType(request.type());
        String reason = request.reason().trim();
        String requestedStatus = normalizeAttendanceStatus(request.requestedStatus(), record.status());

        return new AttendanceAppealResponse(repository.createAttendanceAppeal(record.id(), type, reason, requestedStatus));
    }

    @Transactional
    public AttendanceAppealResponse cancelAttendanceAppeal(long attendanceAppealId) {
        UserProfile user = currentUser();
        AttendanceAppealItem existing = repository.findAttendanceAppeal(user.id(), attendanceAppealId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attendance appeal not found."));
        if (!"requested".equals(existing.status())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only requested attendance appeals can be canceled.");
        }

        int updated = repository.cancelAttendanceAppeal(user.id(), attendanceAppealId);
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Attendance appeal could not be canceled.");
        }

        AttendanceAppealItem canceled = repository.findAttendanceAppeal(user.id(), attendanceAppealId)
                .orElse(new AttendanceAppealItem(
                        existing.id(),
                        existing.attendanceRecordId(),
                        existing.type(),
                        existing.reason(),
                        existing.requestedStatus(),
                        "canceled",
                        existing.requestedAt(),
                        false
        ));
        return new AttendanceAppealResponse(canceled);
    }

    @Transactional
    public AttendanceAppealResponse resolveAttendanceAppeal(
            long attendanceAppealId,
            AttendanceAppealResolveRequest request
    ) {
        UserProfile user = currentUser();
        if (!canResolveAttendanceAppeals(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Attendance appeal resolution permission is required.");
        }

        AttendanceAppealItem existing = repository.findAttendanceAppealForStaff(attendanceAppealId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attendance appeal not found."));
        if (!"requested".equals(existing.status())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only requested attendance appeals can be resolved.");
        }

        String decision = normalizeAttendanceAppealDecision(request.status());
        String resolvedStatus = null;
        if ("approved".equals(decision)) {
            String requestedStatus = normalizeNullable(request.resolvedStatus());
            if (requestedStatus == null) {
                requestedStatus = normalizeNullable(existing.requestedStatus());
            }
            if (requestedStatus == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Approved attendance appeal requires a resolved status.");
            }
            resolvedStatus = normalizeAttendanceStatus(requestedStatus, requestedStatus);
        }
        String comment = normalizeWithDefault(
                request.comment(),
                "approved".equals(decision) ? "Attendance appeal approved." : "Attendance appeal rejected."
        );

        int updated = repository.resolveAttendanceAppeal(user.id(), attendanceAppealId, decision, resolvedStatus, comment);
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Attendance appeal could not be resolved.");
        }
        if ("approved".equals(decision)) {
            repository.updateAttendanceRecordStatusFromAppeal(attendanceAppealId, resolvedStatus);
        }

        AttendanceAppealItem resolved = repository.findAttendanceAppealForStaff(attendanceAppealId)
                .orElse(new AttendanceAppealItem(
                        existing.id(),
                        existing.attendanceRecordId(),
                        existing.type(),
                        existing.reason(),
                        existing.requestedStatus(),
                        decision,
                        existing.requestedAt(),
                        existing.recordDate(),
                        resolvedStatus,
                        OffsetDateTime.now(ZoneOffset.ofHours(9)),
                        comment,
                        user.name(),
                        false
                ));
        return new AttendanceAppealResponse(resolved);
    }

    public NotificationsResponse notifications(int page, int size) {
        UserProfile user = currentUser();
        long total = safe(() -> repository.countNotifications(user.id()), 0L);
        List<NotificationItem> items = total == 0
                ? List.of()
                : safe(() -> repository.findNotifications(user.id(), size, offset(page, size)), List.of());
        return new NotificationsResponse(items, pageMeta(page, size, total));
    }

    @Transactional
    public NotificationReadResponse markNotificationRead(long notificationId) {
        UserProfile user = currentUser();
        NotificationItem existing = repository.findNotification(user.id(), notificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found."));
        if (!existing.read()) {
            repository.markNotificationRead(user.id(), notificationId);
        }
        NotificationItem updated = repository.findNotification(user.id(), notificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found."));
        long unreadCount = repository.countUnreadNotifications(user.id());
        return new NotificationReadResponse(updated, unreadCount);
    }

    @Transactional
    public NotificationsReadAllResponse markAllNotificationsRead() {
        UserProfile user = currentUser();
        repository.markAllNotificationsRead(user.id());
        List<NotificationItem> items = safe(() -> repository.findNotifications(user.id(), 20, 0), List.of());
        long unreadCount = repository.countUnreadNotifications(user.id());
        return new NotificationsReadAllResponse(items, unreadCount);
    }

    public NotificationDeleteResponse deleteNotification(long notificationId) {
        UserProfile user = currentUser();
        repository.findNotification(user.id(), notificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found."));
        repository.deleteNotification(user.id(), notificationId);
        return new NotificationDeleteResponse(notificationId, true, repository.countUnreadNotifications(user.id()));
    }

    public CurriculumResponse curriculum() {
        UserProfile user = currentUser();
        return new CurriculumResponse(safe(() -> repository.findCurriculum(user.id()), List.of()));
    }

    public ReplayResponse replays() {
        UserProfile user = currentUser();
        return new ReplayResponse(safe(() -> repository.findReplays(user.id()), List.of()));
    }

    public ElearningProgressResponse elearningInProgress(String status, String keyword, int page, int size) {
        UserProfile user = currentUser();
        String normalizedStatus = normalizeElearningStatus(status);
        long total = safe(() -> repository.countElearningProgress(user.id(), normalizedStatus, keyword), 0L);
        List<com.edussafy.backend.priority.dto.PriorityDtos.ElearningProgressItem> items = total == 0
                ? List.of()
                : safe(
                        () -> repository.findElearningProgress(user.id(), normalizedStatus, keyword, size, offset(page, size)),
                        List.of()
                );
        return new ElearningProgressResponse(items, pageMeta(page, size, total));
    }

    public ElearningProgressDetailResponse elearningProgressDetail(long courseId) {
        UserProfile user = currentUser();
        ElearningProgressDetail item = repository.findElearningProgressDetail(user.id(), courseId)
                .map(detail -> detail.withLessons(safe(() -> repository.findElearningLessons(user.id(), courseId), List.of())))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "E-learning progress not found."));
        return new ElearningProgressDetailResponse(item);
    }

    @Transactional
    public ElearningResumeResponse resumeElearning(long courseId) {
        UserProfile user = currentUser();
        int updated = repository.touchElearningResume(user.id(), courseId);
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "E-learning progress not found.");
        }
        ElearningProgressDetail item = repository.findElearningProgressDetail(user.id(), courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "E-learning progress not found."));
        return new ElearningResumeResponse(new ElearningResumeItem(
                item.courseId(),
                item.resumeUrl(),
                item.lastLearningAt() == null ? OffsetDateTime.now(ZoneOffset.UTC) : item.lastLearningAt(),
                item.status()
        ));
    }

    public MaterialsResponse materials(String keyword, String type, int page, int size) {
        UserProfile user = currentUser();
        long total = safe(() -> repository.countMaterials(user.id(), keyword, type), 0L);
        List<MaterialItem> items = total == 0
                ? List.of()
                : safe(() -> attachResources(repository.findMaterials(user.id(), keyword, type, size, offset(page, size))),
                        List.of());
        return new MaterialsResponse(items, pageMeta(page, size, total));
    }

    public MaterialDetailResponse material(long id) {
        UserProfile user = currentUser();
        MaterialItem item = safe(() -> p3Repository.findMaterial(id, user.id())
                .map(material -> material.withResources(safe(() -> p3Repository.findMaterialResources(id), List.of())))
                .orElse(fallbackMaterial(id)), fallbackMaterial(id));
        return new MaterialDetailResponse(item);
    }

    @Transactional
    public MaterialViewResponse recordMaterialView(long id) {
        UserProfile user = currentUser();
        int updatedRows = p3Repository.incrementMaterialViewCount(id);
        if (updatedRows == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Learning material not found.");
        }
        MaterialItem item = p3Repository.findMaterial(id, user.id())
                .map(material -> material.withResources(safe(() -> p3Repository.findMaterialResources(id), List.of())))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Learning material not found."));
        return new MaterialViewResponse(item);
    }

    @Transactional
    public MaterialReactionResponse createMaterialReaction(long id, String type) {
        UserProfile user = currentUser();
        String normalizedType = normalizeMaterialReaction(type);
        p3Repository.findMaterial(id, user.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Learning material not found."));
        p3Repository.createMaterialReaction(id, user.id(), normalizedType);
        return materialReaction(id, user.id());
    }

    @Transactional
    public MaterialReactionResponse deleteMaterialReaction(long id, String type) {
        UserProfile user = currentUser();
        String normalizedType = normalizeMaterialReaction(type);
        p3Repository.findMaterial(id, user.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Learning material not found."));
        p3Repository.deleteMaterialReaction(id, user.id(), normalizedType);
        return materialReaction(id, user.id());
    }

    public MaterialResourcesResponse materialResources(long id) {
        return new MaterialResourcesResponse(safe(() -> p3Repository.findMaterialResources(id), List.of()));
    }

    @Transactional
    public MaterialResourceAttachmentCreateResponse createMaterialResourceAttachment(
            long materialId,
            long resourceId,
            MaterialResourceAttachmentRequest request
    ) {
        UserProfile user = currentUser();
        if (!canManageLearning(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only staff can attach learning material resources.");
        }
        MaterialResourceItem resource = p3Repository.findMaterialResource(materialId, resourceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Learning material resource not found."));

        byte[] fileBytes = decodeAttachment(request.contentBase64(), "Learning material attachment");
        String filename = sanitizeAttachmentFilename(request.filename(), "Learning material attachment");
        String mimeType = normalizeWithDefault(request.mimeType(), "application/octet-stream");
        String checksum = sha256Hex(fileBytes);
        String storageKey = "learning/materials/%d/resources/%d/%s-%s".formatted(
                materialId,
                resourceId,
                checksum.substring(0, 12),
                filename
        );
        String storedPath = "/learning/materials/%d/resources/%d/attachments/%s".formatted(
                materialId,
                resourceId,
                checksum
        );

        long attachmentId = p2Repository.createOrFindAttachment(
                filename,
                storageKey,
                storedPath,
                mimeType,
                fileBytes.length,
                checksum
        );
        p3Repository.linkMaterialResourceAttachment(resourceId, attachmentId);
        MaterialResourceAttachmentItem attachment = p3Repository.findMaterialResourceAttachment(resourceId, attachmentId)
                .filter(item -> item.materialId() == materialId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Learning material attachment not found."));
        storeMaterialResourceAttachment(attachment, fileBytes);
        return new MaterialResourceAttachmentCreateResponse(attachment, resource);
    }

    public MaterialResourceAttachmentDownload downloadMaterialResourceAttachment(
            long materialId,
            long resourceId,
            long attachmentId
    ) {
        currentUser();
        p3Repository.findMaterialResource(materialId, resourceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Learning material resource not found."));
        MaterialResourceAttachmentItem attachment = p3Repository.findMaterialResourceAttachment(resourceId, attachmentId)
                .filter(item -> item.materialId() == materialId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Learning material attachment not found."));
        return new MaterialResourceAttachmentDownload(attachment, readMaterialResourceAttachment(attachment));
    }

    public QuestsResponse quests(int page, int size) {
        UserProfile user = currentUser();
        long total = safe(() -> repository.countQuests(user.id()), 0L);
        List<QuestItem> items = total == 0
                ? List.of()
                : safe(() -> repository.findQuests(user.id(), size, offset(page, size)), List.of());
        return new QuestsResponse(items, pageMeta(page, size, total));
    }

    public QuestDetailResponse quest(long id) {
        UserProfile user = currentUser();
        QuestItem item = safe(() -> p3Repository.findQuest(user.id(), id).orElse(fallbackQuest(id)), fallbackQuest(id));
        return new QuestDetailResponse(item);
    }

    public QuestSubmissionDetailResponse questSubmission(long id) {
        UserProfile user = currentUser();
        p3Repository.findQuest(user.id(), id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quest not found."));
        QuestSubmissionItem item = p3Repository.findQuestSubmission(user.id(), id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quest submission not found."));
        return new QuestSubmissionDetailResponse(item);
    }

    @Transactional
    public QuestSubmissionResponse submitQuest(long id, QuestSubmissionRequest request) {
        UserProfile user = currentUser();
        String content = request.content() == null ? "" : request.content().trim();
        if (content.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quest submission content is required.");
        }
        p3Repository.findQuest(user.id(), id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quest not found."));
        QuestSubmissionItem item = p3Repository.upsertQuestSubmission(user.id(), id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Quest submission was not saved."));
        return new QuestSubmissionResponse(item);
    }

    @Transactional
    public QuestSubmissionAttachmentCreateResponse createQuestSubmissionAttachment(
            long questId,
            long submissionId,
            QuestSubmissionAttachmentRequest request
    ) {
        UserProfile user = currentUser();
        QuestSubmissionItem submission = currentUserQuestSubmission(user.id(), questId, submissionId);
        byte[] fileBytes = decodeAttachment(request.contentBase64(), "Quest submission attachment");
        String filename = sanitizeAttachmentFilename(request.filename(), "Quest submission attachment");
        String mimeType = normalizeWithDefault(request.mimeType(), "application/octet-stream");
        String checksum = sha256Hex(fileBytes);
        String storageKey = "quests/%d/submissions/%d/%s-%s".formatted(
                questId,
                submissionId,
                checksum.substring(0, 12),
                filename
        );
        String storedPath = "/quests/%d/submissions/%d/attachments/%s".formatted(questId, submissionId, checksum);
        long attachmentId = p2Repository.createOrFindAttachment(
                filename,
                storageKey,
                storedPath,
                mimeType,
                fileBytes.length,
                checksum
        );
        QuestSubmissionAttachmentItem attachment = p3Repository.findQuestSubmissionAttachment(questId, submissionId, attachmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quest submission attachment not found."));
        storeQuestSubmissionAttachment(attachment, fileBytes);
        return new QuestSubmissionAttachmentCreateResponse(attachment, submission);
    }

    public QuestSubmissionAttachmentDownload downloadQuestSubmissionAttachment(
            long questId,
            long submissionId,
            long attachmentId
    ) {
        UserProfile user = currentUser();
        currentUserQuestSubmission(user.id(), questId, submissionId);
        QuestSubmissionAttachmentItem attachment = p3Repository.findQuestSubmissionAttachment(questId, submissionId, attachmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quest submission attachment not found."));
        return new QuestSubmissionAttachmentDownload(attachment, readQuestSubmissionAttachment(attachment));
    }

    public SurveysResponse surveys(int page, int size) {
        UserProfile user = currentUser();
        long total = safe(() -> repository.countSurveys(user.id()), 0L);
        List<SurveyItem> items = total == 0
                ? List.of()
                : safe(() -> repository.findSurveys(user.id(), size, offset(page, size)), List.of());
        return new SurveysResponse(items, pageMeta(page, size, total));
    }

    public SurveyDetailResponse survey(long id) {
        UserProfile user = currentUser();
        SurveyDetail item = safe(() -> p3Repository.findSurvey(user.id(), id)
                .map(survey -> survey.withQuestions(safe(() -> p3Repository.findSurveyQuestions(id), List.of())))
                .orElse(fallbackSurvey(id)), fallbackSurvey(id));
        return new SurveyDetailResponse(item);
    }

    @Transactional
    public SurveyDetailResponse createSurvey(SurveyCreateRequest request) {
        UserProfile user = currentUser();
        if (!canManageSurveys(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Survey management permission is required.");
        }

        PreparedSurveyCreate draft = prepareSurveyCreate(request);
        long contentScopeId = p3Repository.findDefaultContentScopeId()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Survey content scope is not configured."));
        long surveyId = p3Repository.createSurvey(
                contentScopeId,
                draft.title(),
                draft.category(),
                draft.required(),
                draft.startAt(),
                draft.endAt(),
                draft.status()
        );
        if (surveyId <= 0) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Survey was not created.");
        }

        int questionOrder = 1;
        for (PreparedSurveyQuestion question : draft.questions()) {
            long questionId = p3Repository.createSurveyQuestion(surveyId, question.type(), question.text(), questionOrder++);
            int optionOrder = 1;
            for (String option : question.options()) {
                p3Repository.createSurveyOption(questionId, option, optionOrder++);
            }
        }

        SurveyDetail item = p3Repository.findSurvey(user.id(), surveyId)
                .map(survey -> survey.withQuestions(p3Repository.findSurveyQuestions(surveyId)))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Created survey could not be loaded."));
        return new SurveyDetailResponse(item);
    }

    @Transactional
    public SurveyDetailResponse updateSurvey(long id, SurveyCreateRequest request) {
        UserProfile user = currentUser();
        if (!canManageSurveys(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Survey management permission is required.");
        }
        p3Repository.findSurvey(user.id(), id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Survey not found."));

        PreparedSurveyCreate draft = prepareSurveyCreate(request);
        int updated = p3Repository.updateSurvey(
                id,
                draft.title(),
                draft.category(),
                draft.required(),
                draft.startAt(),
                draft.endAt(),
                draft.status()
        );
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Survey not found.");
        }
        p3Repository.deleteSurveyResponses(id);
        p3Repository.deleteSurveyQuestions(id);
        int questionOrder = 1;
        for (PreparedSurveyQuestion question : draft.questions()) {
            long questionId = p3Repository.createSurveyQuestion(id, question.type(), question.text(), questionOrder++);
            int optionOrder = 1;
            for (String option : question.options()) {
                p3Repository.createSurveyOption(questionId, option, optionOrder++);
            }
        }

        SurveyDetail item = p3Repository.findSurvey(user.id(), id)
                .map(survey -> survey.withQuestions(p3Repository.findSurveyQuestions(id)))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Updated survey could not be loaded."));
        return new SurveyDetailResponse(item);
    }

    @Transactional
    public SurveyDeleteResponse deleteSurvey(long id) {
        UserProfile user = currentUser();
        if (!canManageSurveys(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Survey management permission is required.");
        }
        p3Repository.findSurvey(user.id(), id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Survey not found."));
        int deleted = p3Repository.deleteSurvey(id);
        if (deleted == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Survey not found.");
        }
        return new SurveyDeleteResponse(new SurveyDeleteItem(id, true, false));
    }

    @Transactional
    public SurveyResponseSubmitResponse submitSurvey(long id, SurveyResponseSubmitRequest request) {
        UserProfile user = currentUser();
        SurveyDetail survey = p3Repository.findSurvey(user.id(), id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Survey not found."));
        Map<Long, SurveyQuestionItem> questions = p3Repository.findSurveyQuestions(id).stream()
                .collect(Collectors.toMap(SurveyQuestionItem::id, question -> question));
        List<PreparedSurveyAnswer> answers = prepareSurveyAnswers(request.answers(), questions);

        SurveyResponsePersistence response = p3Repository.saveSurveyResponse(survey.id(), user.id());
        p3Repository.deleteSurveyAnswers(response.id());
        for (PreparedSurveyAnswer answer : answers) {
            long answerId = p3Repository.createSurveyAnswer(
                    response.id(),
                    survey.id(),
                    answer.questionId(),
                    answer.answerText()
            );
            p3Repository.createSurveyAnswerOptions(answerId, answer.questionId(), answer.optionIds());
        }

        return new SurveyResponseSubmitResponse(new SurveyResponseSubmitItem(
                response.id(),
                response.surveyId(),
                response.completed(),
                answers.size(),
                response.respondedAt(),
                false
        ));
    }

    public SurveyResponseDetailResponse surveyResponse(long id) {
        UserProfile user = currentUser();
        p3Repository.findSurvey(user.id(), id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Survey not found."));
        SurveyResponsePersistence response = p3Repository.findSurveyResponse(user.id(), id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Survey response not found."));

        return new SurveyResponseDetailResponse(new SurveyResponseDetail(
                response.id(),
                response.surveyId(),
                response.completed(),
                response.respondedAt(),
                p3Repository.findSurveyResponseAnswers(response.id()),
                false
        ));
    }

    public SupportTicketsResponse supportTickets(int page, int size) {
        UserProfile user = currentUser();
        boolean supportStaff = canAnswerSupport(user);
        long total = supportStaff
                ? safe(p2Repository::countAllSupportTickets, 0L)
                : safe(() -> p2Repository.countSupportTickets(user.id()), 0L);
        List<SupportTicketItem> items = total == 0
                ? List.of()
                : safe(
                        () -> supportStaff
                                ? p2Repository.findAllSupportTickets(size, offset(page, size))
                                : p2Repository.findSupportTickets(user.id(), size, offset(page, size)),
                        List.of()
                );
        return new SupportTicketsResponse(items, pageMeta(page, size, total));
    }

    public SupportTicketDetailResponse supportTicket(long ticketId) {
        UserProfile user = currentUser();
        boolean supportStaff = canAnswerSupport(user);
        SupportTicketItem ticket = (supportStaff
                ? p2Repository.findSupportTicketForStaff(ticketId)
                : p2Repository.findSupportTicket(user.id(), ticketId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Support ticket not found."));
        List<SupportTicketMessageItem> messages = safe(
                () -> supportStaff
                        ? p2Repository.findSupportTicketMessagesForStaff(ticketId)
                        : p2Repository.findSupportTicketMessages(user.id(), ticketId),
                List.of()
        );
        return new SupportTicketDetailResponse(SupportTicketDetail.from(ticket, attachSupportAttachments(messages)));
    }

    @Transactional
    public SupportTicketCreateResponse createSupportTicket(SupportTicketCreateRequest request) {
        UserProfile user = currentUser();
        String title = request.title().trim();
        String content = request.content().trim();

        long ticketId = p2Repository.createSupportTicket(user.id(), title);
        p2Repository.createSupportTicketMessage(ticketId, user.id(), content);
        SupportTicketItem item = p2Repository.findSupportTicket(user.id(), ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Support ticket not found."));
        return new SupportTicketCreateResponse(item);
    }

    @Transactional
    public SupportTicketMessageCreateResponse createSupportTicketMessage(
            long ticketId,
            SupportTicketMessageRequest request
    ) {
        UserProfile user = currentUser();
        SupportTicketItem ticket = p2Repository.findSupportTicket(user.id(), ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Support ticket not found."));
        if ("closed".equals(ticket.status())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Closed support ticket cannot receive messages.");
        }

        String content = request.content().trim();
        long messageId = p2Repository.createSupportTicketMessage(ticket.id(), user.id(), "user_message", content);
        p2Repository.markSupportTicketOpen(ticket.id());
        SupportTicketMessageItem message = p2Repository.findSupportTicketMessage(user.id(), ticket.id(), messageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Support ticket message not found."));
        SupportTicketItem updatedTicket = p2Repository.findSupportTicket(user.id(), ticket.id()).orElse(ticket);
        return new SupportTicketMessageCreateResponse(message, updatedTicket);
    }

    @Transactional
    public SupportTicketMessageCreateResponse createSupportTicketAnswer(
            long ticketId,
            SupportTicketMessageRequest request
    ) {
        UserProfile user = currentUser();
        if (!canAnswerSupport(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Support answer permission is required.");
        }

        SupportTicketItem ticket = p2Repository.findSupportTicketForStaff(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Support ticket not found."));
        if ("closed".equals(ticket.status())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Closed support ticket cannot receive answers.");
        }

        String content = request.content().trim();
        long messageId = p2Repository.createSupportTicketMessage(ticket.id(), user.id(), "admin_reply", content);
        p2Repository.markSupportTicketAnswered(ticket.id());
        SupportTicketMessageItem message = p2Repository.findSupportTicketMessageForStaff(ticket.id(), messageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Support answer not found."));
        SupportTicketItem updatedTicket = p2Repository.findSupportTicketForStaff(ticket.id()).orElse(ticket);
        return new SupportTicketMessageCreateResponse(message, updatedTicket);
    }

    @Transactional
    public SupportTicketAttachmentCreateResponse createSupportTicketMessageAttachment(
            long ticketId,
            long messageId,
            SupportTicketAttachmentRequest request
    ) {
        UserProfile user = currentUser();
        boolean supportStaff = canAnswerSupport(user);
        SupportTicketMessageItem message = (supportStaff
                ? p2Repository.findSupportTicketMessageForStaff(ticketId, messageId)
                : p2Repository.findSupportTicketMessage(user.id(), ticketId, messageId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Support ticket message not found."));
        if (!supportStaff && !Objects.equals(message.senderUserId(), user.id())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot attach files to another sender's support message.");
        }

        byte[] fileBytes = decodeSupportAttachment(request.contentBase64());
        String filename = sanitizeAttachmentFilename(request.filename());
        String mimeType = normalizeWithDefault(request.mimeType(), "application/octet-stream");
        String checksum = sha256Hex(fileBytes);
        String storageKey = "support/tickets/%d/messages/%d/%s-%s".formatted(
                ticketId,
                messageId,
                checksum.substring(0, 12),
                filename
        );
        String storedPath = "/support/tickets/%d/messages/%d/attachments/%s".formatted(ticketId, messageId, checksum);

        long attachmentId = p2Repository.createOrFindAttachment(
                filename,
                storageKey,
                storedPath,
                mimeType,
                fileBytes.length,
                checksum
        );
        p2Repository.linkSupportTicketMessageAttachment(message.id(), attachmentId);
        SupportTicketAttachmentItem attachment = p2Repository.findSupportTicketMessageAttachment(message.id(), attachmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Support attachment not found."));
        storeSupportAttachment(attachment, fileBytes);
        List<SupportTicketAttachmentItem> attachments = p2Repository.findSupportTicketMessageAttachments(List.of(message.id()));
        return new SupportTicketAttachmentCreateResponse(attachment, message.withAttachments(attachments));
    }

    public SupportTicketAttachmentDownload downloadSupportTicketMessageAttachment(
            long ticketId,
            long messageId,
            long attachmentId
    ) {
        UserProfile user = currentUser();
        boolean supportStaff = canAnswerSupport(user);
        SupportTicketMessageItem message = (supportStaff
                ? p2Repository.findSupportTicketMessageForStaff(ticketId, messageId)
                : p2Repository.findSupportTicketMessage(user.id(), ticketId, messageId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Support ticket message not found."));
        SupportTicketAttachmentItem attachment = p2Repository.findSupportTicketMessageAttachment(message.id(), attachmentId)
                .filter(item -> item.messageId() == message.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Support attachment not found."));

        return new SupportTicketAttachmentDownload(attachment, readSupportAttachment(attachment));
    }

    public ClassmatesResponse classmates() {
        UserProfile user = currentUser();
        List<ClassmateItem> items = safe(() -> p2Repository.findClassmates(user.id()), List.of());
        return new ClassmatesResponse(items);
    }

    public ClassmateNotificationResponse createClassmateNotification(
            long recipientUserId,
            ClassmateNotificationRequest request
    ) {
        UserProfile user = currentUser();
        p2Repository.findClassmates(user.id()).stream()
                .filter(classmate -> classmate.id() == recipientUserId)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Classmate not found."));
        String type = normalizeWithDefault(
                request == null ? null : request.type(),
                DEFAULT_CLASSMATE_NOTIFICATION_TYPE
        ).toLowerCase(Locale.ROOT);
        String message = normalizeWithDefault(
                request == null ? null : request.message(),
                DEFAULT_CLASSMATE_NOTIFICATION_MESSAGE
        );
        String title = "Classmate contact request";
        long notificationId = repository.createNotification(user.id(), title, message);
        repository.createNotificationRecipient(notificationId, recipientUserId);
        NotificationItem notification = repository.findNotification(recipientUserId, notificationId)
                .orElse(new NotificationItem(notificationId, title, message, OffsetDateTime.now(), false));

        return new ClassmateNotificationResponse(new ClassmateNotificationItem(
                notificationId,
                recipientUserId,
                type,
                message,
                "sent",
                notification.createdAt(),
                notification,
                false
        ));
    }

    public ProfileResponse profile() {
        UserProfile user = currentUser();
        ProfileDetails fallback = profileFromUser(user);
        ProfileDetails profile = safe(() -> p2Repository.findProfile(user.id()).orElse(fallback), fallback);
        return new ProfileResponse(profile);
    }

    public ProfileResponse updateProfile(ProfileUpdateRequest request) {
        requireProfileVerification();
        UserProfile user = currentUser();
        ProfileDetails current = safe(() -> p2Repository.findProfile(user.id()).orElse(profileFromUser(user)),
                profileFromUser(user));
        ProfileDetails merged = new ProfileDetails(
                current.id(),
                request.name().trim(),
                current.email(),
                current.role(),
                current.learnerNo(),
                current.campusName(),
                current.cohortName(),
                current.trackName(),
                current.className(),
                normalizeNullable(request.zipCode()),
                normalizeNullable(request.addressLine1()),
                normalizeNullable(request.addressLine2()),
                normalizeNullable(request.mobilePhone()),
                normalizeNullable(request.emergencyPhone()),
                request.marketingOptIn() != null ? request.marketingOptIn() : current.marketingOptIn()
        );
        ProfileDetails persisted = safe(
                () -> p2Repository.updateProfile(user.id(), request, merged.marketingOptIn()).orElse(merged),
                merged
        );
        clearProfileVerification();
        return new ProfileResponse(persisted);
    }

    @Transactional
    public AuthActionResponse changeProfilePassword(ProfilePasswordChangeRequest request) {
        if (currentSessionUserId().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        UserProfile user = currentUser();
        String storedHash = safe(() -> repository.findPasswordHash(user.id()).orElse(null), null);
        if (!passwordMatches(request.currentPassword(), storedHash)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "현재 비밀번호가 일치하지 않습니다.");
        }
        if (passwordMatches(request.newPassword(), storedHash)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "새 비밀번호는 현재 비밀번호와 달라야 합니다.");
        }

        int updated = repository.updatePasswordHash(user.id(), passwordHashForStorage(request.newPassword()));
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자 정보를 찾을 수 없습니다.");
        }
        clearProfileVerification();
        return new AuthActionResponse(true, "비밀번호가 변경되었습니다.");
    }

    private UserProfile currentUser() {
        Optional<Long> sessionUserId = currentSessionUserId();
        if (sessionUserId.isPresent()) {
            Optional<UserProfile> sessionUser = safe(() -> repository.findUserById(sessionUserId.get()), Optional.empty());
            if (sessionUser.isPresent()) {
                return sessionUser.get();
            }
            if (hasCurrentRequest()) {
                clearCurrentSession();
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "세션 사용자 정보를 찾을 수 없습니다.");
            }
        }
        if (hasCurrentRequest()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return safe(() -> repository.findDefaultUser().orElse(DEMO_USER), DEMO_USER);
    }

    private Optional<Long> currentSessionUserId() {
        HttpSession session = currentSession(false);
        if (session == null) {
            return Optional.empty();
        }

        return AuthSession.currentUserId(session);
    }

    private void storeCurrentUserId(long userId) {
        HttpSession existingSession = currentSession(false);
        if (existingSession != null) {
            existingSession.invalidate();
        }

        HttpSession session = currentSession(true);
        if (session != null) {
            session.setMaxInactiveInterval(AuthSession.MAX_INACTIVE_SECONDS);
            session.setAttribute(AuthSession.CURRENT_USER_ID, userId);
        }
    }

    private void clearCurrentSession() {
        HttpSession session = currentSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    private void markProfileVerified() {
        HttpSession session = currentSession(false);
        if (session != null && AuthSession.currentUserId(session).isPresent()) {
            AuthSession.markProfileVerified(session, Instant.now());
        }
    }

    private void requireProfileVerification() {
        HttpSession session = currentSession(false);
        if (session == null || AuthSession.currentUserId(session).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        if (!AuthSession.profileVerified(session, Instant.now())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "회원정보 수정 전 비밀번호 확인이 필요합니다.");
        }
    }

    private void clearProfileVerification() {
        AuthSession.clearProfileVerification(currentSession(false));
    }

    private HttpSession currentSession(boolean create) {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            HttpServletRequest request = attributes.getRequest();
            return request.getSession(create);
        }
        return null;
    }

    private boolean hasCurrentRequest() {
        return RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes;
    }

    private String normalizeElearningStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        }
        String normalized = status.trim().toLowerCase(Locale.ROOT);
        if ("all".equals(normalized)) {
            return null;
        }
        if (!ELEARNING_PROGRESS_STATUSES.contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported e-learning progress status.");
        }
        return normalized;
    }

    private String normalizeAccessRole(String role) {
        if (role == null || role.isBlank()) {
            return "learner";
        }
        String normalized = role.trim().toLowerCase(Locale.ROOT);
        if ("student".equals(normalized)) {
            return "learner";
        }
        if ("manager".equals(normalized) || "instructor".equals(normalized)) {
            return "coach";
        }
        if (ROLE_PERMISSIONS.containsKey(normalized)) {
            return normalized;
        }
        return "learner";
    }

    private boolean canAnswerSupport(UserProfile user) {
        String role = normalizeAccessRole(user.role());
        return "coach".equals(role) || "admin".equals(role);
    }

    private boolean canResolveAttendanceAppeals(UserProfile user) {
        String role = normalizeAccessRole(user.role());
        return "coach".equals(role) || "admin".equals(role);
    }

    private boolean canManageSurveys(UserProfile user) {
        String role = normalizeAccessRole(user.role());
        return "coach".equals(role) || "admin".equals(role);
    }

    private boolean canManageLearning(UserProfile user) {
        String role = normalizeAccessRole(user.role());
        return "coach".equals(role) || "admin".equals(role);
    }

    private List<SupportTicketMessageItem> attachSupportAttachments(List<SupportTicketMessageItem> messages) {
        if (messages.isEmpty()) {
            return messages;
        }

        List<Long> messageIds = messages.stream()
                .map(SupportTicketMessageItem::id)
                .toList();
        Map<Long, List<SupportTicketAttachmentItem>> attachments = safe(
                () -> p2Repository.findSupportTicketMessageAttachments(messageIds).stream()
                        .collect(Collectors.groupingBy(SupportTicketAttachmentItem::messageId)),
                Map.of()
        );
        return messages.stream()
                .map(message -> message.withAttachments(attachments.getOrDefault(message.id(), List.of())))
                .toList();
    }

    private byte[] decodeSupportAttachment(String contentBase64) {
        return decodeAttachment(contentBase64, "Support attachment");
    }

    private byte[] decodeAttachment(String contentBase64, String label) {
        try {
            byte[] decoded = Base64.getDecoder().decode(contentBase64.trim());
            if (decoded.length == 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, label + " cannot be empty.");
            }
            if (decoded.length > SUPPORT_ATTACHMENT_MAX_BYTES) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, label + " exceeds the 2MB limit.");
            }
            return decoded;
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, label + " content must be base64.");
        }
    }

    private String sanitizeAttachmentFilename(String filename) {
        return sanitizeAttachmentFilename(filename, "Support attachment");
    }

    private String sanitizeAttachmentFilename(String filename, String label) {
        String normalized = filename.trim().replaceAll("[\\\\/\\p{Cntrl}]+", "_");
        if (normalized.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, label + " filename is required.");
        }
        return normalized.length() <= 255 ? normalized : normalized.substring(0, 255);
    }

    private void storeSupportAttachment(SupportTicketAttachmentItem attachment, byte[] content) {
        Path path = supportAttachmentPath(attachment);
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Support attachment could not be stored.", exception);
        }
    }

    private byte[] readSupportAttachment(SupportTicketAttachmentItem attachment) {
        try {
            return Files.readAllBytes(supportAttachmentPath(attachment));
        } catch (NoSuchFileException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Support attachment file was not found.", exception);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Support attachment could not be read.", exception);
        }
    }

    private Path supportAttachmentPath(SupportTicketAttachmentItem attachment) {
        String storageKey = normalizeWithDefault(attachment.storageKey(), "support/attachments/%d".formatted(attachment.id()));
        return safeAttachmentPath(storageKey, "Support attachment");
    }

    private void storeMaterialResourceAttachment(MaterialResourceAttachmentItem attachment, byte[] content) {
        Path path = materialResourceAttachmentPath(attachment);
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Learning material attachment could not be stored.", exception);
        }
    }

    private byte[] readMaterialResourceAttachment(MaterialResourceAttachmentItem attachment) {
        try {
            return Files.readAllBytes(materialResourceAttachmentPath(attachment));
        } catch (NoSuchFileException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Learning material attachment file was not found.", exception);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Learning material attachment could not be read.", exception);
        }
    }

    private Path materialResourceAttachmentPath(MaterialResourceAttachmentItem attachment) {
        String storageKey = normalizeWithDefault(attachment.storageKey(), "learning/materials/resources/attachments/%d".formatted(attachment.id()));
        return safeAttachmentPath(storageKey, "Learning material attachment");
    }

    private QuestSubmissionItem currentUserQuestSubmission(long userId, long questId, long submissionId) {
        p3Repository.findQuest(userId, questId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quest not found."));
        QuestSubmissionItem submission = p3Repository.findQuestSubmission(userId, questId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quest submission not found."));
        if (submission.id() != submissionId) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Quest submission not found.");
        }
        return submission;
    }

    private void storeQuestSubmissionAttachment(QuestSubmissionAttachmentItem attachment, byte[] content) {
        Path path = questSubmissionAttachmentPath(attachment);
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Quest submission attachment could not be stored.", exception);
        }
    }

    private byte[] readQuestSubmissionAttachment(QuestSubmissionAttachmentItem attachment) {
        try {
            return Files.readAllBytes(questSubmissionAttachmentPath(attachment));
        } catch (NoSuchFileException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Quest submission attachment file was not found.", exception);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Quest submission attachment could not be read.", exception);
        }
    }

    private Path questSubmissionAttachmentPath(QuestSubmissionAttachmentItem attachment) {
        String storageKey = normalizeWithDefault(attachment.storageKey(), "quests/submissions/attachments/%d".formatted(attachment.id()));
        return safeAttachmentPath(storageKey, "Quest submission attachment");
    }

    private Path safeAttachmentPath(String storageKey, String label) {
        Path root = Path.of(System.getProperty("java.io.tmpdir"), "edussafy-attachments").toAbsolutePath().normalize();
        Path path = root.resolve(storageKey).normalize();
        if (!path.startsWith(root)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, label + " storage key is invalid.");
        }
        return path;
    }

    private String sha256Hex(byte[] content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 digest is unavailable.", exception);
        }
    }

    private boolean isAttendanceAppealAvailable(AttendanceRecordItem record) {
        String appealStatus = record.appealStatus();
        return record.appealAvailable() && (appealStatus == null || CLOSED_ATTENDANCE_APPEAL_STATUSES.contains(appealStatus));
    }

    private String normalizeAttendanceAppealType(String value) {
        String normalized = normalizeWithDefault(value, "other").toLowerCase(Locale.ROOT);
        if ("missing_check".equals(normalized)) {
            return "check_in";
        }
        if (!ATTENDANCE_APPEAL_TYPES.contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported attendance appeal type.");
        }
        return normalized;
    }

    private String normalizeAttendanceStatus(String value, String defaultStatus) {
        String normalized = normalizeWithDefault(value, defaultStatus).toLowerCase(Locale.ROOT);
        if (!ATTENDANCE_STATUSES.contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported requested attendance status.");
        }
        return normalized;
    }

    private String normalizeAttendanceAppealDecision(String value) {
        String normalized = normalizeWithDefault(value, "rejected").toLowerCase(Locale.ROOT);
        if (!ATTENDANCE_APPEAL_DECISIONS.contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported attendance appeal decision.");
        }
        return normalized;
    }

    private ProfileDetails profileFromUser(UserProfile user) {
        return new ProfileDetails(
                user.id(),
                user.name(),
                user.email(),
                user.role(),
                null,
                user.campusName(),
                user.cohortName(),
                user.trackName(),
                null,
                null,
                null,
                null,
                null,
                null,
                false
        );
    }

    private boolean passwordMatches(String rawPassword, String storedHash) {
        if (rawPassword == null || rawPassword.isBlank()) {
            return false;
        }
        if (storedHash == null || storedHash.isBlank()) {
            return false;
        }
        if (storedHash.startsWith("{noop}")) {
            return allowNoopPasswordHash && constantTimeEquals(rawPassword, storedHash.substring("{noop}".length()));
        }
        if (storedHash.startsWith("{sha256}")) {
            return constantTimeEquals(passwordHashForStorage(rawPassword), storedHash);
        }
        return false;
    }

    private String passwordHashForStorage(String rawPassword) {
        return "{sha256}" + sha256Hex(rawPassword.getBytes(StandardCharsets.UTF_8));
    }

    private boolean constantTimeEquals(String left, String right) {
        if (left == null || right == null) {
            return false;
        }
        return MessageDigest.isEqual(
                left.getBytes(StandardCharsets.UTF_8),
                right.getBytes(StandardCharsets.UTF_8)
        );
    }

    private List<MaterialItem> attachResources(List<MaterialItem> materials) {
        if (materials.isEmpty()) {
            return materials;
        }
        List<Long> materialIds = materials.stream().map(MaterialItem::id).toList();
        Map<Long, List<MaterialResourceItem>> resources = repository.findMaterialResources(materialIds).stream()
                .collect(Collectors.groupingBy(MaterialResourceItem::materialId));
        return materials.stream()
                .map(material -> material.withResources(resources.getOrDefault(material.id(), List.of())))
                .toList();
    }

    private MaterialItem fallbackMaterial(long id) {
        return new MaterialItem(
                id,
                "Learning Material",
                "document",
                null,
                null,
                0,
                null,
                List.of(),
                0,
                0,
                false,
                false
        );
    }

    private MaterialReactionResponse materialReaction(long materialId, long userId) {
        MaterialItem item = p3Repository.findMaterial(materialId, userId)
                .map(material -> material.withResources(safe(() -> p3Repository.findMaterialResources(materialId), List.of())))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Learning material not found."));
        return new MaterialReactionResponse(item);
    }

    private String normalizeMaterialReaction(String type) {
        String normalizedType = StringUtils.hasText(type) ? type.trim().toLowerCase() : "";
        if (!Set.of("like", "bookmark").contains(normalizedType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported material reaction type.");
        }
        return normalizedType;
    }

    private QuestItem fallbackQuest(long id) {
        return new QuestItem(id, "Quest", "assignment", null, null, null, null, "scheduled", null, null);
    }

    private SurveyDetail fallbackSurvey(long id) {
        return new SurveyDetail(id, "Survey", "etc", false, null, null, "scheduled", false, 0, List.of());
    }

    private PreparedSurveyCreate prepareSurveyCreate(SurveyCreateRequest request) {
        String title = trimRequired(request.title(), "Survey title is required.");
        String category = trimOrDefault(request.category(), "etc").toLowerCase(Locale.ROOT);
        String status = trimOrDefault(request.status(), "draft").toLowerCase(Locale.ROOT);
        if (!SURVEY_CATEGORIES.contains(category)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported survey category.");
        }
        if (!SURVEY_STATUSES.contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported survey status.");
        }
        if (request.startAt() != null && request.endAt() != null && request.endAt().isBefore(request.startAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Survey endAt must be after startAt.");
        }

        List<PreparedSurveyQuestion> questions = request.questions().stream()
                .map(this::prepareSurveyQuestion)
                .toList();
        return new PreparedSurveyCreate(
                title,
                category,
                request.required(),
                request.startAt(),
                request.endAt(),
                status,
                questions
        );
    }

    private PreparedSurveyQuestion prepareSurveyQuestion(SurveyQuestionCreateRequest question) {
        String type = trimOrDefault(question.type(), "long_text").toLowerCase(Locale.ROOT);
        String text = trimRequired(question.text(), "Survey question text is required.");
        if (!SURVEY_QUESTION_TYPES.contains(type)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported survey question type.");
        }

        List<String> options = Optional.ofNullable(question.options()).orElse(List.of()).stream()
                .map(SurveyOptionCreateRequest::text)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(option -> !option.isBlank())
                .distinct()
                .toList();
        if (SURVEY_CHOICE_TYPES.contains(type) && options.size() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choice survey questions require at least two options.");
        }
        if (!SURVEY_CHOICE_TYPES.contains(type) && !options.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Text survey questions cannot include options.");
        }
        return new PreparedSurveyQuestion(type, text, options);
    }

    private UserProfile demoUser(String email) {
        return new UserProfile(
                DEMO_USER.id(),
                DEMO_USER.name(),
                email == null || email.isBlank() ? DEMO_USER.email() : email,
                DEMO_USER.role(),
                DEMO_USER.campusName(),
                DEMO_USER.cohortName(),
                DEMO_USER.trackName()
        );
    }

    private PageMeta pageMeta(int page, int size, long total) {
        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / size);
        return new PageMeta(page, size, total, totalPages);
    }

    private int offset(int page, int size) {
        return (page - 1) * size;
    }

    private List<PreparedSurveyAnswer> prepareSurveyAnswers(
            List<SurveyAnswerRequest> answers,
            Map<Long, SurveyQuestionItem> questions
    ) {
        return answers.stream()
                .map(answer -> prepareSurveyAnswer(answer, questions))
                .toList();
    }

    private PreparedSurveyAnswer prepareSurveyAnswer(
            SurveyAnswerRequest answer,
            Map<Long, SurveyQuestionItem> questions
    ) {
        SurveyQuestionItem question = questions.get(answer.questionId());
        if (question == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Survey answer contains an unknown question.");
        }

        List<Long> optionIds = distinctOptionIds(answer.optionIds());
        String questionType = normalizeWithDefault(question.type(), "long_text").toLowerCase(Locale.ROOT);
        if ("single_choice".equals(questionType) || "multiple_choice".equals(questionType)) {
            validateChoiceAnswer(question, questionType, optionIds);
            return new PreparedSurveyAnswer(question.id(), null, optionIds);
        }

        if (!StringUtils.hasText(answer.answerText())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Survey text answer is required.");
        }
        if (!optionIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Survey text answer cannot include optionIds.");
        }
        return new PreparedSurveyAnswer(question.id(), answer.answerText().trim(), List.of());
    }

    private void validateChoiceAnswer(SurveyQuestionItem question, String questionType, List<Long> optionIds) {
        if ("single_choice".equals(questionType) && optionIds.size() != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Single choice survey answer requires one option.");
        }
        if ("multiple_choice".equals(questionType) && optionIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Multiple choice survey answer requires at least one option.");
        }

        Set<Long> allowedOptionIds = question.options().stream()
                .map(option -> option.id())
                .collect(Collectors.toSet());
        if (!allowedOptionIds.containsAll(optionIds)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Survey answer contains an invalid option.");
        }
    }

    private List<Long> distinctOptionIds(List<Long> optionIds) {
        if (optionIds == null || optionIds.isEmpty()) {
            return List.of();
        }
        return new LinkedHashSet<>(optionIds).stream().toList();
    }

    private String trimRequired(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        return value.trim();
    }

    private String trimOrDefault(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value.trim() : defaultValue;
    }

    private String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalizeWithDefault(String value, String defaultValue) {
        String normalized = normalizeNullable(value);
        return normalized == null ? defaultValue : normalized;
    }

    private <T> T safe(Supplier<T> supplier, T fallback) {
        try {
            return supplier.get();
        } catch (DataAccessException exception) {
            return fallback;
        }
    }

    private record PreparedSurveyAnswer(long questionId, String answerText, List<Long> optionIds) {
    }

    private record PreparedSurveyCreate(
            String title,
            String category,
            boolean required,
            OffsetDateTime startAt,
            OffsetDateTime endAt,
            String status,
            List<PreparedSurveyQuestion> questions
    ) {
    }

    private record PreparedSurveyQuestion(String type, String text, List<String> options) {
    }
}
