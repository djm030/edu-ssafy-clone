package com.edussafy.backend.priority.api;

import com.edussafy.backend.priority.dto.PriorityDtos.QuestDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestSubmissionRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestSubmissionResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestsResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyResponseSubmitRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyResponseSubmitResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveysResponse;
import com.edussafy.backend.priority.service.PriorityApiService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api")
public class QuestSurveyController {

    private final PriorityApiService priorityApiService;

    public QuestSurveyController(PriorityApiService priorityApiService) {
        this.priorityApiService = priorityApiService;
    }

    @GetMapping("/quests")
    public QuestsResponse quests(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return priorityApiService.quests(page, size);
    }

    @GetMapping("/quests/{id}")
    public QuestDetailResponse quest(@PathVariable Long id) {
        return priorityApiService.quest(id);
    }

    @PostMapping("/quests/{id}/submissions")
    @ResponseStatus(HttpStatus.CREATED)
    public QuestSubmissionResponse submitQuest(
            @PathVariable Long id,
            @Valid @RequestBody QuestSubmissionRequest request
    ) {
        return priorityApiService.submitQuest(id, request);
    }

    @GetMapping("/surveys")
    public SurveysResponse surveys(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return priorityApiService.surveys(page, size);
    }

    @GetMapping("/surveys/{id}")
    public SurveyDetailResponse survey(@PathVariable Long id) {
        return priorityApiService.survey(id);
    }

    @PostMapping("/surveys/{id}/responses")
    @ResponseStatus(HttpStatus.CREATED)
    public SurveyResponseSubmitResponse submitSurvey(
            @PathVariable Long id,
            @Valid @RequestBody SurveyResponseSubmitRequest request
    ) {
        return priorityApiService.submitSurvey(id, request);
    }
}
