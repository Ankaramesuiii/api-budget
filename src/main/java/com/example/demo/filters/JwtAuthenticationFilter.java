package com.example.demo.filters;

import com.example.demo.services.JwtService;
import com.example.demo.services.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

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
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String email;

        if (request.getRequestURI().contains("/api/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("No JWT token found in the request: " + authHeader);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Send 401 Unauthorized
            response.getWriter().write("Unauthorized: Missing or invalid token");
            response.getWriter().flush();
            return;
        }

        jwt = authHeader.substring(7);

        try {
            email = jwtService.extractUsername(jwt);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT Token");
            return;
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(email);

            if (jwtService.isTokenValid(jwt, userDetails)) {
                List<GrantedAuthority> authorities = jwtService.extractRoles(jwt).stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                System.out.println("Authorities: " + authorities);
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, authorities
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                System.out.println("Authenticated user: " + authentication.getName());
                System.out.println("Authorities: " + authentication.getAuthorities());

                if (request.getRequestURI().contains("/api/manager/")) {
                    boolean hasManagerRole = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGER"));
                    boolean hasSuperManagerRole = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SUPER_MANAGER"));

                    // Allow access only if the user has either MANAGER or SUPER_MANAGER role
                    if (!hasManagerRole && !hasSuperManagerRole) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN); // Send 403 Forbidden
                        response.getWriter().write("Forbidden: Insufficient permissions");
                        response.getWriter().flush();
                        return;
                    }
                }

                if (request.getRequestURI().contains("/api/super-manager/") &&
                        !authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SUPER_MANAGER"))) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN); // Send 403 Forbidden
                    response.getWriter().write("Forbidden: Insufficient permissions");
                    response.getWriter().flush();
                    return;
                }

                if (request.getRequestURI().contains("/api/team-member/") &&
                        !authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_TEAM_MEMBER"))) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN); // Send 403 Forbidden
                    response.getWriter().write("Forbidden: Insufficient permissions");
                    response.getWriter().flush();
                    return;
                }
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Token is invalid
                response.getWriter().write("Unauthorized: Invalid token");
                response.getWriter().flush();
                return;
            }
        }


        filterChain.doFilter(request, response);
    }
}