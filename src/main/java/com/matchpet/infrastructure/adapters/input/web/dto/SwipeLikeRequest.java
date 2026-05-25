package com.matchpet.infrastructure.adapters.input.web.dto;

import jakarta.validation.constraints.NotBlank;

public record SwipeLikeRequest(
        @NotBlank String petId
) {
}
