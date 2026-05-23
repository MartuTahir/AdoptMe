package com.matchpet.application.ports.input;

import com.matchpet.application.ports.input.dto.GetCompatibilityResult;

public interface GetCompatibilityUseCase {

    GetCompatibilityResult execute(String userId, String petId);
}
