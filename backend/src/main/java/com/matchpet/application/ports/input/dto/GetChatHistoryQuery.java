package com.matchpet.application.ports.input.dto;

public record GetChatHistoryQuery(
        String requestId,
        String userId,
        int page,
        int size
) {
}
