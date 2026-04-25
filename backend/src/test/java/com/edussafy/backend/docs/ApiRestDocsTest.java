package com.edussafy.backend.docs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edussafy.backend.board.api.BoardController;
import com.edussafy.backend.board.dto.BoardPostListResponse;
import com.edussafy.backend.board.dto.PageMeta;
import com.edussafy.backend.board.service.BoardService;
import com.edussafy.backend.priority.api.AttendanceController;
import com.edussafy.backend.priority.api.AuthController;
import com.edussafy.backend.priority.api.CommunityController;
import com.edussafy.backend.priority.api.DashboardController;
import com.edussafy.backend.priority.api.LearningController;
import com.edussafy.backend.priority.api.NotificationController;
import com.edussafy.backend.priority.api.ProfileController;
import com.edussafy.backend.priority.api.QuestSurveyController;
import com.edussafy.backend.priority.api.SupportController;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialReactionItem;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialReactionResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketCreateResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketItem;
import com.edussafy.backend.priority.dto.PriorityDtos.UserProfile;
import com.edussafy.backend.priority.dto.PriorityDtos.UserResponse;
import com.edussafy.backend.priority.security.RoleAccessInterceptor;
import com.edussafy.backend.priority.security.RoleAccessWebConfig;
import com.edussafy.backend.priority.service.PriorityApiService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({
        AuthController.class,
        DashboardController.class,
        AttendanceController.class,
        NotificationController.class,
        LearningController.class,
        QuestSurveyController.class,
        SupportController.class,
        CommunityController.class,
        ProfileController.class,
        BoardController.class
})
@Import({RoleAccessInterceptor.class, RoleAccessWebConfig.class})
@AutoConfigureRestDocs(outputDir = "target/generated-snippets")
class ApiRestDocsTest {

    private static final UserProfile USER = new UserProfile(
            1L, "Demo Learner", "student@ssafy.com", "learner", "Seoul", "12", "Java"
    );

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PriorityApiService priorityApiService;

    @MockBean
    private BoardService boardService;

    @Test
    void documentsAuthLogin() throws Exception {
        given(priorityApiService.login(any())).willReturn(new UserResponse(USER));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"student@ssafy.com\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andDo(document("auth-login", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())));
    }

    @Test
    void documentsMaterialReactionToggle() throws Exception {
        given(priorityApiService.toggleMaterialReaction(eq(5L), any())).willReturn(new MaterialReactionResponse(
                new MaterialReactionItem(5L, "favorite", true, 3L, 1L, 2L, false, true, false, false)
        ));

        mockMvc.perform(post("/api/learning/materials/5/reactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"favorite\"}"))
                .andExpect(status().isCreated())
                .andDo(document("learning-material-reaction", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())));
    }

    @Test
    void documentsBoardPostList() throws Exception {
        given(boardService.getPosts(eq("free"), any()))
                .willReturn(new BoardPostListResponse(List.of(), new PageMeta(1, 20, 0, 0)));

        mockMvc.perform(get("/api/boards/free/posts")
                        .param("page", "1")
                        .param("size", "20")
                        .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andDo(document("board-post-list", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())));
    }

    @Test
    void documentsSupportTicketCreate() throws Exception {
        given(priorityApiService.createSupportTicket(any())).willReturn(new SupportTicketCreateResponse(
                new SupportTicketItem(0L, "Need help", "open", null, null, null, 1L, null)
        ));

        mockMvc.perform(post("/api/support/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Need help\",\"content\":\"Please check this.\"}"))
                .andExpect(status().isCreated())
                .andDo(document("support-ticket-create", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())));
    }
}
