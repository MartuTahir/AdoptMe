package com.matchpet.infrastructure.adapters.input.web.controllers;

import com.matchpet.application.ports.input.GetChatHistoryUseCase;
import com.matchpet.application.ports.input.SendMessageUseCase;
import com.matchpet.application.ports.input.dto.GetChatHistoryQuery;
import com.matchpet.application.ports.input.dto.MessageResult;
import com.matchpet.application.ports.input.dto.SendMessageCommand;
import com.matchpet.infrastructure.adapters.input.web.dto.MessageResponse;
import com.matchpet.infrastructure.adapters.input.web.dto.SendMessageRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static java.util.Objects.requireNonNull;

@RestController
@RequestMapping("/api/chats")
public class ChatController {

    private final SendMessageUseCase sendMessageUseCase;
    private final GetChatHistoryUseCase getChatHistoryUseCase;

    public ChatController(SendMessageUseCase sendMessageUseCase,
                          GetChatHistoryUseCase getChatHistoryUseCase) {
        this.sendMessageUseCase = sendMessageUseCase;
        this.getChatHistoryUseCase = getChatHistoryUseCase;
    }

    @PostMapping("/{requestId}/messages")
    public ResponseEntity<MessageResponse> sendMessage(@PathVariable String requestId,
                                                       @Valid @RequestBody SendMessageRequest request,
                                                       Authentication authentication) {
        requireNonNull(authentication, "Authentication is required");
        MessageResult result = sendMessageUseCase.sendMessage(
                new SendMessageCommand(requestId, authentication.getName(), request.content())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(result));
    }

    @GetMapping("/{requestId}/messages")
    public ResponseEntity<List<MessageResponse>> getHistory(@PathVariable String requestId,
                                                            @RequestParam int page,
                                                            @RequestParam int size,
                                                            Authentication authentication) {
        requireNonNull(authentication, "Authentication is required");
        List<MessageResponse> response = getChatHistoryUseCase.getChatHistory(
                        new GetChatHistoryQuery(requestId, authentication.getName(), page, size)
                ).stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    private MessageResponse toResponse(MessageResult result) {
        return new MessageResponse(result.id(), result.senderId(), result.content(), result.timestamp());
    }
}
