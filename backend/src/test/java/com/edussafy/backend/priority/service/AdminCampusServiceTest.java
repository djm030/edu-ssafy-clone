package com.edussafy.backend.priority.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.edussafy.backend.priority.api.AdminCampusController.CampusCreateRequest;
import com.edussafy.backend.priority.api.AdminCampusController.CampusItem;
import com.edussafy.backend.priority.api.AdminCampusController.CampusStructureResponse;
import com.edussafy.backend.priority.api.AdminCampusController.ClassGroupCreateRequest;
import com.edussafy.backend.priority.api.AdminCampusController.ClassGroupItem;
import com.edussafy.backend.priority.api.AdminCampusController.CohortItem;
import com.edussafy.backend.priority.api.AdminCampusController.TrackItem;
import com.edussafy.backend.priority.repository.AdminCampusRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;

@ExtendWith(MockitoExtension.class)
class AdminCampusServiceTest {

    @Mock
    private AdminCampusRepository repository;

    @InjectMocks
    private AdminCampusService service;

    @Test
    void structureUsesRepositoryData() {
        CampusStructureResponse response = new CampusStructureResponse(
                List.of(new CampusItem(1L, "서울", true)),
                List.of(new CohortItem(12L, "12기", 2026, true)),
                List.of(new TrackItem(21L, "Java", "전공자 Java 트랙", true)),
                List.of(new ClassGroupItem(101L, 1L, 12L, 21L, "서울 1반", null, 0, true))
        );
        given(repository.findStructure()).willReturn(response);

        assertThat(service.structure()).isSameAs(response);
    }

    @Test
    void structureFallsBackWhenDatabaseIsUnavailable() {
        given(repository.findStructure()).willThrow(new DataAccessResourceFailureException("offline"));

        CampusStructureResponse response = service.structure();

        assertThat(response.campuses()).extracting(CampusItem::name).contains("서울");
        assertThat(response.classes()).extracting(ClassGroupItem::name).contains("서울 1반");
    }

    @Test
    void createClassGroupDelegatesToRepository() {
        ClassGroupCreateRequest request = new ClassGroupCreateRequest(1L, 12L, 21L, "서울 2반", "B201", 30);
        ClassGroupItem saved = new ClassGroupItem(102L, 1L, 12L, 21L, "서울 2반", "B201", 30, true);
        given(repository.createClassGroup(request)).willReturn(saved);

        assertThat(service.createClassGroup(request)).isSameAs(saved);
        verify(repository).createClassGroup(request);
    }

    @Test
    void createCampusTrimsNameBeforePersisting() {
        given(repository.createCampus("서울")).willReturn(new CampusItem(2L, "서울", true));

        CampusItem item = service.createCampus(new CampusCreateRequest(" 서울 "));

        assertThat(item.name()).isEqualTo("서울");
        verify(repository).createCampus("서울");
    }
}
