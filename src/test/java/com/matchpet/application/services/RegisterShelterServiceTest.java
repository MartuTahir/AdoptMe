package com.matchpet.application.services;

import com.matchpet.application.ports.input.dto.RegisterShelterCommand;
import com.matchpet.application.ports.input.dto.ShelterResult;
import com.matchpet.application.ports.output.ShelterPersistencePort;
import com.matchpet.domain.model.Shelter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RegisterShelterServiceTest {

    private final ShelterPersistencePort shelterPersistencePort = mock(ShelterPersistencePort.class);
    private final RegisterShelterService service = new RegisterShelterService(shelterPersistencePort);

    @Test
    void shouldRegisterShelterSuccessfully() {
        RegisterShelterCommand command = new RegisterShelterCommand("s1", "Refugio Patitas", "Buenos Aires");

        when(shelterPersistencePort.existsById("s1")).thenReturn(false);
        when(shelterPersistencePort.save(any(Shelter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShelterResult result = service.execute(command);

        assertEquals("s1", result.id());
        assertEquals("Refugio Patitas", result.name());
        assertEquals("Buenos Aires", result.location());
    }

    @Test
    void shouldThrowExceptionWhenRegisteringShelterWithDuplicateId() {
        RegisterShelterCommand command = new RegisterShelterCommand("s1", "Refugio Patitas", "Buenos Aires");

        when(shelterPersistencePort.existsById("s1")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.execute(command));
        assertEquals("Shelter already exists: s1", exception.getMessage());
    }
}
