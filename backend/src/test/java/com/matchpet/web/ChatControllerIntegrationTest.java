package com.matchpet.web;

import com.matchpet.application.ports.input.GetChatHistoryUseCase;
import com.matchpet.application.ports.input.SendMessageUseCase;
import com.matchpet.application.ports.input.dto.MessageResult;
import com.matchpet.domain.exception.AuthorizationException;
import com.matchpet.domain.exception.EntityNotFoundException;
import com.matchpet.infrastructure.adapters.input.web.config.JwtAuthenticationFilter;
import com.matchpet.infrastructure.adapters.input.web.config.JwtTokenService;
import com.matchpet.infrastructure.adapters.input.web.config.SecurityConfig;
import com.matchpet.infrastructure.adapters.input.web.controllers.ChatController;
import com.matchpet.infrastructure.adapters.input.web.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ChatController.class)
@Import({SecurityConfig.class, JwtTokenService.class, JwtAuthenticationFilter.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = {
        "app.security.jwt.secret=test-secret-key-test-secret-key",
        "app.security.jwt.expiration-seconds=3600"
})
class ChatControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @MockBean
    private SendMessageUseCase sendMessageUseCase;

    @MockBean
    private GetChatHistoryUseCase getChatHistoryUseCase;

    @Test
    void shouldCreateMessageWhenRequestIsValid() throws Exception {
        when(sendMessageUseCase.sendMessage(any())).thenReturn(
                new MessageResult("m-1", "adopter@matchpet.com", "Hola", Instant.parse("2026-05-24T10:00:00Z"))
        );

        String token = jwtTokenService.generateToken("adopter@matchpet.com", "ADOPTANTE");

        mockMvc.perform(post("/api/chats/ar-100/messages")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "Hola"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("m-1"))
                .andExpect(jsonPath("$.content").value("Hola"));
    }

    @Test
    void shouldReturnHistoryWhenRequestIsValid() throws Exception {
        when(getChatHistoryUseCase.getChatHistory(any())).thenReturn(List.of(
                new MessageResult("m6", "u-shelter-1", "m6", Instant.parse("2026-05-24T10:25:00Z")),
                new MessageResult("m5", "u-adopter-1", "m5", Instant.parse("2026-05-24T10:20:00Z"))
        ));

        String token = jwtTokenService.generateToken("adopter@matchpet.com", "ADOPTANTE");

        mockMvc.perform(get("/api/chats/ar-100/messages")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("m6"))
                .andExpect(jsonPath("$[1].id").value("m5"));
    }

    @Test
    void shouldReturnForbiddenWhenUserIsNotParticipant() throws Exception {
        when(sendMessageUseCase.sendMessage(any())).thenThrow(new AuthorizationException("User is not a participant of this chat"));

        String token = jwtTokenService.generateToken("adopter@matchpet.com", "ADOPTANTE");

        mockMvc.perform(post("/api/chats/ar-200/messages")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "¿Hay novedades?"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnNotFoundWhenRequestIdDoesNotExist() throws Exception {
        when(getChatHistoryUseCase.getChatHistory(any())).thenThrow(new EntityNotFoundException("Adoption request not found: ar-999"));

        String token = jwtTokenService.generateToken("adopter@matchpet.com", "ADOPTANTE");

        mockMvc.perform(get("/api/chats/ar-999/messages")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnUnauthorizedWhenNoTokenProvided() throws Exception {
        mockMvc.perform(get("/api/chats/ar-100/messages")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isUnauthorized());
    }
}
