package com.clinictraining.medbackend.web;

import com.clinictraining.medbackend.security.JwtTokenService;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtTokenService tokenService;

    public AuthController(JwtTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping("/token")
    public ResponseEntity<Map<String, String>> token(@RequestBody Map<String, String> payload) {
        String username = payload.getOrDefault("username", "demo-user");
        String token = tokenService.generateToken(username);
        return ResponseEntity.ok(Map.of("token", token, "user", username));
    }
}
