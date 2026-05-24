package com.matchpet.application.ports.input.dto;

import java.time.Instant;

public record MessageResult(
        String id,
        String senderId,
        String content,
        Instant timestamp
) {
}
