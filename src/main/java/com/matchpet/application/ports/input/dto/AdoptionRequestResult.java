package com.matchpet.application.ports.input.dto;

import com.matchpet.domain.model.AdoptionRequestStatus;

import java.time.Instant;

public record AdoptionRequestResult(
        String requestId,
        String userId,
        String petId,
        AdoptionRequestStatus status,
        Instant updatedAt
) {
}
