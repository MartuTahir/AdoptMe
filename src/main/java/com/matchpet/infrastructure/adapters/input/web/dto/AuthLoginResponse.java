package com.matchpet.infrastructure.adapters.input.web.dto;

public record AuthLoginResponse(
        String token,
        String tokenType
) {
}
