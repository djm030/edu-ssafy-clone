package com.edussafy.backend.mentoring.api;

import com.edussafy.backend.mentoring.dto.MentorStoryDtos.MentorStoriesResponse;
import com.edussafy.backend.mentoring.dto.MentorStoryDtos.MentorStoryDetailResponse;
import com.edussafy.backend.mentoring.service.MentorStoryService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/mentoring/stories")
public class MentorStoryController {

    private final MentorStoryService mentorStoryService;

    public MentorStoryController(MentorStoryService mentorStoryService) {
        this.mentorStoryService = mentorStoryService;
    }

    @GetMapping
    public MentorStoriesResponse stories(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String keyword
    ) {
        return mentorStoryService.stories(page, size, keyword);
    }

    @GetMapping("/{storyId}")
    public MentorStoryDetailResponse story(@PathVariable @Min(1) long storyId) {
        return mentorStoryService.story(storyId);
    }
}
