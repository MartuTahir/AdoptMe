package com.matchpet.application.ports.input;

import com.matchpet.application.ports.input.dto.RegisterUserCommand;
import com.matchpet.application.ports.input.dto.UserResult;

public interface RegisterUserUseCase {

    UserResult execute(RegisterUserCommand command);
}
