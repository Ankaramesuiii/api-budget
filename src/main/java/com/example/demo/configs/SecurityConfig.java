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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
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
                registry.addMapping("/**")
                        .allowedOrigins("https://icy-meadow-0172b5a03.1.azurestaticapps.net","http://localhost:4200")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                        .allowedHeaders("Authorization", "Cache-Control", "Content-Type");
            }
        };
    }


   /* @Bean
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
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(origins);
        cfg.setAllowedMethods(ALLOWED_METHODS);
        cfg.setAllowedHeaders(ALLOWED_HEADERS);
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }*/

//    @Bean
//    public SecurityFilterChain apiChain(HttpSecurity http) throws Exception {
//        http
//                .securityMatcher("/api/**")
//                .csrf(AbstractHttpConfigurer::disable)
//                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
//                        .requestMatchers("/api/swagger-ui/**").permitAll()
//                        .requestMatchers("/api/auth/**").permitAll()
//                        .requestMatchers("/api/flights/**").permitAll()
//                        .requestMatchers("/api/manager/**").hasRole("MANAGER") // Use hasRole for automatic ROLE_ prefix
//                        .requestMatchers(HttpMethod.GET, "/api/manager/**").hasAuthority("MANAGER_READ") // Fine-grained permission
//                        .requestMatchers("/api/super-manager/**").hasRole("SUPER_MANAGER") // SUPER_MANAGER role
//                        .requestMatchers("/api/team-member/**").hasRole("TEAM_MEMBER") // TEAM_MEMBER role
//                        .requestMatchers("/api/training/**").authenticated()
//                        .requestMatchers("/api/auth/user").authenticated()
//                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
//                        .anyRequest().authenticated())
//                .sessionManagement(session -> session
//                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .logout(logout -> logout
//                        .logoutUrl("/api/auth/logout") // Logout endpoint
//                        .logoutSuccessHandler((request, response, authentication) ->
//                                response.setStatus(HttpServletResponse.SC_OK)
//                        )
//                )
//                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }

    @Bean
    public SecurityFilterChain apiChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**") // Remove this if you want CORS to apply to all requests consistently,
                // but keep it if ALL secure endpoints are strictly under /api/
                // Given your CORS error, it's safer to let CorsFilter handle global /** first.
                // If you keep it, ensure no *other* filter chain is inadvertently
                // interfering with OPTIONS for /api/**

                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for stateless API
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Stateless session policy
                .authorizeHttpRequests(auth -> auth
                        // IMPORTANT: The CorsFilter (via .cors()) should handle OPTIONS requests.
                        // You generally don't need to explicitly permit OPTIONS here if CorsFilter is correctly configured.
                        // However, if issues persist, you can uncomment this line.
                        // .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers("/api/swagger-ui/**").permitAll() // Swagger UI paths
                        .requestMatchers("/api/auth/**").permitAll()      // Authentication endpoints (login, register)
                        .requestMatchers("/api/flights/**").permitAll()   // Example: public flights endpoint

                        // Specific authenticated paths with corrected authority checks
                        // Ensure your JWT payload roles/authorities match these exactly.
                        // Based on your token: "ROLE_SUPER_MANAGER" exists.
                        // For manager/team_member, specific permissions like "manager:read" exist.

                        // Manager endpoints: Requires "ROLE_MANAGER" or any specific manager permission
                        .requestMatchers("/api/manager/**").hasAnyAuthority(
                                "ROLE_MANAGER",
                                "manager:read",
                                "manager:post",
                                "manager:update",
                                "manager:delete"
                        )
                        // If GET on manager requires only "manager:read" specifically:
                        // .requestMatchers(HttpMethod.GET, "/api/manager/**").hasAuthority("manager:read")

                        // Super Manager endpoints: Requires "ROLE_SUPER_MANAGER"
                        .requestMatchers("/api/super-manager/**").hasAuthority("ROLE_SUPER_MANAGER")

                        // Team Member endpoints: Requires "ROLE_TEAM_MEMBER" or any specific team member permission
                        .requestMatchers("/api/team-member/**").hasAnyAuthority(
                                "ROLE_TEAM_MEMBER",
                                "team_member:read",
                                "team_member:post",
                                "team_member:update",
                                "team_member:delete"
                        )

                        // Other authenticated endpoints
                        .requestMatchers("/api/training/**").authenticated() // Example: training data requires any authenticated user
                        .requestMatchers("/api/user/profile").authenticated() // User profile requires any authenticated user
                        .requestMatchers("/api/upload-budgets").authenticated() // Upload budgets requires any authenticated user
                        .requestMatchers("/api/budgets").authenticated() // Access to budgets (GET, DELETE, etc.) also requires authentication


                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll() // Allow error dispatcher types
                        .anyRequest().authenticated() // All other requests within /api/ must be authenticated
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class) // Add JWT filter
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout") // Define logout endpoint
                        .logoutSuccessHandler((request, response, authentication) ->
                                response.setStatus(HttpServletResponse.SC_OK) // Return 200 OK on successful logout
                        )
                );

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