package com.abdullah.todo.config;

import com.abdullah.todo.entity.User;
import com.abdullah.todo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

/**
 * Data initializer for creating default users.
 * Only runs in dev profile.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
@Profile("dev")
public class DataInitializer {

    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initUsers(UserRepository userRepository) {
        return args -> {
            // Create default USER if not exists
            if (!userRepository.existsByUsername("user")) {
                User user = new User(
                        "user",
                        passwordEncoder.encode("password"),
                        Set.of("ROLE_USER"));
                userRepository.save(user);
                log.info("Created default user: user/password");
            }

            // Create default ADMIN if not exists
            if (!userRepository.existsByUsername("admin")) {
                User admin = new User(
                        "admin",
                        passwordEncoder.encode("admin123"),
                        Set.of("ROLE_USER", "ROLE_ADMIN"));
                userRepository.save(admin);
                log.info("Created default admin: admin/admin123");
            }

            log.info("===========================================");
            log.info("Default Users (dev mode only):");
            log.info("  User:  user / password  (ROLE_USER)");
            log.info("  Admin: admin / admin123 (ROLE_USER, ROLE_ADMIN)");
            log.info("===========================================");
        };
    }
}
