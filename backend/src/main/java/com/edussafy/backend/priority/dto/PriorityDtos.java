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

    public record AttendanceSummary(int present, int late, int absent, boolean appealAvailable) {
    }

    public record NotificationsSummary(long unreadCount, List<NotificationItem> latest) {
    }

    public record TodaySummary(String curriculumTitle, String questTitle, String surveyTitle) {
    }

    public record DashboardSummary(
            UserSummary user,
            LevelSummary level,
            AttendanceSummary attendance,
            NotificationsSummary notifications,
            TodaySummary today
    ) {
    }

    public record AttendanceRecordsResponse(AttendanceSummary summary, List<AttendanceRecordItem> items) {
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

    public record ReplayResponse(List<ReplayItem> items) {
    }

    public record ReplayItem(
            long id,
            long curriculumScheduleId,
            String title,
            int versionNo,
            OffsetDateTime publishedAt
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

    public record QuestsResponse(List<QuestItem> items, PageMeta page) {
    }

    public record QuestDetailResponse(QuestItem item) {
    }

    public record QuestSubmissionRequest(
            @NotBlank @Size(max = 4000) String content,
            @Size(max = 500) String attachmentUrl
    ) {
    }

    public record QuestSubmissionResponse(QuestSubmissionItem item) {
    }

    public record QuestSubmissionDetailResponse(QuestSubmissionItem item) {
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

    public record ClassmatesResponse(List<ClassmateItem> items) {
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
