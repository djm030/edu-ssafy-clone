package com.edussafy.backend.board.service;

import com.edussafy.backend.board.dto.BoardCategoryListResponse;
import com.edussafy.backend.board.dto.BoardPostDetailResponse;
import com.edussafy.backend.board.dto.BoardPostDetailResponse.BoardAttachmentItem;
import com.edussafy.backend.board.dto.BoardPostDetailResponse.BoardCommentItem;
import com.edussafy.backend.board.dto.BoardPostDetailResponse.BoardPostDetail;
import com.edussafy.backend.board.dto.BoardPostListItem;
import com.edussafy.backend.board.dto.BoardPostListResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardCommentCreateRequest;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardCommentCreateResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardCommentCreatedItem;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardCommentDeleteResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardCommentDeletedItem;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardCommentUpdateResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardPostCreateRequest;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardPostCreateResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardPostCreatedItem;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardPostDeleteResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardPostDeletedItem;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardPostUpdateResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardAttachmentCreateRequest;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardAttachmentCreateResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardAttachmentCreatedItem;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardAttachmentDeleteResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardAttachmentDeletedItem;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardAttachmentDownload;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardReactionCreateRequest;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardReactionCreateResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardReactionCreatedItem;
import com.edussafy.backend.board.dto.PageMeta;
import com.edussafy.backend.board.error.BoardNotFoundException;
import com.edussafy.backend.board.error.BoardPostNotFoundException;
import com.edussafy.backend.board.error.InvalidBoardQueryException;
import com.edussafy.backend.board.repository.BoardRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.edussafy.backend.priority.security.AuthSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BoardService {

    private static final int BOARD_ATTACHMENT_MAX_BYTES = 2 * 1024 * 1024;

    private final BoardRepository boardRepository;

    public BoardService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    public BoardCategoryListResponse getCategories(String boardCode) {
        long boardId = requireBoardId(boardCode);
        return new BoardCategoryListResponse(boardRepository.findCategories(boardId));
    }

    public BoardPostListResponse getPosts(String boardCode, BoardQuery query) {
        validatePagination(query);
        BoardSort sort = BoardSort.parse(query.sort());
        long boardId = requireBoardId(boardCode);

        long totalItems = boardRepository.countPosts(boardId, query);
        List<BoardPostListItem> items = totalItems == 0
                ? List.of()
                : boardRepository.findPosts(boardId, query, sort);
        if (isAnonymousBoard(boardCode)) {
            items = items.stream().map(this::anonymousListItem).toList();
        }
        int totalPages = totalItems == 0 ? 0 : (int) Math.ceil((double) totalItems / query.size());

        return new BoardPostListResponse(items, new PageMeta(query.page(), query.size(), totalItems, totalPages));
    }

    public BoardPostDetailResponse getPost(String boardCode, long postId) {
        long boardId = requireBoardId(boardCode);
        BoardPostDetail post = boardRepository.findPostDetail(boardId, postId)
                .orElseThrow(() -> new BoardPostNotFoundException(postId));
        BoardPostDetail hydrated = withCommentsAndAttachments(
                post,
                nestComments(boardRepository.findComments(postId)),
                nonNullList(boardRepository.findAttachments(postId))
        );
        return new BoardPostDetailResponse(isAnonymousBoard(boardCode) ? anonymousDetail(hydrated) : hydrated);
    }

    @Transactional
    public BoardPostCreateResponse createPost(String boardCode, BoardPostCreateRequest request) {
        long boardId = requireBoardId(boardCode);
        validateCategory(boardId, request.categoryId());

        Long authorUserId = currentBoardActorUserId();
        long postId = boardRepository.createPost(
                boardId,
                request.categoryId(),
                authorUserId,
                request.title().trim(),
                request.content().trim()
        );
        BoardPostDetail created = boardRepository.findPostDetail(boardId, postId)
                .orElseThrow(() -> new BoardPostNotFoundException(postId));

        return new BoardPostCreateResponse(new BoardPostCreatedItem(
                created.id(),
                created.boardCode(),
                created.category() == null ? null : created.category().id(),
                created.title(),
                created.content(),
                displayAuthorName(boardCode, created.authorName()),
                created.createdAt(),
                false
        ));
    }

    @Transactional
    public BoardPostUpdateResponse updatePost(String boardCode, long postId, BoardPostCreateRequest request) {
        long boardId = requireBoardId(boardCode);
        validateCategory(boardId, request.categoryId());
        BoardPostDetail existing = boardRepository.findPostDetail(boardId, postId)
                .orElseThrow(() -> new BoardPostNotFoundException(postId));
        requirePostMutationAllowed(existing);

        int updated = boardRepository.updatePost(
                boardId,
                postId,
                request.categoryId(),
                request.title().trim(),
                request.content().trim()
        );
        if (updated == 0) {
            throw new BoardPostNotFoundException(postId);
        }
        BoardPostDetail saved = boardRepository.findPostDetail(boardId, postId)
                .orElseThrow(() -> new BoardPostNotFoundException(postId));

        return new BoardPostUpdateResponse(new BoardPostCreatedItem(
                saved.id(),
                saved.boardCode(),
                saved.category() == null ? null : saved.category().id(),
                saved.title(),
                saved.content(),
                displayAuthorName(boardCode, saved.authorName()),
                saved.createdAt(),
                false
        ));
    }

    @Transactional
    public BoardPostDeleteResponse deletePost(String boardCode, long postId) {
        long boardId = requireBoardId(boardCode);
        BoardPostDetail existing = boardRepository.findPostDetail(boardId, postId)
                .orElseThrow(() -> new BoardPostNotFoundException(postId));
        requirePostMutationAllowed(existing);
        int deleted = boardRepository.deletePost(boardId, postId);
        if (deleted == 0) {
            throw new BoardPostNotFoundException(postId);
        }

        return new BoardPostDeleteResponse(new BoardPostDeletedItem(postId, boardCode, true, false));
    }

    @Transactional
    public BoardAttachmentCreateResponse createAttachment(
            String boardCode,
            long postId,
            BoardAttachmentCreateRequest request
    ) {
        long boardId = requireBoardId(boardCode);
        boardRepository.findPostDetail(boardId, postId)
                .orElseThrow(() -> new BoardPostNotFoundException(postId));

        byte[] content = decodeBoardAttachment(request.contentBase64());
        String originalFilename = sanitizeAttachmentFilename(request.originalFilename());
        String mimeType = normalizedOrNull(request.mimeType());
        String checksumSha256 = normalizedOrNull(request.checksumSha256());
        String storageKey = normalizedOrNull(request.storageKey());
        String storedPath = normalizedOrNull(request.storedPath());
        Long fileSize = request.fileSize();
        if (content != null) {
            checksumSha256 = sha256Hex(content);
            fileSize = (long) content.length;
            mimeType = mimeType == null ? "application/octet-stream" : mimeType;
            storageKey = storageKey == null
                    ? "boards/%s/posts/%d/%s-%s".formatted(boardCode, postId, checksumSha256.substring(0, 12), originalFilename)
                    : storageKey;
            storedPath = storedPath == null
                    ? "/boards/%s/posts/%d/attachments/%s".formatted(boardCode, postId, checksumSha256)
                    : storedPath;
        }
        long attachmentId = boardRepository.createAttachment(
                originalFilename,
                storageKey,
                storedPath,
                mimeType,
                fileSize,
                checksumSha256
        );
        boardRepository.attachPost(postId, attachmentId);
        BoardAttachmentItem saved = boardRepository.findAttachment(postId, attachmentId)
                .orElseThrow(() -> new BoardPostNotFoundException(postId));
        if (content != null) {
            storeBoardAttachment(saved, content);
        }

        return new BoardAttachmentCreateResponse(new BoardAttachmentCreatedItem(
                saved.id(),
                postId,
                saved.originalFilename(),
                saved.storageKey(),
                saved.storedPath(),
                saved.mimeType(),
                saved.fileSize(),
                saved.createdAt(),
                false
        ));
    }


    public BoardAttachmentDownload downloadAttachment(String boardCode, long postId, long attachmentId) {
        long boardId = requireBoardId(boardCode);
        boardRepository.findPostDetail(boardId, postId)
                .orElseThrow(() -> new BoardPostNotFoundException(postId));
        BoardAttachmentItem attachment = boardRepository.findAttachment(postId, attachmentId)
                .orElseThrow(() -> new BoardPostNotFoundException(postId));
        return new BoardAttachmentDownload(attachment, readBoardAttachment(attachment));
    }

    @Transactional
    public BoardAttachmentDeleteResponse deleteAttachment(String boardCode, long postId, long attachmentId) {
        long boardId = requireBoardId(boardCode);
        boardRepository.findPostDetail(boardId, postId)
                .orElseThrow(() -> new BoardPostNotFoundException(postId));
        boardRepository.findAttachment(postId, attachmentId)
                .orElseThrow(() -> new BoardPostNotFoundException(postId));
        int deleted = boardRepository.deletePostAttachment(postId, attachmentId);
        if (deleted == 0) {
            throw new BoardPostNotFoundException(postId);
        }

        return new BoardAttachmentDeleteResponse(new BoardAttachmentDeletedItem(attachmentId, postId, true, false));
    }

    @Transactional
    public BoardCommentCreateResponse createComment(
            String boardCode,
            long postId,
            BoardCommentCreateRequest request
    ) {
        long boardId = requireBoardId(boardCode);
        boardRepository.findPostDetail(boardId, postId)
                .orElseThrow(() -> new BoardPostNotFoundException(postId));
        validateParentComment(postId, request.parentCommentId());

        Long authorUserId = currentBoardActorUserId();
        long commentId = boardRepository.createComment(postId, authorUserId, request.parentCommentId(), request.content().trim());
        BoardCommentItem created = boardRepository.findComment(commentId)
                .orElseThrow(() -> new BoardPostNotFoundException(postId));

        return new BoardCommentCreateResponse(new BoardCommentCreatedItem(
                created.id(),
                created.postId(),
                created.parentCommentId(),
                created.content(),
                displayAuthorName(boardCode, created.authorName()),
                created.createdAt(),
                false
        ));
    }

    @Transactional
    public BoardCommentUpdateResponse updateComment(
            String boardCode,
            long postId,
            long commentId,
            BoardCommentCreateRequest request
    ) {
        long boardId = requireBoardId(boardCode);
        boardRepository.findPostDetail(boardId, postId)
                .orElseThrow(() -> new BoardPostNotFoundException(postId));
        BoardCommentItem existing = boardRepository.findComment(commentId)
                .orElseThrow(() -> new BoardPostNotFoundException(postId));
        if (existing.postId() != postId) {
            throw new BoardPostNotFoundException(postId);
        }
        requireCommentMutationAllowed(existing);
        int updated = boardRepository.updateComment(postId, commentId, request.content().trim());
        if (updated == 0) {
            throw new BoardPostNotFoundException(postId);
        }
        BoardCommentItem saved = boardRepository.findComment(commentId)
                .orElseThrow(() -> new BoardPostNotFoundException(postId));

        return new BoardCommentUpdateResponse(new BoardCommentCreatedItem(
                saved.id(),
                saved.postId(),
                saved.parentCommentId(),
                saved.content(),
                displayAuthorName(boardCode, saved.authorName()),
                saved.createdAt(),
                false
        ));
    }

    @Transactional
    public BoardCommentDeleteResponse deleteComment(String boardCode, long postId, long commentId) {
        long boardId = requireBoardId(boardCode);
        boardRepository.findPostDetail(boardId, postId)
                .orElseThrow(() -> new BoardPostNotFoundException(postId));
        BoardCommentItem existing = boardRepository.findComment(commentId)
                .orElseThrow(() -> new BoardPostNotFoundException(postId));
        if (existing.postId() != postId) {
            throw new BoardPostNotFoundException(postId);
        }
        requireCommentMutationAllowed(existing);
        int deleted = boardRepository.deleteComment(postId, commentId);
        if (deleted == 0) {
            throw new BoardPostNotFoundException(postId);
        }

        return new BoardCommentDeleteResponse(new BoardCommentDeletedItem(commentId, postId, true, false));
    }

    @Transactional
    public BoardReactionCreateResponse createReaction(
            String boardCode,
            long postId,
            BoardReactionCreateRequest request
    ) {
        long boardId = requireBoardId(boardCode);
        boardRepository.findPostDetail(boardId, postId)
                .orElseThrow(() -> new BoardPostNotFoundException(postId));
        Long userId = currentBoardActorUserId();
        String type = request.type().trim().toLowerCase();
        if (userId != null) {
            boardRepository.createReaction(postId, userId, type);
        }
        return new BoardReactionCreateResponse(new BoardReactionCreatedItem(
                postId,
                type,
                true,
                false
        ));
    }

    @Transactional
    public BoardReactionCreateResponse deleteReaction(
            String boardCode,
            long postId,
            String type
    ) {
        long boardId = requireBoardId(boardCode);
        boardRepository.findPostDetail(boardId, postId)
                .orElseThrow(() -> new BoardPostNotFoundException(postId));
        Long userId = currentBoardActorUserId();
        String normalizedType = StringUtils.hasText(type) ? type.trim().toLowerCase() : "like";
        if (userId != null) {
            boardRepository.deleteReaction(postId, userId, normalizedType);
        }
        return new BoardReactionCreateResponse(new BoardReactionCreatedItem(
                postId,
                normalizedType,
                false,
                false
        ));
    }

    private long requireBoardId(String boardCode) {
        if (!StringUtils.hasText(boardCode)) {
            throw new BoardNotFoundException(boardCode);
        }
        return boardRepository.findBoardId(boardCode)
                .orElseThrow(() -> new BoardNotFoundException(boardCode));
    }

    private void validatePagination(BoardQuery query) {
        if (query.page() < 1) {
            throw new InvalidBoardQueryException("INVALID_PAGE", "page must be greater than or equal to 1.");
        }
        if (query.size() < 1 || query.size() > 100) {
            throw new InvalidBoardQueryException("INVALID_SIZE", "size must be between 1 and 100.");
        }
    }

    private void validateCategory(long boardId, Long categoryId) {
        if (categoryId != null && !boardRepository.existsCategory(boardId, categoryId)) {
            throw new InvalidBoardQueryException("INVALID_CATEGORY", "categoryId must belong to the selected board.");
        }
    }

    private void validateParentComment(long postId, Long parentCommentId) {
        if (parentCommentId == null) {
            return;
        }
        BoardCommentItem parent = boardRepository.findComment(parentCommentId)
                .orElseThrow(() -> new BoardPostNotFoundException(postId));
        if (parent.postId() != postId) {
            throw new BoardPostNotFoundException(postId);
        }
    }

    private List<BoardCommentItem> nestComments(List<BoardCommentItem> flatComments) {
        Map<Long, BoardCommentItem> byId = new LinkedHashMap<>();
        for (BoardCommentItem comment : flatComments) {
            byId.put(comment.id(), new BoardCommentItem(
                    comment.id(),
                    comment.postId(),
                    comment.parentCommentId(),
                    comment.content(),
                    comment.authorUserId(),
                    comment.authorName(),
                    comment.createdAt(),
                    new ArrayList<>()
            ));
        }

        List<BoardCommentItem> roots = new ArrayList<>();
        for (BoardCommentItem comment : byId.values()) {
            if (comment.parentCommentId() != null && byId.containsKey(comment.parentCommentId())) {
                byId.get(comment.parentCommentId()).replies().add(comment);
            } else {
                roots.add(comment);
            }
        }
        return roots;
    }

    private boolean isAnonymousBoard(String boardCode) {
        return "anonymous".equalsIgnoreCase(boardCode);
    }

    private String displayAuthorName(String boardCode, String authorName) {
        return isAnonymousBoard(boardCode) ? "익명" : authorName;
    }

    private BoardPostListItem anonymousListItem(BoardPostListItem item) {
        return new BoardPostListItem(
                item.id(),
                item.boardCode(),
                item.category(),
                item.title(),
                "익명",
                item.createdAt(),
                item.viewCount(),
                item.commentCount(),
                item.reactionCount(),
                item.bookmarkCount(),
                item.hasAttachment(),
                item.isPinned()
        );
    }

    private BoardPostDetail anonymousDetail(BoardPostDetail post) {
        return new BoardPostDetail(
                post.id(),
                post.boardCode(),
                post.category(),
                post.title(),
                post.content(),
                null,
                "익명",
                post.createdAt(),
                post.updatedAt(),
                post.viewCount(),
                post.engagement(),
                anonymousComments(post.comments()),
                post.attachments(),
                post.hasAttachment(),
                post.isPinned()
        );
    }

    private List<BoardCommentItem> anonymousComments(List<BoardCommentItem> comments) {
        return comments.stream()
                .map(comment -> new BoardCommentItem(
                        comment.id(),
                        comment.postId(),
                        comment.parentCommentId(),
                        comment.content(),
                        null,
                        "익명",
                        comment.createdAt(),
                        anonymousComments(comment.replies() == null ? List.of() : comment.replies())
                ))
                .toList();
    }

    private Long currentBoardActorUserId() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            HttpServletRequest request = attributes.getRequest();
            HttpSession session = request.getSession(false);
            return AuthSession.currentUserId(session)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));
        }
        return boardRepository.findDefaultAuthorUserId().orElse(null);
    }

    private void requirePostMutationAllowed(BoardPostDetail post) {
        Long actorUserId = currentBoardActorUserId();
        if (actorUserId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        if (isBoardModerator()) {
            return;
        }
        if (!Objects.equals(post.authorUserId(), actorUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "게시글 작성자만 수정하거나 삭제할 수 있습니다.");
        }
    }


    private void requireCommentMutationAllowed(BoardCommentItem comment) {
        Long actorUserId = currentBoardActorUserId();
        if (actorUserId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        if (isBoardModerator()) {
            return;
        }
        if (!Objects.equals(comment.authorUserId(), actorUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "댓글 작성자만 수정하거나 삭제할 수 있습니다.");
        }
    }

    private boolean isBoardModerator() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            Object role = attributes.getRequest().getAttribute("currentRole");
            return "coach".equals(role) || "admin".equals(role);
        }
        return false;
    }


    private byte[] decodeBoardAttachment(String contentBase64) {
        if (!StringUtils.hasText(contentBase64)) {
            return null;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(contentBase64.trim());
            if (decoded.length == 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Board attachment cannot be empty.");
            }
            if (decoded.length > BOARD_ATTACHMENT_MAX_BYTES) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Board attachment exceeds the 2MB limit.");
            }
            return decoded;
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Board attachment content must be base64.");
        }
    }

    private String sanitizeAttachmentFilename(String filename) {
        String normalized = filename.trim().replaceAll("[\\\\/\\p{Cntrl}]+", "_");
        if (normalized.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Board attachment filename is required.");
        }
        return normalized.length() <= 255 ? normalized : normalized.substring(0, 255);
    }

    private void storeBoardAttachment(BoardAttachmentItem attachment, byte[] content) {
        Path path = boardAttachmentPath(attachment);
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Board attachment could not be stored.", exception);
        }
    }

    private byte[] readBoardAttachment(BoardAttachmentItem attachment) {
        try {
            return Files.readAllBytes(boardAttachmentPath(attachment));
        } catch (NoSuchFileException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Board attachment file was not found.", exception);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Board attachment could not be read.", exception);
        }
    }

    private Path boardAttachmentPath(BoardAttachmentItem attachment) {
        String storageKey = attachment.storageKey();
        if (!StringUtils.hasText(storageKey)) {
            storageKey = "boards/attachments/%d".formatted(attachment.id());
        }
        Path root = Path.of(System.getProperty("java.io.tmpdir"), "edussafy-attachments").toAbsolutePath().normalize();
        Path path = root.resolve(storageKey.trim()).normalize();
        if (!path.startsWith(root)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Board attachment storage key is invalid.");
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

    private String normalizedOrNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private <T> List<T> nonNullList(List<T> items) {
        return items == null ? List.of() : items;
    }

    private BoardPostDetail withCommentsAndAttachments(
            BoardPostDetail post,
            List<BoardCommentItem> comments,
            List<BoardAttachmentItem> attachments
    ) {
        return new BoardPostDetail(
                post.id(),
                post.boardCode(),
                post.category(),
                post.title(),
                post.content(),
                post.authorUserId(),
                post.authorName(),
                post.createdAt(),
                post.updatedAt(),
                post.viewCount(),
                post.engagement(),
                comments,
                attachments,
                !attachments.isEmpty() || post.hasAttachment(),
                post.isPinned()
        );
    }
}
