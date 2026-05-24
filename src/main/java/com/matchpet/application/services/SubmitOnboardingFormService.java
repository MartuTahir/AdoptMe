package com.matchpet.application.services;

import com.matchpet.application.ports.input.SubmitOnboardingFormUseCase;
import com.matchpet.application.ports.input.dto.OnboardingCommand;
import com.matchpet.application.ports.input.dto.UserResult;
import com.matchpet.application.ports.output.UserPersistencePort;
import com.matchpet.domain.exception.EntityNotFoundException;
import com.matchpet.domain.model.OnboardingForm;
import com.matchpet.domain.model.User;
import com.matchpet.domain.service.TrustScoreCalculator;
import org.springframework.stereotype.Service;

@Service
public class SubmitOnboardingFormService implements SubmitOnboardingFormUseCase {

    private final UserPersistencePort userPersistencePort;
    private final TrustScoreCalculator calculator = new TrustScoreCalculator();

    public SubmitOnboardingFormService(UserPersistencePort userPersistencePort) {
        this.userPersistencePort = userPersistencePort;
    }

    @Override
    public UserResult execute(OnboardingCommand command) {
        User user = userPersistencePort.findById(command.userId())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + command.userId()));

        OnboardingForm form = new OnboardingForm(
                command.housingType(),
                command.availableHours(),
                command.hasPreviousExperience(),
                command.acceptsControlVisits()
        );

        int score = calculator.calculate(form);

        User updatedUser = new User(user.id(), user.name(), score, user.preferences());
        User savedUser = userPersistencePort.save(updatedUser);

        return new UserResult(savedUser.id(), savedUser.name(), savedUser.trustScore(), savedUser.preferences());
    }
}
