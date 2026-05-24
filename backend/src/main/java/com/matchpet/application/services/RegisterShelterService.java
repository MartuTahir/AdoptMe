package com.matchpet.application.services;

import com.matchpet.application.ports.input.RegisterShelterUseCase;
import com.matchpet.application.ports.input.dto.RegisterShelterCommand;
import com.matchpet.application.ports.input.dto.ShelterResult;
import com.matchpet.application.ports.output.ShelterPersistencePort;
import com.matchpet.domain.model.Shelter;
import org.springframework.stereotype.Service;

@Service
public class RegisterShelterService implements RegisterShelterUseCase {

    private final ShelterPersistencePort shelterPersistencePort;

    public RegisterShelterService(ShelterPersistencePort shelterPersistencePort) {
        this.shelterPersistencePort = shelterPersistencePort;
    }

    @Override
    public ShelterResult execute(RegisterShelterCommand command) {
        if (shelterPersistencePort.existsById(command.id())) {
            throw new IllegalArgumentException("Shelter already exists: " + command.id());
        }

        Shelter savedShelter = shelterPersistencePort.save(new Shelter(
                command.id(),
                command.name(),
                command.location()
        ));

        return new ShelterResult(
                savedShelter.id(),
                savedShelter.name(),
                savedShelter.location()
        );
    }
}
