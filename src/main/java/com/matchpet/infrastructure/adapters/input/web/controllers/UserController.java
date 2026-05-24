package com.matchpet.infrastructure.adapters.input.web.controllers;

import com.matchpet.application.ports.input.GetUserMatchesUseCase;
import com.matchpet.application.ports.input.RegisterUserUseCase;
import com.matchpet.application.ports.input.SubmitOnboardingFormUseCase;
import com.matchpet.application.ports.input.dto.MatchResult;
import com.matchpet.application.ports.input.dto.OnboardingCommand;
import com.matchpet.application.ports.input.dto.RegisterUserCommand;
import com.matchpet.application.ports.input.dto.UserResult;
import com.matchpet.domain.model.Trait;
import com.matchpet.infrastructure.adapters.input.web.dto.OnboardingRequest;
import com.matchpet.infrastructure.adapters.input.web.dto.RegisterUserRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final RegisterUserUseCase registerUserUseCase;
    private final SubmitOnboardingFormUseCase submitOnboardingFormUseCase;
    private final GetUserMatchesUseCase getUserMatchesUseCase;

    public UserController(RegisterUserUseCase registerUserUseCase,
                          SubmitOnboardingFormUseCase submitOnboardingFormUseCase,
                          GetUserMatchesUseCase getUserMatchesUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.submitOnboardingFormUseCase = submitOnboardingFormUseCase;
        this.getUserMatchesUseCase = getUserMatchesUseCase;
    }

    @PostMapping("/register/adopter")
    public ResponseEntity<UserResult> registerAdopter(@Valid @RequestBody RegisterUserRequest request) {
        RegisterUserCommand command = new RegisterUserCommand(
                request.id(),
                request.name(),
                request.preferences().stream()
                        .map(t -> new Trait(t.id(), t.name()))
                        .toList()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(registerUserUseCase.execute(command));
    }

    @PostMapping("/register/shelter")
    public ResponseEntity<UserResult> registerShelter(@Valid @RequestBody RegisterUserRequest request) {
        RegisterUserCommand command = new RegisterUserCommand(
                request.id(),
                request.name(),
                request.preferences().stream()
                        .map(t -> new Trait(t.id(), t.name()))
                        .toList()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(registerUserUseCase.execute(command));
    }

    @PostMapping("/onboarding")
    public ResponseEntity<UserResult> submitOnboarding(@Valid @RequestBody OnboardingRequest request) {
        OnboardingCommand command = new OnboardingCommand(
                request.userId(),
                request.housingType(),
                request.availableHours(),
                request.hasPreviousExperience(),
                request.acceptsControlVisits()
        );

        return ResponseEntity.ok(submitOnboardingFormUseCase.execute(command));
    }

    @GetMapping("/me/matches")
    public ResponseEntity<List<MatchResult>> getMyMatches(Authentication authentication) {
        Objects.requireNonNull(authentication, "Authentication is required");
        return ResponseEntity.ok(getUserMatchesUseCase.execute(authentication.getName()));
    }
}
