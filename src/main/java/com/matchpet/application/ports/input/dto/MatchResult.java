package com.matchpet.application.ports.input.dto;

import java.time.Instant;

public record MatchResult(
        String requestId,
        String petId,
        Instant acceptedAt
) {
}
