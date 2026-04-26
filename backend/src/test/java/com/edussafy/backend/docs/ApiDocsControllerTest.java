package com.edussafy.backend.docs;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edussafy.backend.docs.api.ApiDocsController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = ApiDocsController.class, properties = "edussafy.auth.interceptor.enabled=false")
class ApiDocsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void redirectsLegacyApiDocsRouteToSwaggerUi() throws Exception {
        mockMvc.perform(get("/api/docs"))
                .andExpect(status().isFound())
                .andExpect(header().string(HttpHeaders.LOCATION, "/swagger-ui.html"));
    }

    @Test
    void redirectsTrailingSlashApiDocsRouteToSwaggerUi() throws Exception {
        mockMvc.perform(get("/api/docs/"))
                .andExpect(status().isFound())
                .andExpect(header().string(HttpHeaders.LOCATION, "/swagger-ui.html"));
    }
}
