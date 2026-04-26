package com.edussafy.backend.priority.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;

public final class PriorityDtos {

    private PriorityDtos() {
    }

    public record LoginRequest(@NotBlank String email, @NotBlank String password) {
    }

    public record PasswordCheckRequest(@NotBlank String password) {
    }

    public record PasswordCheckResponse(boolean valid) {
    }

    public record ProfileEditAuthorizationResponse(
            boolean verified,
            OffsetDateTime verifiedUntil,
            int ttlSeconds
    ) {
    }

    public record AuthActionResponse(boolean success, String message) {
    }

    public record AuthSessionResponse(
            boolean authenticated,
            OffsetDateTime expiresAt,
            int maxInactiveSeconds,
            long secondsRemaining
    ) {
    }

    public record RoleAccessResponse(String role, List<String> permissions, List<String> deniedRoutes) {
    }

    public record AccessPolicyResponse(List<AccessPolicyItem> items) {
    }

    public record AccessPolicyItem(
            String id,
            String method,
            String pathPattern,
            List<String> allowedRoles,
            String feature,
            String description
    ) {
    }

    public record UserResponse(UserProfile user) {
    }

    public record UserProfile(
            long id,
            String name,
            String email,
            String role,
            String campusName,
            String cohortName,
            String trackName
    ) {
    }

    public record UserSummary(String name, String campusName, String cohortName, String trackName) {
    }

    public record LevelSummary(int level, int exp, int nextLevelExp, int scholarshipPoints, Integer rank) {
    }

    public record LevelDetailResponse(LevelDetail detail) {
    }

    public record LevelDetail(
            LevelSummary current,
            String levelName,
            int expPercent,
            int expRemaining,
            List<LevelTierItem> tiers,
            List<LevelHistoryItem> history,
            List<ScholarshipPointItem> pointBreakdown
    ) {
    }

    public record LevelTierItem(
            String name,
            int minLevel,
            int maxLevel,
            boolean current,
            int progressPercent,
            String description
    ) {
    }

    public record LevelHistoryItem(LocalDate snapshotDate, int rankNo, int exp, int scholarshipPoint) {
    }

    public record ScholarshipPointItem(String category, int points, String description) {
    }

    public record AttendanceSummary(int present, int late, int absent, boolean appealAvailable) {
    }

    public record NotificationsSummary(long unreadCount, List<NotificationItem> latest) {
    }

    public record TodaySummary(String curriculumTitle, String questTitle, String surveyTitle) {
    }

    public record DashboardAttendanceCheck(
            String todayLabel,
            boolean checkInAvailable,
            boolean checkOutAvailable,
            String statusText,
            String message,
            String detailPath
    ) {
    }

    public record DashboardCurriculumSession(
            long id,
            Integer weekNumber,
            LocalDate date,
            String period,
            String title,
            String instructor,
            String location,
            String status,
            String detailPath
    ) {
    }

    public record DashboardQuestCard(
            long id,
            String title,
            String type,
            String status,
            OffsetDateTime startAt,
            OffsetDateTime endAt,
            String detailPath
    ) {
    }

    public record DashboardLearningCard(
            long id,
            String title,
            String category,
            String description,
            int progressPercent,
            long viewCount,
            long likeCount,
            long bookmarkCount,
            String detailPath
    ) {
    }

    public record DashboardBoardPost(
            long id,
            String boardCode,
            String title,
            String authorLabel,
            OffsetDateTime createdAt,
            boolean pinned,
            String detailPath
    ) {
    }

    public record DashboardEbookCard(
            long id,
            String title,
            String category,
            String description,
            String detailPath
    ) {
    }

    public record DashboardHomeWidgets(
            DashboardAttendanceCheck attendanceCheck,
            List<DashboardCurriculumSession> curriculumSessions,
            List<DashboardQuestCard> quests,
            List<DashboardLearningCard> materials,
            List<DashboardLearningCard> elearnings,
            List<DashboardBoardPost> freePosts,
            List<DashboardBoardPost> notices,
            List<DashboardEbookCard> ebooks
    ) {
    }

    public record DashboardSummary(
            UserSummary user,
            LevelSummary level,
            AttendanceSummary attendance,
            NotificationsSummary notifications,
            TodaySummary today,
            DashboardHomeWidgets home
    ) {
    }

    public record EducationStatusResponse(
            EducationAttendanceSummary attendance,
            EducationLearningSummary learning,
            EducationQuestSummary quests,
            EducationPointSummary points
    ) {
    }

    public record EducationAttendanceSummary(
            String month,
            int presentDays,
            int lateDays,
            int absentDays,
            long appealPendingCount
    ) {
    }

    public record EducationLearningSummary(
            long inProgressElearningCount,
            long completedRequiredStudyCount,
            long totalRequiredStudyCount,
            long replayWatchMinutes
    ) {
    }

    public record EducationQuestSummary(long openCount, long submittedCount, long lateCount) {
    }

    public record EducationPointSummary(int scholarshipPoint, int experiencePoint, String levelName) {
    }

    public record EbooksResponse(List<EbookItem> items, PageMeta page) {
    }

    public record EbookDetailResponse(EbookItem item) {
    }

    public record EbookAccessLogResponse(EbookItem item, EbookAccessLogItem accessLog) {
    }

    public record EbookItem(
            long id,
            String title,
            String description,
            String thumbnailUrl,
            String category,
            String externalUrl,
            OffsetDateTime createdAt,
            OffsetDateTime lastAccessedAt,
            long accessCount
    ) {
    }

    public record EbookAccessLogItem(long id, long ebookId, OffsetDateTime accessedAt) {
    }

    public record RequiredStudiesResponse(List<RequiredStudyItem> items, PageMeta page) {
    }

    public record RequiredStudyDetailResponse(RequiredStudyItem item) {
    }

    public record RequiredStudyCompleteResponse(RequiredStudyItem item) {
    }

    public record RequiredStudyItem(
            long id,
            String title,
            String description,
            String category,
            String requiredForTrack,
            OffsetDateTime dueAt,
            String contentType,
            String contentUrl,
            String status,
            int progressPercent,
            OffsetDateTime completedAt
    ) {
    }

    public record LiveSessionsResponse(List<LiveSessionItem> items) {
    }

    public record CurrentLiveSessionResponse(LiveSessionItem item) {
    }

    public record LiveSessionJoinResponse(LiveSessionItem item, LiveSessionJoinLogItem joinLog) {
    }

    public record LiveSessionItem(
            long id,
            String title,
            String track,
            String cohort,
            String classRoom,
            OffsetDateTime startsAt,
            OffsetDateTime endsAt,
            String joinUrl,
            String status,
            OffsetDateTime createdAt,
            OffsetDateTime lastJoinedAt,
            long joinCount
    ) {
    }

    public record LiveSessionJoinLogItem(long id, long sessionId, OffsetDateTime joinedAt) {
    }

    public record AttendanceRecordsResponse(
            AttendanceSummary summary,
            AttendanceRange range,
            List<AttendanceDaySummary> days,
            List<AttendanceRecordItem> items
    ) {
    }

    public record AttendanceRange(LocalDate dateFrom, LocalDate dateTo, String status) {
    }

    public record AttendanceDaySummary(
            LocalDate date,
            String status,
            LocalTime firstCheckInAt,
            LocalTime lastCheckOutAt,
            boolean appealAvailable,
            String appealStatus
    ) {
    }

    public record AttendanceRecordItem(
            long id,
            LocalDate date,
            LocalTime checkInAt,
            LocalTime checkOutAt,
            String status,
            String approvalType,
            boolean appealAvailable,
            Long appealId,
            String appealStatus,
            OffsetDateTime appealRequestedAt
    ) {
    }

    public record AttendanceAppealRequest(
            @NotNull @Positive Long attendanceRecordId,
            @NotBlank @Size(max = 50) String type,
            @NotBlank @Size(max = 1000) String reason,
            @Size(max = 50) String requestedStatus
    ) {
    }

    public record AttendanceAppealResolveRequest(
            @NotBlank @Size(max = 50) String status,
            @Size(max = 50) String resolvedStatus,
            @Size(max = 1000) String comment
    ) {
    }

    public record AttendanceAppealResponse(AttendanceAppealItem item) {
    }

    public record AttendanceAppealsResponse(List<AttendanceAppealItem> items) {
    }

    public record AttendanceAppealItem(
            long id,
            long attendanceRecordId,
            String type,
            String reason,
            String requestedStatus,
            String status,
            OffsetDateTime requestedAt,
            LocalDate recordDate,
            String resolvedStatus,
            OffsetDateTime resolvedAt,
            String resolutionComment,
            String resolvedByName,
            boolean demo
    ) {
        public AttendanceAppealItem(
                long id,
                long attendanceRecordId,
                String type,
                String reason,
                String requestedStatus,
                String status,
                OffsetDateTime requestedAt,
                boolean demo
        ) {
            this(id, attendanceRecordId, type, reason, requestedStatus, status, requestedAt, null, null, null, null, null, demo);
        }
    }

    public record NotificationsResponse(List<NotificationItem> items, PageMeta page) {
    }

    public record NotificationReadResponse(NotificationItem item, long unreadCount) {
    }

    public record NotificationsReadAllResponse(List<NotificationItem> items, long unreadCount) {
    }

    public record NotificationDeleteResponse(long id, boolean deleted, long unreadCount) {
    }

    public record NotificationItem(long id, String title, String body, OffsetDateTime createdAt, boolean read) {
    }

    public record CurriculumResponse(List<CurriculumItem> items) {
    }

    public record CurriculumItem(
            long id,
            Integer weekNo,
            LocalDate classDate,
            LocalTime startTime,
            LocalTime endTime,
            String type,
            String title,
            String instructorName,
            String classroom
    ) {
    }

    public record CurriculumWeeksResponse(List<CurriculumWeekItem> items) {
    }

    public record CurriculumWeekDetailResponse(CurriculumWeekItem item) {
    }

    public record CurriculumWeekItem(
            long id,
            String semester,
            Integer weekNumber,
            String track,
            LocalDate startsAt,
            LocalDate endsAt,
            String status,
            int sessionCount,
            List<CurriculumSessionItem> sessions
    ) {
    }

    public record CurriculumSessionItem(
            long id,
            LocalDate date,
            String period,
            String title,
            String instructor,
            String location,
            String sessionType
    ) {
    }

    public record CurriculumScheduleRow(
            long id,
            long termId,
            String semester,
            long contentScopeId,
            Integer weekNumber,
            String track,
            LocalDate classDate,
            LocalTime startTime,
            LocalTime endTime,
            String sessionType,
            String title,
            String instructor,
            String location
    ) {
    }

    public record ReplayResponse(List<ReplayItem> items) {
    }

    public record ReplayDetailResponse(ReplayItem item) {
    }

    public record ReplayWatchLogResponse(ReplayItem item, ReplayWatchLogItem watchLog) {
    }

    public record ReplayItem(
            long id,
            long curriculumScheduleId,
            String title,
            int versionNo,
            OffsetDateTime publishedAt,
            String category,
            String instructor,
            String classroom,
            LocalDate classDate,
            String scope,
            OffsetDateTime lastWatchedAt,
            long watchCount
    ) {
    }

    public record ReplayWatchLogItem(long id, long replayId, OffsetDateTime watchedAt) {
    }

    public record ElearningProgressResponse(List<ElearningProgressItem> items, PageMeta page) {
    }

    public record ElearningProgressDetailResponse(ElearningProgressDetail item) {
    }

    public record ElearningResumeResponse(ElearningResumeItem item) {
    }

    public record ElearningProgressItem(
            long courseId,
            String title,
            String category,
            String thumbnailUrl,
            String provider,
            String description,
            int progressPercent,
            int completedLessons,
            int totalLessons,
            long totalDurationSeconds,
            String lastLessonTitle,
            OffsetDateTime lastLearningAt,
            String status,
            String resumeUrl
    ) {
    }

    public record ElearningProgressDetail(
            long courseId,
            String title,
            String category,
            String thumbnailUrl,
            String provider,
            String description,
            int progressPercent,
            int completedLessons,
            int totalLessons,
            long totalDurationSeconds,
            String lastLessonTitle,
            OffsetDateTime lastLearningAt,
            String status,
            String resumeUrl,
            List<ElearningLessonItem> lessons
    ) {
        public ElearningProgressDetail withLessons(List<ElearningLessonItem> lessons) {
            return new ElearningProgressDetail(
                    courseId,
                    title,
                    category,
                    thumbnailUrl,
                    provider,
                    description,
                    progressPercent,
                    completedLessons,
                    totalLessons,
                    totalDurationSeconds,
                    lastLessonTitle,
                    lastLearningAt,
                    status,
                    resumeUrl,
                    lessons
            );
        }
    }

    public record ElearningLessonItem(
            long lessonId,
            int lessonNo,
            String title,
            long durationSeconds,
            boolean completed,
            OffsetDateTime completedAt
    ) {
    }

    public record ElearningResumeItem(
            long courseId,
            String resumeUrl,
            OffsetDateTime resumedAt,
            String status
    ) {
    }

    public record BookmarksResponse(List<BookmarkItem> items, PageMeta page) {
    }

    public record BookmarkResponse(BookmarkItem item) {
    }

    public record BookmarkDeleteResponse(long id, boolean deleted) {
    }

    public record BookmarkRequest(
            @NotBlank @Size(max = 50) String targetType,
            @NotNull @Positive Long targetId
    ) {
    }

    public record BookmarkItem(
            long id,
            String targetType,
            long targetId,
            String title,
            String description,
            String thumbnailUrl,
            String targetUrl,
            OffsetDateTime createdAt
    ) {
    }

    public record BookmarkSnapshot(
            String targetType,
            long targetId,
            String title,
            String description,
            String thumbnailUrl,
            String targetUrl
    ) {
    }

    public record DocumentRequestsResponse(List<DocumentRequestItem> items, PageMeta page) {
    }

    public record DocumentRequestDetailResponse(DocumentRequestDetail item) {
    }

    public record DocumentSubmissionResponse(DocumentRequestDetail item, DocumentSubmissionItem submission) {
    }

    public record DocumentSubmissionDeleteResponse(long requestId, long submissionId, boolean canceled) {
    }

    public record DocumentAttachmentDownload(DocumentAttachmentItem item, byte[] content) {
    }

    public record DocumentRequestItem(
            long id,
            String title,
            String description,
            String category,
            boolean required,
            String allowedExtensions,
            long maxFileSizeBytes,
            OffsetDateTime startsAt,
            OffsetDateTime dueAt,
            String status,
            OffsetDateTime submittedAt,
            String reviewComment,
            List<DocumentAttachmentItem> attachments
    ) {
        public DocumentRequestItem withAttachments(List<DocumentAttachmentItem> attachments) {
            return new DocumentRequestItem(
                    id,
                    title,
                    description,
                    category,
                    required,
                    allowedExtensions,
                    maxFileSizeBytes,
                    startsAt,
                    dueAt,
                    status,
                    submittedAt,
                    reviewComment,
                    attachments
            );
        }
    }

    public record DocumentRequestDetail(
            long id,
            String title,
            String description,
            String category,
            boolean required,
            String allowedExtensions,
            long maxFileSizeBytes,
            OffsetDateTime startsAt,
            OffsetDateTime dueAt,
            String status,
            OffsetDateTime submittedAt,
            OffsetDateTime reviewedAt,
            String reviewComment,
            List<DocumentAttachmentItem> attachments
    ) {
        public DocumentRequestDetail withAttachments(List<DocumentAttachmentItem> attachments) {
            return new DocumentRequestDetail(
                    id,
                    title,
                    description,
                    category,
                    required,
                    allowedExtensions,
                    maxFileSizeBytes,
                    startsAt,
                    dueAt,
                    status,
                    submittedAt,
                    reviewedAt,
                    reviewComment,
                    attachments
            );
        }
    }

    public record DocumentSubmissionItem(
            long id,
            long requestId,
            String status,
            OffsetDateTime submittedAt,
            List<DocumentAttachmentItem> attachments
    ) {
    }

    public record DocumentAttachmentItem(
            long id,
            long submissionId,
            long requestId,
            String filename,
            String storageKey,
            String mimeType,
            long fileSize,
            OffsetDateTime createdAt
    ) {
    }

    public record DocumentSubmissionRequest(
            @NotBlank @Size(max = 255) String filename,
            @Size(max = 100) String mimeType,
            @NotBlank @Size(max = 3_000_000) String contentBase64
    ) {
    }

    public record PledgesResponse(List<PledgeItem> items, PageMeta page) {
    }

    public record PledgeDetailResponse(PledgeItem item) {
    }

    public record PledgeAgreementResponse(PledgeItem item, PledgeAgreementItem agreement) {
    }

    public record PledgeAgreementRequest(@NotNull Boolean agreed) {
    }

    public record PledgeItem(
            long id,
            String title,
            String content,
            String version,
            boolean required,
            OffsetDateTime startsAt,
            OffsetDateTime dueAt,
            boolean agreed,
            OffsetDateTime agreedAt,
            String versionSnapshot
    ) {
    }

    public record PledgeAgreementItem(
            long id,
            long pledgeId,
            boolean agreed,
            OffsetDateTime agreedAt,
            String versionSnapshot
    ) {
    }

    public record MaterialsResponse(List<MaterialItem> items, PageMeta page) {
    }

    public record MaterialDetailResponse(MaterialItem item) {
    }

    public record MaterialViewResponse(MaterialItem item) {
    }

    public record MaterialResourcesResponse(List<MaterialResourceItem> items) {
    }

    public record MaterialItem(
            long id,
            String title,
            String type,
            String summary,
            String detailUrl,
            int viewCount,
            OffsetDateTime createdAt,
            List<MaterialResourceItem> resources,
            long likeCount,
            long bookmarkCount,
            boolean liked,
            boolean bookmarked
    ) {
        public MaterialItem withResources(List<MaterialResourceItem> resources) {
            return new MaterialItem(
                    id,
                    title,
                    type,
                    summary,
                    detailUrl,
                    viewCount,
                    createdAt,
                    resources,
                    likeCount,
                    bookmarkCount,
                    liked,
                    bookmarked
            );
        }
    }

    public record MaterialReactionResponse(MaterialItem item) {
    }

    public record MaterialResourceItem(
            long id,
            long materialId,
            String type,
            String title,
            String launchMode,
            String targetUrl,
            int displayOrder
    ) {
    }

    public record MaterialResourceAttachmentRequest(
            @NotBlank @Size(max = 255) String filename,
            @Size(max = 100) String mimeType,
            @NotBlank @Size(max = 3_000_000) String contentBase64
    ) {
    }

    public record MaterialResourceAttachmentItem(
            long id,
            long resourceId,
            long materialId,
            String filename,
            String storageKey,
            String storedPath,
            String mimeType,
            long fileSize,
            String checksumSha256,
            OffsetDateTime createdAt
    ) {
    }

    public record MaterialResourceAttachmentCreateResponse(
            MaterialResourceAttachmentItem item,
            MaterialResourceItem resource
    ) {
    }

    public record MaterialResourceAttachmentDownload(
            MaterialResourceAttachmentItem item,
            byte[] content
    ) {
    }

    public record QuestsResponse(
            List<QuestItem> items,
            PageMeta page,
            QuestListSummary summary,
            QuestListFilters filters
    ) {
    }

    public record QuestListSummary(
            long totalCount,
            long progressCount,
            long submittedCount,
            long gradedCount,
            long overdueCount
    ) {
    }

    public record QuestListFilters(String status, String keyword) {
    }

    public record QuestDetailResponse(QuestItem item) {
    }

    public record QuestSubmissionRequest(
            @NotBlank @Size(max = 4000) String content,
            @Size(max = 500) String attachmentUrl
    ) {
    }

    public record QuestSubmissionAttachmentRequest(
            @NotBlank @Size(max = 255) String filename,
            @Size(max = 100) String mimeType,
            @NotBlank @Size(max = 3_000_000) String contentBase64
    ) {
    }

    public record QuestSubmissionResponse(QuestSubmissionItem item) {
    }

    public record QuestSubmissionDetailResponse(QuestSubmissionItem item) {
    }

    public record QuestSubmissionAttachmentCreateResponse(QuestSubmissionAttachmentItem item, QuestSubmissionItem submission) {
    }

    public record QuestSubmissionAttachmentDownload(QuestSubmissionAttachmentItem item, byte[] content) {
    }

    public record QuestSubmissionAttachmentItem(
            long id,
            long questId,
            long submissionId,
            String filename,
            String storageKey,
            String storedPath,
            String mimeType,
            long fileSize,
            String checksumSha256,
            OffsetDateTime createdAt
    ) {
    }

    public record QuestSubmissionItem(
            long id,
            long questId,
            String status,
            OffsetDateTime submittedAt,
            String resultStatus,
            Double score,
            OffsetDateTime gradedAt,
            boolean demo
    ) {
    }

    public record QuestItem(
            long id,
            String title,
            String type,
            String classification,
            OffsetDateTime startAt,
            OffsetDateTime endAt,
            Integer maxExp,
            String status,
            String submitStatus,
            String resultStatus
    ) {
    }

    public record SurveysResponse(List<SurveyItem> items, PageMeta page) {
    }

    public record SurveyDetailResponse(SurveyDetail item) {
    }

    public record SurveyDeleteResponse(SurveyDeleteItem item) {
    }

    public record SurveyDeleteItem(long id, boolean deleted, boolean demo) {
    }

    public record SurveyCreateRequest(
            @NotBlank @Size(max = 255) String title,
            @NotBlank @Size(max = 50) String category,
            boolean required,
            OffsetDateTime startAt,
            OffsetDateTime endAt,
            @NotBlank @Size(max = 50) String status,
            @NotEmpty List<@Valid SurveyQuestionCreateRequest> questions
    ) {
    }

    public record SurveyQuestionCreateRequest(
            @NotBlank @Size(max = 50) String type,
            @NotBlank @Size(max = 1000) String text,
            List<@Valid SurveyOptionCreateRequest> options
    ) {
    }

    public record SurveyOptionCreateRequest(@NotBlank @Size(max = 255) String text) {
    }

    public record SurveyResponseSubmitRequest(@NotEmpty List<@Valid SurveyAnswerRequest> answers) {
    }

    public record SurveyAnswerRequest(
            @Positive long questionId,
            @Size(max = 4000) String answerText,
            List<Long> optionIds
    ) {
    }

    public record SurveyResponseSubmitResponse(SurveyResponseSubmitItem item) {
    }

    public record SurveyResponseDetailResponse(SurveyResponseDetail item) {
    }

    public record SurveyResponseSubmitItem(
            long id,
            long surveyId,
            boolean completed,
            int answerCount,
            OffsetDateTime respondedAt,
            boolean demo
    ) {
    }

    public record SurveyResponseDetail(
            long id,
            long surveyId,
            boolean completed,
            OffsetDateTime respondedAt,
            List<SurveySavedAnswerItem> answers,
            boolean demo
    ) {
    }

    public record SurveySavedAnswerItem(
            long questionId,
            String answerText,
            List<Long> optionIds
    ) {
    }

    public record SurveyItem(
            long id,
            String title,
            String category,
            boolean required,
            OffsetDateTime startAt,
            OffsetDateTime endAt,
            String status,
            boolean completed
    ) {
    }

    public record SurveyDetail(
            long id,
            String title,
            String category,
            boolean required,
            OffsetDateTime startAt,
            OffsetDateTime endAt,
            String status,
            boolean completed,
            long questionCount,
            List<SurveyQuestionItem> questions
    ) {
        public SurveyDetail withQuestions(List<SurveyQuestionItem> questions) {
            return new SurveyDetail(id, title, category, required, startAt, endAt, status, completed, questionCount, questions);
        }
    }

    public record SurveyQuestionItem(
            long id,
            String type,
            String text,
            int displayOrder,
            List<SurveyOptionItem> options
    ) {
    }

    public record SurveyOptionItem(long id, String text, int displayOrder) {
    }

    public record SupportTicketsResponse(List<SupportTicketItem> items, PageMeta page) {
    }

    public record SupportTicketItem(
            long id,
            String title,
            String status,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt,
            OffsetDateTime closedAt,
            long messageCount,
            OffsetDateTime latestMessageAt
    ) {
    }

    public record SupportTicketCreateRequest(
            @NotBlank @Size(max = 255) String title,
            @NotBlank @Size(max = 4000) String content
    ) {
    }

    public record SupportTicketCreateResponse(SupportTicketItem item) {
    }

    public record SupportTicketDetailResponse(SupportTicketDetail item) {
    }

    public record SupportTicketDetail(
            long id,
            String title,
            String status,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt,
            OffsetDateTime closedAt,
            long messageCount,
            OffsetDateTime latestMessageAt,
            List<SupportTicketMessageItem> messages
    ) {
        public static SupportTicketDetail from(SupportTicketItem item, List<SupportTicketMessageItem> messages) {
            return new SupportTicketDetail(
                    item.id(),
                    item.title(),
                    item.status(),
                    item.createdAt(),
                    item.updatedAt(),
                    item.closedAt(),
                    item.messageCount(),
                    item.latestMessageAt(),
                    messages
            );
        }
    }

    public record SupportTicketMessageItem(
            long id,
            long ticketId,
            Long senderUserId,
            String senderName,
            String type,
            String content,
            OffsetDateTime createdAt,
            List<SupportTicketAttachmentItem> attachments
    ) {
        public SupportTicketMessageItem withAttachments(List<SupportTicketAttachmentItem> attachments) {
            return new SupportTicketMessageItem(id, ticketId, senderUserId, senderName, type, content, createdAt, attachments);
        }
    }

    public record SupportTicketMessageRequest(
            @NotBlank @Size(max = 4000) String content
    ) {
    }

    public record SupportTicketMessageCreateResponse(
            SupportTicketMessageItem item,
            SupportTicketItem ticket
    ) {
    }

    public record SupportTicketAttachmentRequest(
            @NotBlank @Size(max = 255) String filename,
            @Size(max = 100) String mimeType,
            @NotBlank @Size(max = 3_000_000) String contentBase64
    ) {
    }

    public record SupportTicketAttachmentItem(
            long id,
            long messageId,
            String filename,
            String storageKey,
            String storedPath,
            String mimeType,
            long fileSize,
            String checksumSha256,
            OffsetDateTime createdAt
    ) {
    }

    public record SupportTicketAttachmentCreateResponse(
            SupportTicketAttachmentItem item,
            SupportTicketMessageItem message
    ) {
    }

    public record SupportTicketAttachmentDownload(
            SupportTicketAttachmentItem item,
            byte[] content
    ) {
    }

    public record ClassmatesResponse(
            List<ClassmateItem> items,
            ClassmateSummary summary,
            ClassmateFilters filters
    ) {
    }

    public record ClassmateSummary(
            long totalCount,
            long learnerCount,
            long coachCount,
            long staffCount
    ) {
    }

    public record ClassmateFilters(String keyword, String memberRole) {
    }

    public record ClassmateNotificationRequest(
            @Size(max = 50) String type,
            @Size(max = 1000) String message
    ) {
    }

    public record ClassmateNotificationResponse(ClassmateNotificationItem item) {
    }

    public record ClassmateNotificationItem(
            long id,
            long recipientUserId,
            String type,
            String message,
            String status,
            OffsetDateTime createdAt,
            NotificationItem notification,
            boolean demo
    ) {
    }

    public record ClassmateItem(
            long id,
            String name,
            String email,
            String role,
            String memberRole,
            String campusName,
            String cohortName,
            String trackName,
            String className
    ) {
    }

    public record ProfileResponse(ProfileDetails profile) {
    }

    public record ProfileUpdateRequest(
            @NotBlank @Size(max = 100) String name,
            @Size(max = 20) String zipCode,
            @Size(max = 255) String addressLine1,
            @Size(max = 255) String addressLine2,
            @Size(max = 30) String mobilePhone,
            @Size(max = 30) String emergencyPhone,
            Boolean marketingOptIn
    ) {
    }

    public record ProfilePasswordChangeRequest(
            @NotBlank String currentPassword,
            @NotBlank @Size(min = 8, max = 72) String newPassword
    ) {
    }

    public record ProfileDetails(
            long id,
            String name,
            String email,
            String role,
            String learnerNo,
            String campusName,
            String cohortName,
            String trackName,
            String className,
            String zipCode,
            String addressLine1,
            String addressLine2,
            String mobilePhone,
            String emergencyPhone,
            boolean marketingOptIn
    ) {
    }

    public record PageMeta(int page, int size, long totalItems, int totalPages) {
    }
}
