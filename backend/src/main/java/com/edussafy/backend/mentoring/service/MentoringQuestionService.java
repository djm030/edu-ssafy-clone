package com.edussafy.backend.mentoring.service;

import com.edussafy.backend.board.dto.BoardPostDetailResponse.BoardCommentItem;
import com.edussafy.backend.board.dto.BoardPostDetailResponse.BoardPostDetail;
import com.edussafy.backend.board.dto.BoardPostListItem;
import com.edussafy.backend.board.dto.CategoryItem;
import com.edussafy.backend.board.dto.CategorySummary;
import com.edussafy.backend.board.dto.PageMeta;
import com.edussafy.backend.board.error.BoardNotFoundException;
import com.edussafy.backend.board.error.BoardPostNotFoundException;
import com.edussafy.backend.board.repository.BoardRepository;
import com.edussafy.backend.board.service.BoardQuery;
import com.edussafy.backend.board.service.BoardSort;
import com.edussafy.backend.mentoring.dto.MentoringQuestionDtos.MentoringAnswerCreateRequest;
import com.edussafy.backend.mentoring.dto.MentoringQuestionDtos.MentoringAnswerItem;
import com.edussafy.backend.mentoring.dto.MentoringQuestionDtos.MentoringQuestionCreateRequest;
import com.edussafy.backend.mentoring.dto.MentoringQuestionDtos.MentoringQuestionDetail;
import com.edussafy.backend.mentoring.dto.MentoringQuestionDtos.MentoringQuestionItem;
import com.edussafy.backend.mentoring.dto.MentoringQuestionDtos.MentoringQuestionResponse;
import com.edussafy.backend.mentoring.dto.MentoringQuestionDtos.MentoringQuestionStatus;
import com.edussafy.backend.mentoring.dto.MentoringQuestionDtos.MentoringQuestionsResponse;
import com.edussafy.backend.priority.security.AuthSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MentoringQuestionService {

    private static final String BOARD_CODE = "mentoring_qna";
    private static final String ANONYMOUS_MARKER = "<!--MENTORING_QNA:anonymous=true-->";
    private static final String CLOSED_TITLE_PREFIX = "[CLOSED] ";

    private final BoardRepository boardRepository;

    public MentoringQuestionService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    public MentoringQuestionsResponse questions(int page, int size, String keyword) {
        validatePagination(page, size);
        long boardId = requireBoardId();
        BoardQuery query = new BoardQuery(null, normalizeKeyword(keyword), page, size, "createdAt,desc");
        long totalItems = boardRepository.countPosts(boardId, query);
        List<MentoringQuestionItem> items = totalItems == 0
                ? List.of()
                : boardRepository.findPosts(boardId, query, BoardSort.parse(query.sort())).stream()
                        .map(this::toItem)
                        .toList();
        int totalPages = totalItems == 0 ? 0 : (int) Math.ceil((double) totalItems / size);
        return new MentoringQuestionsResponse(items, new PageMeta(page, size, totalItems, totalPages));
    }

    public MentoringQuestionResponse question(long questionId) {
        long boardId = requireBoardId();
        BoardPostDetail detail = boardRepository.findPostDetail(boardId, questionId)
                .orElseThrow(() -> new BoardPostNotFoundException(questionId));
        List<MentoringAnswerItem> answers = visibleAnswers(boardRepository.findComments(questionId));
        return new MentoringQuestionResponse(toDetail(detail, answers));
    }

    @Transactional
    public MentoringQuestionResponse createQuestion(MentoringQuestionCreateRequest request) {
        long boardId = requireBoardId();
        Long categoryId = request.categoryId() == null ? defaultCategoryId(boardId) : request.categoryId();
        if (categoryId != null && !boardRepository.existsCategory(boardId, categoryId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "질문 카테고리가 멘토링 Q&A에 속하지 않습니다.");
        }
        long postId = boardRepository.createPost(
                boardId,
                categoryId,
                currentActorUserId(),
                trimRequired(request.title(), "질문 제목"),
                questionContent(request.content(), request.anonymousAllowed())
        );
        return question(postId);
    }

    @Transactional
    public MentoringQuestionResponse answer(long questionId, MentoringAnswerCreateRequest request) {
        requireMentorRole();
        long boardId = requireBoardId();
        boardRepository.findPostDetail(boardId, questionId)
                .orElseThrow(() -> new BoardPostNotFoundException(questionId));
        boardRepository.createComment(questionId, currentActorUserId(), null, trimRequired(request.content(), "답변 내용"));
        return question(questionId);
    }

    @Transactional
    public MentoringQuestionResponse close(long questionId) {
        long boardId = requireBoardId();
        BoardPostDetail existing = boardRepository.findPostDetail(boardId, questionId)
                .orElseThrow(() -> new BoardPostNotFoundException(questionId));
        requireQuestionOwnerOrMentor(existing);
        if (!isClosed(existing.title())) {
            boardRepository.updatePost(
                    boardId,
                    questionId,
                    existing.category() == null ? null : existing.category().id(),
                    closedTitle(existing.title()),
                    existing.content()
            );
        }
        return question(questionId);
    }

    private long requireBoardId() {
        return boardRepository.findBoardId(BOARD_CODE)
                .orElseThrow(() -> new BoardNotFoundException(BOARD_CODE));
    }

    private MentoringQuestionItem toItem(BoardPostListItem post) {
        boolean closed = isClosed(post.title());
        MentoringQuestionStatus status = closed ? MentoringQuestionStatus.CLOSED
                : post.commentCount() > 0 ? MentoringQuestionStatus.ANSWERED : MentoringQuestionStatus.OPEN;
        return new MentoringQuestionItem(
                post.id(),
                displayTitle(post.title()),
                "멘토 답변을 기다리는 실전 커리어 질문입니다.",
                categoryName(post.category()),
                status,
                false,
                post.authorName(),
                post.commentCount(),
                post.createdAt()
        );
    }

    private MentoringQuestionDetail toDetail(BoardPostDetail detail, List<MentoringAnswerItem> answers) {
        boolean anonymous = isAnonymous(detail.content());
        return new MentoringQuestionDetail(
                detail.id(),
                displayTitle(detail.title()),
                displayContent(detail.content()),
                categoryName(detail.category()),
                isClosed(detail.title()) ? MentoringQuestionStatus.CLOSED
                        : answers.isEmpty() ? MentoringQuestionStatus.OPEN : MentoringQuestionStatus.ANSWERED,
                anonymous,
                anonymous ? "익명 질문자" : detail.authorName(),
                answers.size(),
                detail.createdAt(),
                answers
        );
    }

    private List<MentoringAnswerItem> visibleAnswers(List<BoardCommentItem> comments) {
        return comments.stream()
                .map(comment -> new MentoringAnswerItem(
                        comment.id(),
                        comment.content(),
                        comment.authorName(),
                        comment.createdAt()
                ))
                .toList();
    }

    private Long defaultCategoryId(long boardId) {
        List<CategoryItem> categories = boardRepository.findCategories(boardId);
        return categories.isEmpty() ? null : categories.getFirst().id();
    }

    private String questionContent(String content, boolean anonymous) {
        String trimmed = trimRequired(content, "질문 내용");
        return anonymous ? ANONYMOUS_MARKER + "\n" + trimmed : trimmed;
    }

    private boolean isAnonymous(String content) {
        return content != null && content.startsWith(ANONYMOUS_MARKER);
    }

    private String displayContent(String content) {
        if (content == null) {
            return "";
        }
        if (content.startsWith(ANONYMOUS_MARKER)) {
            return content.substring(ANONYMOUS_MARKER.length()).stripLeading();
        }
        return content;
    }

    private boolean isClosed(String title) {
        return title != null && title.startsWith(CLOSED_TITLE_PREFIX);
    }

    private String displayTitle(String title) {
        return isClosed(title) ? title.substring(CLOSED_TITLE_PREFIX.length()) : title;
    }

    private String closedTitle(String title) {
        String displayTitle = displayTitle(title).trim();
        int maxDisplayLength = 255 - CLOSED_TITLE_PREFIX.length();
        if (displayTitle.length() > maxDisplayLength) {
            displayTitle = displayTitle.substring(0, maxDisplayLength);
        }
        return CLOSED_TITLE_PREFIX + displayTitle;
    }

    private String categoryName(CategorySummary category) {
        return category == null ? "일반" : category.name();
    }

    private String normalizeKeyword(String keyword) {
        return StringUtils.hasText(keyword) ? keyword.trim() : null;
    }

    private void validatePagination(int page, int size) {
        if (page < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page는 1 이상이어야 합니다.");
        }
        if (size < 1 || size > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "size는 1~100 사이여야 합니다.");
        }
    }

    private String trimRequired(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + "은 필수입니다.");
        }
        return value.trim();
    }

    private Long currentActorUserId() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            HttpServletRequest request = attributes.getRequest();
            Object currentUserId = request.getAttribute("currentUserId");
            if (currentUserId instanceof Number number) {
                return number.longValue();
            }
            HttpSession session = request.getSession(false);
            return AuthSession.currentUserId(session)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));
        }
        return boardRepository.findDefaultAuthorUserId().orElse(null);
    }

    private String currentRole() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            Object role = attributes.getRequest().getAttribute("currentRole");
            return role == null ? "learner" : role.toString().toLowerCase(Locale.ROOT);
        }
        return "coach";
    }

    private void requireMentorRole() {
        String role = currentRole();
        if (!Objects.equals(role, "coach") && !Objects.equals(role, "admin")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "멘토 또는 운영자만 답변할 수 있습니다.");
        }
    }

    private void requireQuestionOwnerOrMentor(BoardPostDetail detail) {
        String role = currentRole();
        if (Objects.equals(role, "coach") || Objects.equals(role, "admin")) {
            return;
        }
        Long actorUserId = currentActorUserId();
        if (actorUserId == null || !Objects.equals(actorUserId, detail.authorUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "질문 작성자만 마감할 수 있습니다.");
        }
    }
}
