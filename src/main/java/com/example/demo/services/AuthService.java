package com.example.demo.services;

import com.example.demo.entities.Users;
import com.example.demo.repositories.UsersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class AuthService {

    private final UsersRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    public AuthService(UsersRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public void register(Users user) {
        logger.info("Registering user: {}", user.getEmail());

        // Validation des entrées
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty.");
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty.");
        }

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already taken.");
        }

        logger.info("Assigning roles for user: {}", user.getRole());

        // Encodage et sauvegarde
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        try {
            userRepository.save(user);
            logger.info("User {} registered successfully", user.getEmail());
        } catch (Exception e) {
            logger.error("Failed to register user: {}", user.getEmail(), e);
            throw new RuntimeException("Registration failed");
        }
    }



    public String login(Users user) {
        System.out.println(user);

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        user.getPassword()
                )
        );
        Users loggedInUser = userRepository.findByEmail(user.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return jwtService.generateToken(loggedInUser);
    }
}