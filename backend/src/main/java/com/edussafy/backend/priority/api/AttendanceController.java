package com.edussafy.backend.priority.api;

import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceAppealRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceAppealResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.AttendanceRecordsResponse;
import com.edussafy.backend.priority.service.PriorityApiService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AttendanceController {

    private final PriorityApiService priorityApiService;

    public AttendanceController(PriorityApiService priorityApiService) {
        this.priorityApiService = priorityApiService;
    }

    @GetMapping("/attendance/records")
    public AttendanceRecordsResponse records() {
        return priorityApiService.attendanceRecords();
    }

    @PostMapping("/attendance/appeals")
    @ResponseStatus(HttpStatus.CREATED)
    public AttendanceAppealResponse appeal(@Valid @RequestBody AttendanceAppealRequest request) {
        return priorityApiService.createAttendanceAppeal(request);
    }
}
