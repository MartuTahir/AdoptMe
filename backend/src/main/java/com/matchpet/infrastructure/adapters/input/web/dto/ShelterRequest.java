package com.matchpet.infrastructure.adapters.input.web.dto;

import jakarta.validation.constraints.NotBlank;

public record ShelterRequest(
        @NotBlank String id,
        @NotBlank String name,
        @NotBlank String location
) {
}
