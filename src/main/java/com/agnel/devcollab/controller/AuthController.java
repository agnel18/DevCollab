package com.agnel.devcollab.controller;

import com.agnel.devcollab.entity.User;
import com.agnel.devcollab.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute User user, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "auth/register";
        }
        
        // Auto-create user if they don't exist
        if (!userRepository.existsByEmail(user.getEmail())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);
            return "redirect:/login?registered";
        }
        
        // User already exists - just redirect to login
        return "redirect:/login?exists";
    }

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(required = false) String registered,
                                @RequestParam(required = false) String exists,
                                Model model) {
        if (registered != null) {
            model.addAttribute("success", "Registered! Please login.");
        }
        if (exists != null) {
            model.addAttribute("info", "Email already registered. Please login.");
        }
        return "auth/login";
    }
}