package com.edussafy.backend.priority.api;

import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceAppealRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceAppealResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceAppealResolveRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceAppealsResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceCheckRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceCheckResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceRecordsResponse;
import com.edussafy.backend.priority.service.PriorityApiService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api")
public class AttendanceController {

    private final PriorityApiService priorityApiService;

    public AttendanceController(PriorityApiService priorityApiService) {
        this.priorityApiService = priorityApiService;
    }

    @GetMapping("/attendance/records")
    public AttendanceRecordsResponse records(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) String status
    ) {
        return priorityApiService.attendanceRecords(dateFrom, dateTo, status);
    }

    @PostMapping("/attendance/check")
    public AttendanceCheckResponse check(@Valid @RequestBody AttendanceCheckRequest request) {
        return priorityApiService.attendanceCheck(request);
    }

    @GetMapping("/attendance/appeals")
    public AttendanceAppealsResponse appeals() {
        return priorityApiService.attendanceAppeals();
    }

    @GetMapping("/attendance/appeals/pending")
    public AttendanceAppealsResponse pendingAppeals() {
        return priorityApiService.pendingAttendanceAppeals();
    }

    @PostMapping("/attendance/appeals")
    @ResponseStatus(HttpStatus.CREATED)
    public AttendanceAppealResponse appeal(@Valid @RequestBody AttendanceAppealRequest request) {
        return priorityApiService.createAttendanceAppeal(request);
    }

    @PatchMapping("/attendance/appeals/{appealId}/cancel")
    public AttendanceAppealResponse cancelAppeal(@PathVariable @Min(1) long appealId) {
        return priorityApiService.cancelAttendanceAppeal(appealId);
    }

    @PatchMapping("/attendance/appeals/{appealId}/resolve")
    public AttendanceAppealResponse resolveAppeal(
            @PathVariable @Min(1) long appealId,
            @Valid @RequestBody AttendanceAppealResolveRequest request
    ) {
        return priorityApiService.resolveAttendanceAppeal(appealId, request);
    }
}
