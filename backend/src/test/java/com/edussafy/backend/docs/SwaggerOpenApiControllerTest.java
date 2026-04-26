package com.edussafy.backend.docs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = "edussafy.auth.interceptor.enabled=false")
@AutoConfigureMockMvc
class SwaggerOpenApiControllerTest {

    private static final Path ENDPOINT_CATALOG = Path.of("src", "test", "resources", "api-docs-endpoints.tsv");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void exposesSwaggerUiEntrypoint() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string(HttpHeaders.LOCATION, "/swagger-ui/index.html"));
    }

    @Test
    void exposesOpenApiJsonForRepresentativeImplementedRoutes() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.info.title").value("eduSSAFY Clone API"))
                .andExpect(jsonPath("$.paths['/api/auth/login'].post").exists())
                .andExpect(jsonPath("$.paths['/api/attendance/check'].post").exists())
                .andExpect(jsonPath("$.paths['/api/boards/{boardCode}/posts'].get").exists())
                .andExpect(jsonPath("$.paths['/api/surveys/{id}/responses'].post").exists())
                .andExpect(jsonPath("$.paths['/api/support/tickets/{ticketId}/answers'].post").exists())
                .andExpect(jsonPath("$.paths['/api/learning/materials/{id}/resources/{resourceId}/attachments'].post").exists())
                .andExpect(jsonPath("$.paths['/api/quests/{id}/submissions'].post").exists());
    }

    @Test
    void openApiJsonCoversImplementedApiEndpointCatalog() throws Exception {
        MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode paths = objectMapper.readTree(result.getResponse().getContentAsByteArray()).path("paths");

        List<String> missing = Files.readAllLines(ENDPOINT_CATALOG).stream()
                .filter(line -> !line.isBlank())
                .filter(line -> !line.startsWith("#"))
                .map(line -> line.split("\\t"))
                .filter(fields -> fields.length == 5)
                .filter(fields -> fields[2].startsWith("/api/"))
                .filter(fields -> !fields[2].startsWith("/api/docs"))
                .filter(fields -> !paths.has(fields[2]))
                .map(fields -> fields[1] + " " + fields[2] + " (" + fields[3] + ")")
                .toList();

        assertThat(missing)
                .as("Swagger/OpenAPI should expose every cataloged backend /api route")
                .isEmpty();
    }
}
