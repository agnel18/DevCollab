package com.agnel.devcollab.config;

import com.agnel.devcollab.entity.Board;
import com.agnel.devcollab.entity.User;
import com.agnel.devcollab.repository.BoardRepository;
import com.agnel.devcollab.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initUsers(UserRepository userRepository, BoardRepository boardRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Seed a convenient test user
            String email = "test@example.com";
            User user = null;
            if (!userRepository.existsByEmail(email)) {
                User u = new User();
                u.setEmail(email);
                u.setName("Test User");
                u.setPassword(passwordEncoder.encode("password"));
                u.setRole("USER");
                user = userRepository.save(u);
            } else {
                user = userRepository.findByEmail(email).get();
            }

            // Create a default board for the test user if they don't have one
            if (user != null && boardRepository.findByOwnerId(user.getId()).isEmpty()) {
                Board defaultBoard = new Board("My First Board", user);
                boardRepository.save(defaultBoard);
                System.out.println("Created default board for test user");
            }
        };
    }
}
