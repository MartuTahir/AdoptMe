package com.matchpet.application.services;

import com.matchpet.application.ports.input.SwipeUseCase;
import com.matchpet.application.ports.input.dto.SwipeCommand;
import com.matchpet.application.ports.input.dto.SwipeResult;
import com.matchpet.application.ports.output.PetPersistencePort;
import com.matchpet.application.ports.output.SwipePersistencePort;
import com.matchpet.application.ports.output.UserPersistencePort;
import com.matchpet.domain.exception.EntityNotFoundException;
import com.matchpet.domain.model.SwipeEvent;
import org.springframework.stereotype.Service;

@Service
public class SwipeService implements SwipeUseCase {

    private final UserPersistencePort userPersistencePort;
    private final PetPersistencePort petPersistencePort;
    private final SwipePersistencePort swipePersistencePort;

    public SwipeService(UserPersistencePort userPersistencePort,
                        PetPersistencePort petPersistencePort,
                        SwipePersistencePort swipePersistencePort) {
        this.userPersistencePort = userPersistencePort;
        this.petPersistencePort = petPersistencePort;
        this.swipePersistencePort = swipePersistencePort;
    }

    @Override
    public SwipeResult execute(SwipeCommand command) {
        userPersistencePort.findById(command.userId())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + command.userId()));

        petPersistencePort.findById(command.petId())
                .orElseThrow(() -> new EntityNotFoundException("Pet not found: " + command.petId()));

        SwipeEvent savedEvent = swipePersistencePort.save(
                new SwipeEvent(command.userId(), command.petId(), command.action(), null)
        );

        return new SwipeResult(savedEvent.userId(), savedEvent.petId(), savedEvent.action(), savedEvent.timestamp());
    }
}
