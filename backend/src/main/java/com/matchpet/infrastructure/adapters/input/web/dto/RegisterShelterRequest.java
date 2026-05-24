package com.matchpet.infrastructure.adapters.input.web.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterShelterRequest(
        @NotBlank(message = "id is required") String id,
        @NotBlank(message = "name is required") String name,
        @NotBlank(message = "location is required") String location
) {
}
