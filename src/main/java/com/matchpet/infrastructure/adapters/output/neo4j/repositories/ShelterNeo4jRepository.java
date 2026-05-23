package com.matchpet.infrastructure.adapters.output.neo4j.repositories;

import com.matchpet.domain.model.Shelter;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface ShelterNeo4jRepository extends Neo4jRepository<Shelter, String> {
}
