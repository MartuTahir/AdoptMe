package com.matchpet.infrastructure;

import com.matchpet.application.ports.output.ChatPersistencePort;
import com.matchpet.domain.model.AdoptionRequestStatus;
import com.matchpet.domain.model.Message;
import com.matchpet.infrastructure.adapters.output.neo4j.Neo4jMessageAdapter;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataNeo4jTest
@Testcontainers
@Import(Neo4jMessageAdapter.class)
class Neo4jMessageAdapterIntegrationTest {

    @Container
    @ServiceConnection
    static Neo4jContainer<?> neo4j = new Neo4jContainer<>("neo4j:5");

    @Autowired
    private Neo4jMessageAdapter adapter;

    @Autowired
    private Neo4jClient neo4jClient;

    @BeforeEach
    void setUp() {
        neo4jClient.query("MATCH (n) DETACH DELETE n").run();
        neo4jClient.query("""
                CREATE (u:User {id: 'u-adopter-1'})
                CREATE (s:Shelter {id: 'u-shelter-1'})
                CREATE (p:Pet {id: 'p-10'})
                CREATE (ar:AdoptionRequest {id: 'ar-100', status: 'ACCEPTED'})
                CREATE (u)-[:REQUESTED]->(ar)
                CREATE (ar)-[:FOR]->(p)
                CREATE (p)-[:LOCATED_IN]->(s)
                """).run();
    }

    @Test
    void shouldFindRequestContextForAcceptedRequest() {
        Optional<ChatPersistencePort.ChatRequestContext> context = adapter.findRequestContext("ar-100");

        assertTrue(context.isPresent());
        assertEquals(AdoptionRequestStatus.ACCEPTED, context.get().status());
        assertEquals("u-adopter-1", context.get().adopterId());
        assertEquals("u-shelter-1", context.get().shelterId());
    }

    @Test
    void shouldPersistMessageUnderRequest() {
        Message saved = adapter.saveMessage("ar-100", new Message("m1", "u-adopter-1", "Hola", Instant.parse("2026-05-24T10:00:00Z")));

        assertEquals("m1", saved.id());
        Long count = neo4jClient.query("MATCH (:AdoptionRequest {id: 'ar-100'})-[:CONTAINS]->(m:Message {id: 'm1'}) RETURN count(m)")
                .fetchAs(Long.class)
                .one()
                .orElse(0L);
        assertEquals(1L, count);
    }

    @Test
    void shouldReturnPaginatedHistorySortedDescByTimestamp() {
        neo4jClient.query("""
                MATCH (ar:AdoptionRequest {id: 'ar-100'})
                CREATE (ar)-[:CONTAINS]->(:Message {id: 'm1', senderId: 'u-adopter-1', content: 'm1', timestamp: datetime('2026-05-24T10:00:00Z')})
                CREATE (ar)-[:CONTAINS]->(:Message {id: 'm2', senderId: 'u-shelter-1', content: 'm2', timestamp: datetime('2026-05-24T10:05:00Z')})
                CREATE (ar)-[:CONTAINS]->(:Message {id: 'm3', senderId: 'u-adopter-1', content: 'm3', timestamp: datetime('2026-05-24T10:10:00Z')})
                CREATE (ar)-[:CONTAINS]->(:Message {id: 'm4', senderId: 'u-shelter-1', content: 'm4', timestamp: datetime('2026-05-24T10:15:00Z')})
                CREATE (ar)-[:CONTAINS]->(:Message {id: 'm5', senderId: 'u-adopter-1', content: 'm5', timestamp: datetime('2026-05-24T10:20:00Z')})
                CREATE (ar)-[:CONTAINS]->(:Message {id: 'm6', senderId: 'u-shelter-1', content: 'm6', timestamp: datetime('2026-05-24T10:25:00Z')})
                """).run();

        List<Message> firstPage = adapter.getChatHistory("ar-100", 0, 2);
        List<Message> secondPage = adapter.getChatHistory("ar-100", 2, 2);
        List<Message> emptyPage = adapter.getChatHistory("ar-100", 20, 2);

        assertEquals(2, firstPage.size());
        assertEquals("m6", firstPage.getFirst().id());
        assertEquals("m5", firstPage.get(1).id());

        assertEquals(2, secondPage.size());
        assertEquals("m4", secondPage.getFirst().id());
        assertEquals("m3", secondPage.get(1).id());

        assertTrue(emptyPage.isEmpty());
    }
}
