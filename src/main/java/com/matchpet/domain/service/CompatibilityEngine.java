package com.matchpet.domain.service;

import com.matchpet.domain.model.Trait;

import java.util.HashSet;
import java.util.Set;

public class CompatibilityEngine {

    public CompatibilityResult calculate(Set<Trait> userTraits, Set<Trait> petTraits) {
        Set<Trait> safeUserTraits = userTraits == null ? Set.of() : Set.copyOf(userTraits);
        Set<Trait> safePetTraits = petTraits == null ? Set.of() : Set.copyOf(petTraits);

        Set<Trait> matched = new HashSet<>(safeUserTraits);
        matched.retainAll(safePetTraits);

        return new CompatibilityResult(matched.size(), matched);
    }
}
