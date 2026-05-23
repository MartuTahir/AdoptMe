package com.matchpet.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TraitTest {

    @Test
    void shouldCreateTraitWithValidData() {
        Trait trait = new Trait("t1", "Friendly");

        assertEquals("t1", trait.id());
        assertEquals("Friendly", trait.name());
    }

    @Test
    void shouldRejectBlankId() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new Trait(" ", "Friendly"));

        assertEquals("Trait id is required", ex.getMessage());
    }

    @Test
    void shouldRejectBlankName() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new Trait("t1", " "));

        assertEquals("Trait name is required", ex.getMessage());
    }
}
