package com.example.taskmanager.controller;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST контроллер для управления задачами.
 * Предоставляет API для создания, чтения, обновления и удаления задач.
 * Все эндпоинты защищены и работают в контексте текущего пользователя.
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Получает список всех задач текущего пользователя.
     *
     * @return Список задач
     */
    @GetMapping
    public List<Task> getAllTasks() {
        return taskService.getAllTasks();
    }

    /**
     * Получает задачу по её ID.
     *
     * @param id Идентификатор задачи
     * @return 200 OK с задачей или 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Создает новую задачу.
     *
     * @param task Данные новой задачи
     * @return Созданная задача
     */
    @PostMapping
    public Task createTask(@jakarta.validation.Valid @RequestBody Task task) {
        return taskService.createTask(task);
    }

    /**
     * Обновляет существующую задачу.
     *
     * @param id          Идентификатор задачи
     * @param taskDetails Новые данные задачи
     * @return 200 OK с обновленной задачей или 404 Not Found
     */
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id,
            @jakarta.validation.Valid @RequestBody Task taskDetails) {
        try {
            Task updatedTask = taskService.updateTask(id, taskDetails);
            return ResponseEntity.ok(updatedTask);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Удаляет задачу.
     *
     * @param id Идентификатор задачи
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Обновляет порядок задач (для Drag & Drop).
     * Принимает список ID задач в новом порядке.
     *
     * @param taskIds Список ID задач
     * @return 200 OK
     */
    @PutMapping("/reorder")
    public ResponseEntity<Void> reorderTasks(@RequestBody List<Long> taskIds) {
        taskService.updateTaskOrder(taskIds);
        return ResponseEntity.ok().build();
    }
}
