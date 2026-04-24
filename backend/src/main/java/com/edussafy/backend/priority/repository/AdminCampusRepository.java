package com.edussafy.backend.priority.repository;

import com.edussafy.backend.priority.api.AdminCampusController.CampusItem;
import com.edussafy.backend.priority.api.AdminCampusController.CampusStructureResponse;
import com.edussafy.backend.priority.api.AdminCampusController.ClassGroupCreateRequest;
import com.edussafy.backend.priority.api.AdminCampusController.ClassGroupItem;
import com.edussafy.backend.priority.api.AdminCampusController.ClassGroupUpdateRequest;
import com.edussafy.backend.priority.api.AdminCampusController.CohortItem;
import com.edussafy.backend.priority.api.AdminCampusController.TrackItem;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class AdminCampusRepository {

    private final JdbcClient jdbcClient;

    public AdminCampusRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public CampusStructureResponse findStructure() {
        return new CampusStructureResponse(findCampuses(), findCohorts(), findTracks(), findClassGroups());
    }

    public List<CampusItem> findCampuses() {
        return jdbcClient.sql("""
                SELECT campus_id, campus_name
                FROM campuses
                ORDER BY campus_id ASC
                """)
                .query((rs, rowNum) -> new CampusItem(rs.getLong("campus_id"), rs.getString("campus_name"), true))
                .list();
    }

    public List<CohortItem> findCohorts() {
        return jdbcClient.sql("""
                SELECT cohort_id, cohort_name, start_date
                FROM cohorts
                ORDER BY cohort_id ASC
                """)
                .query((rs, rowNum) -> new CohortItem(
                        rs.getLong("cohort_id"),
                        rs.getString("cohort_name"),
                        yearFromDate(rs.getDate("start_date")),
                        true
                ))
                .list();
    }

    public List<TrackItem> findTracks() {
        return jdbcClient.sql("""
                SELECT track_id, track_name, domain_type
                FROM tracks
                ORDER BY track_id ASC
                """)
                .query((rs, rowNum) -> new TrackItem(
                        rs.getLong("track_id"),
                        rs.getString("track_name"),
                        rs.getString("domain_type"),
                        true
                ))
                .list();
    }

    public List<ClassGroupItem> findClassGroups() {
        return jdbcClient.sql("""
                SELECT class_group_id, campus_id, cohort_id, track_id, class_name
                FROM class_groups
                ORDER BY class_group_id ASC
                """)
                .query((rs, rowNum) -> new ClassGroupItem(
                        rs.getLong("class_group_id"),
                        rs.getLong("campus_id"),
                        rs.getLong("cohort_id"),
                        rs.getLong("track_id"),
                        rs.getString("class_name"),
                        null,
                        0,
                        true
                ))
                .list();
    }

    public CampusItem createCampus(String name) {
        jdbcClient.sql("INSERT INTO campuses (campus_name) VALUES (:name)")
                .param("name", name)
                .update();
        long id = jdbcClient.sql("SELECT campus_id FROM campuses WHERE campus_name = :name")
                .param("name", name)
                .query(Long.class)
                .single();
        return new CampusItem(id, name, true);
    }

    public CampusItem updateCampus(long campusId, String name) {
        jdbcClient.sql("UPDATE campuses SET campus_name = :name WHERE campus_id = :campusId")
                .param("name", name)
                .param("campusId", campusId)
                .update();
        return new CampusItem(campusId, name, true);
    }

    public void deleteCampus(long campusId) {
        jdbcClient.sql("DELETE FROM campuses WHERE campus_id = :campusId")
                .param("campusId", campusId)
                .update();
    }

    public CohortItem createCohort(String name, int year) {
        jdbcClient.sql("""
                INSERT INTO cohorts (cohort_name, start_date, end_date)
                VALUES (:name, :startDate, :endDate)
                """)
                .param("name", name)
                .param("startDate", LocalDate.of(year, 1, 1))
                .param("endDate", LocalDate.of(year, 12, 31))
                .update();
        long id = jdbcClient.sql("SELECT cohort_id FROM cohorts WHERE cohort_name = :name")
                .param("name", name)
                .query(Long.class)
                .single();
        return new CohortItem(id, name, year, true);
    }

    public CohortItem updateCohort(long cohortId, String name, int year) {
        jdbcClient.sql("""
                UPDATE cohorts
                SET cohort_name = :name, start_date = :startDate, end_date = :endDate
                WHERE cohort_id = :cohortId
                """)
                .param("name", name)
                .param("startDate", LocalDate.of(year, 1, 1))
                .param("endDate", LocalDate.of(year, 12, 31))
                .param("cohortId", cohortId)
                .update();
        return new CohortItem(cohortId, name, year, true);
    }

    public void deleteCohort(long cohortId) {
        jdbcClient.sql("DELETE FROM cohorts WHERE cohort_id = :cohortId")
                .param("cohortId", cohortId)
                .update();
    }

    public TrackItem createTrack(String name, String description) {
        jdbcClient.sql("INSERT INTO tracks (track_name, domain_type) VALUES (:name, :description)")
                .param("name", name)
                .param("description", description)
                .update();
        long id = jdbcClient.sql("SELECT track_id FROM tracks WHERE track_name = :name")
                .param("name", name)
                .query(Long.class)
                .single();
        return new TrackItem(id, name, description, true);
    }

    public TrackItem updateTrack(long trackId, String name, String description) {
        jdbcClient.sql("""
                UPDATE tracks
                SET track_name = :name, domain_type = :description
                WHERE track_id = :trackId
                """)
                .param("name", name)
                .param("description", description)
                .param("trackId", trackId)
                .update();
        return new TrackItem(trackId, name, description, true);
    }

    public void deleteTrack(long trackId) {
        jdbcClient.sql("DELETE FROM tracks WHERE track_id = :trackId")
                .param("trackId", trackId)
                .update();
    }

    public ClassGroupItem createClassGroup(ClassGroupCreateRequest request) {
        jdbcClient.sql("""
                INSERT INTO class_groups (campus_id, cohort_id, track_id, class_name)
                VALUES (:campusId, :cohortId, :trackId, :name)
                """)
                .param("campusId", request.campusId())
                .param("cohortId", request.cohortId())
                .param("trackId", request.trackId())
                .param("name", request.name().trim())
                .update();
        long id = jdbcClient.sql("""
                SELECT class_group_id
                FROM class_groups
                WHERE campus_id = :campusId AND cohort_id = :cohortId AND track_id = :trackId AND class_name = :name
                """)
                .param("campusId", request.campusId())
                .param("cohortId", request.cohortId())
                .param("trackId", request.trackId())
                .param("name", request.name().trim())
                .query(Long.class)
                .single();
        return new ClassGroupItem(id, request.campusId(), request.cohortId(), request.trackId(), request.name().trim(), request.classroom(), request.capacity(), true);
    }

    public ClassGroupItem updateClassGroup(long classId, ClassGroupUpdateRequest request) {
        jdbcClient.sql("""
                UPDATE class_groups
                SET campus_id = :campusId, cohort_id = :cohortId, track_id = :trackId, class_name = :name
                WHERE class_group_id = :classId
                """)
                .param("campusId", request.campusId())
                .param("cohortId", request.cohortId())
                .param("trackId", request.trackId())
                .param("name", request.name().trim())
                .param("classId", classId)
                .update();
        return new ClassGroupItem(classId, request.campusId(), request.cohortId(), request.trackId(), request.name().trim(), request.classroom(), request.capacity(), true);
    }

    public void deleteClassGroup(long classId) {
        jdbcClient.sql("DELETE FROM class_groups WHERE class_group_id = :classId")
                .param("classId", classId)
                .update();
    }

    private int yearFromDate(Date date) {
        return date == null ? 0 : date.toLocalDate().getYear();
    }
}
