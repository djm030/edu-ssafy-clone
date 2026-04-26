package com.edussafy.backend.mentoring.api;

import com.edussafy.backend.mentoring.dto.MentoringMeetingDtos.MentoringMeetingApplicationRequest;
import com.edussafy.backend.mentoring.dto.MentoringMeetingDtos.MentoringMeetingApplicationResponse;
import com.edussafy.backend.mentoring.dto.MentoringMeetingDtos.MentoringMeetingApplicationsResponse;
import com.edussafy.backend.mentoring.dto.MentoringMeetingDtos.MentoringMeetingResponse;
import com.edussafy.backend.mentoring.dto.MentoringMeetingDtos.MentoringMeetingsResponse;
import com.edussafy.backend.mentoring.service.MentoringMeetingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/mentoring/meetings")
public class MentoringMeetingController {

    private final MentoringMeetingService mentoringMeetingService;

    public MentoringMeetingController(MentoringMeetingService mentoringMeetingService) {
        this.mentoringMeetingService = mentoringMeetingService;
    }

    @GetMapping
    public MentoringMeetingsResponse meetings(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String keyword
    ) {
        return mentoringMeetingService.meetings(page, size, keyword);
    }

    @GetMapping("/applications/me")
    public MentoringMeetingApplicationsResponse myApplications() {
        return mentoringMeetingService.myApplications();
    }

    @GetMapping("/{meetingId}")
    public MentoringMeetingResponse meeting(@PathVariable @Min(1) long meetingId) {
        return mentoringMeetingService.meeting(meetingId);
    }

    @PostMapping("/{meetingId}/applications")
    @ResponseStatus(HttpStatus.CREATED)
    public MentoringMeetingApplicationResponse apply(
            @PathVariable @Min(1) long meetingId,
            @Valid @RequestBody MentoringMeetingApplicationRequest request
    ) {
        return mentoringMeetingService.apply(meetingId, request);
    }

    @DeleteMapping("/{meetingId}/applications/me")
    public MentoringMeetingApplicationResponse cancel(@PathVariable @Min(1) long meetingId) {
        return mentoringMeetingService.cancel(meetingId);
    }
}
