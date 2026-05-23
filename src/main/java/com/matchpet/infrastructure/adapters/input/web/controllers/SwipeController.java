package com.matchpet.infrastructure.adapters.input.web.controllers;

import com.matchpet.application.ports.input.SwipeUseCase;
import com.matchpet.application.ports.input.dto.SwipeCommand;
import com.matchpet.application.ports.input.dto.SwipeResult;
import com.matchpet.infrastructure.adapters.input.web.dto.SwipeRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/swipes")
public class SwipeController {

    private final SwipeUseCase swipeUseCase;

    public SwipeController(SwipeUseCase swipeUseCase) {
        this.swipeUseCase = swipeUseCase;
    }

    @PostMapping
    public ResponseEntity<SwipeResult> swipe(@Valid @RequestBody SwipeRequest request) {
        SwipeResult result = swipeUseCase.execute(new SwipeCommand(request.userId(), request.petId(), request.action()));
        return ResponseEntity.ok(result);
    }
}
