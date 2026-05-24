package com.matchpet.application.services;

import com.matchpet.application.ports.input.GetUserMatchesUseCase;
import com.matchpet.application.ports.input.dto.MatchResult;
import com.matchpet.application.ports.output.AdoptionRequestPersistencePort;
import com.matchpet.domain.model.AdoptionRequest;
import com.matchpet.domain.model.AdoptionRequestStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserMatchesService implements GetUserMatchesUseCase {

    private final AdoptionRequestPersistencePort adoptionRequestPersistencePort;

    public UserMatchesService(AdoptionRequestPersistencePort adoptionRequestPersistencePort) {
        this.adoptionRequestPersistencePort = adoptionRequestPersistencePort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatchResult> execute(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId is required");
        }

        return adoptionRequestPersistencePort.findByUserIdAndStatus(userId, AdoptionRequestStatus.ACCEPTED)
                .stream()
                .map(this::toResult)
                .toList();
    }

    private MatchResult toResult(AdoptionRequest request) {
        return new MatchResult(
                request.id(),
                request.petId(),
                request.updatedAt()
        );
    }
}
