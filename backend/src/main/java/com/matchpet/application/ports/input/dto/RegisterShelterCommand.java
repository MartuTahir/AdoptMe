package com.matchpet.application.ports.input.dto;

public record RegisterShelterCommand(
        String id,
        String name,
        String location
) {
}
