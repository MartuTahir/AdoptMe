package com.matchpet.application.ports.output;

import com.matchpet.domain.model.AdoptionRequest;
import com.matchpet.domain.model.AdoptionRequestStatus;

import java.util.List;

public interface AdoptionRequestPersistencePort {

    AdoptionRequest createOrGetPending(String userId, String petId);

    AdoptionRequest accept(String requestId, String shelterId);

    AdoptionRequest reject(String requestId, String shelterId);

    List<AdoptionRequest> findByUserIdAndStatus(String userId, AdoptionRequestStatus status);
}
