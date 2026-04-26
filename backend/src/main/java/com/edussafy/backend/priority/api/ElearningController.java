package com.edussafy.backend.priority.api;

import com.edussafy.backend.priority.dto.PriorityDtos.ElearningProgressDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ElearningProgressResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ElearningResumeResponse;
import com.edussafy.backend.priority.service.PriorityApiService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/elearning")
public class ElearningController {

    private final PriorityApiService priorityApiService;

    public ElearningController(PriorityApiService priorityApiService) {
        this.priorityApiService = priorityApiService;
    }

    @GetMapping("/in-progress")
    public ElearningProgressResponse inProgress(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size
    ) {
        return priorityApiService.elearningInProgress(status, keyword, page, size);
    }

    @GetMapping("/in-progress/{courseId}")
    public ElearningProgressDetailResponse detail(@PathVariable @Min(1) long courseId) {
        return priorityApiService.elearningProgressDetail(courseId);
    }

    @PostMapping("/in-progress/{courseId}/resume")
    public ElearningResumeResponse resume(@PathVariable @Min(1) long courseId) {
        return priorityApiService.resumeElearning(courseId);
    }
}
