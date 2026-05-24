package com.matchpet.infrastructure.adapters.output.neo4j;

import com.matchpet.application.ports.output.ShelterPersistencePort;
import com.matchpet.domain.model.Shelter;
import com.matchpet.infrastructure.adapters.output.neo4j.repositories.ShelterNeo4jRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class Neo4jShelterAdapter implements ShelterPersistencePort {

    private final ShelterNeo4jRepository repository;

    public Neo4jShelterAdapter(ShelterNeo4jRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Shelter> findById(String id) {
        return repository.findById(id);
    }

    @Override
    public boolean existsById(String id) {
        return repository.existsById(id);
    }

    @Override
    @Transactional
    public Shelter save(Shelter shelter) {
        return repository.save(shelter);
    }
}
