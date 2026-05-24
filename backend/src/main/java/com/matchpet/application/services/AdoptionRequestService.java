package com.matchpet.application.services;

import com.matchpet.application.ports.input.AcceptAdoptionRequestUseCase;
import com.matchpet.application.ports.input.RejectAdoptionRequestUseCase;
import com.matchpet.application.ports.input.dto.AdoptionDecisionCommand;
import com.matchpet.application.ports.input.dto.AdoptionRequestResult;
import com.matchpet.application.ports.output.AdoptionRequestPersistencePort;
import com.matchpet.domain.model.AdoptionRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdoptionRequestService implements AcceptAdoptionRequestUseCase, RejectAdoptionRequestUseCase {

    private final AdoptionRequestPersistencePort persistencePort;

    public AdoptionRequestService(AdoptionRequestPersistencePort persistencePort) {
        this.persistencePort = persistencePort;
    }

    @Transactional
    @Override
    public AdoptionRequestResult accept(AdoptionDecisionCommand command) {
        AdoptionRequest adoptionRequest = persistencePort.accept(command.requestId(), command.shelterId());
        return mapToResult(adoptionRequest);
    }

    @Transactional
    @Override
    public AdoptionRequestResult reject(AdoptionDecisionCommand command) {
        AdoptionRequest adoptionRequest = persistencePort.reject(command.requestId(), command.shelterId());
        return mapToResult(adoptionRequest);
    }

    private AdoptionRequestResult mapToResult(AdoptionRequest adoptionRequest) {
        return new AdoptionRequestResult(
                adoptionRequest.id(),
                adoptionRequest.userId(),
                adoptionRequest.petId(),
                adoptionRequest.status(),
                adoptionRequest.updatedAt()
        );
    }
}
