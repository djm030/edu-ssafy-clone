package com.edussafy.backend.external.dto;

import java.time.OffsetDateTime;
import java.util.List;

public final class ExternalServiceDtos {

    private ExternalServiceDtos() {
    }

    public record ExternalServicesResponse(List<ExternalServiceItem> items) {
    }

    public record ExternalServiceAccessResponse(ExternalServiceAccessItem item) {
    }

    public record ExternalServiceItem(
            String code,
            String name,
            String url,
            String description,
            boolean enabled,
            OffsetDateTime lastAccessedAt,
            long accessCount
    ) {
    }

    public record ExternalServiceAccessItem(
            String code,
            String name,
            String url,
            OffsetDateTime accessedAt
    ) {
    }
}
