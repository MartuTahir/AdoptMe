package com.matchpet.infrastructure.adapters.input.web.controllers;

import com.matchpet.application.ports.input.RegisterShelterUseCase;
import com.matchpet.application.ports.input.dto.RegisterShelterCommand;
import com.matchpet.application.ports.input.dto.ShelterResult;
import com.matchpet.infrastructure.adapters.input.web.dto.RegisterShelterRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shelters")
public class ShelterController {

    private final RegisterShelterUseCase registerShelterUseCase;

    public ShelterController(RegisterShelterUseCase registerShelterUseCase) {
        this.registerShelterUseCase = registerShelterUseCase;
    }

    @PostMapping
    @PreAuthorize("hasRole('REFUGIO')")
    public ResponseEntity<ShelterResult> register(@Valid @RequestBody RegisterShelterRequest request) {
        RegisterShelterCommand command = new RegisterShelterCommand(
                request.id(),
                request.name(),
                request.location()
        );

        ShelterResult result = registerShelterUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
