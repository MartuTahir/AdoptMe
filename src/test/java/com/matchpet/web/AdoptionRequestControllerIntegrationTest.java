package com.matchpet.web;

import com.matchpet.application.ports.input.AcceptAdoptionRequestUseCase;
import com.matchpet.application.ports.input.RejectAdoptionRequestUseCase;
import com.matchpet.infrastructure.adapters.input.web.config.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdoptionRequestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AcceptAdoptionRequestUseCase acceptUseCase;

    @MockBean
    private RejectAdoptionRequestUseCase rejectUseCase;

    @MockBean
    private JwtTokenService jwtTokenService; // Prevent real JWT validation issues in full boot test

    @Test
    @WithMockUser(roles = "REFUGIO")
    void shouldAcceptAdoptionRequestWhenUserHasRefugioRole() throws Exception {
        String json = """
                {
                    "requestId": "u1:p1"
                }
                """;

        mockMvc.perform(post("/api/adoption-requests/accept")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADOPTANTE")
    void shouldDenyAcceptanceWhenUserIsAdoptante() throws Exception {
        String json = """
                {
                    "requestId": "u1:p1"
                }
                """;

        mockMvc.perform(post("/api/adoption-requests/accept")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "REFUGIO")
    void shouldRejectAdoptionRequestWhenUserHasRefugioRole() throws Exception {
        String json = """
                {
                    "requestId": "u1:p1"
                }
                """;

        mockMvc.perform(post("/api/adoption-requests/reject")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());
    }
}
