package com.edussafy.backend.priority.api;

import com.edussafy.backend.priority.dto.PriorityDtos.CurrentLiveSessionResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.LiveSessionJoinResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.LiveSessionsResponse;
import com.edussafy.backend.priority.service.PriorityApiService;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/live-sessions")
public class LiveSessionController {

    private final PriorityApiService priorityApiService;

    public LiveSessionController(PriorityApiService priorityApiService) {
        this.priorityApiService = priorityApiService;
    }

    @GetMapping("/today")
    public LiveSessionsResponse today() {
        return priorityApiService.todayLiveSessions();
    }

    @GetMapping("/current")
    public CurrentLiveSessionResponse current() {
        return priorityApiService.currentLiveSession();
    }

    @PostMapping("/{sessionId}/join")
    @ResponseStatus(HttpStatus.CREATED)
    public LiveSessionJoinResponse join(@PathVariable @Min(1) long sessionId) {
        return priorityApiService.joinLiveSession(sessionId);
    }
}
