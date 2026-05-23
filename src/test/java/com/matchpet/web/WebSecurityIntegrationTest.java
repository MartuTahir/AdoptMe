package com.matchpet.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matchpet.application.ports.input.GetCompatibilityUseCase;
import com.matchpet.application.ports.input.RegisterPetUseCase;
import com.matchpet.application.ports.input.RegisterUserUseCase;
import com.matchpet.application.ports.input.SwipeUseCase;
import com.matchpet.application.ports.input.SubmitOnboardingFormUseCase;
import com.matchpet.application.ports.input.RegisterShelterUseCase;
import com.matchpet.application.ports.input.dto.GetCompatibilityResult;
import com.matchpet.application.ports.input.dto.PetResult;
import com.matchpet.application.ports.input.dto.SwipeResult;
import com.matchpet.application.ports.input.dto.UserResult;
import com.matchpet.domain.exception.EntityNotFoundException;
import com.matchpet.domain.model.Shelter;
import com.matchpet.domain.model.SwipeAction;
import com.matchpet.domain.model.Trait;
import com.matchpet.infrastructure.adapters.input.web.config.JwtTokenService;
import com.matchpet.infrastructure.adapters.input.web.config.SecurityConfig;
import com.matchpet.infrastructure.adapters.input.web.controllers.AuthController;
import com.matchpet.infrastructure.adapters.input.web.controllers.PetController;
import com.matchpet.infrastructure.adapters.input.web.controllers.RecommendationController;
import com.matchpet.infrastructure.adapters.input.web.controllers.SwipeController;
import com.matchpet.infrastructure.adapters.input.web.controllers.UserController;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        AuthController.class,
        UserController.class,
        PetController.class,
        SwipeController.class,
        RecommendationController.class,
        com.matchpet.infrastructure.adapters.input.web.controllers.ShelterController.class
})
@Import({SecurityConfig.class, JwtTokenService.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = {
        "app.security.jwt.secret=test-secret-key-test-secret-key",
        "app.security.jwt.expiration-seconds=3600"
})
class WebSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenService jwtTokenService;

    @MockBean
    private RegisterUserUseCase registerUserUseCase;

    @MockBean
    private SubmitOnboardingFormUseCase submitOnboardingFormUseCase;

    @MockBean
    private RegisterPetUseCase registerPetUseCase;

    @MockBean
    private SwipeUseCase swipeUseCase;

    @MockBean
    private GetCompatibilityUseCase getCompatibilityUseCase;

    @MockBean
    private RegisterShelterUseCase registerShelterUseCase;

    @Test
    void shouldAllowPublicLoginAndReturnJwt() throws Exception {
        String payload = """
                {
                  "username": "refugio@matchpet.com",
                  "password": "refugio123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void shouldAllowPublicAdopterRegistration() throws Exception {
        when(registerUserUseCase.execute(any())).thenReturn(new UserResult(
                "u1",
                "Martin",
                List.of(new Trait("t1", "Friendly"))
        ));

        String payload = """
                {
                  "id": "u1",
                  "name": "Martin",
                  "preferences": [{"id":"t1","name":"Friendly"}]
                }
                """;

        mockMvc.perform(post("/api/users/register/adopter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("u1"))
                .andExpect(jsonPath("$.name").value("Martin"));
    }

    @Test
    void shouldRejectPetRegistrationWhenNoToken() throws Exception {
        String payload = """
                {
                  "id": "p1",
                  "name": "Luna",
                  "shelter": {"id":"s1","name":"Refugio Norte","location":"CABA"},
                  "traits": [{"id":"t1","name":"Friendly"}]
                }
                """;

        mockMvc.perform(post("/api/pets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowPetRegistrationForRefugioRole() throws Exception {
        when(registerPetUseCase.execute(any())).thenReturn(new PetResult(
                "p1",
                "Luna",
                new Shelter("s1", "Refugio Norte", "CABA"),
                List.of(new Trait("t1", "Friendly"))
        ));

        String token = jwtTokenService.generateToken("refugio@matchpet.com", "REFUGIO");

        String payload = """
                {
                  "id": "p1",
                  "name": "Luna",
                  "shelter": {"id":"s1","name":"Refugio Norte","location":"CABA"},
                  "traits": [{"id":"t1","name":"Friendly"}]
                }
                """;

        mockMvc.perform(post("/api/pets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("p1"));
    }

    @Test
    void shouldRejectPetRegistrationForAdopterRole() throws Exception {
        String token = jwtTokenService.generateToken("adopter@matchpet.com", "ADOPTANTE");

        String payload = """
                {
                  "id": "p1",
                  "name": "Luna",
                  "shelter": {"id":"s1","name":"Refugio Norte","location":"CABA"},
                  "traits": [{"id":"t1","name":"Friendly"}]
                }
                """;

        mockMvc.perform(post("/api/pets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnNotFoundWhenShelterDoesNotExist() throws Exception {
        when(registerPetUseCase.execute(any()))
                .thenThrow(new EntityNotFoundException("Shelter not found: s404"));

        String token = jwtTokenService.generateToken("refugio@matchpet.com", "REFUGIO");

        String payload = """
                {
                  "id": "p1",
                  "name": "Luna",
                  "shelter": {"id":"s404","name":"Refugio Norte","location":"CABA"},
                  "traits": [{"id":"t1","name":"Friendly"}]
                }
                """;

        mockMvc.perform(post("/api/pets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Shelter not found: s404"));
    }

    @Test
    void shouldRequireAuthenticationForSwipeEndpoint() throws Exception {
        String payload = """
                {
                  "userId": "u1",
                  "petId": "p1",
                  "action": "LIKE"
                }
                """;

        mockMvc.perform(post("/api/swipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowSwipeWithJwt() throws Exception {
        when(swipeUseCase.execute(any())).thenReturn(new SwipeResult("u1", "p1", SwipeAction.LIKE, Instant.now()));
        String token = jwtTokenService.generateToken("adopter@matchpet.com", "ADOPTANTE");

        String payload = """
                {
                  "userId": "u1",
                  "petId": "p1",
                  "action": "LIKE"
                }
                """;

        mockMvc.perform(post("/api/swipes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action").value("LIKE"));
    }

    @Test
    void shouldReturnRecommendationWhenAuthenticated() throws Exception {
        when(getCompatibilityUseCase.execute(eq("u1"), eq("p1"))).thenReturn(
                new GetCompatibilityResult(1, List.of(new Trait("t1", "Friendly")))
        );

        String token = jwtTokenService.generateToken("adopter@matchpet.com", "ADOPTANTE");

        mockMvc.perform(get("/api/recommendations")
                        .header("Authorization", "Bearer " + token)
                        .param("userId", "u1")
                        .param("petId", "p1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(1));
    }

    @Test
    void shouldMapDomainNotFoundTo404() throws Exception {
        when(getCompatibilityUseCase.execute(eq("u404"), eq("p1")))
                .thenThrow(new EntityNotFoundException("User not found: u404"));

        String token = jwtTokenService.generateToken("adopter@matchpet.com", "ADOPTANTE");

        mockMvc.perform(get("/api/recommendations")
                        .header("Authorization", "Bearer " + token)
                        .param("userId", "u404")
                        .param("petId", "p1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found: u404"));
    }

    @Test
    void shouldAllowOnboardingForAuthenticatedAdopter() throws Exception {
        when(submitOnboardingFormUseCase.execute(any())).thenReturn(new UserResult(
                "u1",
                "Martin",
                100,
                List.of()
        ));

        String token = jwtTokenService.generateToken("adopter@matchpet.com", "ADOPTANTE");

        String payload = """
                {
                  "userId": "u1",
                  "housingType": "PATIO_CERRADO",
                  "availableHours": 4,
                  "hasPreviousExperience": true,
                  "acceptsControlVisits": true
                }
                """;

        mockMvc.perform(post("/api/users/onboarding")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("u1"))
                .andExpect(jsonPath("$.trustScore").value(100));
    }

    @Test
    void shouldRejectOnboardingWhenNoToken() throws Exception {
        String payload = """
                {
                  "userId": "u1",
                  "housingType": "PATIO_CERRADO",
                  "availableHours": 4,
                  "hasPreviousExperience": true,
                  "acceptsControlVisits": true
                }
                """;

        mockMvc.perform(post("/api/users/onboarding")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectOnboardingWithValidationErrors() throws Exception {
        String token = jwtTokenService.generateToken("adopter@matchpet.com", "ADOPTANTE");

        // Missing availableHours and acceptsControlVisits
        String payload = """
                {
                  "userId": "u1",
                  "housingType": "PATIO_CERRADO",
                  "hasPreviousExperience": true
                }
                """;

        mockMvc.perform(post("/api/users/onboarding")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }
}
