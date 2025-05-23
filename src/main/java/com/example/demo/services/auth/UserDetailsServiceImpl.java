package com.example.demo.services.auth;

import com.example.demo.entities.Manager;
import com.example.demo.entities.SuperManager;
import com.example.demo.entities.TeamMember;
import com.example.demo.entities.Users;
import com.example.demo.exceptions.UserNotFoundException;
import com.example.demo.repositories.UsersRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsersRepository userRepository;

    public UserDetailsServiceImpl(UsersRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        Optional<Users> optionalUser = userRepository.findByEmail(username);

        if (optionalUser.isEmpty()) {
            throw new UserNotFoundException("User with email '" + username + "' not found");
        }
        System.out.println(optionalUser.get().getEmail());

        Users user = optionalUser.get();

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getAuthorities() != null ? user.getAuthorities() : Collections.emptyList() // Avoid null authorities
        );

    }
}