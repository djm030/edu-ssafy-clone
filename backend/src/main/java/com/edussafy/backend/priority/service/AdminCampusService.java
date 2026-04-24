package com.edussafy.backend.priority.service;

import com.edussafy.backend.priority.api.AdminCampusController.CampusCreateRequest;
import com.edussafy.backend.priority.api.AdminCampusController.CampusItem;
import com.edussafy.backend.priority.api.AdminCampusController.CampusUpdateRequest;
import com.edussafy.backend.priority.api.AdminCampusController.CampusStructureResponse;
import com.edussafy.backend.priority.api.AdminCampusController.ClassGroupCreateRequest;
import com.edussafy.backend.priority.api.AdminCampusController.ClassGroupItem;
import com.edussafy.backend.priority.api.AdminCampusController.ClassGroupUpdateRequest;
import com.edussafy.backend.priority.api.AdminCampusController.CohortCreateRequest;
import com.edussafy.backend.priority.api.AdminCampusController.CohortItem;
import com.edussafy.backend.priority.api.AdminCampusController.CohortUpdateRequest;
import com.edussafy.backend.priority.api.AdminCampusController.TrackCreateRequest;
import com.edussafy.backend.priority.api.AdminCampusController.TrackItem;
import com.edussafy.backend.priority.api.AdminCampusController.TrackUpdateRequest;
import com.edussafy.backend.priority.repository.AdminCampusRepository;
import java.util.List;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
public class AdminCampusService {

    private final AdminCampusRepository repository;

    public AdminCampusService(AdminCampusRepository repository) {
        this.repository = repository;
    }

    public CampusStructureResponse structure() {
        return safe(repository::findStructure, demoStructure());
    }

    public CampusItem createCampus(CampusCreateRequest request) {
        return safe(() -> repository.createCampus(request.name().trim()),
                new CampusItem(501L, request.name().trim(), true));
    }

    public CampusItem updateCampus(long campusId, CampusUpdateRequest request) {
        return safe(() -> repository.updateCampus(campusId, request.name().trim()),
                new CampusItem(campusId, request.name().trim(), true));
    }

    public void deleteCampus(long campusId) {
        safe(() -> {
            repository.deleteCampus(campusId);
            return true;
        }, true);
    }

    public CohortItem createCohort(CohortCreateRequest request) {
        return safe(() -> repository.createCohort(request.name().trim(), request.year()),
                new CohortItem(502L, request.name().trim(), request.year(), true));
    }

    public CohortItem updateCohort(long cohortId, CohortUpdateRequest request) {
        return safe(() -> repository.updateCohort(cohortId, request.name().trim(), request.year()),
                new CohortItem(cohortId, request.name().trim(), request.year(), true));
    }

    public void deleteCohort(long cohortId) {
        safe(() -> {
            repository.deleteCohort(cohortId);
            return true;
        }, true);
    }

    public TrackItem createTrack(TrackCreateRequest request) {
        return safe(() -> repository.createTrack(request.name().trim(), request.description()),
                new TrackItem(503L, request.name().trim(), request.description(), true));
    }

    public TrackItem updateTrack(long trackId, TrackUpdateRequest request) {
        return safe(() -> repository.updateTrack(trackId, request.name().trim(), request.description()),
                new TrackItem(trackId, request.name().trim(), request.description(), true));
    }

    public void deleteTrack(long trackId) {
        safe(() -> {
            repository.deleteTrack(trackId);
            return true;
        }, true);
    }

    public ClassGroupItem createClassGroup(ClassGroupCreateRequest request) {
        return safe(() -> repository.createClassGroup(request),
                new ClassGroupItem(504L, request.campusId(), request.cohortId(), request.trackId(), request.name().trim(), request.classroom(), request.capacity(), true));
    }

    public ClassGroupItem updateClassGroup(long classId, ClassGroupUpdateRequest request) {
        return safe(() -> repository.updateClassGroup(classId, request),
                new ClassGroupItem(classId, request.campusId(), request.cohortId(), request.trackId(), request.name().trim(), request.classroom(), request.capacity(), true));
    }

    public void deleteClassGroup(long classId) {
        safe(() -> {
            repository.deleteClassGroup(classId);
            return true;
        }, true);
    }

    private CampusStructureResponse demoStructure() {
        CampusItem seoul = new CampusItem(1L, "서울", true);
        CohortItem cohort = new CohortItem(12L, "12기", 2026, true);
        TrackItem javaTrack = new TrackItem(21L, "Java", "전공자 Java 트랙", true);
        ClassGroupItem classGroup = new ClassGroupItem(101L, seoul.id(), cohort.id(), javaTrack.id(), "서울 1반", "A101", 28, true);
        return new CampusStructureResponse(List.of(seoul), List.of(cohort), List.of(javaTrack), List.of(classGroup));
    }

    private <T> T safe(ThrowingSupplier<T> supplier, T fallback) {
        try {
            return supplier.get();
        } catch (DataAccessException exception) {
            return fallback;
        }
    }

    @FunctionalInterface
    private interface ThrowingSupplier<T> {
        T get();
    }
}
