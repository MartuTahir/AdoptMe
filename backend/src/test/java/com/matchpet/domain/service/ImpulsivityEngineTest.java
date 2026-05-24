package com.matchpet.domain.service;

import com.matchpet.domain.exception.ImpulsiveBehaviorException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ImpulsivityEngineTest {

    private final ImpulsivityEngine engine = new ImpulsivityEngine();

    @Test
    void shouldAllowSwipeWhenLikesAreUnderLimit() {
        assertDoesNotThrow(() -> engine.checkImpulsivity(5));
        assertDoesNotThrow(() -> engine.checkImpulsivity(9));
    }

    @Test
    void shouldBlockSwipeWhenLikesReachLimit() {
        ImpulsiveBehaviorException ex = assertThrows(ImpulsiveBehaviorException.class,
                () -> engine.checkImpulsivity(10));

        assertEquals("Impulsive behavior detected: maximum 10 likes per minute allowed", ex.getMessage());
    }

    @Test
    void shouldBlockSwipeWhenLikesExceedLimit() {
        assertThrows(ImpulsiveBehaviorException.class,
                () -> engine.checkImpulsivity(15));
    }
}
