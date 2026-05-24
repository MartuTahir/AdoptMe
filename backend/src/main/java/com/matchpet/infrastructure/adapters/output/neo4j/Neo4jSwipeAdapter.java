package com.matchpet.infrastructure.adapters.output.neo4j;

import com.matchpet.application.ports.output.SwipePersistencePort;
import com.matchpet.domain.model.SwipeEvent;
import com.matchpet.infrastructure.adapters.output.neo4j.repositories.SwipeNeo4jRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class Neo4jSwipeAdapter implements SwipePersistencePort {

    private final SwipeNeo4jRepository repository;

    public Neo4jSwipeAdapter(SwipeNeo4jRepository repository) {
        this.repository = repository;
    }

    @Override
    public SwipeEvent save(SwipeEvent swipeEvent) {
        repository.saveSwipe(
                swipeEvent.userId(),
                swipeEvent.petId(),
                swipeEvent.action().name(),
                swipeEvent.timestamp()
        );
        return swipeEvent;
    }

    @Override
    public long countLikesSince(String userId, Instant since) {
        return repository.countLikesSince(userId, since);
    }
}
