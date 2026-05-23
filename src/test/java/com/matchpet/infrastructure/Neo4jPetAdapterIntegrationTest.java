package com.matchpet.infrastructure;

import com.matchpet.domain.model.Pet;
import com.matchpet.domain.model.Shelter;
import com.matchpet.domain.model.Trait;
import com.matchpet.domain.exception.EntityNotFoundException;
import com.matchpet.infrastructure.adapters.output.neo4j.Neo4jPetAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataNeo4jTest
@Testcontainers
@Import(Neo4jPetAdapter.class)
class Neo4jPetAdapterIntegrationTest {

    @Container
    @ServiceConnection
    static Neo4jContainer<?> neo4j = new Neo4jContainer<>("neo4j:5");

    @Autowired
    private Neo4jPetAdapter neo4jPetAdapter;

    @Autowired
    private Neo4jClient neo4jClient;

    @BeforeEach
    void cleanGraph() {
        neo4jClient.query("MATCH (n) DETACH DELETE n").run();
    }

    @Test
    void shouldPersistPetLinkedToExistingShelter() {
        Shelter shelter = new Shelter("s1", "Refugio Norte", "CABA");
        Trait calm = new Trait("t1", "Calm");

        neo4jClient.query("CREATE (:Shelter {id: $id, name: $name, location: $location})")
                .bind(shelter.id()).to("id")
                .bind(shelter.name()).to("name")
                .bind(shelter.location()).to("location")
                .run();

        neo4jPetAdapter.save(new Pet("p1", "Luna", shelter, List.of(calm)));

        Pet persisted = neo4jPetAdapter.findById("p1").orElseThrow();
        assertEquals("p1", persisted.id());
        assertEquals("Luna", persisted.name());
        assertEquals("s1", persisted.shelter().id());
        assertEquals(1, persisted.traits().size());

        long locatedInCount = neo4jClient.query("MATCH (:Pet {id: $petId})-[r:LOCATED_IN]->(:Shelter {id: $shelterId}) RETURN count(r)")
                .bind("p1").to("petId")
                .bind("s1").to("shelterId")
                .fetchAs(Long.class)
                .one()
                .orElseThrow();

        assertEquals(1L, locatedInCount);
    }

    @Test
    void shouldFailWhenShelterDoesNotExist() {
        Shelter unknownShelter = new Shelter("s404", "Missing", "Nowhere");
        Pet orphanPet = new Pet("p404", "Ghost", unknownShelter, List.of(new Trait("t1", "Calm")));

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> neo4jPetAdapter.save(orphanPet));

        assertEquals("Shelter not found: s404", ex.getMessage());
    }
}
