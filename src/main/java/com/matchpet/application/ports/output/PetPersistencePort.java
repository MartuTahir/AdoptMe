package com.matchpet.application.ports.output;

import com.matchpet.domain.model.Pet;

import java.util.List;
import java.util.Optional;

public interface PetPersistencePort {

    Optional<Pet> findById(String id);

    Pet save(Pet pet);

    List<Pet> findAll(int skip, int limit);
}
