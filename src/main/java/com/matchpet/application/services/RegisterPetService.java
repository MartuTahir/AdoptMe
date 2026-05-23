package com.matchpet.application.services;

import com.matchpet.application.ports.input.RegisterPetUseCase;
import com.matchpet.application.ports.input.dto.PetResult;
import com.matchpet.application.ports.input.dto.RegisterPetCommand;
import com.matchpet.application.ports.output.PetPersistencePort;
import com.matchpet.domain.model.Pet;
import org.springframework.stereotype.Service;

@Service
public class RegisterPetService implements RegisterPetUseCase {

    private final PetPersistencePort petPersistencePort;

    public RegisterPetService(PetPersistencePort petPersistencePort) {
        this.petPersistencePort = petPersistencePort;
    }

    @Override
    public PetResult execute(RegisterPetCommand command) {
        Pet savedPet = petPersistencePort.save(new Pet(command.id(), command.name(), command.shelter(), command.traits()));
        return new PetResult(savedPet.id(), savedPet.name(), savedPet.shelter(), savedPet.traits());
    }
}
