package com.matchpet.application.services;

import com.matchpet.application.ports.input.RegisterUserUseCase;
import com.matchpet.application.ports.input.dto.RegisterUserCommand;
import com.matchpet.application.ports.input.dto.UserResult;
import com.matchpet.application.ports.output.UserPersistencePort;
import com.matchpet.domain.model.User;
import org.springframework.stereotype.Service;

@Service
public class RegisterUserService implements RegisterUserUseCase {

    private final UserPersistencePort userPersistencePort;

    public RegisterUserService(UserPersistencePort userPersistencePort) {
        this.userPersistencePort = userPersistencePort;
    }

    @Override
    public UserResult execute(RegisterUserCommand command) {
        User savedUser = userPersistencePort.save(new User(command.id(), command.name(), command.preferences()));
        return new UserResult(savedUser.id(), savedUser.name(), savedUser.preferences());
    }
}
