package com.edussafy.backend.priority.api;

import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketCreateRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketCreateResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketAttachmentCreateResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketAttachmentDownload;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketAttachmentRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketMessageCreateResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketMessageRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketsResponse;
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
@RequestMapping("/api/support")
public class SupportController {

    private final PriorityApiService priorityApiService;

    public SupportController(PriorityApiService priorityApiService) {
        this.priorityApiService = priorityApiService;
    }

    @GetMapping("/tickets")
    public SupportTicketsResponse tickets(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return priorityApiService.supportTickets(page, size);
    }

    @GetMapping("/tickets/{ticketId}")
    public SupportTicketDetailResponse ticket(@PathVariable @Min(1) long ticketId) {
        return priorityApiService.supportTicket(ticketId);
    }

    @PostMapping("/tickets")
    @ResponseStatus(HttpStatus.CREATED)
    public SupportTicketCreateResponse createTicket(@Valid @RequestBody SupportTicketCreateRequest request) {
        return priorityApiService.createSupportTicket(request);
    }

    @PostMapping("/tickets/{ticketId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public SupportTicketMessageCreateResponse createTicketMessage(
            @PathVariable @Min(1) long ticketId,
            @Valid @RequestBody SupportTicketMessageRequest request
    ) {
        return priorityApiService.createSupportTicketMessage(ticketId, request);
    }

    @PostMapping("/tickets/{ticketId}/answers")
    @ResponseStatus(HttpStatus.CREATED)
    public SupportTicketMessageCreateResponse createTicketAnswer(
            @PathVariable @Min(1) long ticketId,
            @Valid @RequestBody SupportTicketMessageRequest request
    ) {
        return priorityApiService.createSupportTicketAnswer(ticketId, request);
    }

    @PostMapping("/tickets/{ticketId}/messages/{messageId}/attachments")
    @ResponseStatus(HttpStatus.CREATED)
    public SupportTicketAttachmentCreateResponse createTicketMessageAttachment(
            @PathVariable @Min(1) long ticketId,
            @PathVariable @Min(1) long messageId,
            @Valid @RequestBody SupportTicketAttachmentRequest request
    ) {
        return priorityApiService.createSupportTicketMessageAttachment(ticketId, messageId, request);
    }

    @GetMapping("/tickets/{ticketId}/messages/{messageId}/attachments/{attachmentId}")
    public ResponseEntity<byte[]> downloadTicketMessageAttachment(
            @PathVariable @Min(1) long ticketId,
            @PathVariable @Min(1) long messageId,
            @PathVariable @Min(1) long attachmentId
    ) {
        SupportTicketAttachmentDownload download = priorityApiService.downloadSupportTicketMessageAttachment(
                ticketId,
                messageId,
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
