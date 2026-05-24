package com.matchpet.infrastructure.adapters.input.web.dto;

import java.time.Instant;

public record MessageResponse(
        String id,
        String senderId,
        String content,
        Instant timestamp
) {
}
