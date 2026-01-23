package com.example.taskmanager.controller;

import com.example.taskmanager.model.User;
import com.example.taskmanager.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            userService.registerUser(user);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/user")
    public ResponseEntity<?> getCurrentUser(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(Map.of("username", principal.getName()));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> payload, Principal principal) {
        if (principal == null)
            return ResponseEntity.status(401).build();

        String currentUsername = principal.getName();
        boolean updated = false;

        String newPassword = payload.get("password");
        if (newPassword != null && !newPassword.isBlank()) {
            userService.updateUserPassword(currentUsername, newPassword);
            updated = true;
        }

        String newUsername = payload.get("username");
        if (newUsername != null && !newUsername.isBlank() && !newUsername.equals(currentUsername)) {
            try {
                userService.updateUsername(currentUsername, newUsername);
                // If username changed, we return specific message to trigger logout
                return ResponseEntity.ok("Username updated");
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }

        if (updated)
            return ResponseEntity.ok("Password updated");
        return ResponseEntity.badRequest().body("Nothing to update");
    }
}
