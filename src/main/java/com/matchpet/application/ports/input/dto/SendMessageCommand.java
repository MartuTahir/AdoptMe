package com.matchpet.application.ports.input.dto;

public record SendMessageCommand(
        String requestId,
        String senderId,
        String content
) {
}
