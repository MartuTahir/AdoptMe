package com.matchpet.infrastructure.adapters.output.neo4j.repositories;

import com.matchpet.domain.model.Pet;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

public interface PetNeo4jRepository extends Neo4jRepository<Pet, String> {

    @Query("MATCH (s:Shelter {id: $shelterId}) RETURN count(s) > 0")
    boolean existsShelterById(@Param("shelterId") String shelterId);

    @Query("MATCH (s:Shelter {id: $shelterId})-[:OWNED_BY]-(p:Pet {id: $petId}) RETURN count(p) > 0")
    boolean isOwner(@Param("shelterId") String shelterId, @Param("petId") String petId);
}
