package com.matchpet.application.services;

import com.matchpet.application.ports.input.dto.PetResult;
import com.matchpet.application.ports.input.dto.RegisterPetCommand;
import com.matchpet.application.ports.output.PetPersistencePort;
import com.matchpet.domain.model.Pet;
import com.matchpet.domain.model.Shelter;
import com.matchpet.domain.model.Trait;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RegisterPetServiceTest {

    private final PetPersistencePort petPersistencePort = mock(PetPersistencePort.class);
    private final RegisterPetService service = new RegisterPetService(petPersistencePort);

    @Test
    void shouldRegisterPetWithShelterAndDeduplicatedTraits() {
        Trait calm = new Trait("t1", "Calm");
        Shelter shelter = new Shelter("s1", "Refugio Norte", "CABA");

        when(petPersistencePort.save(any(Pet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PetResult result = service.execute(new RegisterPetCommand("p1", "Luna", shelter, List.of(calm, calm)));

        assertEquals("p1", result.id());
        assertEquals("Luna", result.name());
        assertEquals(shelter, result.shelter());
        assertEquals(1, result.traits().size());
    }
}
