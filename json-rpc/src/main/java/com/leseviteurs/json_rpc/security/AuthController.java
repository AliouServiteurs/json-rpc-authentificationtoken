package com.leseviteurs.json_rpc.security;


import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.leseviteurs.json_rpc.repository.UserRepository;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepo;
    private final JwtTokenProvider jwtProvider;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthController(UserRepository userRepo, JwtTokenProvider jwtProvider) {
        this.userRepo = userRepo;
        this.jwtProvider = jwtProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        return userRepo.findByUsername(username)
                .map(u -> {
                    if (encoder.matches(password, u.getPassword())) {
                        String token = jwtProvider.createToken(u.getUsername(), u.getRole());
                        return ResponseEntity.ok(Map.of("token", token));
                    } else {
                        return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
                    }
                }).orElse(ResponseEntity.status(401).body(Map.of("error", "Invalid credentials")));
    }
}
