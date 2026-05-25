package com.matchpet.infrastructure.adapters.input.web.controllers;

import com.matchpet.application.ports.input.SwipeUseCase;
import com.matchpet.application.ports.input.dto.SwipeCommand;
import com.matchpet.application.ports.input.dto.SwipeResult;
import com.matchpet.domain.model.SwipeAction;
import com.matchpet.infrastructure.adapters.input.web.dto.SwipeLikeRequest;
import com.matchpet.infrastructure.adapters.input.web.dto.SwipeRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/swipes")
public class SwipeController {

    private static final Logger log = LoggerFactory.getLogger(SwipeController.class);

    private final SwipeUseCase swipeUseCase;

    public SwipeController(SwipeUseCase swipeUseCase) {
        this.swipeUseCase = swipeUseCase;
    }

    @PostMapping
    public ResponseEntity<SwipeResult> swipe(@Valid @RequestBody SwipeRequest request) {
        log.info("POST /api/swipes userId={} petId={} action={}",
                request.userId(),
                request.petId(),
                request.action());

        SwipeResult result = swipeUseCase.execute(new SwipeCommand(request.userId(), request.petId(), request.action()));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/like")
    public ResponseEntity<SwipeResult> like(@Valid @RequestBody SwipeLikeRequest request,
                                            Authentication authentication) {
        String userId = authentication.getName();

        log.info("POST /api/swipes/like userId={} petId={} action=LIKE", userId, request.petId());

        SwipeResult result = swipeUseCase.execute(new SwipeCommand(userId, request.petId(), SwipeAction.LIKE));
        return ResponseEntity.ok(result);
    }
}
