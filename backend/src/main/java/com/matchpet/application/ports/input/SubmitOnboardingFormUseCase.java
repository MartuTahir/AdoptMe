package com.matchpet.application.ports.input;

import com.matchpet.application.ports.input.dto.OnboardingCommand;
import com.matchpet.application.ports.input.dto.UserResult;

public interface SubmitOnboardingFormUseCase {
    UserResult execute(OnboardingCommand command);
}
