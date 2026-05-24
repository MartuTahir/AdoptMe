package com.matchpet.infrastructure.adapters.input.web.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Component
public class JwtTokenService {

    private static final String HMAC_ALG = "HmacSHA256";
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    private final ObjectMapper objectMapper;
    private final String secret;
    private final long expirationSeconds;

    public JwtTokenService(ObjectMapper objectMapper,
                           @Value("${app.security.jwt.secret:dev-secret-key-32-chars-minimum}") String secret,
                           @Value("${app.security.jwt.expiration-seconds:3600}") long expirationSeconds) {
        this.objectMapper = objectMapper;
        this.secret = secret;
        this.expirationSeconds = expirationSeconds;
    }

    public String generateToken(String username, String role) {
        long exp = Instant.now().plusSeconds(expirationSeconds).getEpochSecond();

        String headerJson = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(Map.of(
                    "sub", username,
                    "role", role,
                    "exp", exp
            ));
        } catch (Exception e) {
            throw new IllegalStateException("Could not serialize JWT payload", e);
        }

        String header = URL_ENCODER.encodeToString(headerJson.getBytes(StandardCharsets.UTF_8));
        String payload = URL_ENCODER.encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        String signature = sign(header + "." + payload);

        return header + "." + payload + "." + signature;
    }

    public boolean isTokenValid(String token) {
        try {
            DecodedToken decoded = decode(token);
            String expectedSignature = sign(decoded.header() + "." + decoded.payload());
            if (!expectedSignature.equals(decoded.signature())) {
                return false;
            }

            long exp = ((Number) decoded.claims().get("exp")).longValue();
            return Instant.now().getEpochSecond() < exp;
        } catch (Exception ignored) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return (String) decode(token).claims().get("sub");
    }

    public String extractRole(String token) {
        return (String) decode(token).claims().get("role");
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALG);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALG));
            return URL_ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Could not sign JWT token", e);
        }
    }

    private DecodedToken decode(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid token format");
            }

            String payloadJson = new String(URL_DECODER.decode(parts[1]), StandardCharsets.UTF_8);
            Map<String, Object> claims = objectMapper.readValue(payloadJson, new TypeReference<>() {
            });

            return new DecodedToken(parts[0], parts[1], parts[2], claims);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid token", e);
        }
    }

    private record DecodedToken(String header, String payload, String signature, Map<String, Object> claims) {
    }
}
