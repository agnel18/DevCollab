package com.agnel.devcollab;

import com.agnel.devcollab.entity.User;
import com.agnel.devcollab.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class DataInitializer {
    // Not a Spring configuration; kept only to avoid bean name conflicts
    public CommandLineRunner seedTestUser(UserRepository userRepository) {
        return args -> {
            String email = "test@devcollab.com";
            if (userRepository.findByEmail(email).isEmpty()) {
                PasswordEncoder encoder = new BCryptPasswordEncoder();
                User user = new User();
                user.setName("Test User");
                user.setEmail(email);
                user.setPassword(encoder.encode("password"));
                user.setRole("USER");
                userRepository.save(user);
                System.out.println("Seeded test user: " + email);
            }
        };
    }
}
