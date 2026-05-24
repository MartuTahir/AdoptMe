package com.matchpet.domain.model;

public record OnboardingForm(
        String housingType,
        Integer availableHours,
        Boolean hasPreviousExperience,
        Boolean acceptsControlVisits
) {
    public OnboardingForm {
        if (housingType == null || housingType.isBlank()) {
            throw new IllegalArgumentException("Housing type is required");
        }
        if (availableHours == null || availableHours < 0) {
            throw new IllegalArgumentException("Available hours must be greater than or equal to 0");
        }
        if (hasPreviousExperience == null) {
            throw new IllegalArgumentException("Previous experience is required");
        }
        if (acceptsControlVisits == null) {
            throw new IllegalArgumentException("Visits acceptance is required");
        }
    }
}
