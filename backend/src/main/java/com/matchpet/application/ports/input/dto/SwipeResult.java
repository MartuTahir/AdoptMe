package com.matchpet.application.ports.input.dto;

import com.matchpet.domain.model.SwipeAction;

import java.time.Instant;

public record SwipeResult(
        String userId,
        String petId,
        SwipeAction action,
        Instant timestamp
) {
}
