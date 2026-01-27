package com.example.taskmanager.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Сущность пользователя системы.
 * Хранит учетные данные и роль пользователя.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {

    /**
     * Уникальный идентификатор пользователя.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Имя пользователя (логин).
     * Должно быть уникальным.
     */
    @Column(unique = true, nullable = false)
    private String username;

    /**
     * Зашифрованный пароль пользователя.
     */
    @Column(nullable = false)
    private String password;

    /**
     * Роль пользователя в системе (по умолчанию ROLE_USER).
     */
    private String role = "ROLE_USER";
}
