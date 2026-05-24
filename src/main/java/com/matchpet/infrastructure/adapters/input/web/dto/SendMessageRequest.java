package com.matchpet.infrastructure.adapters.input.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendMessageRequest(
        @NotBlank @Size(max = 1000) String content
) {
}
