package com.example.taskmanager.service;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления задачами.
 * Содержит бизнес-логику для работы с задачами текущего пользователя.
 */
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    /**
     * Получает текущего авторизованного пользователя из контекста безопасности.
     *
     * @return Текущий пользователь
     * @throws RuntimeException если пользователь не найден
     */
    private com.example.taskmanager.model.User getCurrentUser() {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Получает список всех задач текущего пользователя.
     *
     * @return Список задач
     */
    public List<Task> getAllTasks() {
        return taskRepository.findByUser(getCurrentUser());
    }

    /**
     * Находит задачу по её идентификатору.
     *
     * @param id Идентификатор задачи
     * @return Optional с задачей, если она найдена
     */
    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    /**
     * Создает новую задачу для текущего пользователя.
     * Если статус не указан, устанавливается PENDING по умолчанию.
     *
     * @param task Объект задачи для создания
     * @return Сохраненная задача
     */
    public Task createTask(Task task) {
        task.setUser(getCurrentUser());
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.PENDING);
        }
        return taskRepository.save(task);
    }

    /**
     * Обновляет существующую задачу.
     *
     * @param id          Идентификатор обновляемой задачи
     * @param taskDetails Объект с новыми данными
     * @return Обновленная задача
     * @throws RuntimeException если задача с указанным id не найдена
     */
    public Task updateTask(Long id, Task taskDetails) {
        return taskRepository.findById(id).map(task -> {
            task.setTitle(taskDetails.getTitle());
            task.setDescription(taskDetails.getDescription());
            task.setDueDate(taskDetails.getDueDate());
            if (taskDetails.getStatus() != null) {
                task.setStatus(taskDetails.getStatus());
            }
            return taskRepository.save(task);
        }).orElseThrow(() -> new RuntimeException("Task not found with id " + id));
    }

    /**
     * Удаляет задачу по идентификатору.
     *
     * @param id Идентификатор задачи
     */
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    /**
     * Обновляет порядок задач (для Drag & Drop).
     *
     * @param taskIds Список ID задач в новом порядке
     */
    public void updateTaskOrder(List<Long> taskIds) {
        for (int i = 0; i < taskIds.size(); i++) {
            Long taskId = taskIds.get(i);
            Optional<Task> taskOpt = taskRepository.findById(taskId);
            if (taskOpt.isPresent()) {
                Task task = taskOpt.get();
                task.setOrderIndex(i);
                taskRepository.save(task);
            }
        }
    }
}
