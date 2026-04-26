package com.edussafy.backend.mentoring.api;

import com.edussafy.backend.mentoring.dto.MentoringNoticeDtos.MentoringNoticeResponse;
import com.edussafy.backend.mentoring.dto.MentoringNoticeDtos.MentoringNoticesResponse;
import com.edussafy.backend.mentoring.service.MentoringNoticeService;
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
@RequestMapping("/api/mentoring/notices")
public class MentoringNoticeController {

    private final MentoringNoticeService mentoringNoticeService;

    public MentoringNoticeController(MentoringNoticeService mentoringNoticeService) {
        this.mentoringNoticeService = mentoringNoticeService;
    }

    @GetMapping
    public MentoringNoticesResponse notices(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return mentoringNoticeService.notices(categoryId, keyword, page, size);
    }

    @GetMapping("/{noticeId}")
    public MentoringNoticeResponse notice(@PathVariable @Min(1) long noticeId) {
        return mentoringNoticeService.notice(noticeId);
    }
}
