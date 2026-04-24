package com.edussafy.backend.priority.api;

import com.edussafy.backend.priority.dto.PriorityDtos.ClassmatesResponse;
import com.edussafy.backend.priority.service.PriorityApiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
