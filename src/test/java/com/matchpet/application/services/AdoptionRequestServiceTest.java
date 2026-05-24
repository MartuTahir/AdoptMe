package com.matchpet.application.services;

import com.matchpet.application.ports.input.dto.AdoptionDecisionCommand;
import com.matchpet.application.ports.input.dto.AdoptionRequestResult;
import com.matchpet.application.ports.output.AdoptionRequestPersistencePort;
import com.matchpet.domain.exception.AuthorizationException;
import com.matchpet.domain.model.AdoptionRequest;
import com.matchpet.domain.model.AdoptionRequestStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdoptionRequestServiceTest {

    private final AdoptionRequestPersistencePort persistencePort = mock(AdoptionRequestPersistencePort.class);
    private final AdoptionRequestService service = new AdoptionRequestService(persistencePort);

    @Test
    void shouldAcceptRequestWhenAuthorized() {
        AdoptionRequest accepted = new AdoptionRequest("r1", "u1", "p1", AdoptionRequestStatus.ACCEPTED,
                Instant.EPOCH, Instant.EPOCH.plusSeconds(30));
        when(persistencePort.accept("r1", "s1")).thenReturn(accepted);

        AdoptionRequestResult result = service.accept(new AdoptionDecisionCommand("r1", "s1"));

        assertEquals("r1", result.requestId());
        assertEquals(AdoptionRequestStatus.ACCEPTED, result.status());
        assertEquals(Instant.EPOCH.plusSeconds(30), result.updatedAt());
    }

    @Test
    void shouldRejectAcceptWhenNotOwner() {
        when(persistencePort.accept("r1", "s1"))
                .thenThrow(new AuthorizationException("Shelter not authorized"));

        assertThrows(AuthorizationException.class,
                () -> service.accept(new AdoptionDecisionCommand("r1", "s1")));
    }

    @Test
    void shouldReturnAcceptedRequestWhenAlreadyAccepted() {
        AdoptionRequest accepted = new AdoptionRequest("r1", "u1", "p1", AdoptionRequestStatus.ACCEPTED,
                Instant.EPOCH, Instant.EPOCH.plusSeconds(60));
        when(persistencePort.accept("r1", "s1")).thenReturn(accepted);

        AdoptionRequestResult result = service.accept(new AdoptionDecisionCommand("r1", "s1"));

        assertEquals(AdoptionRequestStatus.ACCEPTED, result.status());
        assertEquals(Instant.EPOCH.plusSeconds(60), result.updatedAt());
    }

    @Test
    void shouldRejectRequestWhenAuthorized() {
        AdoptionRequest rejected = new AdoptionRequest("r2", "u2", "p2", AdoptionRequestStatus.REJECTED,
                Instant.EPOCH, Instant.EPOCH.plusSeconds(15));
        when(persistencePort.reject("r2", "s2")).thenReturn(rejected);

        AdoptionRequestResult result = service.reject(new AdoptionDecisionCommand("r2", "s2"));

        assertEquals("r2", result.requestId());
        assertEquals(AdoptionRequestStatus.REJECTED, result.status());
    }

    @Test
    void shouldFailRejectWhenAlreadyAccepted() {
        when(persistencePort.reject("r1", "s1"))
                .thenThrow(new IllegalStateException("Cannot reject accepted request"));

        assertThrows(IllegalStateException.class,
                () -> service.reject(new AdoptionDecisionCommand("r1", "s1")));
    }
}
