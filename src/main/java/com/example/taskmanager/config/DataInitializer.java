package com.example.taskmanager.config;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 1. Создаем дефолтного пользователя admin, если нет
        User admin = userRepository.findByUsername("admin")
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setUsername("admin");
                    newUser.setPassword(passwordEncoder.encode("12345"));
                    newUser.setRole("ROLE_ADMIN");
                    return userRepository.save(newUser);
                });

        // 2. Ищем задачи без пользователя (от старой версии) и отдаем их админу
        List<Task> orphanTasks = taskRepository.findAll().stream()
                .filter(task -> task.getUser() == null)
                .toList();

        if (!orphanTasks.isEmpty()) {
            orphanTasks.forEach(task -> task.setUser(admin));
            taskRepository.saveAll(orphanTasks);
            System.out.println("Migrated " + orphanTasks.size() + " tasks to user 'admin'");
        }
    }
}
