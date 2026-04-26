package com.edussafy.backend.docs;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edussafy.backend.board.api.BoardController;
import com.edussafy.backend.board.service.BoardService;
import com.edussafy.backend.config.SecurityHeadersFilter;
import com.edussafy.backend.health.dto.HealthResponse;
import com.edussafy.backend.health.dto.HealthResponse.HealthCheckItem;
import com.edussafy.backend.health.service.HealthService;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = BoardController.class, properties = "edussafy.auth.interceptor.enabled=false")
@Import(SecurityHeadersFilter.class)
class SecurityHeadersFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BoardService boardService;

    @MockBean
    private HealthService healthService;

    @Test
    void apiResponsesIncludeProductionSecurityHeaders() throws Exception {
        given(healthService.getHealth()).willReturn(new HealthResponse(
                "UP",
                OffsetDateTime.parse("2026-04-26T03:30:00Z"),
                "edussafy-backend",
                "test",
                List.of(new HealthCheckItem("database", "UP", true, "MySQL connectivity check passed."))
        ));

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("Referrer-Policy", "strict-origin-when-cross-origin"))
                .andExpect(header().string("Cross-Origin-Resource-Policy", "same-origin"))
                .andExpect(header().string("Cache-Control", "no-store"));
    }
}
