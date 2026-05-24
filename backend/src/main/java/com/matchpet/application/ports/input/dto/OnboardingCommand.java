package com.matchpet.application.ports.input.dto;

public record OnboardingCommand(
        String userId,
        String housingType,
        Integer availableHours,
        Boolean hasPreviousExperience,
        Boolean acceptsControlVisits
) {}
