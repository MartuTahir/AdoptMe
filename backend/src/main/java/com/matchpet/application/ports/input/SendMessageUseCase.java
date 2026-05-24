package com.matchpet.application.ports.input;

import com.matchpet.application.ports.input.dto.MessageResult;
import com.matchpet.application.ports.input.dto.SendMessageCommand;

/**
 * Sends a chat message linked to an adoption request.
 */
public interface SendMessageUseCase {

    MessageResult sendMessage(SendMessageCommand command);
}
