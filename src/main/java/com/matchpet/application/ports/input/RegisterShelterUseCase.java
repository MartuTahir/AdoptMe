package com.matchpet.application.ports.input;

import com.matchpet.application.ports.input.dto.RegisterShelterCommand;
import com.matchpet.application.ports.input.dto.ShelterResult;

public interface RegisterShelterUseCase {

    ShelterResult execute(RegisterShelterCommand command);
}
