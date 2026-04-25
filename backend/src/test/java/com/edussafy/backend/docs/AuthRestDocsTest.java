package com.edussafy.backend.docs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edussafy.backend.priority.api.AuthController;
import com.edussafy.backend.priority.dto.PriorityDtos.AuthSessionResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.LoginRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.UserProfile;
import com.edussafy.backend.priority.dto.PriorityDtos.UserResponse;
import com.edussafy.backend.priority.service.PriorityApiService;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = AuthController.class, properties = "edussafy.auth.interceptor.enabled=false")
@AutoConfigureRestDocs(outputDir = "target/generated-snippets")
class AuthRestDocsTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PriorityApiService priorityApiService;

    @Test
    void documentsLoginEndpoint() throws Exception {
        given(priorityApiService.login(any(LoginRequest.class))).willReturn(new UserResponse(new UserProfile(
                1L,
                "Demo Learner",
                "student@ssafy.com",
                "learner",
                "Seoul",
                "12",
                "Java"
        )));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "student@ssafy.com",
                                  "password": "password"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id").value(1))
                .andDo(document(
                        "auth-login",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("email").description("User email address."),
                                fieldWithPath("password").description("Plain password submitted over the active HTTPS/session channel.")
                        ),
                        responseFields(
                                fieldWithPath("user.id").description("Authenticated user id."),
                                fieldWithPath("user.name").description("Authenticated user display name."),
                                fieldWithPath("user.email").description("Authenticated user email."),
                                fieldWithPath("user.role").description("Normalized application role."),
                                fieldWithPath("user.campusName").description("Campus label."),
                                fieldWithPath("user.cohortName").description("Cohort label."),
                                fieldWithPath("user.trackName").description("Training track label.")
                        )
                ));
    }

    @Test
    void documentsSessionEndpoint() throws Exception {
        OffsetDateTime expiresAt = OffsetDateTime.parse("2026-04-25T18:00:00+09:00");
        given(priorityApiService.authSession()).willReturn(new AuthSessionResponse(true, expiresAt, 1800, 1200));

        mockMvc.perform(get("/api/auth/session").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andDo(document(
                        "auth-session",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("authenticated").description("Whether the current request has a valid server session."),
                                fieldWithPath("expiresAt").description("Session expiry timestamp."),
                                fieldWithPath("maxInactiveSeconds").description("Configured session inactivity timeout in seconds."),
                                fieldWithPath("secondsRemaining").description("Remaining session lifetime in seconds at response time.")
                        )
                ));
    }
}
