package com.example.demo.filters;

import com.example.demo.services.auth.JwtService;
import com.example.demo.services.auth.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;
    
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/api/auth/", "/swagger-ui/", "/v3/api-docs", "/swagger-resources", "/webjars/"
    );
    
    private static final String FORBIDDEN_INSUFFICIENT_PERMISSIONS = "Forbidden: Insufficient permissions";

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsServiceImpl userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String fullUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = fullUri.substring(contextPath.length());
        log.info("Full URI: {}", fullUri);
        log.info("Context path: {}", contextPath);
        log.info("Matched path: {}", path);
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setHeader("Access-Control-Allow-Origin", "https://icy-meadow-0172b5a03.1.azurestaticapps.net");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
            response.setHeader("Access-Control-Allow-Headers", "Authorization, Cache-Control, Content-Type");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        if (isPublicPath(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract and validate JWT token
        String jwt = extractJwtToken(request);
        if (jwt == null) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Missing or invalid token");
            return;
        }

        // Process authentication
        try {
            if (!processAuthentication(request, response, jwt)) {
                return; // Authentication failed or insufficient permissions
            }
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
            return;
        }

        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }
    
    private boolean isPublicPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return PUBLIC_PATHS.stream().anyMatch(uri::contains);
    }
    
    private String extractJwtToken(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No JWT token found in the request: {}", authHeader);
            return null;
        }
        
        return authHeader.substring(7);
    }
    
    private boolean processAuthentication(HttpServletRequest request, HttpServletResponse response, String jwt) 
            throws IOException {
        String email = jwtService.extractUsername(jwt);
        
        if (email == null || SecurityContextHolder.getContext().getAuthentication() != null) {
            return true; // Already authenticated or no email in token
        }
        
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(email);
        
        if (!jwtService.isTokenValid(jwt, userDetails)) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Invalid token");
            return false;
        }
        
        // Set up authentication
        Authentication authentication = createAuthentication(request, jwt, userDetails);
        
        // Check role-based permissions
        return checkRoleBasedPermissions(request, response, authentication);
    }
    
    private Authentication createAuthentication(HttpServletRequest request, String jwt, UserDetails userDetails) {
        List<GrantedAuthority> authorities = jwtService.extractRoles(jwt).stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        
        log.debug("Authorities: {}", authorities);
        
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, authorities
        );
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.debug("Authenticated user: {}", authentication.getName());
        log.debug("Authorities: {}", authentication.getAuthorities());
        
        return authentication;
    }
    
    private boolean checkRoleBasedPermissions(HttpServletRequest request, HttpServletResponse response, 
                                             Authentication authentication) throws IOException {
        String uri = request.getRequestURI();
        
        // Check manager endpoint access
        if (uri.contains("/api/manager/")) {
            boolean hasManagerRole = hasRole(authentication, "ROLE_MANAGER");
            boolean hasSuperManagerRole = hasRole(authentication, "ROLE_SUPER_MANAGER");
            
            if (!hasManagerRole && !hasSuperManagerRole) {
                sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, FORBIDDEN_INSUFFICIENT_PERMISSIONS);
                return false;
            }
        }
        
        // Check super-manager endpoint access
        if (uri.contains("/api/super-manager/") && !hasRole(authentication, "ROLE_SUPER_MANAGER")) {
            sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, FORBIDDEN_INSUFFICIENT_PERMISSIONS);
            return false;
        }
        
        // Check team-member endpoint access
        if (uri.contains("/api/team-member/") && !hasRole(authentication, "ROLE_TEAM_MEMBER")) {
            sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, FORBIDDEN_INSUFFICIENT_PERMISSIONS);
            return false;
        }
        
        return true;
    }
    
    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().contains(new SimpleGrantedAuthority(role));
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setHeader("Access-Control-Allow-Origin", "https://icy-meadow-0172b5a03.1.azurestaticapps.net");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setContentType("text/plain;charset=UTF-8");
        response.setStatus(status);
        response.getWriter().write(message);
        response.getWriter().flush();
    }

}