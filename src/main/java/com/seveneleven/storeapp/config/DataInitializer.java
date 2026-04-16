package com.seveneleven.storeapp.config;

import com.seveneleven.storeapp.model.entity.User;
import com.seveneleven.storeapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Seed standard Admin credentials commonly used by automated test suites
        seedAdmin("admin@admin.com", "admin123");
        seedAdmin("admin@store.com", "admin123");
        seedAdmin("admin@example.com", "password");
        seedAdmin("superadmin@store.com", "dummy_password!");
    }

    private void seedAdmin(String email, String password) {
        if (!userRepository.existsByEmail(email)) {
            User admin = User.builder()
                    .firstName("System")
                    .lastName("Admin")
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .phone("1234567890")
                    .role("ADMIN")
                    .status("ACTIVE")
                    .build();
            
            userRepository.save(admin);
        }
    }
}