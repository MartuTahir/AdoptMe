package com.matchpet.domain.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Node("Pet")
public record Pet(
        @Id String id,
        String name,
        @Relationship(type = "LOCATED_IN") Shelter shelter,
        @Relationship(type = "HAS_TRAIT") List<Trait> traits
) {

    public Pet {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Pet id is required");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Pet name is required");
        }
        if (shelter == null) {
            throw new IllegalArgumentException("Pet shelter is required");
        }

        List<Trait> safeTraits = traits == null ? List.of() : traits;
        traits = List.copyOf(new ArrayList<>(new LinkedHashSet<>(safeTraits)));
    }

    public Pet addTrait(Trait trait) {
        if (trait == null) {
            throw new IllegalArgumentException("Trait is required");
        }

        List<Trait> updated = new ArrayList<>(traits);
        updated.add(trait);
        return new Pet(id, name, shelter, updated);
    }
}
