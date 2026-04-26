package com.edussafy.backend.health.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record HealthResponse(
        String status,
        OffsetDateTime checkedAt,
        String service,
        String profile,
        List<HealthCheckItem> checks
) {

    public record HealthCheckItem(
            String name,
            String status,
            boolean required,
            String message
    ) {
    }
}
