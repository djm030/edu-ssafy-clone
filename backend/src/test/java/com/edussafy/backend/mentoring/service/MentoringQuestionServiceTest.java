package com.edussafy.backend.mentoring.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.edussafy.backend.board.dto.BoardPostDetailResponse.BoardCommentItem;
import com.edussafy.backend.board.dto.BoardPostDetailResponse.BoardPostDetail;
import com.edussafy.backend.board.dto.BoardPostDetailResponse.EngagementSummary;
import com.edussafy.backend.board.dto.BoardPostListItem;
import com.edussafy.backend.board.dto.CategoryItem;
import com.edussafy.backend.board.dto.CategorySummary;
import com.edussafy.backend.board.repository.BoardRepository;
import com.edussafy.backend.board.service.BoardQuery;
import com.edussafy.backend.board.service.BoardSort;
import com.edussafy.backend.mentoring.dto.MentoringQuestionDtos.MentoringAnswerCreateRequest;
import com.edussafy.backend.mentoring.dto.MentoringQuestionDtos.MentoringQuestionCreateRequest;
import com.edussafy.backend.mentoring.dto.MentoringQuestionDtos.MentoringQuestionStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

class MentoringQuestionServiceTest {

    @AfterEach
    void resetRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void listMapsAnsweredQuestionsFromBoardPersistence() {
        BoardRepository repository = mock(BoardRepository.class);
        BoardQuery query = new BoardQuery(null, null, 1, 20, "createdAt,desc");
        given(repository.findBoardId("mentoring_qna")).willReturn(Optional.of(41L));
        given(repository.countPosts(41L, query)).willReturn(1L);
        given(repository.findPosts(41L, query, BoardSort.parse("createdAt,desc")))
                .willReturn(List.of(listItem("포트폴리오를 어떻게 개선할까요?", 2)));
        MentoringQuestionService service = new MentoringQuestionService(repository);

        var response = service.questions(1, 20, null);

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().getFirst().status()).isEqualTo(MentoringQuestionStatus.ANSWERED);
        assertThat(response.items().getFirst().answerCount()).isEqualTo(2);
    }

    @Test
    void createQuestionStoresAnonymousMetadataAndUsesDefaultCategory() {
        BoardRepository repository = mock(BoardRepository.class);
        given(repository.findBoardId("mentoring_qna")).willReturn(Optional.of(41L));
        given(repository.findCategories(41L)).willReturn(List.of(new CategoryItem(7L, "커리어", 1, 0)));
        given(repository.existsCategory(41L, 7L)).willReturn(true);
        given(repository.findDefaultAuthorUserId()).willReturn(Optional.of(3L));
        given(repository.createPost(eq(41L), eq(7L), eq(3L), eq("질문"), any(String.class))).willReturn(91L);
        given(repository.findPostDetail(41L, 91L)).willReturn(Optional.of(detail("질문", "<!--MENTORING_QNA:anonymous=true-->\n내용", 3L)));
        given(repository.findComments(91L)).willReturn(List.of());
        MentoringQuestionService service = new MentoringQuestionService(repository);

        var response = service.createQuestion(new MentoringQuestionCreateRequest(null, " 질문 ", " 내용 ", true));

        assertThat(response.item().anonymous()).isTrue();
        assertThat(response.item().authorName()).isEqualTo("익명 질문자");
        assertThat(response.item().content()).isEqualTo("내용");
    }

    @Test
    void mentorAnswerRequiresCoachRoleAndReturnsAnswer() {
        BoardRepository repository = mock(BoardRepository.class);
        given(repository.findBoardId("mentoring_qna")).willReturn(Optional.of(41L));
        given(repository.findPostDetail(41L, 91L)).willReturn(Optional.of(detail("질문", "내용", 3L)));
        given(repository.findComment(10L)).willReturn(Optional.of(answerComment()));
        given(repository.findComments(91L)).willReturn(List.of(answerComment()));
        given(repository.createComment(91L, 9L, null, "멘토 답변입니다.")).willReturn(10L);
        withRole("coach", 9L);
        MentoringQuestionService service = new MentoringQuestionService(repository);

        var response = service.answer(91L, new MentoringAnswerCreateRequest("멘토 답변입니다."));

        assertThat(response.item().status()).isEqualTo(MentoringQuestionStatus.ANSWERED);
        assertThat(response.item().answers()).extracting("content").containsExactly("멘토 답변입니다.");
    }

    @Test
    void learnerCannotWriteMentorAnswer() {
        BoardRepository repository = mock(BoardRepository.class);
        withRole("learner", 3L);
        MentoringQuestionService service = new MentoringQuestionService(repository);

        assertThatThrownBy(() -> service.answer(91L, new MentoringAnswerCreateRequest("답변")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403 FORBIDDEN");
    }

    @Test
    void ownerCanCloseQuestionAndListShowsClosedPrefixStripped() {
        BoardRepository repository = mock(BoardRepository.class);
        given(repository.findBoardId("mentoring_qna")).willReturn(Optional.of(41L));
        given(repository.findPostDetail(41L, 91L))
                .willReturn(Optional.of(detail("질문", "내용", 3L)))
                .willReturn(Optional.of(detail("[CLOSED] 질문", "내용", 3L)));
        given(repository.findComments(91L)).willReturn(List.of());
        withRole("learner", 3L);
        MentoringQuestionService service = new MentoringQuestionService(repository);

        var response = service.close(91L);

        assertThat(response.item().status()).isEqualTo(MentoringQuestionStatus.CLOSED);
        assertThat(response.item().title()).isEqualTo("질문");
        verify(repository).updatePost(41L, 91L, 7L, "[CLOSED] 질문", "내용");
    }

    private void withRole(String role, long userId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("currentRole", role);
        request.setAttribute("currentUserId", userId);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private BoardPostListItem listItem(String title, long commentCount) {
        return new BoardPostListItem(
                91L,
                "mentoring_qna",
                new CategorySummary(7L, "커리어"),
                title,
                "Demo Student",
                OffsetDateTime.now(),
                4,
                commentCount,
                0,
                0,
                false,
                false
        );
    }

    private BoardPostDetail detail(String title, String content, Long authorUserId) {
        return new BoardPostDetail(
                91L,
                "mentoring_qna",
                new CategorySummary(7L, "커리어"),
                title,
                content,
                authorUserId,
                "Demo Student",
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                4,
                new EngagementSummary(0, 0, 0),
                List.of(),
                List.of(),
                false,
                false
        );
    }

    private BoardCommentItem answerComment() {
        return new BoardCommentItem(
                10L,
                91L,
                null,
                "멘토 답변입니다.",
                9L,
                "Demo Mentor",
                OffsetDateTime.now(),
                List.of()
        );
    }
}
