package com.matchpet.infrastructure.adapters.input.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RegisterUserRequest(
        @NotBlank String id,
        @NotBlank String name,
        @NotNull @Valid List<@Valid TraitRequest> preferences
) {
}
