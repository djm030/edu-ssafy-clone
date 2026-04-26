package com.edussafy.backend.mentoring.service;

import com.edussafy.backend.board.dto.BoardPostDetailResponse.BoardCommentItem;
import com.edussafy.backend.board.dto.BoardPostDetailResponse.BoardPostDetail;
import com.edussafy.backend.board.dto.BoardPostListItem;
import com.edussafy.backend.board.dto.PageMeta;
import com.edussafy.backend.board.error.BoardNotFoundException;
import com.edussafy.backend.board.error.BoardPostNotFoundException;
import com.edussafy.backend.board.repository.BoardRepository;
import com.edussafy.backend.board.service.BoardQuery;
import com.edussafy.backend.board.service.BoardSort;
import com.edussafy.backend.mentoring.dto.MentoringMeetingDtos.ApplicationStatus;
import com.edussafy.backend.mentoring.dto.MentoringMeetingDtos.MeetingStatus;
import com.edussafy.backend.mentoring.dto.MentoringMeetingDtos.MeetingType;
import com.edussafy.backend.mentoring.dto.MentoringMeetingDtos.MentoringMeetingApplicationItem;
import com.edussafy.backend.mentoring.dto.MentoringMeetingDtos.MentoringMeetingApplicationRequest;
import com.edussafy.backend.mentoring.dto.MentoringMeetingDtos.MentoringMeetingApplicationResponse;
import com.edussafy.backend.mentoring.dto.MentoringMeetingDtos.MentoringMeetingApplicationsResponse;
import com.edussafy.backend.mentoring.dto.MentoringMeetingDtos.MentoringMeetingDetail;
import com.edussafy.backend.mentoring.dto.MentoringMeetingDtos.MentoringMeetingItem;
import com.edussafy.backend.mentoring.dto.MentoringMeetingDtos.MentoringMeetingResponse;
import com.edussafy.backend.mentoring.dto.MentoringMeetingDtos.MentoringMeetingsResponse;
import com.edussafy.backend.priority.security.AuthSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MentoringMeetingService {

    private static final String BOARD_CODE = "mentoring_meeting";
    private static final String META_START = "<!--MENTORING_MEETING";
    private static final String META_END = "-->";
    private static final String APPLICATION_MARKER = "<!--MENTORING_MEETING_APPLICATION-->";

    private final BoardRepository boardRepository;
    private final Clock clock;

    public MentoringMeetingService(BoardRepository boardRepository) {
        this(boardRepository, Clock.systemDefaultZone());
    }

    MentoringMeetingService(BoardRepository boardRepository, Clock clock) {
        this.boardRepository = boardRepository;
        this.clock = clock;
    }

    public MentoringMeetingsResponse meetings(int page, int size, String keyword) {
        validatePagination(page, size);
        long boardId = requireBoardId();
        BoardQuery query = new BoardQuery(null, normalizeKeyword(keyword), page, size, "createdAt,desc");
        long totalItems = boardRepository.countPosts(boardId, query);
        List<MentoringMeetingItem> items = totalItems == 0
                ? List.of()
                : boardRepository.findPosts(boardId, query, BoardSort.parse(query.sort())).stream()
                        .map(post -> toItem(post, boardRepository.findPostDetail(boardId, post.id())
                                .orElseThrow(() -> new BoardPostNotFoundException(post.id()))))
                        .toList();
        int totalPages = totalItems == 0 ? 0 : (int) Math.ceil((double) totalItems / size);
        return new MentoringMeetingsResponse(items, new PageMeta(page, size, totalItems, totalPages));
    }

    public MentoringMeetingResponse meeting(long meetingId) {
        long boardId = requireBoardId();
        BoardPostDetail detail = boardRepository.findPostDetail(boardId, meetingId)
                .orElseThrow(() -> new BoardPostNotFoundException(meetingId));
        MeetingMeta meta = parseMeta(detail.content());
        List<BoardCommentItem> applications = applications(meetingId);
        BoardCommentItem mine = myApplication(applications, currentActorUserIdOrNull());
        return new MentoringMeetingResponse(toDetail(detail, meta, applications.size(), mine));
    }

    public MentoringMeetingApplicationsResponse myApplications() {
        long userId = currentActorUserId();
        long boardId = requireBoardId();
        BoardQuery query = new BoardQuery(null, null, 1, 100, "createdAt,desc");
        List<MentoringMeetingApplicationItem> items = boardRepository.findPosts(boardId, query, BoardSort.parse(query.sort())).stream()
                .flatMap(post -> applications(post.id()).stream()
                        .filter(comment -> Objects.equals(comment.authorUserId(), userId))
                        .map(comment -> new MentoringMeetingApplicationItem(
                                post.id(),
                                post.title(),
                                ApplicationStatus.APPLIED,
                                displayApplicationContent(comment.content()),
                                comment.createdAt(),
                                null
                        )))
                .toList();
        return new MentoringMeetingApplicationsResponse(items);
    }

    @Transactional
    public MentoringMeetingApplicationResponse apply(long meetingId, MentoringMeetingApplicationRequest request) {
        long userId = currentActorUserId();
        long boardId = requireBoardId();
        BoardPostDetail detail = boardRepository.findPostDetail(boardId, meetingId)
                .orElseThrow(() -> new BoardPostNotFoundException(meetingId));
        MeetingMeta meta = parseMeta(detail.content());
        if (effectiveStatus(meta) != MeetingStatus.RECRUITING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "모집 중인 간담회만 신청할 수 있습니다.");
        }
        List<BoardCommentItem> applications = applications(meetingId);
        if (myApplication(applications, userId) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 신청한 간담회입니다.");
        }
        if (applications.size() >= meta.capacity()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "간담회 정원이 마감되었습니다.");
        }
        String motivation = trimRequired(request.motivation(), "신청 동기");
        long commentId = boardRepository.createComment(meetingId, userId, null, APPLICATION_MARKER + "\n" + motivation);
        BoardCommentItem saved = boardRepository.findComment(commentId)
                .orElseThrow(() -> new BoardPostNotFoundException(meetingId));
        return new MentoringMeetingApplicationResponse(toApplication(detail, saved, ApplicationStatus.APPLIED, null));
    }

    @Transactional
    public MentoringMeetingApplicationResponse cancel(long meetingId) {
        long userId = currentActorUserId();
        long boardId = requireBoardId();
        BoardPostDetail detail = boardRepository.findPostDetail(boardId, meetingId)
                .orElseThrow(() -> new BoardPostNotFoundException(meetingId));
        BoardCommentItem mine = myApplication(applications(meetingId), userId);
        if (mine == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "신청 내역을 찾을 수 없습니다.");
        }
        int deleted = boardRepository.deleteComment(meetingId, mine.id());
        if (deleted == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "신청 내역을 찾을 수 없습니다.");
        }
        return new MentoringMeetingApplicationResponse(toApplication(detail, mine, ApplicationStatus.CANCELLED, OffsetDateTime.now(clock)));
    }

    private long requireBoardId() {
        return boardRepository.findBoardId(BOARD_CODE)
                .orElseThrow(() -> new BoardNotFoundException(BOARD_CODE));
    }

    private MentoringMeetingItem toItem(BoardPostListItem post, BoardPostDetail detail) {
        MeetingMeta meta = parseMeta(detail.content());
        List<BoardCommentItem> applications = applications(post.id());
        BoardCommentItem mine = myApplication(applications, currentActorUserIdOrNull());
        return new MentoringMeetingItem(
                post.id(),
                post.title(),
                displayDescription(detail.content()),
                meta.meetingType(),
                meta.topic(),
                meta.capacity(),
                applications.size(),
                meta.startsAt(),
                meta.endsAt(),
                meta.applicationStartsAt(),
                meta.applicationEndsAt(),
                effectiveStatus(meta),
                meta.location(),
                meta.meetingUrl(),
                mine == null ? null : ApplicationStatus.APPLIED
        );
    }

    private MentoringMeetingDetail toDetail(
            BoardPostDetail detail,
            MeetingMeta meta,
            int appliedCount,
            BoardCommentItem mine
    ) {
        return new MentoringMeetingDetail(
                detail.id(),
                detail.title(),
                displayDescription(detail.content()),
                meta.meetingType(),
                meta.topic(),
                meta.capacity(),
                appliedCount,
                meta.startsAt(),
                meta.endsAt(),
                meta.applicationStartsAt(),
                meta.applicationEndsAt(),
                effectiveStatus(meta),
                meta.location(),
                meta.meetingUrl(),
                mine == null ? null : ApplicationStatus.APPLIED,
                mine == null ? null : displayApplicationContent(mine.content())
        );
    }

    private MentoringMeetingApplicationItem toApplication(
            BoardPostDetail detail,
            BoardCommentItem comment,
            ApplicationStatus status,
            OffsetDateTime cancelledAt
    ) {
        return new MentoringMeetingApplicationItem(
                detail.id(),
                detail.title(),
                status,
                displayApplicationContent(comment.content()),
                comment.createdAt(),
                cancelledAt
        );
    }

    private MeetingStatus effectiveStatus(MeetingMeta meta) {
        OffsetDateTime now = OffsetDateTime.now(clock);
        if (now.isAfter(meta.endsAt())) {
            return MeetingStatus.DONE;
        }
        if (now.isBefore(meta.applicationStartsAt()) || now.isAfter(meta.applicationEndsAt())) {
            return MeetingStatus.CLOSED;
        }
        return MeetingStatus.RECRUITING;
    }

    private List<BoardCommentItem> applications(long meetingId) {
        return boardRepository.findComments(meetingId).stream()
                .filter(comment -> comment.content() != null && comment.content().startsWith(APPLICATION_MARKER))
                .toList();
    }

    private BoardCommentItem myApplication(List<BoardCommentItem> applications, Long userId) {
        if (userId == null) {
            return null;
        }
        return applications.stream()
                .filter(comment -> Objects.equals(comment.authorUserId(), userId))
                .findFirst()
                .orElse(null);
    }

    private MeetingMeta parseMeta(String content) {
        Map<String, String> values = new LinkedHashMap<>();
        if (content != null && content.startsWith(META_START)) {
            int end = content.indexOf(META_END);
            if (end > META_START.length()) {
                String metadata = content.substring(META_START.length(), end).trim();
                for (String line : metadata.split("\\R")) {
                    int separator = line.indexOf('=');
                    if (separator > 0) {
                        values.put(line.substring(0, separator).trim(), line.substring(separator + 1).trim());
                    }
                }
            }
        }
        return new MeetingMeta(
                MeetingType.valueOf(values.getOrDefault("type", "ONLINE")),
                values.getOrDefault("topic", "멘토링"),
                parsePositiveInt(values.get("capacity"), 20),
                parseTime(values.get("startsAt"), "2026-05-01T19:00:00+09:00"),
                parseTime(values.get("endsAt"), "2026-05-01T20:30:00+09:00"),
                parseTime(values.get("applicationStartsAt"), "2026-04-01T00:00:00+09:00"),
                parseTime(values.get("applicationEndsAt"), "2099-12-31T23:59:59+09:00"),
                values.getOrDefault("location", "온라인"),
                values.get("meetingUrl")
        );
    }

    private String displayDescription(String content) {
        if (content == null) {
            return "";
        }
        if (!content.startsWith(META_START)) {
            return content;
        }
        int end = content.indexOf(META_END);
        return end >= 0 ? content.substring(end + META_END.length()).stripLeading() : content;
    }

    private String displayApplicationContent(String content) {
        if (content == null) {
            return "";
        }
        return content.startsWith(APPLICATION_MARKER)
                ? content.substring(APPLICATION_MARKER.length()).stripLeading()
                : content;
    }

    private int parsePositiveInt(String value, int fallback) {
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? parsed : fallback;
        } catch (RuntimeException exception) {
            return fallback;
        }
    }

    private OffsetDateTime parseTime(String value, String fallback) {
        return OffsetDateTime.parse(StringUtils.hasText(value) ? value : fallback);
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

    private String normalizeKeyword(String keyword) {
        return StringUtils.hasText(keyword) ? keyword.trim() : null;
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

    private record MeetingMeta(
            MeetingType meetingType,
            String topic,
            int capacity,
            OffsetDateTime startsAt,
            OffsetDateTime endsAt,
            OffsetDateTime applicationStartsAt,
            OffsetDateTime applicationEndsAt,
            String location,
            String meetingUrl
    ) {
    }
}
