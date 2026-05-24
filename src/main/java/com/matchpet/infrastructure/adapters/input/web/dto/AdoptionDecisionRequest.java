package com.matchpet.infrastructure.adapters.input.web.dto;

import jakarta.validation.constraints.NotBlank;

public record AdoptionDecisionRequest(
    @NotBlank(message = "Request ID is mandatory")
    String requestId
) {}
