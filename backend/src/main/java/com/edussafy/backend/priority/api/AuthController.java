package com.edussafy.backend.priority.api;

import com.edussafy.backend.priority.dto.PriorityDtos.LoginRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.PasswordCheckRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.PasswordCheckResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.UserResponse;
import com.edussafy.backend.priority.service.PriorityApiService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final PriorityApiService priorityApiService;

    public AuthController(PriorityApiService priorityApiService) {
        this.priorityApiService = priorityApiService;
    }

    @PostMapping("/auth/login")
    public UserResponse login(@Valid @RequestBody LoginRequest request) {
        return priorityApiService.login(request);
    }

    @GetMapping("/me")
    public UserResponse me() {
        return priorityApiService.me();
    }

    @PostMapping("/profile/password-check")
    public PasswordCheckResponse passwordCheck(@Valid @RequestBody PasswordCheckRequest request) {
        return priorityApiService.passwordCheck(request);
    }
}
