package com.matchpet.application.ports.input.dto;

public record AdoptionDecisionCommand(String requestId, String shelterId) {
    public AdoptionDecisionCommand {
        if (requestId == null || requestId.isBlank()) {
            throw new IllegalArgumentException("Adoption request id is required");
        }
        if (shelterId == null || shelterId.isBlank()) {
            throw new IllegalArgumentException("Shelter id is required");
        }
    }
}
