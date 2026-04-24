package com.edussafy.backend.priority.api;

import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketCreateRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketCreateResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.SupportTicketsResponse;
import com.edussafy.backend.priority.service.PriorityApiService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
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

    @PostMapping("/tickets")
    @ResponseStatus(HttpStatus.CREATED)
    public SupportTicketCreateResponse createTicket(@Valid @RequestBody SupportTicketCreateRequest request) {
        return priorityApiService.createSupportTicket(request);
    }
}
