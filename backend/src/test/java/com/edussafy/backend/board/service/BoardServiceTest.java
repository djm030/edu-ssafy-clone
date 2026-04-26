package com.edussafy.backend.board.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

import com.edussafy.backend.board.dto.BoardPostDetailResponse.BoardAttachmentItem;
import com.edussafy.backend.board.dto.BoardPostDetailResponse.BoardCommentItem;
import com.edussafy.backend.board.dto.BoardPostDetailResponse.BoardPostDetail;
import com.edussafy.backend.board.dto.BoardPostDetailResponse.BoardSafetySummary;
import com.edussafy.backend.board.dto.BoardPostDetailResponse.EngagementSummary;
import com.edussafy.backend.board.dto.BoardPostListItem;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardAttachmentCreateRequest;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardAttachmentCreateResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardAttachmentDeleteResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardAttachmentDownload;
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
import com.edussafy.backend.priority.security.AuthSession;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

class BoardServiceTest {

    @Test
    void getPostAttachesPersistedComments() {
        BoardRepository repository = mock(BoardRepository.class);
        BoardPostDetail post = detail(10L, "Original", List.of());
        BoardCommentItem comment = new BoardCommentItem(44L, 10L, null, "Persisted comment", 7L, "Demo Student", OffsetDateTime.now(), List.of());
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
        BoardCommentItem parent = new BoardCommentItem(44L, 10L, null, "Persisted comment", 7L, "Demo Student", now, List.of());
        BoardCommentItem reply = new BoardCommentItem(45L, 10L, 44L, "Nested reply", 8L, "Demo Manager", now, List.of());
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
    void getPostIncrementsViewCountBeforeReturningDetail() {
        BoardRepository repository = mock(BoardRepository.class);
        BoardPostDetail post = detail(10L, "Notice", List.of());
        given(repository.findBoardId("notice")).willReturn(Optional.of(3L));
        given(repository.findPostDetail(3L, 10L)).willReturn(Optional.of(post));
        BoardService service = new BoardService(repository);

        BoardPostDetail response = service.getPost("notice", 10L).post();

        assertThat(response.id()).isEqualTo(10L);
        verify(repository).incrementViewCount(3L, 10L);
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
        given(repository.findDefaultAuthorUserId()).willReturn(Optional.of(7L));
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
    void updatePostRejectsNonAuthorInWebRequest() {
        BoardRepository repository = mock(BoardRepository.class);
        BoardPostDetail existing = detail(33L, "Before", List.of());
        given(repository.findBoardId("free")).willReturn(Optional.of(1L));
        given(repository.existsCategory(1L, 4L)).willReturn(true);
        given(repository.findPostDetail(1L, 33L)).willReturn(Optional.of(existing));
        BoardService service = new BoardService(repository);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthSession.CURRENT_USER_ID, 8L);
        request.setSession(session);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        try {
            assertThatThrownBy(() -> service.updatePost(
                    "free",
                    33L,
                    new BoardPostCreateRequest(4L, "Updated", "Changed body")
            ))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("403")
                    .hasMessageContaining("게시글 작성자만");
            verify(repository, never()).updatePost(1L, 33L, 4L, "Updated", "Changed body");
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    void deletePostRequiresBoardPostAndDeletes() {
        BoardRepository repository = mock(BoardRepository.class);
        BoardPostDetail existing = detail(33L, "Before", List.of());
        given(repository.findBoardId("free")).willReturn(Optional.of(1L));
        given(repository.findPostDetail(1L, 33L)).willReturn(Optional.of(existing));
        given(repository.findDefaultAuthorUserId()).willReturn(Optional.of(7L));
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
    void anonymousPostListMasksAuthorName() {
        BoardRepository repository = mock(BoardRepository.class);
        BoardPostListItem persisted = new BoardPostListItem(
                70L,
                "anonymous",
                new CategorySummary(11L, "General"),
                "Secret question",
                "Real Student",
                OffsetDateTime.now(),
                12,
                2,
                3,
                1,
                false,
                false
        );
        given(repository.findBoardId("anonymous")).willReturn(Optional.of(2L));
        given(repository.countPosts(2L, new BoardQuery(null, null, 1, 20, "createdAt,desc"))).willReturn(1L);
        given(repository.findPosts(2L, new BoardQuery(null, null, 1, 20, "createdAt,desc"), BoardSort.parse("createdAt,desc")))
                .willReturn(List.of(persisted));
        BoardService service = new BoardService(repository);

        BoardPostListItem response = service.getPosts(
                "anonymous",
                new BoardQuery(null, null, 1, 20, "createdAt,desc")
        ).items().getFirst();

        assertThat(response.authorName()).isEqualTo("익명");
        assertThat(response.title()).isEqualTo("Secret question");
        assertThat(response.boardCode()).isEqualTo("anonymous");
    }

    @Test
    void anonymousPostDetailMasksPostAndCommentAuthors() {
        BoardRepository repository = mock(BoardRepository.class);
        BoardPostDetail post = detail(70L, "anonymous", "Secret question", "Hidden body", 7L, "Real Student", List.of());
        BoardCommentItem comment = new BoardCommentItem(80L, 70L, null, "Same here", 8L, "Classmate", OffsetDateTime.now(), List.of());
        given(repository.findBoardId("anonymous")).willReturn(Optional.of(2L));
        given(repository.findPostDetail(2L, 70L)).willReturn(Optional.of(post));
        given(repository.findComments(70L)).willReturn(List.of(comment));
        given(repository.findAttachments(70L)).willReturn(List.of());
        BoardService service = new BoardService(repository);

        BoardPostDetail response = service.getPost("anonymous", 70L).post();

        assertThat(response.authorUserId()).isNull();
        assertThat(response.authorName()).isEqualTo("익명");
        assertThat(response.comments()).hasSize(1);
        assertThat(response.comments().getFirst().authorUserId()).isNull();
        assertThat(response.comments().getFirst().authorName()).isEqualTo("익명");
    }

    @Test
    void anonymousPostDetailExposesWatchSafetyFromReportCount() {
        BoardRepository repository = mock(BoardRepository.class);
        BoardPostDetail post = detailWithSafety(
                73L,
                "anonymous",
                "검토 대상 질문",
                "아직 표시되는 본문",
                7L,
                "Real Student",
                new BoardSafetySummary("watch", "신고 검토", 2, true, "신고가 누적되어 운영자 검토 대기 상태입니다."),
                List.of()
        );
        given(repository.findBoardId("anonymous")).willReturn(Optional.of(2L));
        given(repository.findPostDetail(2L, 73L)).willReturn(Optional.of(post));
        given(repository.findComments(73L)).willReturn(List.of());
        given(repository.findAttachments(73L)).willReturn(List.of());
        BoardService service = new BoardService(repository);

        BoardPostDetail response = service.getPost("anonymous", 73L).post();

        assertThat(response.authorUserId()).isNull();
        assertThat(response.authorName()).isEqualTo("익명");
        assertThat(response.title()).isEqualTo("검토 대상 질문");
        assertThat(response.content()).isEqualTo("아직 표시되는 본문");
        assertThat(response.safety()).isNotNull();
        assertThat(response.safety().status()).isEqualTo("watch");
        assertThat(response.safety().reportCount()).isEqualTo(2);
    }

    @Test
    void anonymousPostDetailBlindsBodyAndCommentsAfterReportThreshold() {
        BoardRepository repository = mock(BoardRepository.class);
        BoardPostDetail post = detailWithSafety(
                74L,
                "anonymous",
                "원문 제목",
                "원문 본문",
                7L,
                "Real Student",
                new BoardSafetySummary("blinded", "블라인드", 3, true, "신고 누적으로 숨김 처리되었습니다."),
                List.of(new BoardCommentItem(81L, 74L, null, "원문 댓글", 8L, "Classmate", OffsetDateTime.now(), List.of()))
        );
        given(repository.findBoardId("anonymous")).willReturn(Optional.of(2L));
        given(repository.findPostDetail(2L, 74L)).willReturn(Optional.of(post));
        given(repository.findComments(74L)).willReturn(post.comments());
        given(repository.findAttachments(74L)).willReturn(List.of());
        BoardService service = new BoardService(repository);

        BoardPostDetail response = service.getPost("anonymous", 74L).post();

        assertThat(response.authorUserId()).isNull();
        assertThat(response.title()).contains("블라인드");
        assertThat(response.content()).contains("본문을 표시하지 않습니다");
        assertThat(response.comments()).isEmpty();
        assertThat(response.safety().status()).isEqualTo("blinded");
        assertThat(response.safety().reportCount()).isEqualTo(3);
    }

    @Test
    void createAnonymousPostStoresRealAuthorButReturnsMaskedAuthor() {
        BoardRepository repository = mock(BoardRepository.class);
        BoardPostDetail created = detail(71L, "anonymous", "Secret", "Body", 7L, "Demo Student", List.of());
        given(repository.findBoardId("anonymous")).willReturn(Optional.of(2L));
        given(repository.findDefaultAuthorUserId()).willReturn(Optional.of(7L));
        given(repository.createPost(2L, null, 7L, "Secret", "Body")).willReturn(71L);
        given(repository.findPostDetail(2L, 71L)).willReturn(Optional.of(created));
        BoardService service = new BoardService(repository);

        BoardPostCreateResponse response = service.createPost("anonymous", new BoardPostCreateRequest(null, " Secret ", " Body "));

        assertThat(response.item().boardCode()).isEqualTo("anonymous");
        assertThat(response.item().authorName()).isEqualTo("익명");
        verify(repository).createPost(2L, null, 7L, "Secret", "Body");
    }

    @Test
    void updateAnonymousPostRejectsNonAuthorInWebRequest() {
        BoardRepository repository = mock(BoardRepository.class);
        BoardPostDetail existing = detail(72L, "anonymous", "Before", "Body", 7L, "Demo Student", List.of());
        given(repository.findBoardId("anonymous")).willReturn(Optional.of(2L));
        given(repository.findPostDetail(2L, 72L)).willReturn(Optional.of(existing));
        BoardService service = new BoardService(repository);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthSession.CURRENT_USER_ID, 8L);
        request.setSession(session);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        try {
            assertThatThrownBy(() -> service.updatePost(
                    "anonymous",
                    72L,
                    new BoardPostCreateRequest(null, "Updated", "Changed body")
            ))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("403")
                    .hasMessageContaining("게시글 작성자만");
            verify(repository, never()).updatePost(2L, 72L, null, "Updated", "Changed body");
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
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
                null,
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
    void createAttachmentWithContentStoresBytesAndDownloadReadsThem() throws Exception {
        BoardRepository repository = mock(BoardRepository.class);
        BoardPostDetail post = detail(33L, "Before", List.of());
        OffsetDateTime createdAt = OffsetDateTime.now();
        String checksum = "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824";
        String storageKey = "boards/free/posts/33/2cf24dba5fb0-guide.txt";
        BoardAttachmentItem attachment = new BoardAttachmentItem(
                91L,
                "guide.txt",
                storageKey,
                "/boards/free/posts/33/attachments/" + checksum,
                "text/plain",
                5L,
                createdAt
        );
        given(repository.findBoardId("free")).willReturn(Optional.of(1L));
        given(repository.findPostDetail(1L, 33L)).willReturn(Optional.of(post));
        given(repository.createAttachment(
                "guide.txt",
                storageKey,
                "/boards/free/posts/33/attachments/" + checksum,
                "text/plain",
                5L,
                checksum
        )).willReturn(91L);
        given(repository.findAttachment(33L, 91L)).willReturn(Optional.of(attachment));
        BoardService service = new BoardService(repository);
        Path storedFile = Path.of(System.getProperty("java.io.tmpdir"), "edussafy-attachments", storageKey);
        Files.deleteIfExists(storedFile);

        BoardAttachmentCreateResponse created = service.createAttachment("free", 33L, new BoardAttachmentCreateRequest(
                "guide.txt",
                null,
                null,
                "text/plain",
                null,
                null,
                "aGVsbG8="
        ));
        BoardAttachmentDownload download = service.downloadAttachment("free", 33L, 91L);

        assertThat(created.item().id()).isEqualTo(91L);
        assertThat(created.item().fileSize()).isEqualTo(5L);
        assertThat(Files.readAllBytes(storedFile)).isEqualTo("hello".getBytes());
        assertThat(download.item().originalFilename()).isEqualTo("guide.txt");
        assertThat(download.content()).isEqualTo("hello".getBytes());
        verify(repository).attachPost(33L, 91L);
    }

    @Test
    void createCommentRequiresBoardPostAndPersists() {
        BoardRepository repository = mock(BoardRepository.class);
        BoardPostDetail post = detail(10L, "Original", List.of());
        BoardCommentItem comment = new BoardCommentItem(44L, 10L, null, "Nice", 7L, "Demo Student", OffsetDateTime.now(), List.of());
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
        BoardCommentItem parent = new BoardCommentItem(44L, 10L, null, "Question", 7L, "Demo Student", OffsetDateTime.now(), List.of());
        BoardCommentItem reply = new BoardCommentItem(45L, 10L, 44L, "Reply", 7L, "Demo Student", OffsetDateTime.now(), List.of());
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
        BoardCommentItem existing = new BoardCommentItem(44L, 10L, null, "Before", 7L, "Demo Student", OffsetDateTime.now(), List.of());
        BoardCommentItem updated = new BoardCommentItem(44L, 10L, null, "After", 7L, "Demo Student", OffsetDateTime.now(), List.of());
        given(repository.findBoardId("free")).willReturn(Optional.of(1L));
        given(repository.findPostDetail(1L, 10L)).willReturn(Optional.of(post));
        given(repository.findComment(44L)).willReturn(Optional.of(existing), Optional.of(updated), Optional.of(updated));
        given(repository.findDefaultAuthorUserId()).willReturn(Optional.of(7L));
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
    void updateCommentRejectsNonAuthorInWebRequest() {
        BoardRepository repository = mock(BoardRepository.class);
        BoardPostDetail post = detail(10L, "Original", List.of());
        BoardCommentItem existing = new BoardCommentItem(44L, 10L, null, "Before", 7L, "Demo Student", OffsetDateTime.now(), List.of());
        given(repository.findBoardId("free")).willReturn(Optional.of(1L));
        given(repository.findPostDetail(1L, 10L)).willReturn(Optional.of(post));
        given(repository.findComment(44L)).willReturn(Optional.of(existing));
        BoardService service = new BoardService(repository);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthSession.CURRENT_USER_ID, 8L);
        request.setSession(session);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        try {
            assertThatThrownBy(() -> service.updateComment(
                    "free",
                    10L,
                    44L,
                    new BoardCommentCreateRequest(null, "After")
            ))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("403")
                    .hasMessageContaining("댓글 작성자만");
            verify(repository, never()).updateComment(10L, 44L, "After");
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    void deleteCommentAllowsModeratorInWebRequest() {
        BoardRepository repository = mock(BoardRepository.class);
        BoardPostDetail post = detail(10L, "Original", List.of());
        BoardCommentItem existing = new BoardCommentItem(44L, 10L, null, "Before", 7L, "Demo Student", OffsetDateTime.now(), List.of());
        given(repository.findBoardId("free")).willReturn(Optional.of(1L));
        given(repository.findPostDetail(1L, 10L)).willReturn(Optional.of(post));
        given(repository.findComment(44L)).willReturn(Optional.of(existing));
        given(repository.deleteComment(10L, 44L)).willReturn(1);
        BoardService service = new BoardService(repository);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthSession.CURRENT_USER_ID, 8L);
        request.setSession(session);
        request.setAttribute("currentRole", "coach");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        try {
            BoardCommentDeleteResponse response = service.deleteComment("free", 10L, 44L);

            assertThat(response.item().deleted()).isTrue();
            verify(repository).deleteComment(10L, 44L);
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
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

    @Test
    void persistsAnonymousBoardReportReaction() {
        BoardRepository repository = mock(BoardRepository.class);
        BoardPostDetail post = detail(75L, "anonymous", "Secret", "Body", 7L, "Demo Student", List.of());
        given(repository.findBoardId("anonymous")).willReturn(Optional.of(2L));
        given(repository.findPostDetail(2L, 75L)).willReturn(Optional.of(post));
        given(repository.findDefaultAuthorUserId()).willReturn(Optional.of(8L));
        BoardService service = new BoardService(repository);

        var created = service.createReaction("anonymous", 75L, new BoardReactionCreateRequest(" Report "));

        assertThat(created.item().postId()).isEqualTo(75L);
        assertThat(created.item().type()).isEqualTo("report");
        assertThat(created.item().active()).isTrue();
        verify(repository).createReaction(75L, 8L, "report");
    }

    private BoardPostDetail detail(long id, String title, List<BoardCommentItem> comments) {
        return detail(id, title, "Body", comments);
    }

    private BoardPostDetail detail(long id, String title, String content, List<BoardCommentItem> comments) {
        return detail(id, "free", title, content, 7L, "Demo Student", comments);
    }

    private BoardPostDetail detail(
            long id,
            String boardCode,
            String title,
            String content,
            long authorUserId,
            String authorName,
            List<BoardCommentItem> comments
    ) {
        return new BoardPostDetail(
                id,
                boardCode,
                new CategorySummary("anonymous".equals(boardCode) ? 11L : 4L, "General"),
                title,
                content,
                authorUserId,
                authorName,
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

    private BoardPostDetail detailWithSafety(
            long id,
            String boardCode,
            String title,
            String content,
            long authorUserId,
            String authorName,
            BoardSafetySummary safety,
            List<BoardCommentItem> comments
    ) {
        return new BoardPostDetail(
                id,
                boardCode,
                new CategorySummary("anonymous".equals(boardCode) ? 11L : 4L, "General"),
                title,
                content,
                authorUserId,
                authorName,
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                0,
                new EngagementSummary(comments.size(), 0, 0),
                comments,
                List.of(),
                false,
                false,
                safety
        );
    }
}
