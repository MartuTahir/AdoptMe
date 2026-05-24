package com.matchpet.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AdoptionRequestTest {

    @Test
    void shouldDefaultToPendingWithComposedId() {
        AdoptionRequest request = new AdoptionRequest(null, "u1", "p1", null, null, null);

        assertEquals("u1:p1", request.id());
        assertEquals(AdoptionRequestStatus.PENDING, request.status());
        assertNotNull(request.createdAt());
        assertNotNull(request.updatedAt());
    }

    @Test
    void shouldAcceptPendingRequest() {
        AdoptionRequest request = new AdoptionRequest("r1", "u1", "p1", AdoptionRequestStatus.PENDING,
                Instant.EPOCH, Instant.EPOCH);

        AdoptionRequest accepted = request.accept();

        assertEquals(AdoptionRequestStatus.ACCEPTED, accepted.status());
        assertEquals("r1", accepted.id());
    }

    @Test
    void shouldRejectPendingRequest() {
        AdoptionRequest request = new AdoptionRequest("r1", "u1", "p1", AdoptionRequestStatus.PENDING,
                Instant.EPOCH, Instant.EPOCH);

        AdoptionRequest rejected = request.reject();

        assertEquals(AdoptionRequestStatus.REJECTED, rejected.status());
    }

    @Test
    void shouldReturnSameWhenAcceptingAcceptedRequest() {
        AdoptionRequest request = new AdoptionRequest("r1", "u1", "p1", AdoptionRequestStatus.ACCEPTED,
                Instant.EPOCH, Instant.EPOCH);

        AdoptionRequest accepted = request.accept();

        assertSame(request, accepted);
    }

    @Test
    void shouldFailWhenRejectingAcceptedRequest() {
        AdoptionRequest request = new AdoptionRequest("r1", "u1", "p1", AdoptionRequestStatus.ACCEPTED,
                Instant.EPOCH, Instant.EPOCH);

        assertThrows(IllegalStateException.class, request::reject);
    }
}
