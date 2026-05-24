package com.matchpet.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShelterTest {

    @Test
    void shouldCreateShelterWithValidData() {
        Shelter shelter = new Shelter("s1", "Refugio Norte", "CABA");

        assertEquals("s1", shelter.id());
        assertEquals("Refugio Norte", shelter.name());
        assertEquals("CABA", shelter.location());
    }

    @Test
    void shouldRejectBlankLocation() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new Shelter("s1", "Refugio Norte", " "));

        assertEquals("Shelter location is required", ex.getMessage());
    }
}
