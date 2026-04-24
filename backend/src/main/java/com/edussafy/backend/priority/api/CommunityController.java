package com.edussafy.backend.priority.api;

import com.edussafy.backend.priority.dto.PriorityDtos.ClassmateNotificationRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.ClassmateNotificationResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ClassmatesResponse;
import com.edussafy.backend.priority.service.PriorityApiService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/community")
public class CommunityController {

    private final PriorityApiService priorityApiService;

    public CommunityController(PriorityApiService priorityApiService) {
        this.priorityApiService = priorityApiService;
    }

    @GetMapping("/classmates")
    public ClassmatesResponse classmates() {
        return priorityApiService.classmates();
    }

    @PostMapping("/classmates/{userId}/notifications")
    @ResponseStatus(HttpStatus.CREATED)
    public ClassmateNotificationResponse createClassmateNotification(
            @PathVariable @Positive long userId,
            @Valid @RequestBody ClassmateNotificationRequest request
    ) {
        return priorityApiService.createClassmateNotification(userId, request);
    }
}
