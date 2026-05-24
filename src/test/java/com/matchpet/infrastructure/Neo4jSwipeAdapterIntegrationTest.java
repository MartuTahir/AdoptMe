package com.matchpet.infrastructure;

import com.matchpet.domain.model.Pet;
import com.matchpet.domain.model.Shelter;
import com.matchpet.domain.model.SwipeAction;
import com.matchpet.domain.model.SwipeEvent;
import com.matchpet.domain.model.User;
import com.matchpet.infrastructure.adapters.output.neo4j.Neo4jPetAdapter;
import com.matchpet.infrastructure.adapters.output.neo4j.Neo4jSwipeAdapter;
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

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataNeo4jTest
@Testcontainers
@Import({Neo4jUserAdapter.class, Neo4jPetAdapter.class, Neo4jSwipeAdapter.class})
class Neo4jSwipeAdapterIntegrationTest {

    @Container
    @ServiceConnection
    static Neo4jContainer<?> neo4j = new Neo4jContainer<>("neo4j:5");

    @Autowired
    private Neo4jUserAdapter neo4jUserAdapter;

    @Autowired
    private Neo4jPetAdapter neo4jPetAdapter;

    @Autowired
    private Neo4jSwipeAdapter neo4jSwipeAdapter;

    @Autowired
    private Neo4jClient neo4jClient;

    private User user;
    private Pet pet;

    @BeforeEach
    void setUp() {
        neo4jClient.query("MATCH (n) DETACH DELETE n").run();

        user = new User("u1", "Martin", List.of());
        neo4jUserAdapter.save(user);

        Shelter shelter = new Shelter("s1", "Refugio Norte", "CABA");
        neo4jClient.query("CREATE (:Shelter {id: $id, name: $name, location: $location})")
                .bind(shelter.id()).to("id")
                .bind(shelter.name()).to("name")
                .bind(shelter.location()).to("location")
                .run();

        pet = new Pet("p1", "Luna", shelter, List.of());
        neo4jPetAdapter.save(pet);
    }

    @Test
    void shouldPersistSwipeRelationship() {
        SwipeEvent event = new SwipeEvent("u1", "p1", SwipeAction.LIKE, Instant.now());
        
        SwipeEvent saved = neo4jSwipeAdapter.save(event);
        
        assertNotNull(saved);
        assertEquals("u1", saved.userId());
        assertEquals("p1", saved.petId());
        assertEquals(SwipeAction.LIKE, saved.action());

        long relationshipCount = neo4jClient.query(
                "MATCH (u:User {id: $userId})-[r:SWIPED {action: 'LIKE'}]->(p:Pet {id: $petId}) RETURN count(r)"
        )
                .bind("u1").to("userId")
                .bind("p1").to("petId")
                .fetchAs(Long.class)
                .one()
                .orElse(0L);

        assertEquals(1L, relationshipCount);
    }

    @Test
    void shouldCountLikesSinceCorrectly() {
        Instant now = Instant.now();
        Instant oneMinuteAgo = now.minusSeconds(60);
        Instant twoMinutesAgo = now.minusSeconds(120);

        // 3 likes within the 1-minute window
        neo4jSwipeAdapter.save(new SwipeEvent("u1", "p1", SwipeAction.LIKE, now.minusSeconds(10)));
        neo4jSwipeAdapter.save(new SwipeEvent("u1", "p1", SwipeAction.LIKE, now.minusSeconds(30)));
        neo4jSwipeAdapter.save(new SwipeEvent("u1", "p1", SwipeAction.LIKE, now.minusSeconds(50)));

        // 1 like outside the 1-minute window (2 minutes ago)
        neo4jSwipeAdapter.save(new SwipeEvent("u1", "p1", SwipeAction.LIKE, twoMinutesAgo));

        // 1 dislike within the 1-minute window (should not be counted)
        neo4jSwipeAdapter.save(new SwipeEvent("u1", "p1", SwipeAction.DISLIKE, now.minusSeconds(20)));

        long count = neo4jSwipeAdapter.countLikesSince("u1", oneMinuteAgo);
        assertEquals(3L, count);
    }
}
