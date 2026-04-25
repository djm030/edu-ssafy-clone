package com.edussafy.backend.priority.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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

    public record AuthActionResponse(boolean success, String message) {
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
            boolean appealAvailable
    ) {
    }

    public record AttendanceAppealRequest(
            @NotBlank @Size(max = 50) String type,
            @NotBlank @Size(max = 1000) String reason,
            @Size(max = 50) String requestedStatus
    ) {
    }

    public record AttendanceAppealResponse(AttendanceAppealItem item) {
    }

    public record AttendanceAppealItem(
            long id,
            String type,
            String reason,
            String requestedStatus,
            String status,
            OffsetDateTime requestedAt,
            boolean demo
    ) {
    }

    public record NotificationsResponse(List<NotificationItem> items, PageMeta page) {
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

    public record MaterialResourcesResponse(List<MaterialResourceItem> items) {
    }

    public record MaterialReactionRequest(@NotBlank @Size(max = 50) String type) {
    }

    public record MaterialReactionResponse(MaterialReactionItem item) {
    }

    public record MaterialReactionItem(
            long materialId,
            String type,
            boolean active,
            long likeCount,
            long bookmarkCount,
            long favoriteCount,
            boolean liked,
            boolean bookmarked,
            boolean favorited,
            boolean demo
    ) {
    }

    public record MaterialReactionSummary(
            long materialId,
            long likeCount,
            long bookmarkCount,
            long favoriteCount,
            boolean liked,
            boolean bookmarked,
            boolean favorited
    ) {
        public static MaterialReactionSummary empty(long materialId) {
            return new MaterialReactionSummary(materialId, 0, 0, 0, false, false, false);
        }
    }

    public record MaterialItem(
            long id,
            String title,
            String type,
            String summary,
            String detailUrl,
            int viewCount,
            OffsetDateTime createdAt,
            long likeCount,
            long bookmarkCount,
            long favoriteCount,
            boolean liked,
            boolean bookmarked,
            boolean favorited,
            List<MaterialResourceItem> resources
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
                    likeCount,
                    bookmarkCount,
                    favoriteCount,
                    liked,
                    bookmarked,
                    favorited,
                    resources
            );
        }

        public MaterialItem withReactions(MaterialReactionSummary reactions) {
            return new MaterialItem(
                    id,
                    title,
                    type,
                    summary,
                    detailUrl,
                    viewCount,
                    createdAt,
                    reactions.likeCount(),
                    reactions.bookmarkCount(),
                    reactions.favoriteCount(),
                    reactions.liked(),
                    reactions.bookmarked(),
                    reactions.favorited(),
                    resources
            );
        }
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

    public record QuestSubmissionItem(
            long id,
            long questId,
            String status,
            OffsetDateTime submittedAt,
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

    public record SurveyResponseSubmitItem(
            long id,
            long surveyId,
            boolean completed,
            int answerCount,
            OffsetDateTime respondedAt,
            boolean demo
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
            long questionCount
    ) {
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

    public record AttachmentUploadRequest(
            @NotBlank @Size(max = 255) String originalFilename,
            @Size(max = 100) String storageKey,
            @Size(max = 500) String storedPath,
            @Size(max = 100) String mimeType,
            Long fileSize,
            @Size(max = 64) String checksumSha256
    ) {
    }

    public record AttachmentResponse(AttachmentItem item) {
    }

    public record AttachmentItem(
            long id,
            String originalFilename,
            String storageKey,
            String storedPath,
            String mimeType,
            Long fileSize,
            String checksumSha256,
            String downloadUrl,
            OffsetDateTime createdAt,
            boolean demo
    ) {
    }

    public record SupportTicketDetailResponse(SupportTicketDetail item) {
    }

    public record SupportTicketDetail(
            SupportTicketItem ticket,
            List<SupportTicketMessageItem> messages,
            List<AttachmentItem> attachments
    ) {
    }

    public record SupportTicketMessageCreateRequest(
            @NotBlank @Size(max = 4000) String content,
            List<Long> attachmentIds
    ) {
    }

    public record SupportTicketAnswerRequest(
            @NotBlank @Size(max = 4000) String content,
            List<Long> attachmentIds
    ) {
    }

    public record SupportTicketStatusUpdateRequest(@NotBlank @Size(max = 50) String status) {
    }

    public record SupportTicketMessageResponse(SupportTicketMessageItem item) {
    }

    public record SupportTicketStatusResponse(SupportTicketItem item) {
    }

    public record SupportTicketAttachmentRequest(@Positive long attachmentId) {
    }

    public record SupportTicketAttachmentResponse(long ticketId, AttachmentItem attachment, boolean demo) {
    }

    public record SupportTicketMessagesResponse(List<SupportTicketMessageItem> items) {
    }

    public record SupportTicketMessageItem(
            long id,
            long ticketId,
            long senderUserId,
            String type,
            String content,
            OffsetDateTime createdAt,
            List<AttachmentItem> attachments,
            boolean demo
    ) {
    }

    public record NotificationActionResponse(NotificationItem item) {
    }

    public record NotificationSendRequest(
            @NotBlank @Size(max = 255) String title,
            @NotBlank @Size(max = 1000) String body,
            List<Long> recipientUserIds
    ) {
    }

    public record PageMeta(int page, int size, long totalItems, int totalPages) {
    }
}
