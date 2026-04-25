package com.edussafy.backend.board.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.edussafy.backend.board.dto.BoardPostDetailResponse.BoardCommentItem;
import com.edussafy.backend.board.dto.BoardPostDetailResponse.BoardPostDetail;
import com.edussafy.backend.board.dto.BoardPostDetailResponse.EngagementSummary;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardCommentCreateRequest;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardCommentCreateResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardPostCreateRequest;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardPostCreateResponse;
import com.edussafy.backend.board.dto.CategorySummary;
import com.edussafy.backend.board.repository.BoardRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class BoardServiceTest {

    @Test
    void getPostAttachesPersistedComments() {
        BoardRepository repository = mock(BoardRepository.class);
        BoardPostDetail post = detail(10L, "Original", List.of());
        BoardCommentItem comment = new BoardCommentItem(44L, 10L, null, "Persisted comment", "Demo Student", OffsetDateTime.now(), List.of());
        given(repository.findBoardId("free")).willReturn(Optional.of(1L));
        given(repository.findPostDetail(1L, 10L)).willReturn(Optional.of(post));
        given(repository.findComments(10L)).willReturn(List.of(comment));
        BoardService service = new BoardService(repository);

        BoardPostDetail response = service.getPost("free", 10L).post();

        assertThat(response.comments()).containsExactly(comment);
        assertThat(response.engagement().commentCount()).isEqualTo(0);
    }

    @Test
    void getPostNestsPersistedCommentReplies() {
        BoardRepository repository = mock(BoardRepository.class);
        BoardPostDetail post = detail(10L, "Original", List.of());
        OffsetDateTime now = OffsetDateTime.now();
        BoardCommentItem parent = new BoardCommentItem(44L, 10L, null, "Persisted comment", "Demo Student", now, List.of());
        BoardCommentItem reply = new BoardCommentItem(45L, 10L, 44L, "Nested reply", "Demo Manager", now, List.of());
        given(repository.findBoardId("free")).willReturn(Optional.of(1L));
        given(repository.findPostDetail(1L, 10L)).willReturn(Optional.of(post));
        given(repository.findComments(10L)).willReturn(List.of(parent, reply));
        BoardService service = new BoardService(repository);

        BoardPostDetail response = service.getPost("free", 10L).post();

        assertThat(response.comments()).hasSize(1);
        assertThat(response.comments().getFirst().id()).isEqualTo(44L);
        assertThat(response.comments().getFirst().replies()).hasSize(1);
        assertThat(response.comments().getFirst().replies().getFirst().parentCommentId()).isEqualTo(44L);
    }

    @Test
    void createPostPersistsAndReturnsStoredShape() {
        BoardRepository repository = mock(BoardRepository.class);
        BoardPostDetail created = detail(33L, "Hello", List.of());
        given(repository.findBoardId("free")).willReturn(Optional.of(1L));
        given(repository.existsCategory(1L, 4L)).willReturn(true);
        given(repository.findDefaultAuthorUserId()).willReturn(Optional.of(7L));
        given(repository.createPost(1L, 4L, 7L, "Hello", "Body")).willReturn(33L);
        given(repository.findPostDetail(1L, 33L)).willReturn(Optional.of(created));
        BoardService service = new BoardService(repository);

        BoardPostCreateResponse response = service.createPost("free", new BoardPostCreateRequest(4L, " Hello ", " Body "));

        assertThat(response.item().id()).isEqualTo(33L);
        assertThat(response.item().title()).isEqualTo("Hello");
        assertThat(response.item().demo()).isFalse();
        verify(repository).createPost(1L, 4L, 7L, "Hello", "Body");
    }

    @Test
    void createCommentRequiresBoardPostAndPersists() {
        BoardRepository repository = mock(BoardRepository.class);
        BoardPostDetail post = detail(10L, "Original", List.of());
        BoardCommentItem comment = new BoardCommentItem(44L, 10L, null, "Nice", "Demo Student", OffsetDateTime.now(), List.of());
        given(repository.findBoardId("free")).willReturn(Optional.of(1L));
        given(repository.findPostDetail(1L, 10L)).willReturn(Optional.of(post));
        given(repository.findDefaultAuthorUserId()).willReturn(Optional.of(7L));
        given(repository.createComment(10L, 7L, null, "Nice")).willReturn(44L);
        given(repository.findComment(44L)).willReturn(Optional.of(comment));
        BoardService service = new BoardService(repository);

        BoardCommentCreateResponse response = service.createComment("free", 10L, new BoardCommentCreateRequest(null, " Nice "));

        assertThat(response.item().id()).isEqualTo(44L);
        assertThat(response.item().postId()).isEqualTo(10L);
        assertThat(response.item().content()).isEqualTo("Nice");
        assertThat(response.item().demo()).isFalse();
        verify(repository).createComment(10L, 7L, null, "Nice");
    }

    @Test
    void createReplyPersistsParentCommentId() {
        BoardRepository repository = mock(BoardRepository.class);
        BoardPostDetail post = detail(10L, "Original", List.of());
        BoardCommentItem parent = new BoardCommentItem(44L, 10L, null, "Question", "Demo Student", OffsetDateTime.now(), List.of());
        BoardCommentItem reply = new BoardCommentItem(45L, 10L, 44L, "Reply", "Demo Student", OffsetDateTime.now(), List.of());
        given(repository.findBoardId("free")).willReturn(Optional.of(1L));
        given(repository.findPostDetail(1L, 10L)).willReturn(Optional.of(post));
        given(repository.findComment(44L)).willReturn(Optional.of(parent));
        given(repository.findDefaultAuthorUserId()).willReturn(Optional.of(7L));
        given(repository.createComment(10L, 7L, 44L, "Reply")).willReturn(45L);
        given(repository.findComment(45L)).willReturn(Optional.of(reply));
        BoardService service = new BoardService(repository);

        BoardCommentCreateResponse response = service.createComment("free", 10L, new BoardCommentCreateRequest(44L, " Reply "));

        assertThat(response.item().id()).isEqualTo(45L);
        assertThat(response.item().parentCommentId()).isEqualTo(44L);
        verify(repository).createComment(10L, 7L, 44L, "Reply");
    }

    private BoardPostDetail detail(long id, String title, List<BoardCommentItem> comments) {
        return new BoardPostDetail(
                id,
                "free",
                new CategorySummary(4L, "General"),
                title,
                "Body",
                "Demo Student",
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                0,
                new EngagementSummary(comments.size(), 0, 0),
                comments,
                false,
                false
        );
    }
}
