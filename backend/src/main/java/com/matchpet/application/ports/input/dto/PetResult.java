package com.matchpet.application.ports.input.dto;

import com.matchpet.domain.model.Shelter;
import com.matchpet.domain.model.Trait;

import java.util.List;

public record PetResult(
        String id,
        String name,
        Shelter shelter,
        List<Trait> traits
) {
}
