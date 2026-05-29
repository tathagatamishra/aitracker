package com.example.aitracker.auth;

import com.example.aitracker.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final com.example.aitracker.user.UserService userService;

    public AuthController(AuthService authService, com.example.aitracker.user.UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        User createdUser = userService.registerUser(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", createdUser.getId(),
                "name", createdUser.getName(),
                "email", createdUser.getEmail(),
                "createdAt", createdUser.getCreatedAt()
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        User user = authService.authenticate(request);

        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "message", "Login successful"
        ));
    }
}