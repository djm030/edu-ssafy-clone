package com.edussafy.backend.external.service;

import com.edussafy.backend.board.dto.BoardPostDetailResponse.BoardCommentItem;
import com.edussafy.backend.board.dto.BoardPostDetailResponse.BoardPostDetail;
import com.edussafy.backend.board.dto.BoardPostListItem;
import com.edussafy.backend.board.error.BoardNotFoundException;
import com.edussafy.backend.board.error.BoardPostNotFoundException;
import com.edussafy.backend.board.repository.BoardRepository;
import com.edussafy.backend.board.service.BoardQuery;
import com.edussafy.backend.board.service.BoardSort;
import com.edussafy.backend.external.dto.ExternalServiceDtos.ExternalServiceAccessItem;
import com.edussafy.backend.external.dto.ExternalServiceDtos.ExternalServiceAccessResponse;
import com.edussafy.backend.external.dto.ExternalServiceDtos.ExternalServiceItem;
import com.edussafy.backend.external.dto.ExternalServiceDtos.ExternalServicesResponse;
import com.edussafy.backend.priority.security.AuthSession;
import jakarta.servlet.http.HttpSession;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ExternalServiceService {

    private static final String BOARD_CODE = "external_service";
    private static final String META_START = "<!--EXTERNAL_SERVICE";
    private static final String META_END = "-->";
    private static final String ACCESS_MARKER = "<!--EXTERNAL_SERVICE_ACCESS";

    private final BoardRepository boardRepository;
    private final Clock clock;

    @Autowired
    public ExternalServiceService(BoardRepository boardRepository) {
        this(boardRepository, Clock.systemDefaultZone());
    }

    ExternalServiceService(BoardRepository boardRepository, Clock clock) {
        this.boardRepository = boardRepository;
        this.clock = clock;
    }

    public ExternalServicesResponse services() {
        long boardId = requireBoardId();
        BoardQuery query = new BoardQuery(null, null, 1, 100, "createdAt,asc");
        List<ExternalServiceItem> items = boardRepository.findPosts(boardId, query, BoardSort.parse(query.sort())).stream()
                .map(post -> toItem(detail(boardId, post.id())))
                .toList();
        return new ExternalServicesResponse(items);
    }

    @Transactional
    public ExternalServiceAccessResponse logAccess(String code) {
        long userId = currentActorUserId();
        long boardId = requireBoardId();
        BoardPostDetail detail = findByCode(boardId, code);
        ExternalMeta meta = parseMeta(detail.content(), detail.title());
        if (!meta.enabled()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비활성화된 외부 서비스입니다.");
        }
        OffsetDateTime accessedAt = OffsetDateTime.now(clock);
        boardRepository.createComment(detail.id(), userId, null, ACCESS_MARKER + " code=" + meta.code() + " -->");
        return new ExternalServiceAccessResponse(new ExternalServiceAccessItem(meta.code(), detail.title(), meta.url(), accessedAt));
    }

    private ExternalServiceItem toItem(BoardPostDetail detail) {
        ExternalMeta meta = parseMeta(detail.content(), detail.title());
        List<BoardCommentItem> accesses = accessLogs(detail.id());
        OffsetDateTime lastAccessedAt = accesses.stream()
                .map(BoardCommentItem::createdAt)
                .max(Comparator.naturalOrder())
                .orElse(null);
        return new ExternalServiceItem(
                meta.code(),
                detail.title(),
                meta.url(),
                displayDescription(detail.content()),
                meta.enabled(),
                lastAccessedAt,
                accesses.size()
        );
    }

    private BoardPostDetail findByCode(long boardId, String code) {
        String normalized = normalizeCode(code);
        BoardQuery query = new BoardQuery(null, null, 1, 100, "createdAt,asc");
        return boardRepository.findPosts(boardId, query, BoardSort.parse(query.sort())).stream()
                .map(post -> detail(boardId, post.id()))
                .filter(detail -> normalizeCode(parseMeta(detail.content(), detail.title()).code()).equals(normalized))
                .findFirst()
                .orElseThrow(() -> new BoardPostNotFoundException(0));
    }

    private BoardPostDetail detail(long boardId, long postId) {
        return boardRepository.findPostDetail(boardId, postId).orElseThrow(() -> new BoardPostNotFoundException(postId));
    }

    private List<BoardCommentItem> accessLogs(long postId) {
        return boardRepository.findComments(postId).stream()
                .filter(comment -> comment.content() != null && comment.content().startsWith(ACCESS_MARKER))
                .toList();
    }

    private ExternalMeta parseMeta(String content, String fallbackName) {
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
        return new ExternalMeta(
                values.getOrDefault("code", normalizeCode(fallbackName)),
                values.getOrDefault("url", "#"),
                Boolean.parseBoolean(values.getOrDefault("enabled", "true"))
        );
    }

    private String displayDescription(String content) {
        if (content == null || !content.startsWith(META_START)) {
            return content == null ? "" : content;
        }
        int end = content.indexOf(META_END);
        return end >= 0 ? content.substring(end + META_END.length()).stripLeading() : content;
    }

    private long requireBoardId() {
        return boardRepository.findBoardId(BOARD_CODE).orElseThrow(() -> new BoardNotFoundException(BOARD_CODE));
    }

    private String normalizeCode(String code) {
        return StringUtils.hasText(code) ? code.trim().toUpperCase().replace('-', '_') : "";
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

    private record ExternalMeta(String code, String url, boolean enabled) {
    }
}
