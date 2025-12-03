package com.agnel.devcollab.config;

import com.agnel.devcollab.entity.User;
import com.agnel.devcollab.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Seed a convenient test user
            String email = "test@example.com";
            if (!userRepository.existsByEmail(email)) {
                User u = new User();
                u.setEmail(email);
                u.setName("Test User");
                u.setPassword(passwordEncoder.encode("password"));
                u.setRole("USER");
                userRepository.save(u);
            }
        };
    }
}
