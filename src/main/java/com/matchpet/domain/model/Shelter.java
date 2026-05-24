package com.matchpet.domain.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("Shelter")
public record Shelter(
        @Id String id,
        String name,
        String location
) {

    public Shelter {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Shelter id is required");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Shelter name is required");
        }
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("Shelter location is required");
        }
    }
}
