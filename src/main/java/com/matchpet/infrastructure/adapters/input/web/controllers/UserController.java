package com.matchpet.infrastructure.adapters.input.web.controllers;

import com.matchpet.application.ports.input.RegisterUserUseCase;
import com.matchpet.application.ports.input.dto.RegisterUserCommand;
import com.matchpet.application.ports.input.dto.UserResult;
import com.matchpet.domain.model.Trait;
import com.matchpet.infrastructure.adapters.input.web.dto.RegisterUserRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final RegisterUserUseCase registerUserUseCase;

    public UserController(RegisterUserUseCase registerUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
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
}
