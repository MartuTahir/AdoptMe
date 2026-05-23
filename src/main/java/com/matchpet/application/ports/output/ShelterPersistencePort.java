package com.matchpet.application.ports.output;

import com.matchpet.domain.model.Shelter;

import java.util.Optional;

public interface ShelterPersistencePort {

    Optional<Shelter> findById(String id);

    boolean existsById(String id);

    Shelter save(Shelter shelter);
}
