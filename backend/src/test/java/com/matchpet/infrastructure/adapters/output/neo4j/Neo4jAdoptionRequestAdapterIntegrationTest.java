package com.matchpet.infrastructure;

import com.matchpet.domain.model.*;
import com.matchpet.infrastructure.adapters.output.neo4j.Neo4jAdoptionRequestAdapter;
import com.matchpet.infrastructure.adapters.output.neo4j.Neo4jPetAdapter;
import com.matchpet.infrastructure.adapters.output.neo4j.Neo4jUserAdapter;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataNeo4jTest
@Testcontainers
@Import({Neo4jUserAdapter.class, Neo4jPetAdapter.class, Neo4jAdoptionRequestAdapter.class})
class Neo4jAdoptionRequestAdapterIntegrationTest {

    @Container
    @ServiceConnection
    static Neo4jContainer<?> neo4j = new Neo4jContainer<>("neo4j:5");

    @Autowired
    private Neo4jUserAdapter neo4jUserAdapter;

    @Autowired
    private Neo4jPetAdapter neo4jPetAdapter;

    @Autowired
    private Neo4jAdoptionRequestAdapter adoptionRequestAdapter;

    @Autowired
    private Neo4jClient neo4jClient;

    private User user;
    private Pet pet;
    private Shelter shelter;

    @BeforeEach
    void setUp() {
        neo4jClient.query("MATCH (n) DETACH DELETE n").run();

        user = new User("u1", "Martin", List.of());
        neo4jUserAdapter.save(user);

        shelter = new Shelter("s1", "Refugio Norte", "CABA");
        neo4jClient.query("CREATE (:Shelter {id: $id, name: $name, location: $location})")
                .bind(shelter.id()).to("id")
                .bind(shelter.name()).to("name")
                .bind(shelter.location()).to("location")
                .run();

        pet = new Pet("p1", "Luna", shelter, List.of());
        neo4jPetAdapter.save(pet);
    }

    @Test
    void shouldCreateNewAdoptionRequest() {
        AdoptionRequest request = adoptionRequestAdapter.createOrGetPending("u1", "p1");

        assertNotNull(request);
        assertEquals("u1:p1", request.id());
        assertEquals(AdoptionRequestStatus.PENDING, request.status());

        // Verify nodes and relationships in Neo4j
        Optional<String> status = neo4jClient.query(
                "MATCH (u:User {id: 'u1'})-[r:REQUESTED]->(ar:AdoptionRequest {id: 'u1:p1'})-[:FOR]->(p:Pet {id: 'p1'}) " +
                "RETURN ar.status"
        ).fetchAs(String.class).one();

        assertTrue(status.isPresent());
        assertEquals("PENDING", status.get());
    }

    @Test
    void shouldBeIdempotentWhenCreatingAdoptionRequest() {
        AdoptionRequest first = adoptionRequestAdapter.createOrGetPending("u1", "p1");
        AdoptionRequest second = adoptionRequestAdapter.createOrGetPending("u1", "p1");

        assertNotNull(first);
        assertNotNull(second);
        assertEquals(first.id(), second.id());
        
        Long count = neo4jClient.query("MATCH (ar:AdoptionRequest {id: 'u1:p1'}) RETURN count(ar)")
                .fetchAs(Long.class).one().orElse(0L);
        assertEquals(1L, count);
    }

    @Test
    void shouldAcceptAdoptionRequestAndCreateMatch() {
        adoptionRequestAdapter.createOrGetPending("u1", "p1");

        AdoptionRequest accepted = adoptionRequestAdapter.accept("u1:p1", "s1");

        assertNotNull(accepted);
        assertEquals(AdoptionRequestStatus.ACCEPTED, accepted.status());

        // Verify MATCHED relationship
        Long matchCount = neo4jClient.query(
                "MATCH (u:User {id: 'u1'})-[r:MATCHED]->(p:Pet {id: 'p1'}) RETURN count(r)"
        ).fetchAs(Long.class).one().orElse(0L);

        assertEquals(1L, matchCount);
    }

    @Test
    void shouldRejectAdoptionRequest() {
        adoptionRequestAdapter.createOrGetPending("u1", "p1");

        AdoptionRequest rejected = adoptionRequestAdapter.reject("u1:p1", "s1");

        assertNotNull(rejected);
        assertEquals(AdoptionRequestStatus.REJECTED, rejected.status());

        // Verify NO MATCHED relationship
        Long matchCount = neo4jClient.query(
                "MATCH (u:User {id: 'u1'})-[r:MATCHED]->(p:Pet {id: 'p1'}) RETURN count(r)"
        ).fetchAs(Long.class).one().orElse(0L);

        assertEquals(0L, matchCount);
    }
}
