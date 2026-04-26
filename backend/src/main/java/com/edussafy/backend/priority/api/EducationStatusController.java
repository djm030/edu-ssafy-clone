package com.edussafy.backend.priority.api;

import com.edussafy.backend.priority.dto.PriorityDtos.EducationStatusResponse;
import com.edussafy.backend.priority.service.PriorityApiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mycampus")
public class EducationStatusController {

    private final PriorityApiService priorityApiService;

    public EducationStatusController(PriorityApiService priorityApiService) {
        this.priorityApiService = priorityApiService;
    }

    @GetMapping("/education-status")
    public EducationStatusResponse educationStatus() {
        return priorityApiService.educationStatus();
    }
}
