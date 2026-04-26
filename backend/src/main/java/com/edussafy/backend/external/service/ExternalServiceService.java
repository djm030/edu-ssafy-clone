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
import org.springframework.core.env.Environment;
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
    private static final String CONFIG_PREFIX = "edussafy.external-services.";

    private final BoardRepository boardRepository;
    private final Clock clock;
    private final Environment environment;

    @Autowired
    public ExternalServiceService(BoardRepository boardRepository, Environment environment) {
        this(boardRepository, Clock.systemDefaultZone(), environment);
    }

    ExternalServiceService(BoardRepository boardRepository, Clock clock) {
        this(boardRepository, clock, null);
    }

    ExternalServiceService(BoardRepository boardRepository, Clock clock, Environment environment) {
        this.boardRepository = boardRepository;
        this.clock = clock;
        this.environment = environment;
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
        LaunchPolicy policy = launchPolicy(meta);
        if (!policy.launchable()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, policy.disabledReason());
        }
        OffsetDateTime accessedAt = OffsetDateTime.now(clock);
        boardRepository.createComment(detail.id(), userId, null, ACCESS_MARKER + " code=" + meta.code() + " launchType=" + policy.launchType() + " -->");
        return new ExternalServiceAccessResponse(new ExternalServiceAccessItem(
                meta.code(),
                detail.title(),
                policy.url(),
                policy.launchType(),
                policy.openInNewWindow(),
                accessedAt
        ));
    }

    private ExternalServiceItem toItem(BoardPostDetail detail) {
        ExternalMeta meta = parseMeta(detail.content(), detail.title());
        LaunchPolicy policy = launchPolicy(meta);
        List<BoardCommentItem> accesses = accessLogs(detail.id());
        OffsetDateTime lastAccessedAt = accesses.stream()
                .map(BoardCommentItem::createdAt)
                .max(Comparator.naturalOrder())
                .orElse(null);
        return new ExternalServiceItem(
                meta.code(),
                detail.title(),
                policy.url(),
                displayDescription(detail.content()),
                policy.enabled(),
                policy.launchable(),
                policy.launchType(),
                policy.policyLabel(),
                policy.disabledReason(),
                policy.requiresAuth(),
                policy.openInNewWindow(),
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
                Boolean.parseBoolean(values.getOrDefault("enabled", "true")),
                values.getOrDefault("launchType", "EXTERNAL_LINK"),
                values.getOrDefault("policyLabel", "외부 링크"),
                values.getOrDefault("disabledReason", ""),
                Boolean.parseBoolean(values.getOrDefault("requiresAuth", "true")),
                Boolean.parseBoolean(values.getOrDefault("openInNewWindow", "true"))
        );
    }

    private LaunchPolicy launchPolicy(ExternalMeta meta) {
        String propertyKey = propertyKey(meta.code());
        String configuredUrl = property(CONFIG_PREFIX + propertyKey + ".url");
        String url = StringUtils.hasText(configuredUrl) ? configuredUrl.trim() : meta.url();
        boolean enabled = meta.enabled();
        String configuredEnabled = property(CONFIG_PREFIX + propertyKey + ".enabled");
        if (StringUtils.hasText(configuredEnabled)) {
            enabled = Boolean.parseBoolean(configuredEnabled);
        }
        String configuredReason = property(CONFIG_PREFIX + propertyKey + ".disabled-reason");
        String disabledReason = StringUtils.hasText(configuredReason) ? configuredReason.trim() : meta.disabledReason();
        boolean launchable = enabled && isLaunchUrl(url);
        if (!launchable && !StringUtils.hasText(disabledReason)) {
            disabledReason = enabled
                    ? "외부 서비스 URL이 아직 운영 설정으로 연결되지 않았습니다."
                    : "현재 계정 또는 운영 정책상 외부 서비스가 비활성화되어 있습니다.";
        }
        return new LaunchPolicy(
                url,
                enabled,
                launchable,
                normalizeLaunchType(meta.launchType()),
                meta.policyLabel(),
                disabledReason,
                meta.requiresAuth(),
                meta.openInNewWindow()
        );
    }

    private boolean isLaunchUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return false;
        }
        String normalized = url.trim();
        if (normalized.equals("#") || normalized.equalsIgnoreCase("#none") || normalized.equalsIgnoreCase("#none;")) {
            return false;
        }
        return normalized.startsWith("https://") || normalized.startsWith("http://");
    }

    private String property(String key) {
        return environment == null ? null : environment.getProperty(key);
    }

    private String propertyKey(String code) {
        return normalizeCode(code).toLowerCase().replace('_', '-');
    }

    private String normalizeLaunchType(String launchType) {
        return StringUtils.hasText(launchType) ? launchType.trim().toUpperCase().replace('-', '_') : "EXTERNAL_LINK";
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

    private record ExternalMeta(
            String code,
            String url,
            boolean enabled,
            String launchType,
            String policyLabel,
            String disabledReason,
            boolean requiresAuth,
            boolean openInNewWindow
    ) {
    }

    private record LaunchPolicy(
            String url,
            boolean enabled,
            boolean launchable,
            String launchType,
            String policyLabel,
            String disabledReason,
            boolean requiresAuth,
            boolean openInNewWindow
    ) {
    }
}
