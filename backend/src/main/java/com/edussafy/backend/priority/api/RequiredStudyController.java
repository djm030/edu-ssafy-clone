package com.edussafy.backend.priority.api;

import com.edussafy.backend.priority.dto.PriorityDtos.RequiredStudiesResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.RequiredStudyCompleteResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.RequiredStudyDetailResponse;
import com.edussafy.backend.priority.service.PriorityApiService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/required-studies")
public class RequiredStudyController {

    private final PriorityApiService priorityApiService;

    public RequiredStudyController(PriorityApiService priorityApiService) {
        this.priorityApiService = priorityApiService;
    }

    @GetMapping
    public RequiredStudiesResponse requiredStudies(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return priorityApiService.requiredStudies(page, size);
    }

    @GetMapping("/{studyId}")
    public RequiredStudyDetailResponse requiredStudy(@PathVariable @Min(1) long studyId) {
        return priorityApiService.requiredStudy(studyId);
    }

    @PostMapping("/{studyId}/complete")
    @ResponseStatus(HttpStatus.CREATED)
    public RequiredStudyCompleteResponse completeRequiredStudy(@PathVariable @Min(1) long studyId) {
        return priorityApiService.completeRequiredStudy(studyId);
    }
}
