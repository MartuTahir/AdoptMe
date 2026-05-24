package com.matchpet.infrastructure.adapters.output.neo4j;

import com.matchpet.application.ports.output.UserPersistencePort;
import com.matchpet.domain.model.User;
import com.matchpet.infrastructure.adapters.output.neo4j.repositories.UserNeo4jRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class Neo4jUserAdapter implements UserPersistencePort {

    private final UserNeo4jRepository userNeo4jRepository;

    public Neo4jUserAdapter(UserNeo4jRepository userNeo4jRepository) {
        this.userNeo4jRepository = userNeo4jRepository;
    }

    @Override
    public Optional<User> findById(String id) {
        return userNeo4jRepository.findById(id);
    }

    @Override
    public User save(User user) {
        return userNeo4jRepository.save(user);
    }
}
