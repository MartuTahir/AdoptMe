package com.matchpet.infrastructure.adapters.input.web.dto;

import java.time.Instant;

public record ApiErrorResponse(
        String message,
        Instant timestamp,
        String path
) {
}
