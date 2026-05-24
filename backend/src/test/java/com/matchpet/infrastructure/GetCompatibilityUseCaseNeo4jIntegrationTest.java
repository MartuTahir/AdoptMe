package com.matchpet.infrastructure;

import com.matchpet.application.ports.input.dto.GetCompatibilityResult;
import com.matchpet.application.services.GetCompatibilityService;
import com.matchpet.domain.model.Pet;
import com.matchpet.domain.model.Shelter;
import com.matchpet.domain.model.Trait;
import com.matchpet.domain.model.User;
import com.matchpet.domain.service.CompatibilityEngine;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataNeo4jTest
@Testcontainers
@Import({Neo4jUserAdapter.class, Neo4jPetAdapter.class, CompatibilityEngine.class, GetCompatibilityService.class})
class GetCompatibilityUseCaseNeo4jIntegrationTest {

    @Container
    @ServiceConnection
    static Neo4jContainer<?> neo4j = new Neo4jContainer<>("neo4j:5");

    @Autowired
    private Neo4jUserAdapter neo4jUserAdapter;

    @Autowired
    private Neo4jPetAdapter neo4jPetAdapter;

    @Autowired
    private GetCompatibilityService getCompatibilityService;

    @Autowired
    private Neo4jClient neo4jClient;

    @BeforeEach
    void cleanGraph() {
        neo4jClient.query("MATCH (n) DETACH DELETE n").run();
    }

    @Test
    void shouldReturnPositiveScoreWhenTraitsOverlapInRealGraph() {
        Trait friendly = new Trait("t1", "Friendly");
        Trait active = new Trait("t2", "Active");
        Trait calm = new Trait("t3", "Calm");
        Shelter shelter = new Shelter("s1", "Refugio Norte", "CABA");

        neo4jUserAdapter.save(new User("u1", "Martin", List.of(friendly, active)));
        neo4jClient.query("CREATE (:Shelter {id: $id, name: $name, location: $location})")
                .bind(shelter.id()).to("id")
                .bind(shelter.name()).to("name")
                .bind(shelter.location()).to("location")
                .run();
        neo4jPetAdapter.save(new Pet("p1", "Luna", shelter, List.of(friendly, calm)));

        GetCompatibilityResult result = getCompatibilityService.execute("u1", "p1");

        assertEquals(1, result.score());
        assertEquals(List.of(friendly), result.matchedTraits());
    }

    @Test
    void shouldReturnZeroScoreWhenThereAreNoSharedTraitsInRealGraph() {
        Trait friendly = new Trait("t1", "Friendly");
        Trait active = new Trait("t2", "Active");
        Trait calm = new Trait("t3", "Calm");
        Trait senior = new Trait("t4", "Senior");
        Shelter shelter = new Shelter("s1", "Refugio Norte", "CABA");

        neo4jUserAdapter.save(new User("u1", "Martin", List.of(friendly, active)));
        neo4jClient.query("CREATE (:Shelter {id: $id, name: $name, location: $location})")
                .bind(shelter.id()).to("id")
                .bind(shelter.name()).to("name")
                .bind(shelter.location()).to("location")
                .run();
        neo4jPetAdapter.save(new Pet("p1", "Luna", shelter, List.of(calm, senior)));

        GetCompatibilityResult result = getCompatibilityService.execute("u1", "p1");

        assertEquals(0, result.score());
        assertEquals(List.of(), result.matchedTraits());
    }
}
