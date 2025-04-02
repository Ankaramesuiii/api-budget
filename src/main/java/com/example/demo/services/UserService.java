package com.example.demo.services;

import com.example.demo.entities.*;
import com.example.demo.enums.Post;
import com.example.demo.enums.Role;
import com.example.demo.repositories.*;
import com.github.javafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
@Service
public class UserService {
    @Autowired private SuperManagerRepository superManagerRepository;
    @Autowired private ManagerRepository managerRepository;
    @Autowired private TeamMemberRepository teamMemberRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    private final Faker faker = new Faker();
    private final Random random = new Random();

    public void setUserFields(Users user, String cuid, Role role) {
        user.setCuid(cuid);
        user.setPassword(passwordEncoder.encode("password"));
        user.setEmail(generateEmail(user.getName()));
        user.setPhone(faker.phoneNumber().phoneNumber());
        user.setRole(role);
        user.setStatus("Active");
    }

    public String generateEmail(String name) {
        String[] names = name.split(" ");
        String firstName = names[0].toLowerCase().replaceAll("[^A-Za-z0-9]","");
        String lastName = names.length > 1 ? names[1].toLowerCase().replaceAll("[^A-Za-z0-9]","") : "";
        return firstName + "." + lastName + "@sofrecom.com";
    }

    public Post getRandomPost() {
        Post[] posts = Post.values();
        return posts[random.nextInt(posts.length)];
    }
}

