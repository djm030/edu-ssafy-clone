package com.edussafy.backend.docs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edussafy.backend.docs.api.ApiDocsController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = ApiDocsController.class, properties = "edussafy.auth.interceptor.enabled=false")
class ApiDocsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void servesSpringRestDocsHtmlFromBackendApiRoute() throws Exception {
        String html = mockMvc.perform(get("/api/docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(html).contains("eduSSAFY Clone API Docs");
        assertThat(html).contains("Spring REST Docs");
        assertThat(html).contains("Complete Implemented Endpoint Catalog");
        assertThat(html).contains("Implemented backend API endpoints (141)");
        assertThat(html).contains("/api/auth/login");
        assertThat(html).contains("/api/attendance/check");
        assertThat(html).contains("/api/surveys/{id}/responses");
        assertThat(html).contains("/api/support/tickets/{ticketId}/answers");
    }
}
