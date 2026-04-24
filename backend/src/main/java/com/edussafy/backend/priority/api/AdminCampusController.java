package com.edussafy.backend.priority.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/campus-structure")
public class AdminCampusController {

    private final AtomicLong idSequence = new AtomicLong(500L);

    @GetMapping
    public CampusStructureResponse structure() {
        CampusItem seoul = new CampusItem(1L, "서울", true);
        CohortItem cohort = new CohortItem(12L, "12기", 2026, true);
        TrackItem javaTrack = new TrackItem(21L, "Java", "전공자 Java 트랙", true);
        ClassGroupItem classGroup = new ClassGroupItem(101L, seoul.id(), cohort.id(), javaTrack.id(), "서울 1반", "A101", 28, true);
        return new CampusStructureResponse(List.of(seoul), List.of(cohort), List.of(javaTrack), List.of(classGroup));
    }

    @PostMapping("/campuses")
    public ResponseEntity<ItemResponse<CampusItem>> createCampus(@Valid @RequestBody CampusCreateRequest request) {
        CampusItem item = new CampusItem(idSequence.incrementAndGet(), request.name().trim(), true);
        return ResponseEntity.created(URI.create("/api/admin/campus-structure/campuses/" + item.id())).body(new ItemResponse<>(item));
    }

    @PostMapping("/cohorts")
    public ResponseEntity<ItemResponse<CohortItem>> createCohort(@Valid @RequestBody CohortCreateRequest request) {
        CohortItem item = new CohortItem(idSequence.incrementAndGet(), request.name().trim(), request.year(), true);
        return ResponseEntity.created(URI.create("/api/admin/campus-structure/cohorts/" + item.id())).body(new ItemResponse<>(item));
    }

    @PostMapping("/tracks")
    public ResponseEntity<ItemResponse<TrackItem>> createTrack(@Valid @RequestBody TrackCreateRequest request) {
        TrackItem item = new TrackItem(idSequence.incrementAndGet(), request.name().trim(), request.description(), true);
        return ResponseEntity.created(URI.create("/api/admin/campus-structure/tracks/" + item.id())).body(new ItemResponse<>(item));
    }

    @PostMapping("/classes")
    public ResponseEntity<ItemResponse<ClassGroupItem>> createClassGroup(@Valid @RequestBody ClassGroupCreateRequest request) {
        ClassGroupItem item = new ClassGroupItem(
                idSequence.incrementAndGet(), request.campusId(), request.cohortId(), request.trackId(),
                request.name().trim(), request.classroom(), request.capacity(), true
        );
        return ResponseEntity.created(URI.create("/api/admin/campus-structure/classes/" + item.id())).body(new ItemResponse<>(item));
    }

    public record CampusStructureResponse(List<CampusItem> campuses, List<CohortItem> cohorts, List<TrackItem> tracks, List<ClassGroupItem> classes) {}
    public record ItemResponse<T>(T item) {}
    public record CampusItem(long id, String name, boolean active) {}
    public record CohortItem(long id, String name, int year, boolean active) {}
    public record TrackItem(long id, String name, String description, boolean active) {}
    public record ClassGroupItem(long id, long campusId, long cohortId, long trackId, String name, String classroom, int capacity, boolean active) {}
    public record CampusCreateRequest(@NotBlank String name) {}
    public record CohortCreateRequest(@NotBlank String name, int year) {}
    public record TrackCreateRequest(@NotBlank String name, String description) {}
    public record ClassGroupCreateRequest(@Positive long campusId, @Positive long cohortId, @Positive long trackId, @NotBlank String name, String classroom, int capacity) {}
}
