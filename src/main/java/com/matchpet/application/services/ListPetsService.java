package com.matchpet.application.services;

import com.matchpet.application.ports.input.ListPetsUseCase;
import com.matchpet.application.ports.input.dto.PetResult;
import com.matchpet.application.ports.output.PetPersistencePort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class ListPetsService implements ListPetsUseCase {

    private final PetPersistencePort petPersistencePort;

    public ListPetsService(PetPersistencePort petPersistencePort) {
        this.petPersistencePort = petPersistencePort;
    }

    @Override
    public Page<PetResult> execute(int page, int size) {
        return petPersistencePort.findAll(PageRequest.of(page, size))
                .map(pet -> new PetResult(pet.id(), pet.name(), pet.shelter(), pet.traits()));
    }
}
