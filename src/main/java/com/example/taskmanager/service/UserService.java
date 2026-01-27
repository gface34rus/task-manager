package com.example.taskmanager.service;

import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Сервис для управления пользователями и аутентификации.
 * Реализует интерфейс UserDetailsService для интеграции со Spring Security.
 */
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Регистрирует нового пользователя.
     * Пароль хешируется перед сохранением.
     *
     * @param user Объект пользователя с открытым паролем
     * @return Сохраненный пользователь с хешированным паролем
     */
    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    /**
     * Ищет пользователя по имени (логину).
     *
     * @param username Имя пользователя
     * @return Пользователь
     * @throws UsernameNotFoundException если пользователь не найден
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    /**
     * Загружает данные пользователя для Spring Security.
     *
     * @param username Имя пользователя
     * @return UserDetails объект
     * @throws UsernameNotFoundException если пользователь не найден
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findByUsername(username);
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.emptyList());
    }

    /**
     * Обновляет пароль пользователя.
     *
     * @param username    Имя пользователя
     * @param newPassword Новый открытый пароль
     */
    public void updateUserPassword(String username, String newPassword) {
        User user = findByUsername(username);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Обновляет имя пользователя.
     * Проверяет, не занято ли новое имя.
     *
     * @param oldUsername Текущее имя пользователя
     * @param newUsername Новое имя пользователя
     * @throws RuntimeException если новое имя уже занято
     */
    public void updateUsername(String oldUsername, String newUsername) {
        if (userRepository.findByUsername(newUsername).isPresent()) {
            throw new RuntimeException("Username already taken");
        }
        User user = findByUsername(oldUsername);
        user.setUsername(newUsername);
        userRepository.save(user);
    }
}
