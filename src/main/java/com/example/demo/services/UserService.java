package com.example.demo.services;

import com.example.demo.entities.*;
import com.example.demo.enums.Post;
import com.example.demo.enums.Role;
import com.example.demo.repositories.*;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final SuperManagerRepository superManagerRepository;
    private final ManagerRepository managerRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final PasswordEncoder passwordEncoder;
    private final Faker faker = new Faker();
    private final Random random = new Random();
    
    @Value("${app.default.password}")
    private String defaultPassword;

    public void setUserFields(Users user, String cuid, Role role) {
        user.setCuid(cuid);
        user.setPassword(passwordEncoder.encode(defaultPassword));
        user.setEmail(generateEmail(user.getName()));
        user.setPhone(getPhoneNumber());
        user.setRole(role);
        user.setStatus("Active");
    }

    public static String generateEmail(String fullName) {
        String cleanedName = Normalizer.normalize(fullName, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")  // remove accents
                .replaceAll("[^a-zA-Z ]", "");    // remove special characters

        String[] parts = cleanedName.trim().split("\\s+");
        String first = parts[0].toLowerCase();
        String last = parts.length > 1 ? parts[parts.length - 1].toLowerCase() : "";

        return first + "." + last + "@sofrecom.com";
    }


    public Post getRandomPost() {
        Post[] posts = Post.values();
        return posts[random.nextInt(posts.length)];
    }

    public String getPhoneNumber() {
        String[] validPrefixes = {
                "20", "21", "22", "23", "24", "25", "26", "27", "28", "29",
                "50", "51", "52", "53", "54", "55", "56", "57", "58", "59",
                "90", "91", "92", "93", "94", "95", "96", "97", "98", "99"
        };

        String prefix = validPrefixes[random.nextInt(validPrefixes.length)];
        String number = String.format("%06d", random.nextInt(1_000_000));
        return prefix + number;
    }

    public String getCuid(String prefix) {
        return prefix + UUID.randomUUID().toString().substring(0, 4);
    }
}

