package com.matchpet.infrastructure.adapters.output.neo4j.repositories;

import com.matchpet.domain.model.User;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface UserNeo4jRepository extends Neo4jRepository<User, String> {
}
