package com.matchpet.application.ports.input;

import com.matchpet.application.ports.input.dto.PetResult;
import com.matchpet.application.ports.input.dto.RegisterPetCommand;

public interface RegisterPetUseCase {

    PetResult execute(RegisterPetCommand command);
}
