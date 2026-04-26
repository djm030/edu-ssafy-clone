package com.edussafy.backend.mentoring.api;

import com.edussafy.backend.mentoring.dto.MentoringMeetingResultDtos.MentoringMeetingResultResponse;
import com.edussafy.backend.mentoring.dto.MentoringMeetingResultDtos.MentoringMeetingResultsResponse;
import com.edussafy.backend.mentoring.dto.MentoringMeetingResultDtos.MentoringMeetingReviewCreateRequest;
import com.edussafy.backend.mentoring.dto.MentoringMeetingResultDtos.MentoringMeetingReviewResponse;
import com.edussafy.backend.mentoring.dto.MentoringMeetingResultDtos.MentoringMeetingReviewUpdateRequest;
import com.edussafy.backend.mentoring.dto.MentoringMeetingResultDtos.MentoringMeetingReviewsResponse;
import com.edussafy.backend.mentoring.service.MentoringMeetingResultService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/mentoring")
public class MentoringMeetingResultController {

    private final MentoringMeetingResultService service;

    public MentoringMeetingResultController(MentoringMeetingResultService service) {
        this.service = service;
    }

    @GetMapping("/meeting-results")
    public MentoringMeetingResultsResponse results(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return service.results(page, size);
    }

    @GetMapping("/meeting-results/{meetingId}")
    public MentoringMeetingResultResponse result(@PathVariable @Min(1) long meetingId) {
        return service.result(meetingId);
    }

    @GetMapping("/meeting-reviews")
    public MentoringMeetingReviewsResponse reviews(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return service.reviews(page, size);
    }

    @PostMapping("/meeting-reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public MentoringMeetingReviewResponse createReview(@Valid @RequestBody MentoringMeetingReviewCreateRequest request) {
        return service.createReview(request);
    }

    @GetMapping("/meeting-reviews/{reviewId}")
    public MentoringMeetingReviewResponse review(@PathVariable @Min(1) long reviewId) {
        return service.review(reviewId);
    }

    @PutMapping("/meeting-reviews/{reviewId}")
    public MentoringMeetingReviewResponse updateReview(
            @PathVariable @Min(1) long reviewId,
            @Valid @RequestBody MentoringMeetingReviewUpdateRequest request
    ) {
        return service.updateReview(reviewId, request);
    }

    @DeleteMapping("/meeting-reviews/{reviewId}")
    public MentoringMeetingReviewResponse deleteReview(@PathVariable @Min(1) long reviewId) {
        return service.deleteReview(reviewId);
    }
}
