package com.matchpet.infrastructure.adapters.input.web.controllers;

import com.matchpet.application.ports.input.GetCompatibilityUseCase;
import com.matchpet.application.ports.input.dto.GetCompatibilityResult;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final GetCompatibilityUseCase getCompatibilityUseCase;

    public RecommendationController(GetCompatibilityUseCase getCompatibilityUseCase) {
        this.getCompatibilityUseCase = getCompatibilityUseCase;
    }

    @GetMapping
    public ResponseEntity<GetCompatibilityResult> getCompatibility(@RequestParam @NotBlank String userId,
                                                                   @RequestParam @NotBlank String petId) {
        return ResponseEntity.ok(getCompatibilityUseCase.execute(userId, petId));
    }
}
