package com.matchpet.application.ports.input;

import com.matchpet.application.ports.input.dto.AdoptionDecisionCommand;
import com.matchpet.application.ports.input.dto.AdoptionRequestResult;

public interface AcceptAdoptionRequestUseCase {

    AdoptionRequestResult accept(AdoptionDecisionCommand command);
}
