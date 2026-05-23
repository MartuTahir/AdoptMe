package com.matchpet.application.ports.output;

import com.matchpet.domain.model.SwipeEvent;

public interface SwipePersistencePort {

    SwipeEvent save(SwipeEvent swipeEvent);
}
