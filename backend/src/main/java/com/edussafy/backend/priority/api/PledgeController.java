package com.edussafy.backend.priority.api;

import com.edussafy.backend.priority.dto.PriorityDtos.PledgeAgreementRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.PledgeAgreementResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.PledgeDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.PledgesResponse;
import com.edussafy.backend.priority.service.PriorityApiService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
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
@RequestMapping("/api/pledges")
public class PledgeController {

    private final PriorityApiService priorityApiService;

    public PledgeController(PriorityApiService priorityApiService) {
        this.priorityApiService = priorityApiService;
    }

    @GetMapping
    public PledgesResponse pledges(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return priorityApiService.pledges(page, size);
    }

    @GetMapping("/{pledgeId}")
    public PledgeDetailResponse pledge(@PathVariable @Min(1) long pledgeId) {
        return priorityApiService.pledge(pledgeId);
    }

    @PostMapping("/{pledgeId}/agreements")
    @ResponseStatus(HttpStatus.CREATED)
    public PledgeAgreementResponse agree(
            @PathVariable @Min(1) long pledgeId,
            @Valid @RequestBody PledgeAgreementRequest request
    ) {
        return priorityApiService.agreePledge(pledgeId, request);
    }
}
