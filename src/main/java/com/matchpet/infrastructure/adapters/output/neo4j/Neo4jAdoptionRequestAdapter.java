package com.matchpet.infrastructure.adapters.output.neo4j;

import com.matchpet.application.ports.output.AdoptionRequestPersistencePort;
import com.matchpet.domain.model.AdoptionRequest;
import com.matchpet.domain.model.AdoptionRequestStatus;
import org.neo4j.driver.Value;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class Neo4jAdoptionRequestAdapter implements AdoptionRequestPersistencePort {

    private final Neo4jClient neo4jClient;

    public Neo4jAdoptionRequestAdapter(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    @Override
    public AdoptionRequest createOrGetPending(String userId, String petId) {
        String requestId = AdoptionRequest.composeId(userId, petId);
        
        return neo4jClient.query(
                "MATCH (u:User {id: $userId}) " +
                "MATCH (p:Pet {id: $petId}) " +
                "MERGE (ar:AdoptionRequest {id: $requestId}) " +
                "ON CREATE SET ar.userId = $userId, ar.petId = $petId, ar.status = 'PENDING', ar.createdAt = datetime($now), ar.updatedAt = datetime($now) " +
                "MERGE (u)-[:REQUESTED]->(ar) " +
                "MERGE (ar)-[:FOR]->(p) " +
                "RETURN ar"
        )
        .bind(userId).to("userId")
        .bind(petId).to("petId")
        .bind(requestId).to("requestId")
        .bind(Instant.now().toString()).to("now")
        .fetchAs(AdoptionRequest.class)
        .mappedBy((typeSystem, record) -> {
            Value node = record.get("ar");
            return new AdoptionRequest(
                node.get("id").asString(),
                node.get("userId").asString(),
                node.get("petId").asString(),
                AdoptionRequestStatus.valueOf(node.get("status").asString()),
                node.get("createdAt").asZonedDateTime().toInstant(),
                node.get("updatedAt").asZonedDateTime().toInstant()
            );
        })
        .one()
        .orElseThrow();
    }

    @Override
    public AdoptionRequest accept(String requestId, String shelterId) {
        return updateStatus(requestId, AdoptionRequestStatus.ACCEPTED);
    }

    @Override
    public AdoptionRequest reject(String requestId, String shelterId) {
        return updateStatus(requestId, AdoptionRequestStatus.REJECTED);
    }

    @Override
    public List<AdoptionRequest> findByUserIdAndStatus(String userId, AdoptionRequestStatus status) {
        return new ArrayList<>(neo4jClient.query(
                "MATCH (u:User {id: $userId})-[:REQUESTED]->(ar:AdoptionRequest) " +
                "WHERE ar.status = $status " +
                "RETURN ar"
        )
        .bind(userId).to("userId")
        .bind(status.name()).to("status")
        .fetchAs(AdoptionRequest.class)
        .mappedBy((typeSystem, record) -> {
            Value node = record.get("ar");
            return new AdoptionRequest(
                node.get("id").asString(),
                node.get("userId").asString(),
                node.get("petId").asString(),
                AdoptionRequestStatus.valueOf(node.get("status").asString()),
                node.get("createdAt").asZonedDateTime().toInstant(),
                node.get("updatedAt").asZonedDateTime().toInstant()
            );
        })
        .all());
    }

    private AdoptionRequest updateStatus(String requestId, AdoptionRequestStatus status) {
        String query = "MATCH (ar:AdoptionRequest {id: $requestId}) " +
                       "SET ar.status = $status, ar.updatedAt = datetime($now) ";
        
        if (status == AdoptionRequestStatus.ACCEPTED) {
            query += "WITH ar " +
                     "MATCH (u:User)-[:REQUESTED]->(ar)-[:FOR]->(p:Pet) " +
                     "MERGE (u)-[:MATCHED]->(p) ";
        }
        
        query += "RETURN ar";

        return neo4jClient.query(query)
                .bind(requestId).to("requestId")
                .bind(status.name()).to("status")
                .bind(Instant.now().toString()).to("now")
                .fetchAs(AdoptionRequest.class)
                .mappedBy((typeSystem, record) -> {
                    Value node = record.get("ar");
                    return new AdoptionRequest(
                        node.get("id").asString(),
                        node.get("userId").asString(),
                        node.get("petId").asString(),
                        AdoptionRequestStatus.valueOf(node.get("status").asString()),
                        node.get("createdAt").asZonedDateTime().toInstant(),
                        node.get("updatedAt").asZonedDateTime().toInstant()
                    );
                })
                .one()
                .orElseThrow();
    }
}
