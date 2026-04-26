package com.edussafy.backend.mentoring.service;

import com.edussafy.backend.board.dto.BoardPostDetailResponse.BoardCommentItem;
import com.edussafy.backend.board.dto.BoardPostDetailResponse.BoardPostDetail;
import com.edussafy.backend.board.dto.BoardPostListItem;
import com.edussafy.backend.board.dto.CategoryItem;
import com.edussafy.backend.board.dto.PageMeta;
import com.edussafy.backend.board.error.BoardNotFoundException;
import com.edussafy.backend.board.error.BoardPostNotFoundException;
import com.edussafy.backend.board.repository.BoardRepository;
import com.edussafy.backend.board.service.BoardQuery;
import com.edussafy.backend.board.service.BoardSort;
import com.edussafy.backend.mentoring.dto.MentoringMeetingResultDtos.MentoringMeetingResultDetail;
import com.edussafy.backend.mentoring.dto.MentoringMeetingResultDtos.MentoringMeetingResultItem;
import com.edussafy.backend.mentoring.dto.MentoringMeetingResultDtos.MentoringMeetingResultResponse;
import com.edussafy.backend.mentoring.dto.MentoringMeetingResultDtos.MentoringMeetingResultsResponse;
import com.edussafy.backend.mentoring.dto.MentoringMeetingResultDtos.MentoringMeetingReviewCreateRequest;
import com.edussafy.backend.mentoring.dto.MentoringMeetingResultDtos.MentoringMeetingReviewDetail;
import com.edussafy.backend.mentoring.dto.MentoringMeetingResultDtos.MentoringMeetingReviewItem;
import com.edussafy.backend.mentoring.dto.MentoringMeetingResultDtos.MentoringMeetingReviewResponse;
import com.edussafy.backend.mentoring.dto.MentoringMeetingResultDtos.MentoringMeetingReviewUpdateRequest;
import com.edussafy.backend.mentoring.dto.MentoringMeetingResultDtos.MentoringMeetingReviewsResponse;
import com.edussafy.backend.priority.security.AuthSession;
import jakarta.servlet.http.HttpSession;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MentoringMeetingResultService {

    private static final String MEETING_BOARD_CODE = "mentoring_meeting";
    private static final String RESULT_BOARD_CODE = "mentoring_meeting_result";
    private static final String REVIEW_BOARD_CODE = "mentoring_meeting_review";
    private static final String MEETING_META_START = "<!--MENTORING_MEETING";
    private static final String RESULT_META_START = "<!--MENTORING_MEETING_RESULT";
    private static final String REVIEW_META_START = "<!--MENTORING_MEETING_REVIEW";
    private static final String META_END = "-->";
    private static final String APPLICATION_MARKER = "<!--MENTORING_MEETING_APPLICATION-->";

    private final BoardRepository boardRepository;
    private final Clock clock;

    @Autowired
    public MentoringMeetingResultService(BoardRepository boardRepository) {
        this(boardRepository, Clock.systemDefaultZone());
    }

    MentoringMeetingResultService(BoardRepository boardRepository, Clock clock) {
        this.boardRepository = boardRepository;
        this.clock = clock;
    }

    public MentoringMeetingResultsResponse results(int page, int size) {
        validatePagination(page, size);
        long boardId = requireBoardId(RESULT_BOARD_CODE);
        BoardQuery query = new BoardQuery(null, null, page, size, "createdAt,desc");
        long totalItems = boardRepository.countPosts(boardId, query);
        List<MentoringMeetingResultItem> items = totalItems == 0 ? List.of() : boardRepository.findPosts(boardId, query, BoardSort.parse(query.sort())).stream()
                .map(post -> toResultItem(post, resultDetail(boardId, post.id())))
                .toList();
        int totalPages = totalItems == 0 ? 0 : (int) Math.ceil((double) totalItems / size);
        return new MentoringMeetingResultsResponse(items, new PageMeta(page, size, totalItems, totalPages));
    }

    public MentoringMeetingResultResponse result(long meetingId) {
        long boardId = requireBoardId(RESULT_BOARD_CODE);
        BoardPostDetail detail = findResultByMeetingId(boardId, meetingId)
                .orElseThrow(() -> new BoardPostNotFoundException(meetingId));
        ResultMeta meta = parseResultMeta(detail.content(), detail.id());
        List<MentoringMeetingReviewItem> reviews = reviewDetailsForMeeting(meta.meetingId()).stream()
                .map(this::toReviewItem)
                .toList();
        double averageRating = averageRating(reviews);
        return new MentoringMeetingResultResponse(new MentoringMeetingResultDetail(
                meta.meetingId(),
                detail.id(),
                detail.title(),
                displayContent(detail.content(), RESULT_META_START),
                meta.startsAt(),
                meta.endedAt(),
                meta.participantCount(),
                reviews,
                averageRating
        ));
    }

    public MentoringMeetingReviewsResponse reviews(int page, int size) {
        validatePagination(page, size);
        long boardId = requireBoardId(REVIEW_BOARD_CODE);
        BoardQuery query = new BoardQuery(null, null, page, size, "createdAt,desc");
        long totalItems = boardRepository.countPosts(boardId, query);
        List<MentoringMeetingReviewItem> items = totalItems == 0 ? List.of() : boardRepository.findPosts(boardId, query, BoardSort.parse(query.sort())).stream()
                .map(post -> toReviewItem(reviewDetail(boardId, post.id())))
                .toList();
        int totalPages = totalItems == 0 ? 0 : (int) Math.ceil((double) totalItems / size);
        return new MentoringMeetingReviewsResponse(items, new PageMeta(page, size, totalItems, totalPages));
    }

    public MentoringMeetingReviewResponse review(long reviewId) {
        long boardId = requireBoardId(REVIEW_BOARD_CODE);
        return new MentoringMeetingReviewResponse(toReviewDetail(reviewDetail(boardId, reviewId)));
    }

    @Transactional
    public MentoringMeetingReviewResponse createReview(MentoringMeetingReviewCreateRequest request) {
        long userId = currentActorUserId();
        validateReviewAllowed(request.meetingId(), userId);
        long boardId = requireBoardId(REVIEW_BOARD_CODE);
        Long categoryId = firstCategoryId(boardId);
        long postId = boardRepository.createPost(
                boardId,
                categoryId,
                userId,
                trimRequired(request.title(), "후기 제목"),
                reviewContent(request.meetingId(), request.rating(), trimRequired(request.content(), "후기 내용"))
        );
        return new MentoringMeetingReviewResponse(toReviewDetail(reviewDetail(boardId, postId)));
    }

    @Transactional
    public MentoringMeetingReviewResponse updateReview(long reviewId, MentoringMeetingReviewUpdateRequest request) {
        long boardId = requireBoardId(REVIEW_BOARD_CODE);
        BoardPostDetail existing = reviewDetail(boardId, reviewId);
        requireOwner(existing);
        ReviewMeta meta = parseReviewMeta(existing.content());
        int updated = boardRepository.updatePost(
                boardId,
                reviewId,
                existing.category() == null ? firstCategoryId(boardId) : existing.category().id(),
                trimRequired(request.title(), "후기 제목"),
                reviewContent(meta.meetingId(), request.rating(), trimRequired(request.content(), "후기 내용"))
        );
        if (updated == 0) {
            throw new BoardPostNotFoundException(reviewId);
        }
        return new MentoringMeetingReviewResponse(toReviewDetail(reviewDetail(boardId, reviewId)));
    }

    @Transactional
    public MentoringMeetingReviewResponse deleteReview(long reviewId) {
        long boardId = requireBoardId(REVIEW_BOARD_CODE);
        BoardPostDetail existing = reviewDetail(boardId, reviewId);
        requireOwner(existing);
        MentoringMeetingReviewDetail deletedItem = toReviewDetail(existing);
        int deleted = boardRepository.deletePost(boardId, reviewId);
        if (deleted == 0) {
            throw new BoardPostNotFoundException(reviewId);
        }
        return new MentoringMeetingReviewResponse(deletedItem);
    }

    private BoardPostDetail resultDetail(long boardId, long postId) {
        return boardRepository.findPostDetail(boardId, postId).orElseThrow(() -> new BoardPostNotFoundException(postId));
    }

    private BoardPostDetail reviewDetail(long boardId, long postId) {
        return boardRepository.findPostDetail(boardId, postId).orElseThrow(() -> new BoardPostNotFoundException(postId));
    }

    private MentoringMeetingResultItem toResultItem(BoardPostListItem post, BoardPostDetail detail) {
        ResultMeta meta = parseResultMeta(detail.content(), detail.id());
        List<MentoringMeetingReviewItem> reviews = reviewDetailsForMeeting(meta.meetingId()).stream().map(this::toReviewItem).toList();
        return new MentoringMeetingResultItem(
                meta.meetingId(),
                post.id(),
                post.title(),
                excerpt(displayContent(detail.content(), RESULT_META_START)),
                meta.startsAt(),
                meta.endedAt(),
                meta.participantCount(),
                reviews.size(),
                averageRating(reviews)
        );
    }

    private MentoringMeetingReviewItem toReviewItem(BoardPostDetail detail) {
        ReviewMeta meta = parseReviewMeta(detail.content());
        return new MentoringMeetingReviewItem(
                detail.id(),
                meta.meetingId(),
                meetingTitle(meta.meetingId()),
                detail.title(),
                excerpt(displayContent(detail.content(), REVIEW_META_START)),
                meta.rating(),
                detail.authorName(),
                detail.createdAt(),
                isEditable(detail)
        );
    }

    private MentoringMeetingReviewDetail toReviewDetail(BoardPostDetail detail) {
        ReviewMeta meta = parseReviewMeta(detail.content());
        return new MentoringMeetingReviewDetail(
                detail.id(),
                meta.meetingId(),
                meetingTitle(meta.meetingId()),
                detail.title(),
                displayContent(detail.content(), REVIEW_META_START),
                meta.rating(),
                detail.authorName(),
                detail.createdAt(),
                detail.updatedAt(),
                isEditable(detail)
        );
    }

    private List<BoardPostDetail> reviewDetailsForMeeting(long meetingId) {
        long boardId = requireBoardId(REVIEW_BOARD_CODE);
        BoardQuery query = new BoardQuery(null, null, 1, 100, "createdAt,desc");
        return boardRepository.findPosts(boardId, query, BoardSort.parse(query.sort())).stream()
                .map(post -> reviewDetail(boardId, post.id()))
                .filter(detail -> parseReviewMeta(detail.content()).meetingId() == meetingId)
                .toList();
    }

    private double averageRating(List<MentoringMeetingReviewItem> reviews) {
        return reviews.stream().mapToInt(MentoringMeetingReviewItem::rating).average().orElse(0.0);
    }

    private String meetingTitle(long meetingId) {
        long meetingBoardId = requireBoardId(MEETING_BOARD_CODE);
        return boardRepository.findPostDetail(meetingBoardId, meetingId).map(BoardPostDetail::title).orElse("간담회");
    }

    private void validateReviewAllowed(long meetingId, long userId) {
        long meetingBoardId = requireBoardId(MEETING_BOARD_CODE);
        BoardPostDetail meeting = boardRepository.findPostDetail(meetingBoardId, meetingId)
                .orElseThrow(() -> new BoardPostNotFoundException(meetingId));
        MeetingMeta meta = parseMeetingMeta(meeting.content());
        if (!OffsetDateTime.now(clock).isAfter(meta.endsAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "종료된 간담회만 후기를 작성할 수 있습니다.");
        }
        boolean applied = boardRepository.findComments(meetingId).stream()
                .filter(comment -> comment.content() != null && comment.content().startsWith(APPLICATION_MARKER))
                .anyMatch(comment -> Objects.equals(comment.authorUserId(), userId));
        if (!applied) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "간담회 신청자만 후기를 작성할 수 있습니다.");
        }
    }

    private Optional<BoardPostDetail> findResultByMeetingId(long resultBoardId, long meetingId) {
        BoardQuery query = new BoardQuery(null, null, 1, 100, "createdAt,desc");
        return boardRepository.findPosts(resultBoardId, query, BoardSort.parse(query.sort())).stream()
                .map(post -> resultDetail(resultBoardId, post.id()))
                .filter(detail -> parseResultMeta(detail.content(), detail.id()).meetingId() == meetingId)
                .findFirst();
    }

    private String reviewContent(long meetingId, int rating, String content) {
        return """
                <!--MENTORING_MEETING_REVIEW
                meetingId=%d
                rating=%d
                -->
                %s
                """.formatted(meetingId, rating, content).stripTrailing();
    }

    private ResultMeta parseResultMeta(String content, long fallbackMeetingId) {
        Map<String, String> values = parseMeta(content, RESULT_META_START);
        return new ResultMeta(
                parseLong(values.get("meetingId"), fallbackMeetingId),
                parseTime(values.get("startsAt"), "2026-04-01T19:00:00+09:00"),
                parseTime(values.get("endedAt"), values.getOrDefault("endsAt", "2026-04-01T20:30:00+09:00")),
                parsePositiveInt(values.get("participantCount"), 0)
        );
    }

    private ReviewMeta parseReviewMeta(String content) {
        Map<String, String> values = parseMeta(content, REVIEW_META_START);
        return new ReviewMeta(parseLong(values.get("meetingId"), 0), parsePositiveInt(values.get("rating"), 5));
    }

    private MeetingMeta parseMeetingMeta(String content) {
        Map<String, String> values = parseMeta(content, MEETING_META_START);
        return new MeetingMeta(parseTime(values.get("endsAt"), "2026-04-01T20:30:00+09:00"));
    }

    private Map<String, String> parseMeta(String content, String startMarker) {
        Map<String, String> values = new LinkedHashMap<>();
        if (content != null && content.startsWith(startMarker)) {
            int end = content.indexOf(META_END);
            if (end > startMarker.length()) {
                String metadata = content.substring(startMarker.length(), end).trim();
                for (String line : metadata.split("\\R")) {
                    int separator = line.indexOf('=');
                    if (separator > 0) {
                        values.put(line.substring(0, separator).trim(), line.substring(separator + 1).trim());
                    }
                }
            }
        }
        return values;
    }

    private String displayContent(String content, String marker) {
        if (content == null || !content.startsWith(marker)) {
            return content == null ? "" : content;
        }
        int end = content.indexOf(META_END);
        return end >= 0 ? content.substring(end + META_END.length()).stripLeading() : content;
    }

    private String excerpt(String value) {
        String normalized = value == null ? "" : value.strip();
        return normalized.length() <= 120 ? normalized : normalized.substring(0, 117) + "...";
    }

    private Long firstCategoryId(long boardId) {
        return boardRepository.findCategories(boardId).stream().findFirst().map(CategoryItem::id).orElse(null);
    }

    private long requireBoardId(String boardCode) {
        return boardRepository.findBoardId(boardCode).orElseThrow(() -> new BoardNotFoundException(boardCode));
    }

    private void requireOwner(BoardPostDetail detail) {
        long actorUserId = currentActorUserId();
        if (isModerator()) {
            return;
        }
        if (!Objects.equals(detail.authorUserId(), actorUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "후기 작성자만 수정하거나 삭제할 수 있습니다.");
        }
    }

    private boolean isEditable(BoardPostDetail detail) {
        Long actorUserId = currentActorUserIdOrNull();
        return actorUserId != null && (isModerator() || Objects.equals(detail.authorUserId(), actorUserId));
    }

    private boolean isModerator() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            Object role = attributes.getRequest().getAttribute("currentRole");
            return "coach".equals(role) || "admin".equals(role);
        }
        return false;
    }

    private Long currentActorUserIdOrNull() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            Object currentUserId = attributes.getRequest().getAttribute("currentUserId");
            if (currentUserId instanceof Number number) {
                return number.longValue();
            }
            HttpSession session = attributes.getRequest().getSession(false);
            return AuthSession.currentUserId(session).orElse(null);
        }
        return boardRepository.findDefaultAuthorUserId().orElse(null);
    }

    private long currentActorUserId() {
        Long userId = currentActorUserIdOrNull();
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return userId;
    }

    private String trimRequired(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + "은 필수입니다.");
        }
        return value.trim();
    }

    private void validatePagination(int page, int size) {
        if (page < 1 || size < 1 || size > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page and size must be within the supported range.");
        }
    }

    private int parsePositiveInt(String value, int fallback) {
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? parsed : fallback;
        } catch (RuntimeException exception) {
            return fallback;
        }
    }

    private long parseLong(String value, long fallback) {
        try {
            return Long.parseLong(value);
        } catch (RuntimeException exception) {
            return fallback;
        }
    }

    private OffsetDateTime parseTime(String value, String fallback) {
        return OffsetDateTime.parse(StringUtils.hasText(value) ? value : fallback);
    }

    private record ResultMeta(long meetingId, OffsetDateTime startsAt, OffsetDateTime endedAt, int participantCount) {
    }

    private record ReviewMeta(long meetingId, int rating) {
    }

    private record MeetingMeta(OffsetDateTime endsAt) {
    }
}
