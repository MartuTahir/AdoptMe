package com.matchpet.domain.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PetTest {

    @Test
    void shouldCreatePetWithShelterAndDeduplicatedTraits() {
        Shelter shelter = new Shelter("s1", "Refugio Norte", "CABA");
        Trait calm = new Trait("t1", "Calm");

        Pet pet = new Pet("p1", "Luna", shelter, List.of(calm, calm));

        assertEquals("p1", pet.id());
        assertEquals(shelter, pet.shelter());
        assertEquals(1, pet.traits().size());
    }

    @Test
    void shouldRejectPetWithoutShelter() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new Pet("p1", "Luna", null, List.of()));

        assertEquals("Pet shelter is required", ex.getMessage());
    }

    @Test
    void shouldAddTraitOnceWhenDuplicated() {
        Shelter shelter = new Shelter("s1", "Refugio Norte", "CABA");
        Trait calm = new Trait("t1", "Calm");

        Pet pet = new Pet("p1", "Luna", shelter, List.of())
                .addTrait(calm)
                .addTrait(calm);

        assertEquals(1, pet.traits().size());
    }
}
