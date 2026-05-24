package com.matchpet.infrastructure.adapters.input.web.dto;

import jakarta.validation.constraints.NotBlank;

public record TraitRequest(
        @NotBlank String id,
        @NotBlank String name
) {
}
