package com.edussafy.backend.priority.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import com.edussafy.backend.priority.service.AdminCampusService;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/campus-structure")
public class AdminCampusController {

    private final AdminCampusService adminCampusService;

    public AdminCampusController(AdminCampusService adminCampusService) {
        this.adminCampusService = adminCampusService;
    }

    @GetMapping
    public CampusStructureResponse structure() {
        return adminCampusService.structure();
    }

    @PostMapping("/campuses")
    public ResponseEntity<ItemResponse<CampusItem>> createCampus(@Valid @RequestBody CampusCreateRequest request) {
        CampusItem item = adminCampusService.createCampus(request);
        return ResponseEntity.created(URI.create("/api/admin/campus-structure/campuses/" + item.id())).body(new ItemResponse<>(item));
    }

    @PutMapping("/campuses/{campusId}")
    public ItemResponse<CampusItem> updateCampus(@PathVariable long campusId, @Valid @RequestBody CampusUpdateRequest request) {
        return new ItemResponse<>(adminCampusService.updateCampus(campusId, request));
    }

    @DeleteMapping("/campuses/{campusId}")
    public ResponseEntity<Void> deleteCampus(@PathVariable long campusId) {
        adminCampusService.deleteCampus(campusId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/cohorts")
    public ResponseEntity<ItemResponse<CohortItem>> createCohort(@Valid @RequestBody CohortCreateRequest request) {
        CohortItem item = adminCampusService.createCohort(request);
        return ResponseEntity.created(URI.create("/api/admin/campus-structure/cohorts/" + item.id())).body(new ItemResponse<>(item));
    }

    @PutMapping("/cohorts/{cohortId}")
    public ItemResponse<CohortItem> updateCohort(@PathVariable long cohortId, @Valid @RequestBody CohortUpdateRequest request) {
        return new ItemResponse<>(adminCampusService.updateCohort(cohortId, request));
    }

    @DeleteMapping("/cohorts/{cohortId}")
    public ResponseEntity<Void> deleteCohort(@PathVariable long cohortId) {
        adminCampusService.deleteCohort(cohortId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/tracks")
    public ResponseEntity<ItemResponse<TrackItem>> createTrack(@Valid @RequestBody TrackCreateRequest request) {
        TrackItem item = adminCampusService.createTrack(request);
        return ResponseEntity.created(URI.create("/api/admin/campus-structure/tracks/" + item.id())).body(new ItemResponse<>(item));
    }

    @PutMapping("/tracks/{trackId}")
    public ItemResponse<TrackItem> updateTrack(@PathVariable long trackId, @Valid @RequestBody TrackUpdateRequest request) {
        return new ItemResponse<>(adminCampusService.updateTrack(trackId, request));
    }

    @DeleteMapping("/tracks/{trackId}")
    public ResponseEntity<Void> deleteTrack(@PathVariable long trackId) {
        adminCampusService.deleteTrack(trackId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/classes")
    public ResponseEntity<ItemResponse<ClassGroupItem>> createClassGroup(@Valid @RequestBody ClassGroupCreateRequest request) {
        ClassGroupItem item = adminCampusService.createClassGroup(request);
        return ResponseEntity.created(URI.create("/api/admin/campus-structure/classes/" + item.id())).body(new ItemResponse<>(item));
    }

    @PutMapping("/classes/{classId}")
    public ItemResponse<ClassGroupItem> updateClassGroup(
            @PathVariable long classId,
            @Valid @RequestBody ClassGroupUpdateRequest request
    ) {
        return new ItemResponse<>(adminCampusService.updateClassGroup(classId, request));
    }

    @DeleteMapping("/classes/{classId}")
    public ResponseEntity<Void> deleteClassGroup(@PathVariable long classId) {
        adminCampusService.deleteClassGroup(classId);
        return ResponseEntity.noContent().build();
    }

    public record CampusStructureResponse(List<CampusItem> campuses, List<CohortItem> cohorts, List<TrackItem> tracks, List<ClassGroupItem> classes) {}
    public record ItemResponse<T>(T item) {}
    public record CampusItem(long id, String name, boolean active) {}
    public record CohortItem(long id, String name, int year, boolean active) {}
    public record TrackItem(long id, String name, String description, boolean active) {}
    public record ClassGroupItem(long id, long campusId, long cohortId, long trackId, String name, String classroom, int capacity, boolean active) {}
    public record CampusCreateRequest(@NotBlank String name) {}
    public record CampusUpdateRequest(@NotBlank String name) {}
    public record CohortCreateRequest(@NotBlank String name, int year) {}
    public record CohortUpdateRequest(@NotBlank String name, int year) {}
    public record TrackCreateRequest(@NotBlank String name, String description) {}
    public record TrackUpdateRequest(@NotBlank String name, String description) {}
    public record ClassGroupCreateRequest(@Positive long campusId, @Positive long cohortId, @Positive long trackId, @NotBlank String name, String classroom, int capacity) {}
    public record ClassGroupUpdateRequest(@Positive long campusId, @Positive long cohortId, @Positive long trackId, @NotBlank String name, String classroom, int capacity) {}
}
