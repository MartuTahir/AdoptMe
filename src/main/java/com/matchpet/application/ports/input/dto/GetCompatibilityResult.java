package com.matchpet.application.ports.input.dto;

import com.matchpet.domain.model.Trait;

import java.util.List;

public record GetCompatibilityResult(
        int score,
        List<Trait> matchedTraits
) {
}
