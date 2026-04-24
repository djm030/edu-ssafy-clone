package com.edussafy.backend.priority.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edussafy.backend.priority.security.RoleAccessInterceptor;
import com.edussafy.backend.priority.security.RoleAccessWebConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminCampusController.class)
@Import({RoleAccessInterceptor.class, RoleAccessWebConfig.class})
class AdminCampusAccessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void learnerCannotReadAdminCampusStructure() throws Exception {
        mockMvc.perform(get("/api/admin/campus-structure"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    @Test
    void coachCannotCreateAdminCampusClassGroup() throws Exception {
        mockMvc.perform(post("/api/admin/campus-structure/classes")
                        .header(RoleAccessInterceptor.ROLE_HEADER, "coach")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"campusId\":1,\"cohortId\":12,\"trackId\":21,\"name\":\"서울 2반\",\"capacity\":30}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    @Test
    void adminCanReadAndCreateAdminCampusStructure() throws Exception {
        mockMvc.perform(get("/api/admin/campus-structure")
                        .header(RoleAccessInterceptor.ROLE_HEADER, "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.campuses[0].name").value("서울"));

        mockMvc.perform(post("/api/admin/campus-structure/classes")
                        .header(RoleAccessInterceptor.ROLE_HEADER, "admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"campusId\":1,\"cohortId\":12,\"trackId\":21,\"name\":\"서울 2반\",\"capacity\":30}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.item.name").value("서울 2반"));
    }
}
