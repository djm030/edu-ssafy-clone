package com.edussafy.backend.mentoring.api;

import com.edussafy.backend.mentoring.dto.MentoringQuestionDtos.MentoringAnswerCreateRequest;
import com.edussafy.backend.mentoring.dto.MentoringQuestionDtos.MentoringQuestionCreateRequest;
import com.edussafy.backend.mentoring.dto.MentoringQuestionDtos.MentoringQuestionResponse;
import com.edussafy.backend.mentoring.dto.MentoringQuestionDtos.MentoringQuestionsResponse;
import com.edussafy.backend.mentoring.service.MentoringQuestionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/mentoring/questions")
public class MentoringQuestionController {

    private final MentoringQuestionService mentoringQuestionService;

    public MentoringQuestionController(MentoringQuestionService mentoringQuestionService) {
        this.mentoringQuestionService = mentoringQuestionService;
    }

    @GetMapping
    public MentoringQuestionsResponse questions(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String keyword
    ) {
        return mentoringQuestionService.questions(page, size, keyword);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MentoringQuestionResponse createQuestion(@Valid @RequestBody MentoringQuestionCreateRequest request) {
        return mentoringQuestionService.createQuestion(request);
    }

    @GetMapping("/{questionId}")
    public MentoringQuestionResponse question(@PathVariable @Min(1) long questionId) {
        return mentoringQuestionService.question(questionId);
    }

    @PostMapping("/{questionId}/answers")
    @ResponseStatus(HttpStatus.CREATED)
    public MentoringQuestionResponse answer(
            @PathVariable @Min(1) long questionId,
            @Valid @RequestBody MentoringAnswerCreateRequest request
    ) {
        return mentoringQuestionService.answer(questionId, request);
    }

    @PatchMapping("/{questionId}/close")
    public MentoringQuestionResponse close(@PathVariable @Min(1) long questionId) {
        return mentoringQuestionService.close(questionId);
    }
}
