package com.matchpet.infrastructure;

import com.matchpet.domain.model.Trait;
import com.matchpet.domain.model.User;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataNeo4jTest
@Testcontainers
@Import(Neo4jUserAdapter.class)
class Neo4jUserAdapterIntegrationTest {

    @Container
    @ServiceConnection
    static Neo4jContainer<?> neo4j = new Neo4jContainer<>("neo4j:5");

    @Autowired
    private Neo4jUserAdapter neo4jUserAdapter;

    @Autowired
    private Neo4jClient neo4jClient;

    @BeforeEach
    void cleanGraph() {
        neo4jClient.query("MATCH (n) DETACH DELETE n").run();
    }

    @Test
    void shouldPersistAndLoadUserWithPreferenceRelationships() {
        Trait friendly = new Trait("t1", "Friendly");
        Trait active = new Trait("t2", "Active");

        neo4jUserAdapter.save(new User("u1", "Martin", List.of(friendly, active)));

        User persisted = neo4jUserAdapter.findById("u1").orElseThrow();

        assertEquals("u1", persisted.id());
        assertEquals("Martin", persisted.name());
        assertEquals(2, persisted.preferences().size());

        long prefersCount = neo4jClient.query("MATCH (:User {id: $userId})-[:PREFERS]->(:Trait) RETURN count(*)")
                .bind("u1").to("userId")
                .fetchAs(Long.class)
                .one()
                .orElseThrow();

        assertEquals(2L, prefersCount);
    }

    @Test
    void shouldStoreDuplicatedTraitsOnlyOncePerUser() {
        Trait friendly = new Trait("t1", "Friendly");

        neo4jUserAdapter.save(new User("u1", "Martin", List.of(friendly, friendly)));

        User persisted = neo4jUserAdapter.findById("u1").orElseThrow();

        assertEquals(1, persisted.preferences().size());
        assertEquals("t1", persisted.preferences().getFirst().id());

        long prefersCount = neo4jClient.query("MATCH (:User {id: $userId})-[r:PREFERS]->(:Trait) RETURN count(r)")
                .bind("u1").to("userId")
                .fetchAs(Long.class)
                .one()
                .orElseThrow();

        assertEquals(1L, prefersCount);

        long traitCount = neo4jClient.query("MATCH (:User {id: $userId})-[:PREFERS]->(t:Trait {id: $traitId}) RETURN count(t)")
                .bind("u1").to("userId")
                .bind("t1").to("traitId")
                .fetchAs(Long.class)
                .one()
                .orElseThrow();

        assertEquals(1L, traitCount);
    }

    @Test
    void shouldPersistAndLoadUserWithTrustScore() {
        neo4jUserAdapter.save(new User("u1", "Martin", 85, List.of()));

        User persisted = neo4jUserAdapter.findById("u1").orElseThrow();

        assertEquals(85, persisted.trustScore());

        Integer dbScore = neo4jClient.query("MATCH (u:User {id: $userId}) RETURN u.trustScore")
                .bind("u1").to("userId")
                .fetchAs(Integer.class)
                .one()
                .orElseThrow();

        assertEquals(85, dbScore);
    }
}
