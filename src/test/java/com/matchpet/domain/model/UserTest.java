package com.matchpet.domain.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserTest {

    @Test
    void shouldDeduplicatePreferencesOnCreation() {
        Trait friendly = new Trait("t1", "Friendly");

        User user = new User("u1", "Martin", List.of(friendly, friendly));

        assertEquals(1, user.preferences().size());
        assertEquals(friendly, user.preferences().getFirst());
    }

    @Test
    void shouldAddTraitOnceWhenDuplicated() {
        Trait active = new Trait("t2", "Active");

        User user = new User("u1", "Martin", List.of())
                .addPreference(active)
                .addPreference(active);

        assertEquals(1, user.preferences().size());
    }

    @Test
    void shouldRejectBlankName() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new User("u1", " ", List.of()));

        assertEquals("User name is required", ex.getMessage());
    }

    @Test
    void shouldDefaultTrustScoreToZeroWhenUsingCompatConstructor() {
        User user = new User("u1", "Martin", List.of());
        assertEquals(0, user.trustScore());
    }

    @Test
    void shouldCreateUserWithSpecificTrustScore() {
        User user = new User("u1", "Martin", 85, List.of());
        assertEquals(85, user.trustScore());
    }
}

