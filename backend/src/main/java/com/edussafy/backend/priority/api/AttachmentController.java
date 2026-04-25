package com.edussafy.backend.priority.api;

import com.edussafy.backend.priority.dto.PriorityDtos.AttachmentResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.AttachmentUploadRequest;
import com.edussafy.backend.priority.service.PriorityApiService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {

    private final PriorityApiService priorityApiService;

    public AttachmentController(PriorityApiService priorityApiService) {
        this.priorityApiService = priorityApiService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AttachmentResponse uploadMetadata(@Valid @RequestBody AttachmentUploadRequest request) {
        return priorityApiService.createAttachment(request);
    }

    @GetMapping("/{id}/download")
    public AttachmentResponse downloadMetadata(@PathVariable Long id) {
        return priorityApiService.attachment(id);
    }
}
