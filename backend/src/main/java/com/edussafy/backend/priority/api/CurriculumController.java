package com.edussafy.backend.priority.api;

import com.edussafy.backend.priority.dto.PriorityDtos.CurriculumWeekDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.CurriculumWeeksResponse;
import com.edussafy.backend.priority.service.PriorityApiService;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/curriculum")
public class CurriculumController {

    private final PriorityApiService priorityApiService;

    public CurriculumController(PriorityApiService priorityApiService) {
        this.priorityApiService = priorityApiService;
    }

    @GetMapping("/weeks")
    public CurriculumWeeksResponse weeks(
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String track,
            @RequestParam(required = false) @Pattern(regexp = "planned|current|done") String status
    ) {
        return priorityApiService.curriculumWeeks(semester, track, status);
    }

    @GetMapping("/weeks/{weekId}")
    public CurriculumWeekDetailResponse week(@PathVariable long weekId) {
        return priorityApiService.curriculumWeek(weekId);
    }
}
