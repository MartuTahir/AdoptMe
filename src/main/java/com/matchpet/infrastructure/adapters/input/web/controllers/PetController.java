package com.matchpet.infrastructure.adapters.input.web.controllers;

import com.matchpet.application.ports.input.RegisterPetUseCase;
import com.matchpet.application.ports.input.dto.PetResult;
import com.matchpet.application.ports.input.dto.RegisterPetCommand;
import com.matchpet.domain.model.Shelter;
import com.matchpet.domain.model.Trait;
import com.matchpet.infrastructure.adapters.input.web.dto.RegisterPetRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pets")
public class PetController {

    private final RegisterPetUseCase registerPetUseCase;

    public PetController(RegisterPetUseCase registerPetUseCase) {
        this.registerPetUseCase = registerPetUseCase;
    }

    @PostMapping
    @PreAuthorize("hasRole('REFUGIO')")
    public ResponseEntity<PetResult> register(@Valid @RequestBody RegisterPetRequest request) {
        RegisterPetCommand command = new RegisterPetCommand(
                request.id(),
                request.name(),
                new Shelter(request.shelter().id(), request.shelter().name(), request.shelter().location()),
                request.traits().stream().map(t -> new Trait(t.id(), t.name())).toList()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(registerPetUseCase.execute(command));
    }
}
