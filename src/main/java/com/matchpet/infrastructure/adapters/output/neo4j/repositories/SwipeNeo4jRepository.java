package com.matchpet.infrastructure.adapters.output.neo4j.repositories;

import com.matchpet.domain.model.User;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.time.Instant;

public interface SwipeNeo4jRepository extends Neo4jRepository<User, String> {

    @Query("MATCH (u:User {id: $userId}), (p:Pet {id: $petId}) " +
           "CREATE (u)-[r:SWIPED {action: $action, timestamp: $timestamp}]->(p)")
    void saveSwipe(String userId, String petId, String action, Instant timestamp);

    @Query("MATCH (u:User {id: $userId})-[r:SWIPED {action: 'LIKE'}]->(:Pet) " +
           "WHERE r.timestamp >= $since " +
           "RETURN count(r)")
    long countLikesSince(String userId, Instant since);
}
