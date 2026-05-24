package com.matchpet.infrastructure.adapters.input.web.controllers;

import com.matchpet.infrastructure.adapters.input.web.config.JwtTokenService;
import com.matchpet.infrastructure.adapters.input.web.dto.AuthLoginRequest;
import com.matchpet.infrastructure.adapters.input.web.dto.AuthLoginResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenService jwtTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthLoginResponse> login(@Valid @RequestBody AuthLoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );

            String role = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("ROLE_ADOPTANTE")
                    .replace("ROLE_", "");

            String token = jwtTokenService.generateToken(authentication.getName(), role);
            return ResponseEntity.ok(new AuthLoginResponse(token, "Bearer"));
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Invalid credentials");
        }
    }
}
