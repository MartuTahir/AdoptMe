package com.matchpet.domain.model;

import java.time.Instant;

public record SwipeEvent(
        String userId,
        String petId,
        SwipeAction action,
        Instant timestamp
) {
    public SwipeEvent {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("Swipe userId is required");
        }
        if (petId == null || petId.isBlank()) {
            throw new IllegalArgumentException("Swipe petId is required");
        }
        if (action == null) {
            throw new IllegalArgumentException("Swipe action is required");
        }
        timestamp = timestamp == null ? Instant.now() : timestamp;
    }
}
