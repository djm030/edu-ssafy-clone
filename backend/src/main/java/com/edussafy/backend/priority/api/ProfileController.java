package com.edussafy.backend.priority.api;

import com.edussafy.backend.priority.dto.PriorityDtos.AuthActionResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ProfilePasswordChangeRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.ProfileUpdateRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.ProfileResponse;
import com.edussafy.backend.priority.service.PriorityApiService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final PriorityApiService priorityApiService;

    public ProfileController(PriorityApiService priorityApiService) {
        this.priorityApiService = priorityApiService;
    }

    @GetMapping
    public ProfileResponse profile() {
        return priorityApiService.profile();
    }

    @PutMapping
    public ProfileResponse updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        return priorityApiService.updateProfile(request);
    }

    @PatchMapping("/password")
    public AuthActionResponse changePassword(@Valid @RequestBody ProfilePasswordChangeRequest request) {
        return priorityApiService.changeProfilePassword(request);
    }
}
