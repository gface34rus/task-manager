package com.example.taskmanager.service;

import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void registerUser_ShouldEncodePasswordAndSave() {
        User user = new User();
        user.setUsername("newuser");
        user.setPassword("password");

        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(user)).thenReturn(user);

        userService.registerUser(user);

        verify(passwordEncoder).encode("password");
        verify(userRepository).save(user);
    }

    @Test
    void updateUsername_Success() {
        String oldName = "old";
        String newName = "new";
        User user = new User();
        user.setUsername(oldName);

        when(userRepository.findByUsername(newName)).thenReturn(Optional.empty());
        when(userRepository.findByUsername(oldName)).thenReturn(Optional.of(user));

        userService.updateUsername(oldName, newName);

        assertEquals(newName, user.getUsername());
        verify(userRepository).save(user);
    }

    @Test
    void updateUsername_WhateverTaken_ShouldThrow() {
        when(userRepository.findByUsername("takenName")).thenReturn(Optional.of(new User()));

        assertThrows(RuntimeException.class, () -> userService.updateUsername("me", "takenName"));
    }

    @Test
    void updateUserPassword_ShouldEncodeAndSave() {
        String username = "user";
        User user = new User();
        user.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPass");

        userService.updateUserPassword(username, "newPass");

        verify(passwordEncoder).encode("newPass");
        verify(userRepository).save(user);
    }
}
