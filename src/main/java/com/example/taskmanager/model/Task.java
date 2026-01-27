package com.example.taskmanager.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Сущность задачи.
 * Представляет собой задачу, которую пользователь может создать, редактировать
 * и отслеживать.
 */
@Entity
@Data
@NoArgsConstructor
@Table(name = "tasks")
public class Task {

    /**
     * Уникальный идентификатор задачи.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Заголовок задачи.
     * Обязательное поле.
     */
    @jakarta.validation.constraints.NotBlank(message = "Title is required")
    @Column(nullable = false)
    private String title;

    /**
     * Описание задачи.
     * Может содержать детальную информацию о том, что нужно сделать.
     */
    private String description;

    /**
     * Статус выполнения задачи (например, PENDING, IN_PROGRESS, COMPLETED).
     */
    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    /**
     * Срок выполнения задачи.
     */
    private java.time.LocalDate dueDate;

    /**
     * Индекс сортировки для Drag & Drop.
     * Определяет порядок отображения задачи в списке.
     */
    @Column(name = "order_index")
    private Integer orderIndex = 0;

    /**
     * Пользователь, которому принадлежит задача.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Дата и время создания задачи.
     * Заполняется автоматически при сохранении.
     */
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /**
     * Конструктор для создания новой задачи.
     *
     * @param title       Заголовок задачи
     * @param description Описание задачи
     * @param status      Начальный статус
     */
    public Task(String title, String description, TaskStatus status) {
        this.title = title;
        this.description = description;
        this.status = status;
    }
}
