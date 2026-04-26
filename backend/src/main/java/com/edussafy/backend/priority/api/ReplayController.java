package com.edussafy.backend.priority.api;

import com.edussafy.backend.priority.dto.PriorityDtos.ReplayDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ReplayResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ReplayWatchLogResponse;
import com.edussafy.backend.priority.service.PriorityApiService;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/replays")
public class ReplayController {

    private final PriorityApiService priorityApiService;

    public ReplayController(PriorityApiService priorityApiService) {
        this.priorityApiService = priorityApiService;
    }

    @GetMapping("/my")
    public ReplayResponse myReplays(@RequestParam(required = false) String keyword) {
        return priorityApiService.myReplays(keyword);
    }

    @GetMapping("/all")
    public ReplayResponse allReplays(@RequestParam(required = false) String keyword) {
        return priorityApiService.allReplays(keyword);
    }

    @GetMapping("/{replayId}")
    public ReplayDetailResponse replay(@PathVariable @Min(1) long replayId) {
        return priorityApiService.replay(replayId);
    }

    @PostMapping("/{replayId}/watch-log")
    @ResponseStatus(HttpStatus.CREATED)
    public ReplayWatchLogResponse watch(@PathVariable @Min(1) long replayId) {
        return priorityApiService.watchReplay(replayId);
    }
}
