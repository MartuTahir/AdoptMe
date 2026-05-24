package com.matchpet.application.services;

import com.matchpet.application.ports.input.GetChatHistoryUseCase;
import com.matchpet.application.ports.input.SendMessageUseCase;
import com.matchpet.application.ports.input.dto.GetChatHistoryQuery;
import com.matchpet.application.ports.input.dto.MessageResult;
import com.matchpet.application.ports.input.dto.SendMessageCommand;
import com.matchpet.application.ports.output.ChatPersistencePort;
import com.matchpet.domain.exception.AuthorizationException;
import com.matchpet.domain.exception.EntityNotFoundException;
import com.matchpet.domain.model.AdoptionRequestStatus;
import com.matchpet.domain.model.Message;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Application service for chat use cases (send messages and retrieve history).
 * Security checks and business validations are enforced at this layer.
 */
@Service
public class ChatService implements SendMessageUseCase, GetChatHistoryUseCase {

    private final ChatPersistencePort chatPersistencePort;

    public ChatService(ChatPersistencePort chatPersistencePort) {
        this.chatPersistencePort = chatPersistencePort;
    }

    @Override
    @Transactional
    public MessageResult sendMessage(SendMessageCommand command) {
        validateRequestId(command.requestId());
        validateSender(command.senderId());

        Message draft = new Message(null, command.senderId(), command.content(), null);

        ChatPersistencePort.ChatRequestContext context = loadAndAuthorize(command.requestId(), command.senderId());
        validateAcceptedStatus(context.status());

        Message saved = chatPersistencePort.saveMessage(command.requestId(), draft);

        return mapToResult(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageResult> getChatHistory(GetChatHistoryQuery query) {
        validateRequestId(query.requestId());
        validateSender(query.userId());

        if (query.page() < 0 || query.size() <= 0) {
            throw new IllegalArgumentException("Pagination parameters are invalid");
        }

        ChatPersistencePort.ChatRequestContext context = loadAndAuthorize(query.requestId(), query.userId());
        validateAcceptedStatus(context.status());

        int skip = query.page() * query.size();
        return chatPersistencePort.getChatHistory(query.requestId(), skip, query.size())
                .stream()
                .map(this::mapToResult)
                .toList();
    }

    private ChatPersistencePort.ChatRequestContext loadAndAuthorize(String requestId, String userId) {
        ChatPersistencePort.ChatRequestContext context = chatPersistencePort.findRequestContext(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Adoption request not found: " + requestId));

        boolean isAdopter = userId.equals(context.adopterId());
        boolean isShelter = userId.equals(context.shelterId());
        if (!isAdopter && !isShelter) {
            throw new AuthorizationException("User is not a participant of this chat");
        }

        return context;
    }

    private void validateAcceptedStatus(AdoptionRequestStatus status) {
        if (status != AdoptionRequestStatus.ACCEPTED) {
            throw new IllegalStateException("Chat is allowed only for ACCEPTED requests");
        }
    }

    private void validateRequestId(String requestId) {
        if (requestId == null || requestId.isBlank()) {
            throw new IllegalArgumentException("requestId is required");
        }
    }

    private void validateSender(String senderId) {
        if (senderId == null || senderId.isBlank()) {
            throw new IllegalArgumentException("userId is required");
        }
    }

    private MessageResult mapToResult(Message message) {
        return new MessageResult(message.id(), message.senderId(), message.content(), message.timestamp());
    }
}
