package com.edussafy.backend.board.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.edussafy.backend.board.dto.BoardPostDetailResponse.BoardAttachmentItem;
import com.edussafy.backend.board.dto.BoardPostDetailResponse.BoardCommentItem;
import com.edussafy.backend.board.dto.BoardPostDetailResponse.BoardPostDetail;
import com.edussafy.backend.board.dto.BoardPostDetailResponse.EngagementSummary;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardAttachmentCreateRequest;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardAttachmentCreateResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardAttachmentDeleteResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardCommentCreateRequest;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardCommentCreateResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardCommentDeleteResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardCommentUpdateResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardPostCreateRequest;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardPostCreateResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardPostDeleteResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardPostUpdateResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardReactionCreateRequest;
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
    void updatePostPersistsAndReturnsStoredShape() {
        BoardRepository repository = mock(BoardRepository.class);
        BoardPostDetail existing = detail(33L, "Before", List.of());
        BoardPostDetail updated = detail(33L, "Updated", "Changed body", List.of());
        given(repository.findBoardId("free")).willReturn(Optional.of(1L));
        given(repository.existsCategory(1L, 4L)).willReturn(true);
        given(repository.findPostDetail(1L, 33L)).willReturn(Optional.of(existing), Optional.of(updated));
        given(repository.updatePost(1L, 33L, 4L, "Updated", "Changed body")).willReturn(1);
        BoardService service = new BoardService(repository);

        BoardPostUpdateResponse response = service.updatePost("free", 33L, new BoardPostCreateRequest(4L, " Updated ", " Changed body "));

        assertThat(response.item().id()).isEqualTo(33L);
        assertThat(response.item().title()).isEqualTo("Updated");
        assertThat(response.item().content()).isEqualTo("Changed body");
        assertThat(response.item().demo()).isFalse();
        verify(repository).updatePost(1L, 33L, 4L, "Updated", "Changed body");
    }

    @Test
    void deletePostRequiresBoardPostAndDeletes() {
        BoardRepository repository = mock(BoardRepository.class);
        BoardPostDetail existing = detail(33L, "Before", List.of());
        given(repository.findBoardId("free")).willReturn(Optional.of(1L));
        given(repository.findPostDetail(1L, 33L)).willReturn(Optional.of(existing));
        given(repository.deletePost(1L, 33L)).willReturn(1);
        BoardService service = new BoardService(repository);

        BoardPostDeleteResponse response = service.deletePost("free", 33L);

        assertThat(response.item().id()).isEqualTo(33L);
        assertThat(response.item().boardCode()).isEqualTo("free");
        assertThat(response.item().deleted()).isTrue();
        assertThat(response.item().demo()).isFalse();
        verify(repository).deletePost(1L, 33L);
    }

    @Test
    void createAndDeleteAttachmentPersistBoardAttachmentLink() {
        BoardRepository repository = mock(BoardRepository.class);
        BoardPostDetail post = detail(33L, "Before", List.of());
        OffsetDateTime createdAt = OffsetDateTime.now();
        BoardAttachmentItem attachment = new BoardAttachmentItem(
                91L,
                "guide.pdf",
                "board/33/guide.pdf",
                "/uploads/board/33/guide.pdf",
                "application/pdf",
                2048L,
                createdAt
        );
        given(repository.findBoardId("free")).willReturn(Optional.of(1L));
        given(repository.findPostDetail(1L, 33L)).willReturn(Optional.of(post), Optional.of(post));
        given(repository.createAttachment(
                "guide.pdf",
                "board/33/guide.pdf",
                "/uploads/board/33/guide.pdf",
                "application/pdf",
                2048L,
                null
        )).willReturn(91L);
        given(repository.findAttachment(33L, 91L)).willReturn(Optional.of(attachment), Optional.of(attachment));
        given(repository.deletePostAttachment(33L, 91L)).willReturn(1);
        BoardService service = new BoardService(repository);

        BoardAttachmentCreateResponse created = service.createAttachment("free", 33L, new BoardAttachmentCreateRequest(
                " guide.pdf ",
                " board/33/guide.pdf ",
                " /uploads/board/33/guide.pdf ",
                " application/pdf ",
                2048L,
                null
        ));
        BoardAttachmentDeleteResponse deleted = service.deleteAttachment("free", 33L, 91L);

        assertThat(created.item().id()).isEqualTo(91L);
        assertThat(created.item().originalFilename()).isEqualTo("guide.pdf");
        assertThat(created.item().demo()).isFalse();
        assertThat(deleted.item().deleted()).isTrue();
        assertThat(deleted.item().demo()).isFalse();
        verify(repository).attachPost(33L, 91L);
        verify(repository).deletePostAttachment(33L, 91L);
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

    @Test
    void updateAndDeleteCommentRequireBoardPostAndPersist() {
        BoardRepository repository = mock(BoardRepository.class);
        BoardPostDetail post = detail(10L, "Original", List.of());
        BoardCommentItem existing = new BoardCommentItem(44L, 10L, null, "Before", "Demo Student", OffsetDateTime.now(), List.of());
        BoardCommentItem updated = new BoardCommentItem(44L, 10L, null, "After", "Demo Student", OffsetDateTime.now(), List.of());
        given(repository.findBoardId("free")).willReturn(Optional.of(1L));
        given(repository.findPostDetail(1L, 10L)).willReturn(Optional.of(post));
        given(repository.findComment(44L)).willReturn(Optional.of(existing), Optional.of(updated), Optional.of(updated));
        given(repository.updateComment(10L, 44L, "After")).willReturn(1);
        given(repository.deleteComment(10L, 44L)).willReturn(1);
        BoardService service = new BoardService(repository);

        BoardCommentUpdateResponse updateResponse = service.updateComment("free", 10L, 44L, new BoardCommentCreateRequest(null, " After "));
        BoardCommentDeleteResponse deleteResponse = service.deleteComment("free", 10L, 44L);

        assertThat(updateResponse.item().id()).isEqualTo(44L);
        assertThat(updateResponse.item().content()).isEqualTo("After");
        assertThat(updateResponse.item().demo()).isFalse();
        assertThat(deleteResponse.item().id()).isEqualTo(44L);
        assertThat(deleteResponse.item().postId()).isEqualTo(10L);
        assertThat(deleteResponse.item().deleted()).isTrue();
        assertThat(deleteResponse.item().demo()).isFalse();
        verify(repository).updateComment(10L, 44L, "After");
        verify(repository).deleteComment(10L, 44L);
    }

    @Test
    void persistsAndDeletesBoardReaction() {
        BoardRepository repository = mock(BoardRepository.class);
        BoardPostDetail post = detail(10L, "Original", List.of());
        given(repository.findBoardId("free")).willReturn(Optional.of(1L));
        given(repository.findPostDetail(1L, 10L)).willReturn(Optional.of(post));
        given(repository.findDefaultAuthorUserId()).willReturn(Optional.of(7L));
        BoardService service = new BoardService(repository);

        var created = service.createReaction("free", 10L, new BoardReactionCreateRequest(" Like "));
        var deleted = service.deleteReaction("free", 10L, "LIKE");

        assertThat(created.item().postId()).isEqualTo(10L);
        assertThat(created.item().type()).isEqualTo("like");
        assertThat(created.item().active()).isTrue();
        assertThat(created.item().demo()).isFalse();
        assertThat(deleted.item().postId()).isEqualTo(10L);
        assertThat(deleted.item().type()).isEqualTo("like");
        assertThat(deleted.item().active()).isFalse();
        assertThat(deleted.item().demo()).isFalse();
        verify(repository).createReaction(10L, 7L, "like");
        verify(repository).deleteReaction(10L, 7L, "like");
    }

    private BoardPostDetail detail(long id, String title, List<BoardCommentItem> comments) {
        return detail(id, title, "Body", comments);
    }

    private BoardPostDetail detail(long id, String title, String content, List<BoardCommentItem> comments) {
        return new BoardPostDetail(
                id,
                "free",
                new CategorySummary(4L, "General"),
                title,
                content,
                "Demo Student",
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                0,
                new EngagementSummary(comments.size(), 0, 0),
                comments,
                List.of(),
                false,
                false
        );
    }
}
