package com.edussafy.backend.priority.api;

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

import com.edussafy.backend.priority.api.AdminCampusController.CampusItem;
import com.edussafy.backend.priority.api.AdminCampusController.CampusUpdateRequest;
import com.edussafy.backend.priority.api.AdminCampusController.CampusStructureResponse;
import com.edussafy.backend.priority.api.AdminCampusController.ClassGroupCreateRequest;
import com.edussafy.backend.priority.api.AdminCampusController.ClassGroupItem;
import com.edussafy.backend.priority.api.AdminCampusController.ClassGroupUpdateRequest;
import com.edussafy.backend.priority.api.AdminCampusController.CohortItem;
import com.edussafy.backend.priority.api.AdminCampusController.TrackItem;
import com.edussafy.backend.priority.security.RoleAccessInterceptor;
import com.edussafy.backend.priority.security.RoleAccessWebConfig;
import com.edussafy.backend.priority.service.AdminCampusService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminCampusController.class)
@Import({RoleAccessInterceptor.class, RoleAccessWebConfig.class})
class AdminCampusAccessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminCampusService adminCampusService;

    @Test
    void learnerCannotReadAdminCampusStructure() throws Exception {
        mockMvc.perform(get("/api/admin/campus-structure"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));

        verifyNoInteractions(adminCampusService);
    }

    @Test
    void coachCannotCreateAdminCampusClassGroup() throws Exception {
        mockMvc.perform(post("/api/admin/campus-structure/classes")
                        .header(RoleAccessInterceptor.ROLE_HEADER, "coach")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"campusId\":1,\"cohortId\":12,\"trackId\":21,\"name\":\"서울 2반\",\"capacity\":30}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));

        verifyNoInteractions(adminCampusService);
    }

    @Test
    void adminCanReadAndCreateAdminCampusStructure() throws Exception {
        given(adminCampusService.structure()).willReturn(new CampusStructureResponse(
                List.of(new CampusItem(1L, "서울", true)),
                List.of(new CohortItem(12L, "12기", 2026, true)),
                List.of(new TrackItem(21L, "Java", "전공자 Java 트랙", true)),
                List.of(new ClassGroupItem(101L, 1L, 12L, 21L, "서울 1반", "A101", 28, true))
        ));
        given(adminCampusService.createClassGroup(any(ClassGroupCreateRequest.class)))
                .willReturn(new ClassGroupItem(102L, 1L, 12L, 21L, "서울 2반", null, 30, true));

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

    @Test
    void adminCanUpdateAndDeleteAdminCampus() throws Exception {
        given(adminCampusService.updateCampus(eq(1L), any(CampusUpdateRequest.class)))
                .willReturn(new CampusItem(1L, "서울 수정", true));

        mockMvc.perform(put("/api/admin/campus-structure/campuses/1")
                        .header(RoleAccessInterceptor.ROLE_HEADER, "admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"서울 수정\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.name").value("서울 수정"));

        mockMvc.perform(delete("/api/admin/campus-structure/campuses/1")
                        .header(RoleAccessInterceptor.ROLE_HEADER, "admin"))
                .andExpect(status().isNoContent());
    }

    @Test
    void adminCanUpdateAndDeleteAdminCampusClassGroup() throws Exception {
        given(adminCampusService.updateClassGroup(eq(101L), any(ClassGroupUpdateRequest.class)))
                .willReturn(new ClassGroupItem(101L, 1L, 12L, 21L, "서울 1반 수정", "B201", 30, true));

        mockMvc.perform(put("/api/admin/campus-structure/classes/101")
                        .header(RoleAccessInterceptor.ROLE_HEADER, "admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"campusId\":1,\"cohortId\":12,\"trackId\":21,\"name\":\"서울 1반 수정\",\"classroom\":\"B201\",\"capacity\":30}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.name").value("서울 1반 수정"));

        mockMvc.perform(delete("/api/admin/campus-structure/classes/101")
                        .header(RoleAccessInterceptor.ROLE_HEADER, "admin"))
                .andExpect(status().isNoContent());
    }
}
