package com.edussafy.backend.docs;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edussafy.backend.board.api.BoardController;
import com.edussafy.backend.board.service.BoardService;
import com.edussafy.backend.health.dto.HealthResponse;
import com.edussafy.backend.health.dto.HealthResponse.HealthCheckItem;
import com.edussafy.backend.health.service.HealthService;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = BoardController.class, properties = "edussafy.auth.interceptor.enabled=false")
@AutoConfigureRestDocs(outputDir = "target/generated-snippets")
class ApiRestDocsTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BoardService boardService;

    @MockBean
    private HealthService healthService;

    @Test
    void documentsHealthEndpoint() throws Exception {
        given(healthService.getHealth()).willReturn(new HealthResponse(
                "UP",
                OffsetDateTime.parse("2026-04-26T03:30:00Z"),
                "edussafy-backend",
                "prod",
                List.of(
                        new HealthCheckItem("database", "UP", true, "MySQL connectivity check passed."),
                        new HealthCheckItem("temp-storage", "UP", true, "Temporary attachment storage is writable.")
                )
        ));

        mockMvc.perform(get("/api/health").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andDo(document(
                        "health-check",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("status").description("Overall backend service availability status."),
                                fieldWithPath("checkedAt").description("UTC timestamp when the probe was evaluated."),
                                fieldWithPath("service").description("Backend service identifier."),
                                fieldWithPath("profile").description("Active Spring profile summary."),
                                fieldWithPath("checks").description("Individual production readiness probes."),
                                fieldWithPath("checks[].name").description("Probe name."),
                                fieldWithPath("checks[].status").description("Probe status."),
                                fieldWithPath("checks[].required").description("Whether this probe blocks readiness."),
                                fieldWithPath("checks[].message").description("Human-readable probe result.")
                        )
                ));
    }
}
