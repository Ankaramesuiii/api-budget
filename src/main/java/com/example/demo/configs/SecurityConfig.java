package com.example.demo.configs;

import com.example.demo.filters.JwtAuthenticationFilter;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    List<String> origins = List.of(
            "https://icy-meadow-0172b5a03.1.azurestaticapps.net",
            "http://localhost:4200"
    );

    private static final List<String> ALLOWED_METHODS = List.of(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
    );

    // List of allowed headers
    private static final List<String> ALLOWED_HEADERS = List.of(
            "Authorization", "Cache-Control", "Content-Type"
    );

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Apply to all endpoints
                        .allowedOrigins(origins.toArray(new String[0])) // Use the list
                        .allowedMethods(ALLOWED_METHODS.toArray(new String[0])) // Allow these methods
                        .allowedHeaders(ALLOWED_HEADERS.toArray(new String[0])) // Allow these headers
                        .allowCredentials(true) // Crucial for sending Authorization header
                        .maxAge(3600); // Cache preflight results for 1 hour
            }
        };
    }

    @Bean
    public SecurityFilterChain apiChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/swagger-ui/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/flights/**").permitAll()
                        .requestMatchers("/api/manager/**").hasRole("MANAGER") // Use hasRole for automatic ROLE_ prefix
                        .requestMatchers(HttpMethod.GET, "/api/manager/**").hasAuthority("MANAGER_READ") // Fine-grained permission
                        .requestMatchers("/api/super-manager/**").hasRole("SUPER_MANAGER") // SUPER_MANAGER role
                        .requestMatchers("/api/team-member/**").hasRole("TEAM_MEMBER") // TEAM_MEMBER role
                        .requestMatchers("/api/training/import").authenticated()
                        .requestMatchers("/api/auth/user").authenticated()
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout") // Logout endpoint
                        .logoutSuccessHandler((request, response, authentication) ->
                                response.setStatus(HttpServletResponse.SC_OK)
                        )
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}