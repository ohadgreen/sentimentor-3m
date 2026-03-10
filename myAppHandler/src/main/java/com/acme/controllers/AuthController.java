package com.acme.controllers;

import com.acme.model.User;
import com.acme.repositories.UserRepository;
import com.acme.security.GoogleTokenVerifier;
import com.acme.security.JwtService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final GoogleTokenVerifier googleTokenVerifier;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthController(GoogleTokenVerifier googleTokenVerifier,
                          JwtService jwtService,
                          UserRepository userRepository) {
        this.googleTokenVerifier = googleTokenVerifier;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> body) {
        String idToken = body.get("idToken");
        if (idToken == null || idToken.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "idToken is required"));
        }

        GoogleIdToken.Payload payload = googleTokenVerifier.verify(idToken);
        if (payload == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid Google ID token"));
        }

        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String picture = (String) payload.get("picture");

        // Upsert user
        User user = userRepository.findByEmail(email).orElseGet(() -> new User(email, name, picture));
        user.setName(name);
        user.setPictureUrl(picture);
        userRepository.save(user);

        String appToken = jwtService.generateToken(email, name);
        return ResponseEntity.ok(Map.of("token", appToken));
    }
}
