package com.edussafy.backend.priority.api;

import com.edussafy.backend.priority.dto.PriorityDtos.DocumentAttachmentDownload;
import com.edussafy.backend.priority.dto.PriorityDtos.DocumentRequestDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.DocumentRequestsResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.DocumentSubmissionDeleteResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.DocumentSubmissionRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.DocumentSubmissionResponse;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final PriorityApiService priorityApiService;

    public DocumentController(PriorityApiService priorityApiService) {
        this.priorityApiService = priorityApiService;
    }

    @GetMapping("/requests")
    public DocumentRequestsResponse requests(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return priorityApiService.documentRequests(page, size);
    }

    @GetMapping("/requests/{requestId}")
    public DocumentRequestDetailResponse detail(@PathVariable @Min(1) long requestId) {
        return priorityApiService.documentRequest(requestId);
    }

    @PostMapping("/requests/{requestId}/submissions")
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentSubmissionResponse submit(
            @PathVariable @Min(1) long requestId,
            @Valid @RequestBody DocumentSubmissionRequest request
    ) {
        return priorityApiService.submitDocument(requestId, request);
    }

    @DeleteMapping("/requests/{requestId}/submissions/{submissionId}")
    public DocumentSubmissionDeleteResponse cancel(
            @PathVariable @Min(1) long requestId,
            @PathVariable @Min(1) long submissionId
    ) {
        return priorityApiService.cancelDocumentSubmission(requestId, submissionId);
    }

    @GetMapping("/submissions/{submissionId}/attachments/{attachmentId}")
    public ResponseEntity<byte[]> downloadAttachment(
            @PathVariable @Min(1) long submissionId,
            @PathVariable @Min(1) long attachmentId
    ) {
        DocumentAttachmentDownload download = priorityApiService.downloadDocumentAttachment(submissionId, attachmentId);
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
}
