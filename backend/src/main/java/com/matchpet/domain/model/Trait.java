package com.matchpet.domain.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("Trait")
public record Trait(@Id String id, String name) {

    public Trait {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Trait id is required");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Trait name is required");
        }
    }
}
