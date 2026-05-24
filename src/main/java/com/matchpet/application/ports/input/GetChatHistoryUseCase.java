package com.matchpet.application.ports.input;

import com.matchpet.application.ports.input.dto.GetChatHistoryQuery;
import com.matchpet.application.ports.input.dto.MessageResult;

import java.util.List;

/**
 * Retrieves paginated chat history for a specific adoption request.
 */
public interface GetChatHistoryUseCase {

    List<MessageResult> getChatHistory(GetChatHistoryQuery query);
}
