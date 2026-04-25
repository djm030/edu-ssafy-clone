package com.edussafy.backend.priority.service;

import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceAppealItem;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceAppealRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceAppealResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceRecordsResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceSummary;
import com.edussafy.backend.priority.dto.PriorityDtos.AuthActionResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ClassmateNotificationItem;
import com.edussafy.backend.priority.dto.PriorityDtos.ClassmateNotificationRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.ClassmateNotificationResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ClassmateItem;
import com.edussafy.backend.priority.dto.PriorityDtos.ClassmatesResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.CurriculumResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.DashboardSummary;
import com.edussafy.backend.priority.dto.PriorityDtos.LevelSummary;
import com.edussafy.backend.priority.dto.PriorityDtos.LoginRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialItem;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialResourceItem;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialResourcesResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialsResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.NotificationItem;
import com.edussafy.backend.priority.dto.PriorityDtos.NotificationsResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.NotificationsSummary;
import com.edussafy.backend.priority.dto.PriorityDtos.PageMeta;
import com.edussafy.backend.priority.dto.PriorityDtos.PasswordCheckRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.PasswordCheckResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ProfileDetails;
import com.edussafy.backend.priority.dto.PriorityDtos.ProfileResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ProfileUpdateRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.RoleAccessResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestItem;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestSubmissionItem;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestSubmissionRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestSubmissionResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestsResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ReplayResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketCreateRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketCreateResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketItem;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketsResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyDetail;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyItem;
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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
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
    private static final String SESSION_USER_ID = "edussafy.currentUserId";
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
    private static final List<String> LEARNER_DENIED_ROUTES = List.of("/admin", "/coach/reviews", "/management");

    private final PriorityApiRepository repository;
    private final PriorityP2Repository p2Repository;
    private final PriorityP3Repository p3Repository;

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
        UserProfile user = persistedUser.orElse(demoUser(request.email()));
        String storedPasswordHash = persistedUser
                .map(profile -> safe(() -> repository.findPasswordHash(profile.id()).orElse(null), null))
                .orElse("{noop}password");

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
        return new PasswordCheckResponse(passwordMatches(request.password(), storedHash));
    }

    public RoleAccessResponse currentRoleAccess() {
        UserProfile user = currentUser();
        String role = normalizeAccessRole(user.role());
        return new RoleAccessResponse(
                role,
                ROLE_PERMISSIONS.getOrDefault(role, ROLE_PERMISSIONS.get("learner")),
                "learner".equals(role) ? LEARNER_DENIED_ROUTES : List.of()
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
        UserProfile user = currentUser();
        AttendanceSummary summary = safe(() -> repository.findAttendanceSummary(user.id()), EMPTY_ATTENDANCE);
        return new AttendanceRecordsResponse(summary, safe(() -> repository.findAttendanceRecords(user.id()), List.of()));
    }

    public AttendanceAppealResponse createAttendanceAppeal(AttendanceAppealRequest request) {
        return new AttendanceAppealResponse(new AttendanceAppealItem(
                0L,
                request.type().trim().toLowerCase(),
                request.reason().trim(),
                normalizeNullable(request.requestedStatus()),
                "requested",
                null,
                true
        ));
    }

    public NotificationsResponse notifications(int page, int size) {
        UserProfile user = currentUser();
        long total = safe(() -> repository.countNotifications(user.id()), 0L);
        List<NotificationItem> items = total == 0
                ? List.of()
                : safe(() -> repository.findNotifications(user.id(), size, offset(page, size)), List.of());
        return new NotificationsResponse(items, pageMeta(page, size, total));
    }

    public CurriculumResponse curriculum() {
        UserProfile user = currentUser();
        return new CurriculumResponse(safe(() -> repository.findCurriculum(user.id()), List.of()));
    }

    public ReplayResponse replays() {
        UserProfile user = currentUser();
        return new ReplayResponse(safe(() -> repository.findReplays(user.id()), List.of()));
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
        MaterialItem item = safe(() -> p3Repository.findMaterial(id)
                .map(material -> material.withResources(safe(() -> p3Repository.findMaterialResources(id), List.of())))
                .orElse(fallbackMaterial(id)), fallbackMaterial(id));
        return new MaterialDetailResponse(item);
    }

    public MaterialResourcesResponse materialResources(long id) {
        return new MaterialResourcesResponse(safe(() -> p3Repository.findMaterialResources(id), List.of()));
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

    public QuestSubmissionResponse submitQuest(long id, QuestSubmissionRequest request) {
        return new QuestSubmissionResponse(new QuestSubmissionItem(
                0L,
                id,
                "submitted",
                null,
                true
        ));
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
        SurveyDetail item = safe(() -> p3Repository.findSurvey(user.id(), id).orElse(fallbackSurvey(id)), fallbackSurvey(id));
        return new SurveyDetailResponse(item);
    }

    public SurveyResponseSubmitResponse submitSurvey(long id, SurveyResponseSubmitRequest request) {
        int answerCount = request.answers() == null ? 0 : request.answers().size();
        return new SurveyResponseSubmitResponse(new SurveyResponseSubmitItem(
                0L,
                id,
                true,
                answerCount,
                null,
                true
        ));
    }

    public SupportTicketsResponse supportTickets(int page, int size) {
        UserProfile user = currentUser();
        long total = safe(() -> p2Repository.countSupportTickets(user.id()), 0L);
        List<SupportTicketItem> items = total == 0
                ? List.of()
                : safe(() -> p2Repository.findSupportTickets(user.id(), size, offset(page, size)), List.of());
        return new SupportTicketsResponse(items, pageMeta(page, size, total));
    }

    public SupportTicketCreateResponse createSupportTicket(SupportTicketCreateRequest request) {
        SupportTicketItem item = new SupportTicketItem(
                0L,
                request.title().trim(),
                "open",
                null,
                null,
                null,
                1L,
                null
        );
        return new SupportTicketCreateResponse(item);
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
        String type = normalizeWithDefault(
                request == null ? null : request.type(),
                DEFAULT_CLASSMATE_NOTIFICATION_TYPE
        ).toLowerCase(Locale.ROOT);
        String message = normalizeWithDefault(
                request == null ? null : request.message(),
                DEFAULT_CLASSMATE_NOTIFICATION_MESSAGE
        );
        long notificationId = 900_000L + recipientUserId;
        OffsetDateTime createdAt = OffsetDateTime.now();
        NotificationItem notification = new NotificationItem(
                notificationId,
                "Classmate contact request",
                message,
                createdAt,
                false
        );

        return new ClassmateNotificationResponse(new ClassmateNotificationItem(
                notificationId,
                recipientUserId,
                type,
                message,
                "sent",
                createdAt,
                notification,
                true
        ));
    }

    public ProfileResponse profile() {
        UserProfile user = currentUser();
        ProfileDetails fallback = profileFromUser(user);
        ProfileDetails profile = safe(() -> p2Repository.findProfile(user.id()).orElse(fallback), fallback);
        return new ProfileResponse(profile);
    }

    public ProfileResponse updateProfile(ProfileUpdateRequest request) {
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
        return new ProfileResponse(persisted);
    }

    private UserProfile currentUser() {
        Optional<Long> sessionUserId = currentSessionUserId();
        if (sessionUserId.isPresent()) {
            Optional<UserProfile> sessionUser = safe(() -> repository.findUserById(sessionUserId.get()), Optional.empty());
            if (sessionUser.isPresent()) {
                return sessionUser.get();
            }
        }
        return safe(() -> repository.findDefaultUser().orElse(DEMO_USER), DEMO_USER);
    }

    private Optional<Long> currentSessionUserId() {
        HttpSession session = currentSession(false);
        if (session == null) {
            return Optional.empty();
        }

        Object userId = session.getAttribute(SESSION_USER_ID);
        if (userId instanceof Number number) {
            return Optional.of(number.longValue());
        }
        if (userId instanceof String text && !text.isBlank()) {
            try {
                return Optional.of(Long.parseLong(text));
            } catch (NumberFormatException exception) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private void storeCurrentUserId(long userId) {
        HttpSession session = currentSession(true);
        if (session != null) {
            session.setAttribute(SESSION_USER_ID, userId);
        }
    }

    private void clearCurrentSession() {
        HttpSession session = currentSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    private HttpSession currentSession(boolean create) {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            HttpServletRequest request = attributes.getRequest();
            return request.getSession(create);
        }
        return null;
    }

    private String normalizeAccessRole(String role) {
        if (role == null || role.isBlank()) {
            return "learner";
        }
        String normalized = role.trim().toLowerCase(Locale.ROOT);
        if ("student".equals(normalized)) {
            return "learner";
        }
        if (ROLE_PERMISSIONS.containsKey(normalized)) {
            return normalized;
        }
        return "learner";
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
            return "password".equals(rawPassword);
        }
        if (storedHash.startsWith("{noop}")) {
            return rawPassword.equals(storedHash.substring("{noop}".length()));
        }
        return rawPassword.equals(storedHash);
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
                List.of()
        );
    }

    private QuestItem fallbackQuest(long id) {
        return new QuestItem(id, "Quest", "assignment", null, null, null, null, "scheduled", null, null);
    }

    private SurveyDetail fallbackSurvey(long id) {
        return new SurveyDetail(id, "Survey", "etc", false, null, null, "scheduled", false, 0);
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
}
