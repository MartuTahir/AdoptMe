package com.matchpet.domain.service;

import com.matchpet.domain.model.Trait;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CompatibilityEngineTest {

    private final CompatibilityEngine engine = new CompatibilityEngine();

    @Test
    void shouldCalculatePositiveScoreWhenTraitsIntersect() {
        Trait friendly = new Trait("t1", "Friendly");
        Trait active = new Trait("t2", "Active");
        Trait calm = new Trait("t3", "Calm");

        CompatibilityResult result = engine.calculate(Set.of(friendly, active), Set.of(friendly, calm));

        assertEquals(1, result.score());
        assertEquals(Set.of(friendly), result.matchedTraits());
    }

    @Test
    void shouldCalculateZeroScoreWhenTraitsDoNotIntersect() {
        Trait friendly = new Trait("t1", "Friendly");
        Trait active = new Trait("t2", "Active");
        Trait calm = new Trait("t3", "Calm");
        Trait senior = new Trait("t4", "Senior");

        CompatibilityResult result = engine.calculate(Set.of(friendly, active), Set.of(calm, senior));

        assertEquals(0, result.score());
        assertEquals(Set.of(), result.matchedTraits());
    }
}
