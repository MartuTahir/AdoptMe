package com.matchpet.application.ports.output;

import com.matchpet.domain.model.SwipeEvent;
import java.time.Instant;

public interface SwipePersistencePort {

    SwipeEvent save(SwipeEvent swipeEvent);

    long countLikesSince(String userId, Instant since);
}
