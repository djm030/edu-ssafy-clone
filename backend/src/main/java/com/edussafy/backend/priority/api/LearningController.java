package com.edussafy.backend.priority.api;

import com.edussafy.backend.priority.dto.PriorityDtos.CurriculumResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialResourceAttachmentCreateResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialResourceAttachmentDownload;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialResourceAttachmentRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialReactionResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialResourcesResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialViewResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialsResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ReplayResponse;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/learning")
public class LearningController {

    private final PriorityApiService priorityApiService;

    public LearningController(PriorityApiService priorityApiService) {
        this.priorityApiService = priorityApiService;
    }

    @GetMapping("/curriculum")
    public CurriculumResponse curriculum() {
        return priorityApiService.curriculum();
    }

    @GetMapping("/replays")
    public ReplayResponse replays() {
        return priorityApiService.replays();
    }

    @GetMapping("/materials")
    public MaterialsResponse materials(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return priorityApiService.materials(keyword, type, page, size);
    }

    @GetMapping("/materials/{id}")
    public MaterialDetailResponse material(@PathVariable Long id) {
        return priorityApiService.material(id);
    }

    @PostMapping("/materials/{id}/views")
    public MaterialViewResponse recordMaterialView(@PathVariable Long id) {
        return priorityApiService.recordMaterialView(id);
    }

    @PostMapping("/materials/{id}/reactions/{type}")
    public MaterialReactionResponse createMaterialReaction(@PathVariable Long id, @PathVariable String type) {
        return priorityApiService.createMaterialReaction(id, type);
    }

    @DeleteMapping("/materials/{id}/reactions/{type}")
    public MaterialReactionResponse deleteMaterialReaction(@PathVariable Long id, @PathVariable String type) {
        return priorityApiService.deleteMaterialReaction(id, type);
    }

    @GetMapping("/materials/{id}/resources")
    public MaterialResourcesResponse materialResources(@PathVariable Long id) {
        return priorityApiService.materialResources(id);
    }

    @PostMapping("/materials/{id}/resources/{resourceId}/attachments")
    @ResponseStatus(HttpStatus.CREATED)
    public MaterialResourceAttachmentCreateResponse createMaterialResourceAttachment(
            @PathVariable @Min(1) long id,
            @PathVariable @Min(1) long resourceId,
            @Valid @RequestBody MaterialResourceAttachmentRequest request
    ) {
        return priorityApiService.createMaterialResourceAttachment(id, resourceId, request);
    }

    @GetMapping("/materials/{id}/resources/{resourceId}/attachments/{attachmentId}")
    public ResponseEntity<byte[]> downloadMaterialResourceAttachment(
            @PathVariable @Min(1) long id,
            @PathVariable @Min(1) long resourceId,
            @PathVariable @Min(1) long attachmentId
    ) {
        MaterialResourceAttachmentDownload download = priorityApiService.downloadMaterialResourceAttachment(
                id,
                resourceId,
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
}
