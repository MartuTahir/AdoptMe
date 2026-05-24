package com.matchpet.infrastructure.adapters.input.web.dto;

import com.matchpet.domain.model.SwipeAction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SwipeRequest(
        @NotBlank String userId,
        @NotBlank String petId,
        @NotNull SwipeAction action
) {
}
