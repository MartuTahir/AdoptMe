package com.matchpet.infrastructure.adapters.input.web.controllers;

import com.matchpet.application.ports.input.RegisterPetUseCase;
import com.matchpet.application.ports.input.dto.PetResult;
import com.matchpet.application.ports.input.dto.RegisterPetCommand;
import com.matchpet.application.ports.input.ListPetsUseCase;
import com.matchpet.domain.model.Shelter;
import com.matchpet.domain.model.Trait;
import com.matchpet.infrastructure.adapters.input.web.dto.RegisterPetRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/pets")
public class PetController {

    private static final Logger log = LoggerFactory.getLogger(PetController.class);

    private final RegisterPetUseCase registerPetUseCase;
    private final ListPetsUseCase listPetsUseCase;

    public PetController(RegisterPetUseCase registerPetUseCase, ListPetsUseCase listPetsUseCase) {
        this.registerPetUseCase = registerPetUseCase;
        this.listPetsUseCase = listPetsUseCase;
    }

    @GetMapping
    public ResponseEntity<Page<PetResult>> listPets(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/pets page={} size={}", page, size);
        return ResponseEntity.ok(listPetsUseCase.execute(page, size));
    }

    @PostMapping
    @PreAuthorize("hasRole('REFUGIO')")
    public ResponseEntity<PetResult> register(@Valid @RequestBody RegisterPetRequest request) {
        log.info("POST /api/pets id={} shelterId={} traitsCount={}",
                request.id(),
                request.shelter().id(),
                request.traits().size());

        RegisterPetCommand command = new RegisterPetCommand(
                request.id(),
                request.name(),
                new Shelter(request.shelter().id(), request.shelter().name(), request.shelter().location()),
                request.traits().stream().map(t -> new Trait(t.id(), t.name())).toList()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(registerPetUseCase.execute(command));
    }
}
