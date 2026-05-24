package com.matchpet.application.services;

import com.matchpet.application.ports.input.SwipeUseCase;
import com.matchpet.application.ports.input.dto.SwipeCommand;
import com.matchpet.application.ports.input.dto.SwipeResult;
import com.matchpet.application.ports.output.AdoptionRequestPersistencePort;
import com.matchpet.application.ports.output.PetPersistencePort;
import com.matchpet.application.ports.output.SwipePersistencePort;
import com.matchpet.application.ports.output.UserPersistencePort;
import com.matchpet.domain.exception.EntityNotFoundException;
import com.matchpet.domain.model.SwipeAction;
import com.matchpet.domain.model.SwipeEvent;
import com.matchpet.domain.service.ImpulsivityEngine;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class SwipeService implements SwipeUseCase {

    private final UserPersistencePort userPersistencePort;
    private final PetPersistencePort petPersistencePort;
    private final SwipePersistencePort swipePersistencePort;
    private final AdoptionRequestPersistencePort adoptionRequestPersistencePort;
    private final ImpulsivityEngine impulsivityEngine = new ImpulsivityEngine();

    public SwipeService(UserPersistencePort userPersistencePort,
                        PetPersistencePort petPersistencePort,
                        SwipePersistencePort swipePersistencePort,
                        AdoptionRequestPersistencePort adoptionRequestPersistencePort) {
        this.userPersistencePort = userPersistencePort;
        this.petPersistencePort = petPersistencePort;
        this.swipePersistencePort = swipePersistencePort;
        this.adoptionRequestPersistencePort = adoptionRequestPersistencePort;
    }

    @Override
    public SwipeResult execute(SwipeCommand command) {
        userPersistencePort.findById(command.userId())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + command.userId()));

        petPersistencePort.findById(command.petId())
                .orElseThrow(() -> new EntityNotFoundException("Pet not found: " + command.petId()));

        if (command.action() == SwipeAction.LIKE) {
            long recentLikes = swipePersistencePort.countLikesSince(command.userId(), Instant.now().minusSeconds(60));
            impulsivityEngine.checkImpulsivity(recentLikes);
        }

        SwipeEvent savedEvent = swipePersistencePort.save(
                new SwipeEvent(command.userId(), command.petId(), command.action(), null)
        );

        if (command.action() == SwipeAction.LIKE) {
            adoptionRequestPersistencePort.createOrGetPending(command.userId(), command.petId());
        }

        return new SwipeResult(savedEvent.userId(), savedEvent.petId(), savedEvent.action(), savedEvent.timestamp());
    }
}
