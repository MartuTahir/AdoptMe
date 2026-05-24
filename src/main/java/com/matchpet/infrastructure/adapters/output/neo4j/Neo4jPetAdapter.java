package com.matchpet.infrastructure.adapters.output.neo4j;

import com.matchpet.application.ports.output.PetPersistencePort;
import com.matchpet.domain.exception.EntityNotFoundException;
import com.matchpet.domain.model.Pet;
import com.matchpet.infrastructure.adapters.output.neo4j.repositories.PetNeo4jRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
public class Neo4jPetAdapter implements PetPersistencePort {

    private final PetNeo4jRepository petNeo4jRepository;

    public Neo4jPetAdapter(PetNeo4jRepository petNeo4jRepository) {
        this.petNeo4jRepository = petNeo4jRepository;
    }

    @Override
    public Optional<Pet> findById(String id) {
        return petNeo4jRepository.findById(id);
    }

    @Override
    @Transactional
    public Pet save(Pet pet) {
        if (!petNeo4jRepository.existsShelterById(pet.shelter().id())) {
            throw new EntityNotFoundException("Shelter not found: " + pet.shelter().id());
        }

        return petNeo4jRepository.save(pet);
    }

    @Override
    public List<Pet> findAll(int skip, int limit) {
        return petNeo4jRepository.findAll(PageRequest.of(skip / limit, limit)).getContent();
    }
}
