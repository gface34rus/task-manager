package com.example.taskmanager.service;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    private com.example.taskmanager.model.User getCurrentUser() {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<Task> getAllTasks() {
        return taskRepository.findByUser(getCurrentUser());
    }

    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    public Task createTask(Task task) {
        task.setUser(getCurrentUser());
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.PENDING);
        }
        return taskRepository.save(task);
    }

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

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

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
