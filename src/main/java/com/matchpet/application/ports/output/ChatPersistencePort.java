package com.matchpet.application.ports.output;

import com.matchpet.domain.model.AdoptionRequestStatus;
import com.matchpet.domain.model.Message;

import java.util.List;
import java.util.Optional;

public interface ChatPersistencePort {

    Optional<ChatRequestContext> findRequestContext(String requestId);

    Message saveMessage(String requestId, Message message);

    List<Message> getChatHistory(String requestId, int skip, int limit);

    record ChatRequestContext(
            String requestId,
            AdoptionRequestStatus status,
            String adopterId,
            String shelterId
    ) {
    }
}
