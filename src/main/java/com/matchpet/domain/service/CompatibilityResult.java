package com.matchpet.domain.service;

import com.matchpet.domain.model.Trait;

import java.util.Set;

public record CompatibilityResult(int score, Set<Trait> matchedTraits) {

    public CompatibilityResult {
        if (score < 0) {
            throw new IllegalArgumentException("Compatibility score cannot be negative");
        }
        matchedTraits = matchedTraits == null ? Set.of() : Set.copyOf(matchedTraits);
    }
}
