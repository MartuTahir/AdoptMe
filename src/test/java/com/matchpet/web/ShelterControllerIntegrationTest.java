package com.matchpet.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matchpet.application.ports.input.RegisterShelterUseCase;
import com.matchpet.application.ports.input.dto.ShelterResult;
import com.matchpet.infrastructure.adapters.input.web.config.JwtTokenService;
import com.matchpet.infrastructure.adapters.input.web.config.SecurityConfig;
import com.matchpet.infrastructure.adapters.input.web.controllers.ShelterController;
import com.matchpet.infrastructure.adapters.input.web.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {ShelterController.class})
@Import({SecurityConfig.class, JwtTokenService.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = {
        "app.security.jwt.secret=test-secret-key-test-secret-key",
        "app.security.jwt.expiration-seconds=3600"
})
class ShelterControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @MockBean
    private RegisterShelterUseCase registerShelterUseCase;

    @Test
    void shouldAllowShelterRegistrationForRefugioRole() throws Exception {
        when(registerShelterUseCase.execute(any())).thenReturn(new ShelterResult(
                "s1",
                "Refugio Patitas",
                "Buenos Aires"
        ));

        String token = jwtTokenService.generateToken("refugio@matchpet.com", "REFUGIO");

        String payload = """
                {
                  "id": "s1",
                  "name": "Refugio Patitas",
                  "location": "Buenos Aires"
                }
                """;

        mockMvc.perform(post("/api/shelters")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("s1"))
                .andExpect(jsonPath("$.name").value("Refugio Patitas"))
                .andExpect(jsonPath("$.location").value("Buenos Aires"));
    }

    @Test
    void shouldRejectShelterRegistrationWhenNoToken() throws Exception {
        String payload = """
                {
                  "id": "s1",
                  "name": "Refugio Patitas",
                  "location": "Buenos Aires"
                }
                """;

        mockMvc.perform(post("/api/shelters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectShelterRegistrationForAdopterRole() throws Exception {
        String token = jwtTokenService.generateToken("adopter@matchpet.com", "ADOPTANTE");

        String payload = """
                {
                  "id": "s1",
                  "name": "Refugio Patitas",
                  "location": "Buenos Aires"
                }
                """;

        mockMvc.perform(post("/api/shelters")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectShelterRegistrationWithValidationErrors() throws Exception {
        String token = jwtTokenService.generateToken("refugio@matchpet.com", "REFUGIO");

        // Missing location and empty name
        String payload = """
                {
                  "id": "s1",
                  "name": ""
                }
                """;

        mockMvc.perform(post("/api/shelters")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }
}
