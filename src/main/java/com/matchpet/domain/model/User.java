package com.matchpet.domain.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Node("User")
public record User(
        @Id String id,
        String name,
        @Relationship(type = "PREFERS") List<Trait> preferences
) {

    public User {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("User id is required");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("User name is required");
        }

        List<Trait> safePreferences = preferences == null ? List.of() : preferences;
        preferences = List.copyOf(new ArrayList<>(new LinkedHashSet<>(safePreferences)));
    }

    public User addPreference(Trait trait) {
        if (trait == null) {
            throw new IllegalArgumentException("Trait is required");
        }

        List<Trait> updated = new ArrayList<>(preferences);
        updated.add(trait);
        return new User(id, name, updated);
    }
}
