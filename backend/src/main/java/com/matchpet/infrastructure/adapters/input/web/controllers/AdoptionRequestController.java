package com.matchpet.infrastructure.adapters.input.web.controllers;

import com.matchpet.application.ports.input.AcceptAdoptionRequestUseCase;
import com.matchpet.application.ports.input.RejectAdoptionRequestUseCase;
import com.matchpet.application.ports.input.dto.AdoptionDecisionCommand;
import com.matchpet.application.ports.input.dto.AdoptionRequestResult;
import com.matchpet.infrastructure.adapters.input.web.dto.AdoptionDecisionRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/adoption-requests")
public class AdoptionRequestController {

    private final AcceptAdoptionRequestUseCase acceptUseCase;
    private final RejectAdoptionRequestUseCase rejectUseCase;

    public AdoptionRequestController(AcceptAdoptionRequestUseCase acceptUseCase,
                                     RejectAdoptionRequestUseCase rejectUseCase) {
        this.acceptUseCase = acceptUseCase;
        this.rejectUseCase = rejectUseCase;
    }

    @PostMapping("/accept")
    @PreAuthorize("hasRole('REFUGIO')")
    public ResponseEntity<AdoptionRequestResult> accept(@Valid @RequestBody AdoptionDecisionRequest request,
                                                        Authentication authentication) {
        AdoptionDecisionCommand command = new AdoptionDecisionCommand(request.requestId(), authentication.getName());
        return ResponseEntity.ok(acceptUseCase.accept(command));
    }

    @PostMapping("/reject")
    @PreAuthorize("hasRole('REFUGIO')")
    public ResponseEntity<AdoptionRequestResult> reject(@Valid @RequestBody AdoptionDecisionRequest request,
                                                        Authentication authentication) {
        AdoptionDecisionCommand command = new AdoptionDecisionCommand(request.requestId(), authentication.getName());
        return ResponseEntity.ok(rejectUseCase.reject(command));
    }
}
