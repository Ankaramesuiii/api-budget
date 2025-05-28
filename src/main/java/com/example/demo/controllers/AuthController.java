package com.example.demo.controllers;

import com.example.demo.entities.Users;
import com.example.demo.exceptions.UnauthorizedException;
import com.example.demo.services.auth.AuthService;
import com.example.demo.services.auth.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();

    private final AuthService authService;
    private final JwtService jwtService;


    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody Users user) {
        log.info("Registering user: {}", user);

        authService.register(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Users user) {
        String token = authService.login(user);
        return ResponseEntity.ok(token);
    }


    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        // Perform logout
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            log.info("Logging out user: {}", auth.getName());
            logoutHandler.logout(request, response, auth); // Clear security context
        } else {
            log.info("Logging out user with no active session");
        }

        return ResponseEntity.ok("Logged out successfully");
    }

    // get current user
    @GetMapping("/user")
    public Map<String, Object> dashboard() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User is not authenticated");
        }
        log.debug("Auth: {}", authentication);
        String username = authentication.getName(); // Get only the username

        Map<String, Object> response = new HashMap<>();
        response.put("username", username);
        response.put("roles", authentication.getAuthorities());

        return response; // Spring automatically converts it to JSON
    }
}