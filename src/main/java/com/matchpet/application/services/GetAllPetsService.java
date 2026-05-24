package com.matchpet.application.services;

import com.matchpet.application.ports.input.GetAllPetsUseCase;
import com.matchpet.application.ports.input.dto.PetResult;
import com.matchpet.application.ports.output.PetPersistencePort;
import com.matchpet.domain.model.Pet;
import com.matchpet.domain.model.Shelter;
import com.matchpet.domain.model.Trait;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GetAllPetsService implements GetAllPetsUseCase {

    private final PetPersistencePort petPersistencePort;

    public GetAllPetsService(PetPersistencePort petPersistencePort) {
        this.petPersistencePort = petPersistencePort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PetResult> execute(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page must be >= 0");
        }
        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("Size must be between 1 and 100");
        }

        int skip = page * size;
        return petPersistencePort.findAll(skip, size).stream()
                .map(this::toResult)
                .toList();
    }

    private PetResult toResult(Pet pet) {
        return new PetResult(
                pet.id(),
                pet.name(),
                new Shelter(pet.shelter().id(), pet.shelter().name(), pet.shelter().location()),
                pet.traits().stream().map(t -> new Trait(t.id(), t.name())).toList()
        );
    }
}
