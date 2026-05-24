package com.matchpet.application.services;

import com.matchpet.application.ports.input.GetCompatibilityUseCase;
import com.matchpet.application.ports.input.dto.GetCompatibilityResult;
import com.matchpet.application.ports.output.PetPersistencePort;
import com.matchpet.application.ports.output.UserPersistencePort;
import com.matchpet.domain.exception.EntityNotFoundException;
import com.matchpet.domain.model.Trait;
import com.matchpet.domain.model.User;
import com.matchpet.domain.service.CompatibilityEngine;
import com.matchpet.domain.service.CompatibilityResult;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class GetCompatibilityService implements GetCompatibilityUseCase {

    private final UserPersistencePort userPersistencePort;
    private final PetPersistencePort petPersistencePort;
    private final CompatibilityEngine compatibilityEngine;

    public GetCompatibilityService(UserPersistencePort userPersistencePort,
                                   PetPersistencePort petPersistencePort,
                                   CompatibilityEngine compatibilityEngine) {
        this.userPersistencePort = userPersistencePort;
        this.petPersistencePort = petPersistencePort;
        this.compatibilityEngine = compatibilityEngine;
    }

    @Override
    public GetCompatibilityResult execute(String userId, String petId) {
        User user = userPersistencePort.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        com.matchpet.domain.model.Pet pet = petPersistencePort.findById(petId)
                .orElseThrow(() -> new EntityNotFoundException("Pet not found: " + petId));

        CompatibilityResult result = compatibilityEngine.calculate(
                Set.copyOf(user.preferences()),
                Set.copyOf(pet.traits())
        );

        return new GetCompatibilityResult(result.score(), result.matchedTraits().stream().toList());
    }
}
