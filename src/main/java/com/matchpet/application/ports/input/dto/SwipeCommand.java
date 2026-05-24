package com.matchpet.application.ports.input.dto;

import com.matchpet.domain.model.SwipeAction;

public record SwipeCommand(
        String userId,
        String petId,
        SwipeAction action
) {
}
