package com.matchpet.application.services;

import com.matchpet.application.ports.input.dto.OnboardingCommand;
import com.matchpet.application.ports.input.dto.UserResult;
import com.matchpet.application.ports.output.UserPersistencePort;
import com.matchpet.domain.exception.EntityNotFoundException;
import com.matchpet.domain.model.User;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SubmitOnboardingFormServiceTest {

    private final UserPersistencePort userPersistencePort = mock(UserPersistencePort.class);
    private final SubmitOnboardingFormService service = new SubmitOnboardingFormService(userPersistencePort);

    @Test
    void shouldSubmitOnboardingFormSuccessfully() {
        User existingUser = new User("u1", "Martin", 0, List.of());
        when(userPersistencePort.findById("u1")).thenReturn(Optional.of(existingUser));
        when(userPersistencePort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OnboardingCommand command = new OnboardingCommand(
                "u1",
                "PATIO_CERRADO",
                4,
                true,
                true
        );

        UserResult result = service.execute(command);

        assertEquals("u1", result.id());
        assertEquals(100, result.trustScore());
        verify(userPersistencePort, times(1)).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        when(userPersistencePort.findById("u1")).thenReturn(Optional.empty());

        OnboardingCommand command = new OnboardingCommand(
                "u1",
                "PATIO_CERRADO",
                4,
                true,
                true
        );

        assertThrows(EntityNotFoundException.class, () -> service.execute(command));
        verify(userPersistencePort, never()).save(any());
    }
}
