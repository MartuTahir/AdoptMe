package com.matchpet.infrastructure;

import com.matchpet.domain.model.Shelter;
import com.matchpet.infrastructure.adapters.output.neo4j.Neo4jShelterAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@DataNeo4jTest
@Testcontainers
@Import(Neo4jShelterAdapter.class)
class Neo4jShelterAdapterIntegrationTest {

    @Container
    @ServiceConnection
    static Neo4jContainer<?> neo4j = new Neo4jContainer<>("neo4j:5");

    @Autowired
    private Neo4jShelterAdapter neo4jShelterAdapter;

    @Autowired
    private Neo4jClient neo4jClient;

    @BeforeEach
    void cleanGraph() {
        neo4jClient.query("MATCH (n) DETACH DELETE n").run();
        // Aplicar manualmente la constraint si no se carga automáticamente por el runner de tests en Testcontainers
        try {
            neo4jClient.query("CREATE CONSTRAINT shelter_id_unique IF NOT EXISTS FOR (s:Shelter) REQUIRE s.id IS UNIQUE").run();
        } catch (Exception ignored) {
        }
    }

    @Test
    void shouldPersistShelterSuccessfully() {
        Shelter shelter = new Shelter("s1", "Refugio Patitas", "Buenos Aires");

        Shelter saved = neo4jShelterAdapter.save(shelter);
        assertNotNull(saved);
        assertEquals("s1", saved.id());
        assertEquals("Refugio Patitas", saved.name());
        assertEquals("Buenos Aires", saved.location());

        Shelter found = neo4jShelterAdapter.findById("s1").orElseThrow();
        assertEquals("s1", found.id());
        assertEquals("Refugio Patitas", found.name());
        assertEquals("Buenos Aires", found.location());
    }

    @Test
    void shouldCheckIfExistsById() {
        Shelter shelter = new Shelter("s1", "Refugio Patitas", "Buenos Aires");
        assertFalse(neo4jShelterAdapter.existsById("s1"));

        neo4jShelterAdapter.save(shelter);
        assertTrue(neo4jShelterAdapter.existsById("s1"));
    }

    @Test
    void shouldThrowExceptionWhenIdIsDuplicatedByConstraint() {
        Shelter shelter1 = new Shelter("s1", "Refugio Patitas", "Buenos Aires");
        Shelter shelter2 = new Shelter("s1", "Refugio Diferente", "Cordoba");

        neo4jShelterAdapter.save(shelter1);

        assertThrows(DataIntegrityViolationException.class, () -> {
            neo4jShelterAdapter.save(shelter2);
        });
    }
}
