package com.matchpet.application.services;

import com.matchpet.application.ports.output.PetPersistencePort;
import com.matchpet.application.ports.output.UserPersistencePort;
import com.matchpet.application.ports.input.dto.GetCompatibilityResult;
import com.matchpet.domain.exception.EntityNotFoundException;
import com.matchpet.domain.model.Pet;
import com.matchpet.domain.model.Shelter;
import com.matchpet.domain.model.Trait;
import com.matchpet.domain.model.User;
import com.matchpet.domain.service.CompatibilityEngine;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetCompatibilityServiceTest {

    private final UserPersistencePort userPersistencePort = mock(UserPersistencePort.class);
    private final PetPersistencePort petPersistencePort = mock(PetPersistencePort.class);
    private final CompatibilityEngine compatibilityEngine = new CompatibilityEngine();
    private final GetCompatibilityService service =
            new GetCompatibilityService(userPersistencePort, petPersistencePort, compatibilityEngine);

    @Test
    void shouldReturnPositiveScoreWhenThereAreMatchingTraits() {
        Trait friendly = new Trait("t1", "Friendly");
        Trait active = new Trait("t2", "Active");
        Trait calm = new Trait("t3", "Calm");

        User user = new User("u1", "Martin", List.of(friendly, active));
        Pet pet = new Pet("p1", "Luna", new Shelter("s1", "Refugio Norte", "CABA"), List.of(friendly, calm));

        when(userPersistencePort.findById("u1")).thenReturn(Optional.of(user));
        when(petPersistencePort.findById("p1")).thenReturn(Optional.of(pet));

        GetCompatibilityResult result = service.execute("u1", "p1");

        assertEquals(1, result.score());
        assertEquals(List.of(friendly), result.matchedTraits());
    }

    @Test
    void shouldReturnZeroScoreWhenThereAreNoMatchingTraits() {
        Trait friendly = new Trait("t1", "Friendly");
        Trait active = new Trait("t2", "Active");
        Trait calm = new Trait("t3", "Calm");
        Trait senior = new Trait("t4", "Senior");

        User user = new User("u1", "Martin", List.of(friendly, active));
        Pet pet = new Pet("p1", "Luna", new Shelter("s1", "Refugio Norte", "CABA"), List.of(calm, senior));

        when(userPersistencePort.findById("u1")).thenReturn(Optional.of(user));
        when(petPersistencePort.findById("p1")).thenReturn(Optional.of(pet));

        GetCompatibilityResult result = service.execute("u1", "p1");

        assertEquals(0, result.score());
        assertEquals(List.of(), result.matchedTraits());
    }

    @Test
    void shouldFailWhenUserDoesNotExist() {
        when(userPersistencePort.findById("u-404")).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> service.execute("u-404", "p1"));

        assertEquals("User not found: u-404", ex.getMessage());
    }

    @Test
    void shouldFailWhenPetDoesNotExist() {
        Trait friendly = new Trait("t1", "Friendly");
        User user = new User("u1", "Martin", List.of(friendly));

        when(userPersistencePort.findById("u1")).thenReturn(Optional.of(user));
        when(petPersistencePort.findById("p-404")).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> service.execute("u1", "p-404"));

        assertEquals("Pet not found: p-404", ex.getMessage());
    }
}
