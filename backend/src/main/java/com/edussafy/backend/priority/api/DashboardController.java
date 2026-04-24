package com.edussafy.backend.priority.api;

import com.edussafy.backend.priority.dto.PriorityDtos.DashboardSummary;
import com.edussafy.backend.priority.service.PriorityApiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DashboardController {

    private final PriorityApiService priorityApiService;

    public DashboardController(PriorityApiService priorityApiService) {
        this.priorityApiService = priorityApiService;
    }

    @GetMapping("/dashboard/summary")
    public DashboardSummary summary() {
        return priorityApiService.dashboardSummary();
    }
}
