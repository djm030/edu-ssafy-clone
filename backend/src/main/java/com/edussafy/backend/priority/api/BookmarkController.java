package com.edussafy.backend.priority.api;

import com.edussafy.backend.priority.dto.PriorityDtos.BookmarkDeleteResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.BookmarkRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.BookmarkResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.BookmarksResponse;
import com.edussafy.backend.priority.service.PriorityApiService;
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
@RequestMapping("/api/me/bookmarks")
public class BookmarkController {

    private final PriorityApiService priorityApiService;

    public BookmarkController(PriorityApiService priorityApiService) {
        this.priorityApiService = priorityApiService;
    }

    @GetMapping
    public BookmarksResponse bookmarks(
            @RequestParam(required = false) String targetType,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return priorityApiService.bookmarks(targetType, page, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookmarkResponse create(@Valid @RequestBody BookmarkRequest request) {
        return priorityApiService.createBookmark(request);
    }

    @DeleteMapping("/{bookmarkId}")
    public BookmarkDeleteResponse delete(@PathVariable @Min(1) long bookmarkId) {
        return priorityApiService.deleteBookmark(bookmarkId);
    }
}
