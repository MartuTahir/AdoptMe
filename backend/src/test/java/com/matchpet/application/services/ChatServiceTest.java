package com.matchpet.application.services;

import com.matchpet.application.ports.input.dto.GetChatHistoryQuery;
import com.matchpet.application.ports.input.dto.MessageResult;
import com.matchpet.application.ports.input.dto.SendMessageCommand;
import com.matchpet.application.ports.output.ChatPersistencePort;
import com.matchpet.domain.exception.AuthorizationException;
import com.matchpet.domain.exception.EntityNotFoundException;
import com.matchpet.domain.model.AdoptionRequestStatus;
import com.matchpet.domain.model.Message;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatServiceTest {

    private final ChatPersistencePort chatPersistencePort = mock(ChatPersistencePort.class);
    private final ChatService service = new ChatService(chatPersistencePort);

    @Test
    void shouldSendMessageWhenAdopterIsParticipantAndRequestAccepted() {
        when(chatPersistencePort.findRequestContext("ar-100")).thenReturn(Optional.of(
                new ChatPersistencePort.ChatRequestContext(
                        "ar-100",
                        AdoptionRequestStatus.ACCEPTED,
                        "u-adopter-1",
                        "u-shelter-1"
                )
        ));
        when(chatPersistencePort.saveMessage(eq("ar-100"), any(Message.class))).thenAnswer(invocation -> invocation.getArgument(1));

        MessageResult result = service.sendMessage(
                new SendMessageCommand("ar-100", "u-adopter-1", "Hola")
        );

        assertEquals("u-adopter-1", result.senderId());
        assertEquals("Hola", result.content());
        verify(chatPersistencePort).saveMessage(eq("ar-100"), any(Message.class));
    }

    @Test
    void shouldSendMessageWhenShelterIsParticipantAndRequestAccepted() {
        when(chatPersistencePort.findRequestContext("ar-100")).thenReturn(Optional.of(
                new ChatPersistencePort.ChatRequestContext(
                        "ar-100",
                        AdoptionRequestStatus.ACCEPTED,
                        "u-adopter-1",
                        "u-shelter-1"
                )
        ));
        when(chatPersistencePort.saveMessage(eq("ar-100"), any(Message.class))).thenAnswer(invocation -> invocation.getArgument(1));

        MessageResult result = service.sendMessage(
                new SendMessageCommand("ar-100", "u-shelter-1", "Está muy bien")
        );

        assertEquals("u-shelter-1", result.senderId());
        assertEquals("Está muy bien", result.content());
        verify(chatPersistencePort).saveMessage(eq("ar-100"), any(Message.class));
    }

    @Test
    void shouldRejectSendWhenAuthenticatedUserIsNotParticipant() {
        when(chatPersistencePort.findRequestContext("ar-100")).thenReturn(Optional.of(
                new ChatPersistencePort.ChatRequestContext(
                        "ar-100",
                        AdoptionRequestStatus.ACCEPTED,
                        "u-adopter-1",
                        "u-shelter-1"
                )
        ));

        assertThrows(AuthorizationException.class,
                () -> service.sendMessage(new SendMessageCommand("ar-100", "u-random-9", "Quiero sumarme")));
    }

    @Test
    void shouldRejectSendWhenRequestStatusIsNotAccepted() {
        when(chatPersistencePort.findRequestContext("ar-301")).thenReturn(Optional.of(
                new ChatPersistencePort.ChatRequestContext(
                        "ar-301",
                        AdoptionRequestStatus.PENDING,
                        "u-adopter-1",
                        "u-shelter-1"
                )
        ));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> service.sendMessage(new SendMessageCommand("ar-301", "u-adopter-1", "Hola")));
        assertEquals("Chat is allowed only for ACCEPTED requests", exception.getMessage());
    }

    @Test
    void shouldRejectMessageContentLongerThan1000Characters() {
        String content = "a".repeat(1001);

        assertThrows(IllegalArgumentException.class,
                () -> service.sendMessage(new SendMessageCommand("ar-100", "u-adopter-1", content)));
    }

    @Test
    void shouldRejectWhenRequestDoesNotExist() {
        when(chatPersistencePort.findRequestContext("ar-999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.sendMessage(new SendMessageCommand("ar-999", "u-adopter-1", "Hola")));
    }

    @Test
    void shouldReturnHistoryWhenParticipantIsAuthorizedAndRequestAccepted() {
        Instant now = Instant.parse("2026-05-24T10:25:00Z");
        when(chatPersistencePort.findRequestContext("ar-100")).thenReturn(Optional.of(
                new ChatPersistencePort.ChatRequestContext(
                        "ar-100",
                        AdoptionRequestStatus.ACCEPTED,
                        "u-adopter-1",
                        "u-shelter-1"
                )
        ));
        when(chatPersistencePort.getChatHistory("ar-100", 2, 2)).thenReturn(List.of(
                new Message("m4", "u-shelter-1", "m4", now.minusSeconds(600)),
                new Message("m3", "u-adopter-1", "m3", now.minusSeconds(900))
        ));

        List<MessageResult> history = service.getChatHistory(new GetChatHistoryQuery("ar-100", "u-adopter-1", 1, 2));

        assertEquals(2, history.size());
        assertEquals("m4", history.getFirst().content());
        assertEquals("m3", history.get(1).content());
    }

    @Test
    void shouldRejectHistoryWhenAuthenticatedUserIsNotParticipant() {
        when(chatPersistencePort.findRequestContext("ar-100")).thenReturn(Optional.of(
                new ChatPersistencePort.ChatRequestContext(
                        "ar-100",
                        AdoptionRequestStatus.ACCEPTED,
                        "u-adopter-1",
                        "u-shelter-1"
                )
        ));

        assertThrows(AuthorizationException.class,
                () -> service.getChatHistory(new GetChatHistoryQuery("ar-100", "u-random-9", 0, 20)));
    }

    @Test
    void shouldRejectHistoryWhenRequestDoesNotExist() {
        when(chatPersistencePort.findRequestContext("ar-999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.getChatHistory(new GetChatHistoryQuery("ar-999", "u-adopter-1", 0, 20)));
    }

    @ParameterizedTest
    @EnumSource(value = AdoptionRequestStatus.class, names = {"PENDING", "REJECTED"})
    void shouldRejectSendWhenRequestStatusIsNotAcceptedForAnyNonAcceptedStatus(AdoptionRequestStatus status) {
        when(chatPersistencePort.findRequestContext("ar-301")).thenReturn(Optional.of(
                new ChatPersistencePort.ChatRequestContext(
                        "ar-301",
                        status,
                        "u-adopter-1",
                        "u-shelter-1"
                )
        ));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> service.sendMessage(new SendMessageCommand("ar-301", "u-adopter-1", "Hola")));
        assertEquals("Chat is allowed only for ACCEPTED requests", exception.getMessage());
    }

    @ParameterizedTest
    @EnumSource(value = AdoptionRequestStatus.class, names = {"PENDING", "REJECTED"})
    void shouldRejectHistoryWhenRequestStatusIsNotAcceptedForAnyNonAcceptedStatus(AdoptionRequestStatus status) {
        when(chatPersistencePort.findRequestContext("ar-401")).thenReturn(Optional.of(
                new ChatPersistencePort.ChatRequestContext(
                        "ar-401",
                        status,
                        "u-adopter-1",
                        "u-shelter-1"
                )
        ));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> service.getChatHistory(new GetChatHistoryQuery("ar-401", "u-adopter-1", 0, 20)));
        assertEquals("Chat is allowed only for ACCEPTED requests", exception.getMessage());
    }

    @Test
    void shouldRejectHistoryRequestForInvalidPagination() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getChatHistory(new GetChatHistoryQuery("ar-100", "u-adopter-1", -1, 0)));
    }
}
