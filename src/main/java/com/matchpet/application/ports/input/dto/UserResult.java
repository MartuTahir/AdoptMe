package com.matchpet.application.ports.input.dto;

import com.matchpet.domain.model.Trait;

import java.util.List;

public record UserResult(
        String id,
        String name,
        Integer trustScore,
        List<Trait> preferences
) {
    public UserResult(String id, String name, List<Trait> preferences) {
        this(id, name, 0, preferences);
    }
}
