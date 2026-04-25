package com.edussafy.backend.board.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edussafy.backend.board.dto.BoardCategoryListResponse;
import com.edussafy.backend.board.dto.BoardPostDetailResponse;
import com.edussafy.backend.board.dto.BoardPostDetailResponse.BoardPostDetail;
import com.edussafy.backend.board.dto.BoardPostDetailResponse.EngagementSummary;
import com.edussafy.backend.board.dto.BoardPostListResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardAttachmentCreateResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardAttachmentCreatedItem;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardAttachmentDeleteResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardAttachmentDeletedItem;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardCommentCreateResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardCommentCreatedItem;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardCommentDeleteResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardCommentDeletedItem;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardCommentUpdateResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardPostCreateResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardPostCreatedItem;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardPostDeleteResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardPostDeletedItem;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardPostUpdateResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardReactionCreateResponse;
import com.edussafy.backend.board.dto.BoardWriteDtos.BoardReactionCreatedItem;
import com.edussafy.backend.board.dto.CategorySummary;
import com.edussafy.backend.board.dto.CategoryItem;
import com.edussafy.backend.board.dto.PageMeta;
import com.edussafy.backend.board.error.BoardNotFoundException;
import com.edussafy.backend.board.error.BoardPostNotFoundException;
import com.edussafy.backend.board.service.BoardService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = BoardController.class, properties = "edussafy.auth.interceptor.enabled=false")
class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BoardService boardService;

    @Test
    void healthReturnsUp() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void categoriesReturnItems() throws Exception {
        given(boardService.getCategories("notice"))
                .willReturn(new BoardCategoryListResponse(List.of(new CategoryItem(1L, "General", 1))));

        mockMvc.perform(get("/api/boards/notice/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(1))
                .andExpect(jsonPath("$.items[0].name").value("General"))
                .andExpect(jsonPath("$.items[0].sortOrder").value(1));
    }

    @Test
    void postsReturnEmptyPageShape() throws Exception {
        given(boardService.getPosts(eq("free"), any()))
                .willReturn(new BoardPostListResponse(List.of(), new PageMeta(1, 20, 0, 0)));

        mockMvc.perform(get("/api/boards/free/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.page.page").value(1))
                .andExpect(jsonPath("$.page.size").value(20))
                .andExpect(jsonPath("$.page.totalItems").value(0))
                .andExpect(jsonPath("$.page.totalPages").value(0));
    }

    @Test
    void postDetailReturnsEngagementShape() throws Exception {
        given(boardService.getPost("notice", 10L)).willReturn(new BoardPostDetailResponse(new BoardPostDetail(
                10L,
                "notice",
                new CategorySummary(1L, "General"),
                "Notice title",
                "Notice content",
                "Admin",
                null,
                null,
                7,
                new EngagementSummary(2, 3, 1),
                List.of(),
                List.of(),
                true,
                true
        )));

        mockMvc.perform(get("/api/boards/notice/posts/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.post.id").value(10))
                .andExpect(jsonPath("$.post.title").value("Notice title"))
                .andExpect(jsonPath("$.post.content").value("Notice content"))
                .andExpect(jsonPath("$.post.category.name").value("General"))
                .andExpect(jsonPath("$.post.engagement.commentCount").value(2))
                .andExpect(jsonPath("$.post.hasAttachment").value(true));
    }

    @Test
    void invalidPageReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/boards/free/posts?page=0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));

        verifyNoInteractions(boardService);
    }

    @Test
    void missingBoardReturnsNotFound() throws Exception {
        given(boardService.getPosts(eq("missing"), any()))
                .willThrow(new BoardNotFoundException("missing"));

        mockMvc.perform(get("/api/boards/missing/posts"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("BOARD_NOT_FOUND"));
    }

    @Test
    void missingPostReturnsNotFound() throws Exception {
        given(boardService.getPost("free", 999L)).willThrow(new BoardPostNotFoundException(999L));

        mockMvc.perform(get("/api/boards/free/posts/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("BOARD_POST_NOT_FOUND"));
    }

    @Test
    void createPostReturnsPersistedShape() throws Exception {
        given(boardService.createPost(eq("free"), any())).willReturn(new BoardPostCreateResponse(
                new BoardPostCreatedItem(33L, "free", null, "Hello", "Body", "Demo Learner", null, false)
        ));

        mockMvc.perform(post("/api/boards/free/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Hello\",\"content\":\"Body\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.item.id").value(33))
                .andExpect(jsonPath("$.item.boardCode").value("free"))
                .andExpect(jsonPath("$.item.title").value("Hello"))
                .andExpect(jsonPath("$.item.demo").value(false));
    }

    @Test
    void updateAndDeletePostReturnPersistedShape() throws Exception {
        given(boardService.updatePost(eq("free"), eq(33L), any())).willReturn(new BoardPostUpdateResponse(
                new BoardPostCreatedItem(33L, "free", null, "Updated", "Changed body", "Demo Learner", null, false)
        ));
        given(boardService.deletePost("free", 33L)).willReturn(new BoardPostDeleteResponse(
                new BoardPostDeletedItem(33L, "free", true, false)
        ));

        mockMvc.perform(put("/api/boards/free/posts/33")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Updated\",\"content\":\"Changed body\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.id").value(33))
                .andExpect(jsonPath("$.item.title").value("Updated"))
                .andExpect(jsonPath("$.item.content").value("Changed body"))
                .andExpect(jsonPath("$.item.demo").value(false));
        mockMvc.perform(delete("/api/boards/free/posts/33"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.id").value(33))
                .andExpect(jsonPath("$.item.boardCode").value("free"))
                .andExpect(jsonPath("$.item.deleted").value(true))
                .andExpect(jsonPath("$.item.demo").value(false));
    }

    @Test
    void createAndDeleteAttachmentReturnPersistedShape() throws Exception {
        given(boardService.createAttachment(eq("free"), eq(33L), any())).willReturn(new BoardAttachmentCreateResponse(
                new BoardAttachmentCreatedItem(
                        91L,
                        33L,
                        "guide.pdf",
                        "board/33/guide.pdf",
                        "/uploads/board/33/guide.pdf",
                        "application/pdf",
                        2048L,
                        null,
                        false
                )
        ));
        given(boardService.deleteAttachment("free", 33L, 91L)).willReturn(new BoardAttachmentDeleteResponse(
                new BoardAttachmentDeletedItem(91L, 33L, true, false)
        ));

        mockMvc.perform(post("/api/boards/free/posts/33/attachments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "originalFilename":"guide.pdf",
                                  "storageKey":"board/33/guide.pdf",
                                  "storedPath":"/uploads/board/33/guide.pdf",
                                  "mimeType":"application/pdf",
                                  "fileSize":2048
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.item.id").value(91))
                .andExpect(jsonPath("$.item.postId").value(33))
                .andExpect(jsonPath("$.item.originalFilename").value("guide.pdf"))
                .andExpect(jsonPath("$.item.demo").value(false));
        mockMvc.perform(delete("/api/boards/free/posts/33/attachments/91"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.id").value(91))
                .andExpect(jsonPath("$.item.deleted").value(true))
                .andExpect(jsonPath("$.item.demo").value(false));
    }

    @Test
    void createCommentReturnsPersistedShapeAndReactionCanBeToggled() throws Exception {
        given(boardService.createComment(eq("free"), eq(10L), any())).willReturn(new BoardCommentCreateResponse(
                new BoardCommentCreatedItem(44L, 10L, 40L, "Comment", "Demo Learner", null, false)
        ));
        given(boardService.updateComment(eq("free"), eq(10L), eq(44L), any())).willReturn(new BoardCommentUpdateResponse(
                new BoardCommentCreatedItem(44L, 10L, 40L, "Updated comment", "Demo Learner", null, false)
        ));
        given(boardService.deleteComment("free", 10L, 44L)).willReturn(new BoardCommentDeleteResponse(
                new BoardCommentDeletedItem(44L, 10L, true, false)
        ));
        given(boardService.createReaction(eq("free"), eq(10L), any())).willReturn(new BoardReactionCreateResponse(
                new BoardReactionCreatedItem(10L, "like", true, false)
        ));
        given(boardService.deleteReaction("free", 10L, "like")).willReturn(new BoardReactionCreateResponse(
                new BoardReactionCreatedItem(10L, "like", false, false)
        ));

        mockMvc.perform(post("/api/boards/free/posts/10/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"parentCommentId\":40,\"content\":\"Comment\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.item.id").value(44))
                .andExpect(jsonPath("$.item.postId").value(10))
                .andExpect(jsonPath("$.item.parentCommentId").value(40))
                .andExpect(jsonPath("$.item.demo").value(false));
        mockMvc.perform(put("/api/boards/free/posts/10/comments/44")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Updated comment\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.id").value(44))
                .andExpect(jsonPath("$.item.content").value("Updated comment"))
                .andExpect(jsonPath("$.item.demo").value(false));
        mockMvc.perform(delete("/api/boards/free/posts/10/comments/44"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.id").value(44))
                .andExpect(jsonPath("$.item.postId").value(10))
                .andExpect(jsonPath("$.item.deleted").value(true))
                .andExpect(jsonPath("$.item.demo").value(false));
        mockMvc.perform(post("/api/boards/free/posts/10/reactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"like\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.item.type").value("like"))
                .andExpect(jsonPath("$.item.active").value(true))
                .andExpect(jsonPath("$.item.demo").value(false));
        mockMvc.perform(delete("/api/boards/free/posts/10/reactions/like"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.type").value("like"))
                .andExpect(jsonPath("$.item.active").value(false))
                .andExpect(jsonPath("$.item.demo").value(false));
    }

    @Test
    void boardWritesValidateRequiredFields() throws Exception {
        mockMvc.perform(post("/api/boards/free/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"content\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));

        verifyNoInteractions(boardService);
    }
}
