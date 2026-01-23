package com.example.taskmanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Упрощаем для начала
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/auth/**", "/h2-console/**").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login.html") // Мы создадим эту страницу
                        .loginProcessingUrl("/login") // Spring Security обработает POST сюда
                        .defaultSuccessUrl("/", true)
                        .permitAll())
                .logout(logout -> logout
                        .permitAll())
                .headers(headers -> headers.frameOptions().disable()); // Для H2 консоли

        return http.build();
    }
}
