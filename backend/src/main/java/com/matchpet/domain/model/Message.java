package com.matchpet.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Chat message exchanged between adopter and shelter for an accepted adoption request.
 */
public record Message(
        String id,
        String senderId,
        String content,
        Instant timestamp
) {

    public Message {
        if (senderId == null || senderId.isBlank()) {
            throw new IllegalArgumentException("Message senderId is required");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Message content is required");
        }
        if (content.length() > 1000) {
            throw new IllegalArgumentException("Message content must be at most 1000 characters");
        }

        id = (id == null || id.isBlank()) ? UUID.randomUUID().toString() : id;
        timestamp = timestamp == null ? Instant.now() : timestamp;
    }
}
