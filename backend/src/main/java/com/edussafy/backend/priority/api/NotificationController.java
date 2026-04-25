package com.edussafy.backend.priority.api;

import com.edussafy.backend.priority.dto.PriorityDtos.NotificationReadResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.NotificationsReadAllResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.NotificationsResponse;
import com.edussafy.backend.priority.service.PriorityApiService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api")
public class NotificationController {

    private final PriorityApiService priorityApiService;

    public NotificationController(PriorityApiService priorityApiService) {
        this.priorityApiService = priorityApiService;
    }

    @GetMapping("/notifications")
    public NotificationsResponse notifications(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return priorityApiService.notifications(page, size);
    }

    @PatchMapping("/notifications/{notificationId}/read")
    public NotificationReadResponse markRead(@PathVariable @Min(1) long notificationId) {
        return priorityApiService.markNotificationRead(notificationId);
    }

    @PatchMapping("/notifications/read-all")
    public NotificationsReadAllResponse markAllRead() {
        return priorityApiService.markAllNotificationsRead();
    }
}
