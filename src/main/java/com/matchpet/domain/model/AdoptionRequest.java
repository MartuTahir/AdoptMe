package com.matchpet.domain.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.time.Instant;
import java.util.Objects;

@Node("AdoptionRequest")
public record AdoptionRequest(
        @Id String id,
        String userId,
        String petId,
        AdoptionRequestStatus status,
        Instant createdAt,
        Instant updatedAt
) {

    public AdoptionRequest {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("AdoptionRequest userId is required");
        }
        if (petId == null || petId.isBlank()) {
            throw new IllegalArgumentException("AdoptionRequest petId is required");
        }

        id = (id == null || id.isBlank()) ? composeId(userId, petId) : id;
        status = status == null ? AdoptionRequestStatus.PENDING : status;

        Instant now = Instant.now();
        createdAt = createdAt == null ? now : createdAt;
        updatedAt = updatedAt == null ? createdAt : updatedAt;
    }

    public static String composeId(String userId, String petId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("AdoptionRequest userId is required");
        }
        if (petId == null || petId.isBlank()) {
            throw new IllegalArgumentException("AdoptionRequest petId is required");
        }
        return userId + ":" + petId;
    }

    public AdoptionRequest accept() {
        if (status == AdoptionRequestStatus.ACCEPTED) {
            return this;
        }
        if (status == AdoptionRequestStatus.REJECTED) {
            throw new IllegalStateException("Cannot accept a rejected adoption request");
        }
        return new AdoptionRequest(id, userId, petId, AdoptionRequestStatus.ACCEPTED, createdAt, Instant.now());
    }

    public AdoptionRequest reject() {
        if (status == AdoptionRequestStatus.REJECTED) {
            return this;
        }
        if (status == AdoptionRequestStatus.ACCEPTED) {
            throw new IllegalStateException("Cannot reject an accepted adoption request");
        }
        return new AdoptionRequest(id, userId, petId, AdoptionRequestStatus.REJECTED, createdAt, Instant.now());
    }

    public boolean isSamePair(String userId, String petId) {
        return Objects.equals(this.userId, userId) && Objects.equals(this.petId, petId);
    }
}
