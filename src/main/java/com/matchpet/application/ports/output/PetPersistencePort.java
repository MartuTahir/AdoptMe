package com.matchpet.application.ports.output;

import com.matchpet.domain.model.Pet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface PetPersistencePort {

    Optional<Pet> findById(String id);

    Page<Pet> findAll(Pageable pageable);

    Pet save(Pet pet);
}
