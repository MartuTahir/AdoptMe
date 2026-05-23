package com.matchpet.infrastructure.adapters.input.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OnboardingRequest(
        @NotBlank String userId,
        @NotBlank String housingType,
        @NotNull @Min(0) Integer availableHours,
        @NotNull Boolean hasPreviousExperience,
        @NotNull Boolean acceptsControlVisits
) {}
