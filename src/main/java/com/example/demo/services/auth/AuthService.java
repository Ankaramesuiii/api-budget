package com.example.demo.services.auth;

import com.example.demo.entities.Users;
import com.example.demo.exceptions.InvalidInputException;
import com.example.demo.exceptions.LoginFailedException;
import com.example.demo.exceptions.UserAlreadyExistsException;
import com.example.demo.repositories.ManagerRepository;
import com.example.demo.repositories.SuperManagerRepository;
import com.example.demo.repositories.TeamMemberRepository;
import com.example.demo.repositories.UsersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
@Service
public class AuthService {

    private final UsersRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    public AuthService(UsersRepository userRepository, ManagerRepository managerRepository, SuperManagerRepository superManagerRepository, TeamMemberRepository teamMemberRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public void register(Users user) {
        logger.info("Registering user: {}", user.getEmail());

        // Validation des champs
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new InvalidInputException("Email cannot be null or empty.");
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new InvalidInputException("Password cannot be null or empty.");
        }

        // Vérifier si l'email existe déjà
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already taken.");
        }

        logger.info("Assigning roles for user: {}", user.getRole());

        // Encoder le mot de passe
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Sauvegarder dans la bonne table selon le rôle
        try {
            userRepository.save(user);
            logger.info("User {} registered successfully", user.getEmail());
        } catch (Exception e) {
            logger.error("Failed to register user: {}", user.getEmail(), e);
            throw new RuntimeException("Registration failed due to an unexpected error.");
        }
    }


    public String login(Users user) {
        logger.info("Attempting login for user: {}", user.getUsername());

        try {
            // Authenticate user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getUsername(),
                            user.getPassword()
                    )
            );

            // Fetch user details
            Users loggedInUser = userRepository.findByEmail(user.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found."));

            System.out.println("Logged in user: " + loggedInUser);
            // Generate JWT token
            return jwtService.generateToken(loggedInUser);
        } catch (UsernameNotFoundException e) {
            logger.error("User not found: {}", user.getUsername(), e);
            throw new LoginFailedException("User not found. Please check your credentials.");
        } catch (Exception e) {
            logger.error("Login failed for user: {}", user.getUsername(), e);
            throw new LoginFailedException("Login failed. Please check your credentials.");
        }
    }
}