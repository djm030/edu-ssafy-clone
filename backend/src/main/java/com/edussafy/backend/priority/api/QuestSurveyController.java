package com.edussafy.backend.priority.api;

import com.edussafy.backend.priority.dto.PriorityDtos.QuestDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestSubmissionAttachmentCreateResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestSubmissionAttachmentDownload;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestSubmissionAttachmentRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestSubmissionDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestSubmissionRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestSubmissionResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.QuestsResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyDeleteResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyCreateRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyResponseDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyResponseSubmitRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveyResponseSubmitResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SurveysResponse;
import com.edussafy.backend.priority.service.PriorityApiService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.nio.charset.StandardCharsets;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword
    ) {
        return priorityApiService.quests(page, size, status, keyword);
    }

    @GetMapping("/quests/{id}")
    public QuestDetailResponse quest(@PathVariable Long id) {
        return priorityApiService.quest(id);
    }

    @GetMapping("/quests/{id}/submission")
    public QuestSubmissionDetailResponse questSubmission(@PathVariable Long id) {
        return priorityApiService.questSubmission(id);
    }

    @PostMapping("/quests/{id}/submissions")
    @ResponseStatus(HttpStatus.CREATED)
    public QuestSubmissionResponse submitQuest(
            @PathVariable Long id,
            @Valid @RequestBody QuestSubmissionRequest request
    ) {
        return priorityApiService.submitQuest(id, request);
    }

    @PostMapping("/quests/{id}/submissions/{submissionId}/attachments")
    @ResponseStatus(HttpStatus.CREATED)
    public QuestSubmissionAttachmentCreateResponse createQuestSubmissionAttachment(
            @PathVariable @Min(1) long id,
            @PathVariable @Min(1) long submissionId,
            @Valid @RequestBody QuestSubmissionAttachmentRequest request
    ) {
        return priorityApiService.createQuestSubmissionAttachment(id, submissionId, request);
    }

    @GetMapping("/quests/{id}/submissions/{submissionId}/attachments/{attachmentId}")
    public ResponseEntity<byte[]> downloadQuestSubmissionAttachment(
            @PathVariable @Min(1) long id,
            @PathVariable @Min(1) long submissionId,
            @PathVariable @Min(1) long attachmentId
    ) {
        QuestSubmissionAttachmentDownload download = priorityApiService.downloadQuestSubmissionAttachment(
                id,
                submissionId,
                attachmentId
        );
        String mimeType = download.item().mimeType() == null || download.item().mimeType().isBlank()
                ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                : download.item().mimeType();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(download.item().filename(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(download.content());
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

    @PostMapping("/surveys")
    @ResponseStatus(HttpStatus.CREATED)
    public SurveyDetailResponse createSurvey(@Valid @RequestBody SurveyCreateRequest request) {
        return priorityApiService.createSurvey(request);
    }

    @PutMapping("/surveys/{id}")
    public SurveyDetailResponse updateSurvey(
            @PathVariable Long id,
            @Valid @RequestBody SurveyCreateRequest request
    ) {
        return priorityApiService.updateSurvey(id, request);
    }

    @DeleteMapping("/surveys/{id}")
    public SurveyDeleteResponse deleteSurvey(@PathVariable Long id) {
        return priorityApiService.deleteSurvey(id);
    }

    @GetMapping("/surveys/{id}/responses/current")
    public SurveyResponseDetailResponse surveyResponse(@PathVariable Long id) {
        return priorityApiService.surveyResponse(id);
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
