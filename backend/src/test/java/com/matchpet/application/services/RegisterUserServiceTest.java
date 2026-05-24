package com.matchpet.application.services;

import com.matchpet.application.ports.input.dto.RegisterUserCommand;
import com.matchpet.application.ports.input.dto.UserResult;
import com.matchpet.application.ports.output.UserPersistencePort;
import com.matchpet.domain.model.Trait;
import com.matchpet.domain.model.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RegisterUserServiceTest {

    private final UserPersistencePort userPersistencePort = mock(UserPersistencePort.class);
    private final RegisterUserService service = new RegisterUserService(userPersistencePort);

    @Test
    void shouldRegisterUserWithDeduplicatedTraits() {
        Trait friendly = new Trait("t1", "Friendly");

        when(userPersistencePort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResult result = service.execute(new RegisterUserCommand("u1", "Martin", List.of(friendly, friendly)));

        assertEquals("u1", result.id());
        assertEquals("Martin", result.name());
        assertEquals(1, result.preferences().size());
    }
}
