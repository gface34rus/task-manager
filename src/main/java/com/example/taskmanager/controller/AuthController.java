package com.example.taskmanager.controller;

import com.example.taskmanager.model.User;
import com.example.taskmanager.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.Map;

/**
 * REST контроллер для аутентификации и управления профилем пользователя.
 * Предоставляет API для регистрации и работы с данными пользователя.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * Регистрирует нового пользователя.
     *
     * @param user Данные пользователя (username, password)
     * @return 200 OK при успехе или 400 Bad Request при ошибке
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            userService.registerUser(user);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Получает информацию о текущем авторизованном пользователе.
     *
     * @param principal Объект Principal (автоматически внедряется Spring Security)
     * @return Имя пользователя или 401 Unauthorized
     */
    @GetMapping("/user")
    public ResponseEntity<?> getCurrentUser(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(Map.of("username", principal.getName()));
    }

    /**
     * Обновляет профиль пользователя (имя или пароль).
     *
     * @param payload   Map с новыми данными (username, password)
     * @param principal Текущий пользователь
     * @return Результат обновления
     */
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
                // Если имя изменилось, возвращаем сообщение для повторного входа
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
