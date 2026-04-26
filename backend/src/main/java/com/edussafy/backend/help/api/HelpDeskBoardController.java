package com.edussafy.backend.help.api;

import com.edussafy.backend.board.dto.BoardCategoryListResponse;
import com.edussafy.backend.board.dto.BoardPostDetailResponse;
import com.edussafy.backend.board.dto.BoardPostListResponse;
import com.edussafy.backend.board.service.BoardQuery;
import com.edussafy.backend.help.service.HelpDeskBoardService;
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
@RequestMapping("/api/help")
public class HelpDeskBoardController {

    private final HelpDeskBoardService helpDeskBoardService;

    public HelpDeskBoardController(HelpDeskBoardService helpDeskBoardService) {
        this.helpDeskBoardService = helpDeskBoardService;
    }

    @GetMapping("/notices/categories")
    public BoardCategoryListResponse noticeCategories() {
        return helpDeskBoardService.noticeCategories();
    }

    @GetMapping("/notices")
    public BoardPostListResponse notices(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        return helpDeskBoardService.notices(new BoardQuery(categoryId, keyword, page, size, sort));
    }

    @GetMapping("/notices/{noticeId}")
    public BoardPostDetailResponse notice(@PathVariable @Min(1) long noticeId) {
        return helpDeskBoardService.notice(noticeId);
    }

    @GetMapping("/faqs/categories")
    public BoardCategoryListResponse faqCategories() {
        return helpDeskBoardService.faqCategories();
    }

    @GetMapping("/faqs")
    public BoardPostListResponse faqs(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer size,
            @RequestParam(defaultValue = "id,asc") String sort
    ) {
        return helpDeskBoardService.faqs(new BoardQuery(categoryId, keyword, page, size, sort));
    }

    @GetMapping("/faqs/{faqId}")
    public BoardPostDetailResponse faq(@PathVariable @Min(1) long faqId) {
        return helpDeskBoardService.faq(faqId);
    }
}
