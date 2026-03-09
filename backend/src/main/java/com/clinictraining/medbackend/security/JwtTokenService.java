package com.clinictraining.medbackend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenService {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenService.class);
    private static final String HMAC = "HmacSHA256";

    private final ObjectMapper objectMapper;
    private final byte[] secret;

    public JwtTokenService(ObjectMapper objectMapper,
                           @Value("${security.jwt.secret:local-dev-secret}") String secret) {
        this.objectMapper = objectMapper;
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    public String generateToken(String username) {
        try {
            Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
            Map<String, Object> payload = new HashMap<>();
            payload.put("sub", username);
            payload.put("iat", Instant.now().getEpochSecond());
            payload.put("scope", "USER");

            String headerEncoded = base64(header);
            String payloadEncoded = base64(payload);
            String signature = sign(headerEncoded + "." + payloadEncoded);
            return headerEncoded + "." + payloadEncoded + "." + signature;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to generate token", ex);
        }
    }

    public Optional<String> extractUsername(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return Optional.empty();
            }
            if (!signatureMatches(parts[0], parts[1], parts[2])) {
                return Optional.empty();
            }
            byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = objectMapper.readValue(payloadBytes, Map.class);
            return Optional.ofNullable((String) payload.get("sub"));
        } catch (Exception ex) {
            log.warn("Failed to parse token", ex);
            return Optional.empty();
        }
    }

    private String base64(Map<String, Object> map) throws Exception {
        byte[] bytes = objectMapper.writeValueAsBytes(map);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sign(String data) throws Exception {
        Mac mac = Mac.getInstance(HMAC);
        mac.init(new SecretKeySpec(secret, HMAC));
        byte[] signature = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
    }

    private boolean signatureMatches(String header, String payload, String providedSignature) {
        try {
            String expected = sign(header + "." + payload);
            return expected.equals(providedSignature);
        } catch (Exception ex) {
            log.debug("Token signature validation failed", ex);
            return false;
        }
    }
}
